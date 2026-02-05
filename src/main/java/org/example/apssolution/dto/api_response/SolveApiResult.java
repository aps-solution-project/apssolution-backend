package org.example.apssolution.dto.api_response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SolveApiResult {

    private Integer makespan;
    private String status;
    private List<Schedule> schedules;
    // 🔥 추가: 분석 결과
    private Analysis analysis;


    // ===============================
    // 기존 스케줄 정보
    // ===============================
    @Getter
    @Setter
    @Builder
    public static class Schedule {
        private Integer duration;
        private Integer start;
        private Integer end;

        @JsonProperty("product_id")
        private String productId;

        @JsonProperty("task_id")
        private String taskId;

        @JsonProperty("tool_category_id")
        private String toolCategoryId;

        @JsonProperty("tool_id")
        private String toolId;
    }


    // ===============================
    // 🔥 추가 분석 정보 영역
    // ===============================
    @Getter
    @Setter
    public static class Analysis {

        // 가장 많이 사용된 설비
        private BottleneckTool bottleneckTool;

        // 전체 인력 가동률 (0~1)
        private Double workerUtilization;

        // 공정 간 평균 대기시간
        private Double averageIdleTimeBetweenTasks;

        // 최대 동시 작업자 수
        private Double peakConcurrentWorkers;

        // 🔥 설비 전체 가동률 (0~1)
        private Double equipmentUtilization;

        // 🔥 병목 공정 정보
        private BottleneckProcess bottleneckProcess;
    }

    // -------------------------------
    // 병목 설비 정보
    // -------------------------------
    @Getter
    @Setter
    @Builder
    public static class BottleneckTool {

        private String tool;
        @JsonProperty("toolCategoryId")
        private String toolCategoryId;
        // 해당 설비 총 사용 시간
        private Integer totalUsageTime;
    }

    @Getter
    @Setter
    @Builder
    public static class BottleneckProcess {

        @JsonProperty("taskId")
        private String taskId;

        @JsonProperty("productId")
        private String productId;

        // 해당 공정 소요시간
        private Integer duration;
    }

}
