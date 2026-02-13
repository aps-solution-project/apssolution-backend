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
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LongTaskService {

    private final TaskRepository taskRepository;
    private final ToolRepository toolRepository;
    private final ScenarioRepository scenarioRepository;
    private final ProductRepository productRepository;

    private final SimulationResultSaveService simulationResultSaveService;
    private final SimpMessagingTemplate template;

    private final RestClient restClient;

    @Async("taskExecutor")
    public void processLongTask(Account account, String scenarioId, List<Account> accounts) {
        Scenario scenario = scenarioRepository.findById(scenarioId).get();
        System.out.println("********** Python Calculate Start ********** " + LocalDateTime.now());
        List<Task> myTasks = taskRepository.findAll();
        List<Tool> usingTools = toolRepository.findToolsUsedInScenario(scenario.getId());
        List<Product> myProducts = productRepository.findAll();
        int n = Math.floorDiv(scenario.getMaxWorkerCount(), 2);

        List<Account> copied = new ArrayList<>(accounts);
        Collections.shuffle(copied);

        List<Account> resultAccount = copied.stream().limit(n).toList();


        SolveScenarioRequest request = SolveScenarioRequest.from(scenario, myTasks, usingTools, resultAccount);

        SolveApiResult result;
        try {
            result = restClient.post()
                    .uri("http://192.168.0.20:5000/api/solve")
                    .body(request)
                    .retrieve()
                    .body(SolveApiResult.class);
            // 여기에 디버그 걸어놓고 가기
            System.out.println("********** Python Calculate Finish ********** " + LocalDateTime.now());
        } catch (Exception e) {
            System.out.println("********** Python Calculate Exception Catch!!! ********** " + LocalDateTime.now());
            e.printStackTrace();
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

        System.out.println("********** ResultResponse Check Finish ********** " + LocalDateTime.now());

        // 스케줄로 변환 -> 저장 서비스
        simulationResultSaveService.saveScenarioResult(account, scenarioId, result, myTasks, usingTools, myProducts, resultAccount);

        template.convertAndSend("/topic/scenario/"
                + scenario.getId(), ScenarioSimulationResultResponse.builder().message("refresh").build());
        System.out.println("********** Long Task Service Finish ********** " + LocalDateTime.now());
    }
}
