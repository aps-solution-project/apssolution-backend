package org.example.apssolution.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tool {
    @Id
    private String id;

    @ManyToOne
    private ToolCategory category;
    private String description;
}
