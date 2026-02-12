package org.example.apssolution.dto.search;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apssolution.domain.entity.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class GlobalSearchResponse {
    List<AccountSummary> accounts;
    List<ScenarioSummary> scenarios;
    List<ProductSummary> products;
    List<TaskSummary> tasks;
    List<ToolSummary> tools;
    List<NoticeSummary> notices;

    @Getter
    @Builder
    public static class AccountSummary{
        private String id;
        private String name;
        private String email;
        private String profileImageUrl;

        public static AccountSummary from(Account account){
            return AccountSummary.builder()
                    .id(account.getId())
                    .name(account.getName())
                    .email(account.getEmail())
                    .profileImageUrl(account.getProfileImageUrl())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ScenarioSummary{
        private String id;
        private String title;
        private String description;

        public static ScenarioSummary from(Scenario scenario){
            return ScenarioSummary.builder()
                    .id(scenario.getId())
                    .title(scenario.getTitle())
                    .description(scenario.getDescription())
                    .build();
        }
    }
    @Getter
    @Builder
    public static class ProductSummary{
        private String id;
        private String name;
        private String description;
        private boolean active;

        public static ProductSummary from(Product product){
            return ProductSummary.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .active(product.getActive())
                    .build();
        }
    }
    @Getter
    @Builder
    public static class TaskSummary{
        private String id;
        private String name;
        private  String description;

        public static TaskSummary from(Task task){
            return TaskSummary.builder()
                    .id(task.getId())
                    .name(task.getName())
                    .description(task.getDescription())
                    .build();
        }
    }
    @Getter
    @Builder
    public static class ToolSummary{
        private String id;
        private ToolCategory category;
        private String description;

        public static ToolSummary from(Tool tool){
            return ToolSummary.builder()
                    .id(tool.getId())
                    .category(tool.getCategory())
                    .description(tool.getDescription())
                    .build();
        }
    }
    @Getter
    @Builder
    public static class NoticeSummary{
        private Long id;
        private String title;
        private String content;
        private LocalDateTime createdAt;

        public static NoticeSummary from(Notice notice){
            return NoticeSummary.builder()
                    .id(notice.getId())
                    .title(notice.getTitle())
                    .content(notice.getContent())
                    .createdAt(notice.getCreatedAt())
                    .build();
        }
    }
}
