package org.example.apssolution.service.simulation;

import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.*;
import org.example.apssolution.dto.api_response.SolveApiResult;
import org.example.apssolution.dto.open_ai.ScenarioAiFeedbackRequest;
import org.example.apssolution.dto.request.scenario.SolveScenarioRequest;
import org.example.apssolution.dto.response.chat.ChatMessageResponse;
import org.example.apssolution.dto.response.scenario.ScenarioSimulationResultResponse;
import org.example.apssolution.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LongTaskService {

    private final TaskRepository taskRepository;
    private final ToolRepository toolRepository;
    private final ScenarioRepository scenarioRepository;
    private final ProductRepository productRepository;
    private final ScenarioScheduleRepository scenarioScheduleRepository;

    private final SimulateResultService simulateResultService;
    final SimpMessagingTemplate template;

    private final RestClient restClient;

    @Async("taskExecutor")
    @Transactional
    public void processLongTask(Account account, Scenario scenario) {
        System.out.println("********** Python Calculate Start **********");
        List<Task> myTasks = taskRepository.findAll();
        List<Tool> usingTools = toolRepository.findToolsUsedInScenario(scenario.getId());
        List<Product> myProducts = productRepository.findAll();

        SolveScenarioRequest request = SolveScenarioRequest.from(scenario, myTasks, usingTools);

        SolveApiResult result;
        try {
            result = restClient.post()
                    .uri("http://192.168.0.20:5000/api/solve")
                    .body(request)
                    .retrieve()
                    .body(SolveApiResult.class);
        } catch (Exception e) {
            scenario.setStatus("FAILED");
            scenarioRepository.save(scenario);
            return;
        }

        if (result == null || result.getStatus() == null) {
            scenario.setStatus("FAILED");
            scenarioRepository.save(scenario);
            return;
        } else if (result.getSchedules() == null || result.getSchedules().isEmpty()) {
            scenario.setStatus("FAILED");
            scenarioRepository.save(scenario);
            return;
        }

        scenario.setStatus(result.getStatus().toUpperCase());
        scenario.setMakespan(result.getMakespan());

        List<ScenarioSchedule> scenarioSchedules = result.getSchedules().stream().map(s -> {
            Product product = myProducts.stream().filter(p -> p.getId().equals(s.getProductId())).findFirst().orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 product: " + s.getProductId()));
            Task task = myTasks.stream().filter(t -> t.getId().equals(s.getTaskId())).findFirst().orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 Task: " + s.getTaskId()));
            Tool tool = usingTools.stream().filter(t -> t.getId().equals(s.getToolId())).findFirst().orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 Tool: " + s.getToolId()));
            return ScenarioSchedule.builder()
                    .scenario(scenario)
                    .product(product)
                    .task(task)
                    .worker(null)
                    .tool(tool)
                    .startAt(scenario.getStartAt().plusMinutes(s.getStart()))
                    .endAt(scenario.getStartAt().plusMinutes(s.getEnd()))
                    .build();
        }).toList();

        System.out.println("********** Python Calculate Finish **********");

        if (scenario.getStatus().equals("OPTIMAL") || scenario.getStatus().equals("FEASIBLE")) {
            String feedback = simulateResultService.getSchedulesFeedback(ScenarioAiFeedbackRequest.from(scenario, result));
            System.out.println(feedback);
            scenario.setAiScheduleFeedback(feedback);
            simulateResultService.sendResultMail(account, scenario);
        }
        scenarioScheduleRepository.deleteByScenario(scenario);
        scenarioRepository.save(scenario);
        scenarioScheduleRepository.saveAll(scenarioSchedules);

        template.convertAndSend("/topic/scenario/"
                + scenario.getId(), ScenarioSimulationResultResponse.builder().message("refresh").build());
    }
}
