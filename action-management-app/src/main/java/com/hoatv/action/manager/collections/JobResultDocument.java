package com.hoatv.action.manager.collections;

import java.util.UUID;

import com.hoatv.action.manager.dtos.JobState;
import com.hoatv.action.manager.dtos.JobStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Document("jobs-result-statistics")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class JobResultDocument {

    @Id
    @Builder.Default
    private String hash = UUID.randomUUID().toString();
    private JobState jobState;
    private JobStatus jobStatus;
    private String failureNotes;
    private String jobId;
    private String actionId;
    private long createdAt;
    private long endedAt;
    private long elapsedTime;
    private long startedAt;

}