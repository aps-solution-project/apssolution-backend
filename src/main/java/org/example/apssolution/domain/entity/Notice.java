package org.example.apssolution.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Account writer;
    @ManyToOne
    private Scenario scenario;
    private String title;
    private String content;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // 빌더 패턴 사용 시 attachments 리스트가 null이 되지 않도록 초기화 유지
    private List<NoticeAttachment> attachments = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
