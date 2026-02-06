package org.example.apssolution.service.search;

import lombok.RequiredArgsConstructor;
import org.example.apssolution.dto.search.GlobalSearchResponse;
import org.example.apssolution.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ProductRepository productRepository;
    private final TaskRepository taskRepository;
    private final ScenarioRepository scenarioRepository;
    private final NoticeRepository noticeRepository;
    private final ToolRepository toolRepository;
    private final ChatMessageRepository chatMessageRepository;

    private final PageRequest limit = PageRequest.of(0, 3);

    public GlobalSearchResponse searchAll(String keyword) {

        return GlobalSearchResponse.builder()
                .scenarios(searchScenarios(keyword))
                .products(searchProducts(keyword))
                .tasks(searchTasks(keyword))
                .tools(searchTools(keyword))
                .notices(searchNotices(keyword))
                .build();
    }


    private List<GlobalSearchResponse.ScenarioSummary> searchScenarios(String keyword) {
        return scenarioRepository.search(keyword, limit)
                .stream()
                .map(GlobalSearchResponse.ScenarioSummary::from)
                .toList();
    }

    private List<GlobalSearchResponse.ProductSummary> searchProducts(String keyword) {
        return productRepository.search(keyword, limit)
                .stream()
                .map(GlobalSearchResponse.ProductSummary::from)
                .toList();
    }

    private List<GlobalSearchResponse.TaskSummary> searchTasks(String keyword) {
        return taskRepository.search(keyword, limit)
                .stream()
                .map(GlobalSearchResponse.TaskSummary::from)
                .toList();
    }


    private List<GlobalSearchResponse.ToolSummary> searchTools(String keyword) {
        return toolRepository.search(keyword, limit)
                .stream()
                .map(GlobalSearchResponse.ToolSummary::from)
                .toList();
    }


    private List<GlobalSearchResponse.NoticeSummary> searchNotices(String keyword) {
        return noticeRepository.search(keyword, limit)
                .stream()
                .map(GlobalSearchResponse.NoticeSummary::from)
                .toList();
    }

}

