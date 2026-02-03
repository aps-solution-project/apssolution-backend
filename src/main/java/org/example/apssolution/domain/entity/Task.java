package org.example.apssolution.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.*;
import jakarta.persistence.Id;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    private String id;

    @ManyToOne
    private Product product;

    @ManyToOne
    private ToolCategory toolCategory;

    private Integer seq;
    private String name;
    private String description;
    private Integer duration;
    private Integer requiredWorkers;


    @PrePersist
    public void prePersist(){
        if (this.description == null){
            this.description = "";
        }
        if(this.requiredWorkers == null){
            this.requiredWorkers = 1;
        }
    }
}
