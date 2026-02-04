package org.example.apssolution.dto.response.chat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TotalUnreadCountResponse {
    private Long totalUnreadCount;
}
