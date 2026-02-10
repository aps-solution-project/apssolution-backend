package org.example.apssolution.service.simulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.apssolution.domain.entity.Account;
import org.example.apssolution.domain.entity.Scenario;
import org.example.apssolution.dto.open_ai.ScenarioAiFeedbackRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SimulateResultService {
    private final JavaMailSender javaMailSender;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Value("${aiToken}")
    private String aiToken;

    public String getSchedulesFeedback(ScenarioAiFeedbackRequest sfr) {
        try {
            String prompt = new String(
                    Objects.requireNonNull(getClass().getResourceAsStream("/ai/feedback-prompt.txt"))
                            .readAllBytes(),
                    StandardCharsets.UTF_8
            );

            Map map = Map.of("model", "gpt-4o-mini", "instructions",
                    prompt, "input", objectMapper.writeValueAsString(sfr));

            String json = restClient.post().uri("https://api.openai.com/v1/responses")
                    .header("Authorization", "Bearer " + aiToken)
                    .header("Content-Type", "application/json")
                    .body(map).retrieve().body(String.class);
            JsonNode jn = objectMapper.readTree(json);
            return jn.get("output").get(0).get("content").get(0).get("text").asText();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendResultMail(Account account, Scenario scenario) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(account.getEmail());
        mailMessage.setSubject("스케줄 시뮬레이션 완료 안내");
        mailMessage.setText(
                "안녕하세요" + account.getName() + "님, APS-Solution입니다." +
                        "\n\n요청하신 생산 스케줄 시뮬레이션 계산이 완료되었습니다." +
                        "\n\n시나리오명 : " + scenario.getTitle() +
                        "\n⏱ 계산 상태 : " + scenario.getStatus() +
                        "\n상세 결과 확인 : 시스템 접속 후 시나리오 상세 화면" +
                        "\n\n본 결과는 설비 배치, 작업 순서, 인력 배분이 반영된 스케줄입니다." +
                        "\n운영 계획 수립 시 참고 자료로 활용해주시기 바랍니다." +
                        "\n\n※ 조건을 변경한 뒤 재실행하면 새로운 결과를 얻을 수 있습니다." +
                        "\n\n감사합니다."
        );

        javaMailSender.send(mailMessage);
    }

}
