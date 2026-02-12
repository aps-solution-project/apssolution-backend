package org.example.apssolution.dto.response.scenario;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetUnreadWorkerCountResponse {
    private Integer unreadCount;
}
