package org.example.apssolution.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.Scenario;
import org.example.apssolution.domain.entity.ScenarioProduct;
import org.example.apssolution.dto.request.scenario.CreateScenarioRequest;
import org.example.apssolution.dto.response.scenario.CreateScenarioResponse;
import org.example.apssolution.dto.response.scenario.ScenarioListResponse;
import org.example.apssolution.dto.response.scenario.ScenarioResponse;
import org.example.apssolution.repository.ProductRepository;
import org.example.apssolution.repository.ScenarioProductRepository;
import org.example.apssolution.repository.ScenarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scenarios")
public class ScenarioController {
    final ScenarioRepository scenarioRepository;
    final ScenarioProductRepository scenarioProductRepository;
    final ProductRepository productRepository;

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


    @GetMapping("/{scenarioId}") // 시나리오 상세 조회(스케쥴 포함)
    public ResponseEntity<?> getScenario(@PathVariable("scenarioId") String scenarioId) {
        Scenario scenario = scenarioRepository.findById(scenarioId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "시나리오를 찾을 수 없습니다."));
        ScenarioResponse resp = new ScenarioResponse();
        resp.setScenario(ScenarioResponse.ScenarioSolution.builder()
                .id(scenario.getId())
                .title(scenario.getTitle())
                .description(scenario.getDescription())
                .startAt(scenario.getStartAt())
                .makespan(scenario.getMakespan())
                .maxWorkerCount(scenario.getMaxWorkerCount())
                .build());
        resp.setScenarioProductList(ScenarioResponse.from(scenario));
        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }


    @DeleteMapping("{scenarioId}")
    public ResponseEntity<?> deleteScenario(@PathVariable("scenarioId") String scenarioId) {
        scenarioRepository.deleteById(scenarioId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
