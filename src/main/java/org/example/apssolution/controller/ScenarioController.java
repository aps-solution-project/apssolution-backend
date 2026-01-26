package org.example.apssolution.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.*;
import org.example.apssolution.dto.api_response.SolveApiResult;
import org.example.apssolution.dto.request.scenario.CreateScenarioRequest;
import org.example.apssolution.dto.request.scenario.EditScenarioRequest;
import org.example.apssolution.dto.request.scenario.EditScenarioScheduleRequest;
import org.example.apssolution.dto.request.scenario.SolveScenarioRequest;
import org.example.apssolution.dto.response.scenario.*;
import org.example.apssolution.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scenarios")
@SecurityRequirement(name="bearerAuth")
public class ScenarioController {
    final ScenarioRepository scenarioRepository;
    final ScenarioProductRepository scenarioProductRepository;
    final ScenarioScheduleRepository scenarioScheduleRepository;
    final ProductRepository productRepository;
    final AccountRepository accountRepository;
    final ToolRepository toolRepository;
    final TaskRepository taskRepository;

    @PostMapping // 시나리오 생성
    public ResponseEntity<?> postScenario(@RequestBody @Valid CreateScenarioRequest csr,
                                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            FieldError fe = bindingResult.getFieldError();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fe.getDefaultMessage());
        }

        Scenario scenario = Scenario.builder()
                .title(csr.getTitle())
                .description(csr.getDescription())
                .startAt(csr.getStartAt() == null ? LocalDateTime.now() : csr.getStartAt())
                .maxWorkerCount(csr.getMaxWorkerCount())
                .build();
        scenarioRepository.save(scenario);
        List<ScenarioProduct> scenarioProducts = csr.getScenarioProduct().stream().map(m -> ScenarioProduct.builder()
                .scenario(scenario)
                .product(productRepository.findById(m.getProductId()).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "알 수 없는 품목 정보가 존재합니다.")))
                .qty(m.getQty())
                .build()
        ).toList();
        scenarioProductRepository.saveAll(scenarioProducts);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CreateScenarioResponse.builder()
                        .scenario(CreateScenarioResponse.Scenario.builder()
                                .id(scenario.getId())
                                .title(scenario.getTitle())
                                .description(scenario.getDescription())
                                .status(scenario.getStatus())
                                .startAt(scenario.getStartAt())
                                .makespan(scenario.getMakespan())
                                .maxWorkerCount(scenario.getMaxWorkerCount())
                                .published(scenario.getPublished())
                                .build())
                        .build());
    }


    @GetMapping // 시나리오 전체 조회
    public ResponseEntity<?> getScenarios() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ScenarioListResponse.builder()
                        .scenarios(scenarioRepository.findAll().stream().map(m ->
                                ScenarioListResponse.Scenario.builder()
                                        .id(m.getId())
                                        .title(m.getTitle())
                                        .description(m.getDescription())
                                        .status(m.getStatus())
                                        .startAt(m.getStartAt())
                                        .makespan(m.getMakespan())
                                        .maxWorkerCount(m.getMaxWorkerCount())
                                        .published(m.getPublished())
                                        .build()
                        ).toList()).build());
    }


    @GetMapping("/{scenarioId}") // 시나리오 상세 조회
    public ResponseEntity<?> getScenario(@PathVariable("scenarioId") String scenarioId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ScenarioResponse.builder()
                        .scenario(ScenarioResponse.from(scenarioRepository.findById(scenarioId).orElseThrow(() ->
                                new ResponseStatusException(HttpStatus.NOT_FOUND, "시나리오를 찾을 수 없습니다."))))
                        .build());
    }


    @GetMapping("/{scenarioId}/result") // 시나리오 결과 조회(스케쥴 포함)
    public ResponseEntity<?> getScenarioResult(@PathVariable("scenarioId") String scenarioId) {
        Scenario scenario = scenarioRepository.findById(scenarioId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "시나리오를 찾을 수 없습니다."));
        if (!"OPTIMAL".equals(scenario.getStatus()) || scenario.getScenarioSchedules().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "시나리오 스케쥴이 준비되지 않았습니다.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(ScenarioResultResponse.from(scenario));
    }


    @DeleteMapping("{scenarioId}") // 시나리오 삭제
    public ResponseEntity<?> deleteScenario(@PathVariable("scenarioId") String scenarioId) {
        scenarioRepository.deleteById(scenarioId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @PatchMapping("/{scenarioId}") // 시나리오 수정
    public ResponseEntity<?> editScenario(@PathVariable String scenarioId,
                                          @RequestBody EditScenarioRequest esr) {
        Scenario scenario = scenarioRepository.findById(scenarioId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "시나리오를 찾을 수 없습니다."));
        if ("OPTIMAL".equals(scenario.getStatus()) || !scenario.getScenarioSchedules().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "OPTIMAL 시나리오는 변경할 수 없습니다.");
        } else if (scenario.getPublished()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "배포된 시나리오는 변경할 수 없습니다.");
        }

        scenarioRepository.save(esr.toScenario(scenario));

        return ResponseEntity.status(HttpStatus.OK)
                .body(EditScenarioResponse.builder()
                        .editScenario(EditScenarioResponse.from(scenario))
                        .build());
    }


    @PostMapping("/{scenarioId}/clone") // 시나리오 복제
    public ResponseEntity<?> copyScenario(@PathVariable String scenarioId) {
        Scenario scenario = scenarioRepository.findById(scenarioId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "시나리오를 찾을 수 없습니다."));

        Scenario cloneScenario = Scenario.builder()
                .title(scenario.getTitle() + "-(clone)")
                .description(scenario.getDescription())
                .startAt(scenario.getStartAt())
                .maxWorkerCount(scenario.getMaxWorkerCount())
                .build();

        scenarioRepository.save(cloneScenario);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ScenarioCloneResponse.builder()
                        .cloneScenario(ScenarioCloneResponse.from(cloneScenario))
                        .build());
    }


    @PatchMapping("/{scenarioId}/publish") // 시나리오 배포
    public ResponseEntity<?> publishScenario(@PathVariable String scenarioId) {
        Scenario scenario = scenarioRepository.findById(scenarioId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "시나리오를 찾을 수 없습니다."));
        if (scenario.getPublished()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 배포된 시나리오입니다.");
        } else if (scenario.getScenarioSchedules().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "OPTIMAL 상태인 시나리오만 배포할 수 있습니다.");
        }
        scenario.setPublished(true);
        scenarioRepository.save(scenario);
        return ResponseEntity.status(HttpStatus.OK).body(ScenarioPublishResponse.builder()
                .scenario(ScenarioPublishResponse.from(scenario))
                .build());
    }


    @PatchMapping("/{scenarioId}/unpublish") // 시나리오 배포
    public ResponseEntity<?> unpublishScenario(@PathVariable String scenarioId) {
        Scenario scenario = scenarioRepository.findById(scenarioId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "시나리오를 찾을 수 없습니다."));
        if (!scenario.getPublished()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "아직 배포되지 않은 시나리오입니다.");
        }
        scenario.setPublished(false);
        scenarioRepository.save(scenario);
        return ResponseEntity.status(HttpStatus.OK).body(ScenarioPublishResponse.builder()
                .scenario(ScenarioPublishResponse.from(scenario))
                .build());
    }


    @PatchMapping("/schedules/{scenarioSchedulesId}") // 시나리오 스케쥴 수정(인원, 도구 추가)
    public ResponseEntity<?> editScenarioSchedule(@PathVariable Integer scenarioSchedulesId,
                                                  @RequestBody EditScenarioScheduleRequest esr) {
        ScenarioSchedule scenarioSchedule = scenarioScheduleRepository.findById(scenarioSchedulesId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "시나리오 스케쥴을 찾을 수 없습니다."));
        Account worker = accountRepository.findById(esr.getWorkerId()).orElse(null);
        Tool tool = toolRepository.findById(esr.getToolId()).orElse(null);
        scenarioSchedule.setWorker(worker);
        scenarioSchedule.setTool(tool);
        scenarioScheduleRepository.save(scenarioSchedule);
        return ResponseEntity.status(HttpStatus.OK)
                .body(EditScenarioScheduleResponse.builder()
                        .scenarioSchedule(EditScenarioScheduleResponse.from(scenarioSchedule))
                        .build());
    }


    @PostMapping("/{scenarioId}/simulate") // scenario simulation 수정중
    public ResponseEntity<?> simulateScenario(@PathVariable String scenarioId) {

        Scenario scenario = scenarioRepository.findById(scenarioId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "시나리오를 찾을 수 없습니다."));

        List<Task> myTasks = taskRepository.findAll();
        List<Tool> myTools = toolRepository.findAll();

        RestClient restClient = RestClient.create();
        SolveScenarioRequest request = SolveScenarioRequest.from(scenario, myTasks, myTools);

        SolveApiResult result = restClient.post()
                .uri("http://127.0.0.1:5000/api/solve")
                .body(request).retrieve()
                .body(SolveApiResult.class);

        scenario.setStatus(result.getStatus() == null ? "ERROR" : result.getStatus());
        scenario.setMakespan(result.getMakespan());

        //받은 데이터 ScenarioSchedule로 바꿔서 저장하기 코드 추가 (2026.01.26 숙제)
        List<ScenarioSchedule> scenarioSchedules = result.getSchedules().stream().map(s -> {
            return ScenarioSchedule.builder()
                    .scenario(scenario)
                    .product(productRepository.findById(s.getProductId()).orElse(null))
                    .task(taskRepository.findById(s.getTaskId()).orElse(null))
                    .worker(null)
                    .tool(toolRepository.findById(s.getToolId()).orElse(null))
                    .startAt(scenario.getStartAt().plusMinutes(s.getStart()))
                    .endAt(scenario.getStartAt().plusMinutes(s.getEnd()))
                    .build();
        }).toList();

        scenarioRepository.save(scenario);
        scenarioScheduleRepository.saveAll(scenarioSchedules);

        return ResponseEntity.status(HttpStatus.OK).body(SimulateScenarioResponse.builder()
                .scenarioId(scenarioId)
                .status(result.getStatus())
                .build());
    }
}
