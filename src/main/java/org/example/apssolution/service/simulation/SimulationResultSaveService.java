package org.example.apssolution.service.simulation;

import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.*;
import org.example.apssolution.dto.api_response.SolveApiResult;
import org.example.apssolution.dto.open_ai.ScenarioAiFeedbackRequest;
import org.example.apssolution.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SimulationResultSaveService {

    private final ScenarioRepository scenarioRepository;
    private final ScenarioScheduleRepository scenarioScheduleRepository;
    private final SimulateResultService simulateResultService;

    @Transactional
    public void saveScenarioResult(Account account,
                                   String scenarioId,
                                   SolveApiResult result,
                                   List<Task> myTasks,
                                   List<Tool> usingTools,
                                   List<Product> myProducts,
                                   List<Account> accounts) {
        Scenario scenario = scenarioRepository.findById(scenarioId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.CONFLICT, "UNKNOWN SERVER ERROR"));

        scenario.setStatus(result.getStatus().toUpperCase());
        scenario.setMakespan(result.getMakespan());
        scenarioRepository.save(scenario);

        List<ScenarioSchedule> scenarioSchedules = result.getSchedules().stream().map(s -> {
            Product product = myProducts.stream().filter(p -> p.getId().equals(s.getProductId())).findFirst().orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 product: " + s.getProductId()));
            Task task = myTasks.stream().filter(t -> t.getId().equals(s.getTaskId())).findFirst().orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 Task: " + s.getTaskId()));
            Tool tool = usingTools.stream().filter(t -> t.getId().equals(s.getToolId())).findFirst().orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 Tool: " + s.getToolId()));
            Account worker = accounts.stream().filter(a -> a.getId().equals(s.getAccountId())).findFirst().orElse(null);
            return ScenarioSchedule.builder()
                    .scenario(scenario)
                    .product(product)
                    .task(task)
                    .worker(worker)
                    .tool(tool)
                    .startAt(scenario.getStartAt().plusMinutes(s.getStart()))
                    .endAt(scenario.getStartAt().plusMinutes(s.getEnd()))
                    .build();
        }).toList();

        if (scenario.getStatus().equals("OPTIMAL") || scenario.getStatus().equals("FEASIBLE")) {
            String feedback = simulateResultService.getSchedulesFeedback(ScenarioAiFeedbackRequest.from(scenario, result));
            scenario.setAiScheduleFeedback(feedback);
            simulateResultService.sendResultMail(account, scenario);
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "UNKNOWN SERVER ERROR");
        }

        scenarioScheduleRepository.deleteByScenario(scenario);
        scenarioRepository.save(scenario);
        scenarioScheduleRepository.saveAll(scenarioSchedules);
    }
}
