package org.example.apssolution.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.*;
import org.example.apssolution.dto.request.scenario.CreateScenarioRequest;
import org.example.apssolution.dto.request.scenario.EditScenarioRequest;
import org.example.apssolution.dto.request.scenario.EditScenarioScheduleRequest;
import org.example.apssolution.dto.response.scenario.*;
import org.example.apssolution.repository.*;
import org.example.apssolution.service.simulation.LongTaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scenarios")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Scenario", description = "생산 시나리오 생성, 복제, 시뮬레이션 및 결과 관리 API")
public class ScenarioController {
    private final ScenarioRepository scenarioRepository;
    private final ScenarioProductRepository scenarioProductRepository;
    private final ScenarioScheduleRepository scenarioScheduleRepository;
    private final ScenarioWorkerRepository scenarioWorkerRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final ToolRepository toolRepository;
    private final PersonalScheduleRepository personalScheduleRepository;
    private final SimpMessagingTemplate template;

    final LongTaskService longTaskService;

    @Operation(
            summary = "시나리오 생성",
            description = "신규 시나리오 생성 및 품목 구성 정보 저장 처리"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "시나리오 생성 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "scenario": {
                                        "id": "SCN-BAKERY-001",
                                        "title": "1월 베이커리 생산 계획",
                                        "description": "주간 빵 생산 스케줄",
                                        "status": "CREATED",
                                        "startAt": "2026-01-26T05:00:00",
                                        "makespan": null,
                                        "maxWorkerCount": 5,
                                        "published": false
                                      }
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 품목 포함")
    })
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


    @Operation(
            summary = "시나리오 목록 조회",
            description = "등록된 전체 시나리오 목록 조회"
    )
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "scenarios": [
                                {
                                  "id": "SCN-BAKERY-001",
                                  "title": "1월 베이커리 생산 계획",
                                  "description": "주간 빵 생산 스케줄",
                                  "status": "OPTIMAL",
                                  "startAt": "2026-01-26T05:00:00",
                                  "makespan": 480,
                                  "maxWorkerCount": 5,
                                  "published": true
                                }
                              ]
                            }
                            """)))
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
                                                .createdAt(m.getCreatedAt())
                                                .build()
                                )
                                .sorted(Comparator.comparing(
                                        ScenarioListResponse.Scenario::getCreatedAt,
                                        Comparator.nullsLast(Comparator.reverseOrder())))
                                .toList()).build());
    }


    @Operation(
            summary = "시나리오 상세 조회",
            description = "시나리오 ID 기준 상세 정보 조회"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "scenario": {
                                        "id": "SCN-BAKERY-001",
                                        "title": "1월 베이커리 생산 계획",
                                        "description": "주간 빵 생산 스케줄",
                                        "status": "CREATED",
                                        "startAt": "2026-01-26T05:00:00",
                                        "makespan": null,
                                        "maxWorkerCount": 5,
                                        "published": false,
                                        "scenarioProducts": [
                                          {
                                            "product": {
                                              "id": "BRD-01",
                                              "name": "바게트",
                                              "description": "겉은 바삭, 속은 촉촉한 프랑스 빵",
                                              "active": true,
                                              "createdAt": "2026-01-20T08:30:00"
                                            },
                                            "qty": 50
                                          },
                                          {
                                            "product": {
                                              "id": "BRD-02",
                                              "name": "크루아상",
                                              "description": "버터 풍미가 진한 페이스트리",
                                              "active": true,
                                              "createdAt": "2026-01-20T08:40:00"
                                            },
                                            "qty": 30
                                          }
                                        ]
                                      }
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "404", description = "시나리오 미존재")
    })
    @GetMapping("/{scenarioId}") // 시나리오 상세 조회
    public ResponseEntity<?> getScenario(@PathVariable("scenarioId") String scenarioId) {
        List<ScenarioProduct> scenarioProducts = scenarioProductRepository.findAll()
                .stream().filter(f -> scenarioId.equals(f.getScenario().getId())).toList();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ScenarioResponse.builder()
                        .scenario(ScenarioResponse.from(scenarioRepository.findById(scenarioId).orElseThrow(() ->
                                new ResponseStatusException(HttpStatus.NOT_FOUND, "시나리오를 찾을 수 없습니다.")), scenarioProducts))
                        .build());
    }


    @Operation(
            summary = "시나리오 결과 조회",
            description = "OPTIMAL 상태 시나리오의 스케줄 결과 조회"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "scenario": {
                                        "id": "SCN-BAKERY-001",
                                        "title": "1월 베이커리 생산 계획",
                                        "description": "주간 빵 생산 스케줄",
                                        "startAt": "2026-01-26T05:00:00",
                                        "makespan": 480,
                                        "maxWorkerCount": 5,
                                        "published": false
                                      },
                                      "scenarioProductList": [
                                        {
                                          "id": "BRD-01",
                                          "name": "바게트",
                                          "description": "프랑스 전통 빵",
                                          "scenarioSchedules": [
                                            {
                                              "id": 101,
                                              "scheduleTask": {
                                                "id": "TASK-BAKE-01",
                                                "seq": 1,
                                                "name": "반죽 혼합",
                                                "description": "밀가루, 물, 이스트 혼합",
                                                "duration": 20
                                              },
                                              "worker": {
                                                "id": "baker-1",
                                                "name": "김제빵"
                                              },
                                              "toolId": "MIXER-01",
                                              "startAt": "2026-01-26T05:00:00",
                                              "endAt": "2026-01-26T05:20:00"
                                            },
                                            {
                                              "id": 102,
                                              "scheduleTask": {
                                                "id": "TASK-BAKE-02",
                                                "seq": 2,
                                                "name": "1차 발효",
                                                "description": "온도 27도에서 발효",
                                                "duration": 60
                                              },
                                              "worker": {
                                                "id": "baker-2",
                                                "name": "이제빵"
                                              },
                                              "toolId": "PROOFER-01",
                                              "startAt": "2026-01-26T05:20:00",
                                              "endAt": "2026-01-26T06:20:00"
                                            }
                                          ]
                                        }
                                      ]
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "404", description = "시나리오 미존재"),
            @ApiResponse(responseCode = "409", description = "스케줄 미생성 상태")
    })
    @GetMapping("/{scenarioId}/result") // 시나리오 결과 조회(스케쥴 포함)
    public ResponseEntity<?> getScenarioResult(@PathVariable("scenarioId") String scenarioId) {
        Scenario scenario = scenarioRepository.findById(scenarioId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "시나리오를 찾을 수 없습니다."));
        if ((!"OPTIMAL".equals(scenario.getStatus()) && !"FEASIBLE".equals(scenario.getStatus())) || scenario.getScenarioSchedules().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "시나리오 스케쥴이 준비되지 않았습니다.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(ScenarioResultResponse.from(scenario));
    }


    @Operation(
            summary = "시나리오 삭제",
            description = "시나리오 ID 기준 데이터 삭제 처리"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "시나리오 미존재")
    })
    @DeleteMapping("{scenarioId}") // 시나리오 삭제
    public ResponseEntity<?> deleteScenario(@PathVariable("scenarioId") String scenarioId) {
        scenarioRepository.deleteById(scenarioId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @Operation(
            summary = "시나리오 정보 수정",
            description = "OPTIMAL/배포 상태가 아닌 시나리오 정보 수정 처리"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "editScenario": {
                                        "id": "SCN-BAKERY-001",
                                        "title": "수정된 베이커리 생산 계획",
                                        "description": "설명 수정",
                                        "status": "CREATED",
                                        "startAt": "2026-01-26T05:00:00",
                                        "makespan": null,
                                        "maxWorkerCount": 6,
                                        "published": false
                                      }
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "404", description = "시나리오 미존재"),
            @ApiResponse(responseCode = "409", description = "수정 불가 상태")
    })
    @Transactional
    @PatchMapping("/{scenarioId}") // 시나리오 수정
    public ResponseEntity<?> editScenario(@PathVariable String scenarioId,
                                          @RequestBody EditScenarioRequest esr) {
        Scenario scenario = scenarioRepository.findById(scenarioId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "시나리오를 찾을 수 없습니다."));
        if (!"READY".equals(scenario.getStatus()) || !scenario.getScenarioSchedules().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "READY상태가 아닌 시나리오는 변경할 수 없습니다.");
        } else if (scenario.getPublished()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "배포된 시나리오는 변경할 수 없습니다.");
        }

        esr.toScenario(scenario);
        scenarioProductRepository.deleteByScenarioId(scenarioId);
        List<ScenarioProduct> scenarioProducts = esr.getScenarioProducts().stream().map(sp ->
                ScenarioProduct.builder()
                        .scenario(scenario)
                        .product(productRepository.findById(sp.getProductId()).orElseThrow(() ->
                                new ResponseStatusException(HttpStatus.NOT_FOUND, "알 수 없는 품목입니다.")))
                        .qty(sp.getQty())
                        .build()
        ).toList();
        scenarioProductRepository.saveAll(scenarioProducts);

        return ResponseEntity.status(HttpStatus.OK)
                .body(EditScenarioResponse.builder()
                        .editScenario(EditScenarioResponse.from(scenario))
                        .build());
    }


    @Operation(
            summary = "시나리오 복제",
            description = "기존 시나리오 정보 기반 신규 시나리오 복제 생성"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "복제 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "cloneScenario": {
                                        "id": "SCN-BAKERY-001-clone",
                                        "title": "1월 베이커리 생산 계획-(clone)",
                                        "description": "주간 빵 생산 스케줄",
                                        "status": "CREATED",
                                        "startAt": "2026-01-26T05:00:00",
                                        "makespan": null,
                                        "maxWorkerCount": 5,
                                        "published": false
                                      }
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "404", description = "시나리오 미존재")
    })
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

        List<ScenarioProduct> scenarioProducts = scenarioProductRepository.findByScenarioId(scenarioId).stream().map(sp ->
                ScenarioProduct.builder()
                        .scenario(cloneScenario)
                        .product(sp.getProduct())
                        .qty(sp.getQty())
                        .build()
        ).toList();
        scenarioProductRepository.saveAll(scenarioProducts);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ScenarioCloneResponse.builder()
                        .cloneScenario(ScenarioCloneResponse.from(cloneScenario))
                        .build());
    }


    @Operation(
            summary = "시나리오 배포",
            description = "스케줄 생성 완료된 시나리오 배포 처리"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "배포 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "scenario": {
                                        "id": "SCN-BAKERY-001",
                                        "title": "1월 베이커리 생산 계획",
                                        "description": "주간 빵 생산 스케줄",
                                        "status": "OPTIMAL",
                                        "startAt": "2026-01-26T05:00:00",
                                        "makespan": 480,
                                        "maxWorkerCount": 5,
                                        "published": true
                                      }
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "404", description = "시나리오 미존재"),
            @ApiResponse(responseCode = "409", description = "배포 불가 상태")
    })
    @Transactional
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

        List<ScenarioWorker> workers = scenario.getScenarioSchedules().stream()
                .map(ScenarioSchedule::getWorker)
                .filter(Objects::nonNull).distinct()
                .map(a -> ScenarioWorker.builder()
                        .scenario(scenario)
                        .worker(a)
                        .isRead(false)
                        .build())
                .toList();
        scenarioWorkerRepository.saveAll(workers);

        List<PersonalSchedule> personalSchedules =
                scenario.getScenarioSchedules().stream()
                        .filter(ss -> ss.getWorker() != null) // worker가 null이면 제외
                        .collect(Collectors.groupingBy(
                                ss -> Map.entry(
                                        ss.getWorker(),
                                        ss.getStartAt().toLocalDate()
                                )
                        ))
                        .values().stream()
                        .map(schedules -> {
                            ScenarioSchedule first = schedules.stream()
                                    .min(Comparator.comparing(ScenarioSchedule::getStartAt))
                                    .orElseThrow();

                            ScenarioSchedule last = schedules.stream()
                                    .max(Comparator.comparing(ScenarioSchedule::getEndAt))
                                    .orElseThrow();

                            LocalTime startTime = first.getStartAt().toLocalTime();
                            LocalTime endTime = last.getEndAt().toLocalTime();

                            return PersonalSchedule.builder()
                                    .account(first.getWorker())
                                    .title(scenario.getTitle() + " - 근무일정")
                                    .date(first.getStartAt().toLocalDate())
                                    .startTime(startTime)
                                    .endTime(endTime)
                                    .location("성남 모란 하이미디어")
                                    .color("rose")
                                    .shift(endTime.isBefore(LocalTime.of(18, 0)) ? "day" : "night")
                                    .description(first.getStartAt().toLocalDate() + " - 근무일정")
                                    .scenario(scenario)
                                    .build();
                        })
                        .toList();

        personalScheduleRepository.saveAll(personalSchedules);

        template.convertAndSend("/topic/scenario/"
                + scenario.getId(), ScenarioSimulationResultResponse.builder().message("refresh").build());

        workers.stream().map(ScenarioWorker::getWorker)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(worker -> {
                    template.convertAndSend("/topic/user/"
                            + worker.getId(), ScenarioSimulationResultResponse.builder().message("publishRefresh").build());
                });

        return ResponseEntity.status(HttpStatus.OK).body(ScenarioPublishResponse.builder()
                .scenario(ScenarioPublishResponse.from(scenario))
                .build());
    }


    @Operation(
            summary = "시나리오 배포 해제",
            description = "배포 상태 시나리오 배포 취소 처리"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "배포 해제 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "scenario": {
                                        "id": "SCN-BAKERY-001",
                                        "title": "1월 베이커리 생산 계획",
                                        "description": "주간 빵 생산 스케줄",
                                        "status": "OPTIMAL",
                                        "startAt": "2026-01-26T05:00:00",
                                        "makespan": 480,
                                        "maxWorkerCount": 5,
                                        "published": false
                                      }
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "404", description = "시나리오 미존재"),
            @ApiResponse(responseCode = "409", description = "이미 미배포 상태")
    })
    @Transactional
    @PatchMapping("/{scenarioId}/unpublish") // 시나리오 회수
    public ResponseEntity<?> unpublishScenario(@PathVariable String scenarioId) {
        Scenario scenario = scenarioRepository.findById(scenarioId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "시나리오를 찾을 수 없습니다."));
        if (!scenario.getPublished()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "아직 배포되지 않은 시나리오입니다.");
        }
        scenario.setPublished(false);
        scenarioRepository.save(scenario);
        scenarioWorkerRepository.deleteAllByScenario_Id(scenario.getId());
        personalScheduleRepository.deleteAllByScenario(scenario);
        return ResponseEntity.status(HttpStatus.OK).body(ScenarioPublishResponse.builder()
                .scenario(ScenarioPublishResponse.from(scenario))
                .build());
    }


    @Operation(
            summary = "시나리오 스케줄 자원 수정",
            description = "스케줄에 작업자 및 도구 수동 할당 처리"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "scenarioSchedule": {
                                        "task": {
                                          "id": "TASK-BAKE-03",
                                          "seq": 3,
                                          "name": "성형",
                                          "description": "반죽을 바게트 모양으로 성형",
                                          "duration": 15
                                        },
                                        "worker": {
                                          "id": "baker-3",
                                          "name": "홍길동",
                                          "role": "BAKER",
                                          "email": "baker3@bakery.com"
                                        },
                                        "tool": {
                                          "id": "TABLE-01",
                                          "name": "성형 작업대",
                                          "category": {
                                            "id": "WORKTABLE",
                                            "name": "작업대"
                                          }
                                        },
                                        "startAt": "2026-01-26T06:20:00",
                                        "endAt": "2026-01-26T06:35:00"
                                      }
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "404", description = "스케줄 미존재")
    })
    @PatchMapping("/schedules/{scenarioSchedulesId}") // 시나리오 스케쥴 수정(인원, 도구 추가)
    public ResponseEntity<?> editScenarioSchedule(@PathVariable Integer scenarioSchedulesId,
                                                  @RequestBody EditScenarioScheduleRequest esr) {
        ScenarioSchedule scenarioSchedule = scenarioScheduleRepository.findById(scenarioSchedulesId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "시나리오 스케쥴을 찾을 수 없습니다."));
        Account worker = accountRepository.findById(esr.getWorkerId()).orElse(null);
        Tool tool = toolRepository.findById(esr.getToolId()).orElse(null);
        if(scenarioSchedule.getTask().getRequiredWorkers() > 0){
            scenarioSchedule.setWorker(worker);
        }
        scenarioSchedule.setTool(tool);
        scenarioScheduleRepository.save(scenarioSchedule);
        return ResponseEntity.status(HttpStatus.OK)
                .body(EditScenarioScheduleResponse.builder()
                        .scenarioSchedule(EditScenarioScheduleResponse.from(scenarioSchedule))
                        .build());
    }


    @Operation(
            summary = "시나리오 스케줄링 실행",
            description = "외부 스케줄러 엔진 호출 후 스케줄 결과 저장 처리"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스케줄링 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "scenarioId": "SCN-BAKERY-001",
                                      "status": "OPTIMAL"
                                    }
                                    """)
                    )),
            @ApiResponse(responseCode = "404", description = "시나리오 미존재"),
            @ApiResponse(responseCode = "502", description = "스케줄러 엔진 호출 실패"),
            @ApiResponse(responseCode = "500", description = "스케줄 결과 생성 실패")
    })
    @PostMapping("/{scenarioId}/simulate") // scenario simulation 수정중
    public ResponseEntity<?> simulateScenario(@RequestAttribute Account account,
                                              @PathVariable String scenarioId) {
        Scenario scenario = scenarioRepository.findById(scenarioId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "시나리오를 찾을 수 없습니다."));
        if (!scenario.getStatus().equals("READY")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 가동된 시나리오입니다.");
        }

        scenario.setStatus("PENDING");
        scenarioRepository.save(scenario);
        longTaskService.processLongTask(account, scenarioId);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(SimulateScenarioResponse.builder()
                .scenarioId(scenarioId)
                .status("PENDING")
                .build());
    }


    @GetMapping("/schedules/today")
    public ResponseEntity<?> getTodaySchedules(@RequestAttribute Account account) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(2).atStartOfDay();

        List<ScenarioSchedule> schedules = scenarioScheduleRepository.findDailyScheduleByWorker(account, start, end);
        if (schedules.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).build();
        }else{
            Scenario scenario = scenarioRepository.findById(schedules.getFirst().getScenario().getId()).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "서버가 시나리오를 찾을 수 없습니다."));
            List<ScenarioSchedule> targetSchedules = schedules.stream()
                    .filter(s -> s.getScenario().getId().equals(scenario.getId()))
                    .toList();
            if(targetSchedules.isEmpty()){
                return ResponseEntity.status(HttpStatus.OK).build();
            }else{
                return ResponseEntity.status(HttpStatus.OK).body(PersonalScheduleResponse.from(scenario, schedules));
            }
        }
    }

    @GetMapping("/worker/unread")
    public ResponseEntity<?> getUnreadCount(@RequestAttribute Account account) {
        Integer unreadCount = scenarioWorkerRepository.countByWorkerAndIsReadFalse(account);
        return ResponseEntity.status(HttpStatus.OK).body(GetUnreadWorkerCountResponse.builder().unreadCount(unreadCount).build());
    }
}
