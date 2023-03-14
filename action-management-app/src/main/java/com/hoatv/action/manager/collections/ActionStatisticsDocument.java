package com.hoatv.action.manager.collections;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Document("action-result-statistics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionStatisticsDocument {
    @Id
    @Builder.Default
    private String hash = UUID.randomUUID().toString();
    private String actionId;
    private long createdAt;
    private long numberOfJobs;
    private long numberOfFailureJobs;
    private long numberOfSuccessJobs;
    private double percentCompleted;
}
