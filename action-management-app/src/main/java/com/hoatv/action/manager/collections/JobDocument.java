package com.hoatv.action.manager.collections;

import java.util.UUID;

import com.hoatv.action.manager.dtos.JobCategory;
import com.hoatv.action.manager.dtos.JobDefinitionDTO;
import com.hoatv.fwk.common.ultilities.DateTimeUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Document("jobs")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class JobDocument {

    @Id
    @Builder.Default
    private String hash = UUID.randomUUID().toString();

    private JobCategory jobCategory;
    private String jobContent;
    private String jobDescription;
    private String configurations;
    private String jobName;
    private String actionId;
    private long createdAt;

    public static JobDocument fromJobDefinition(JobDefinitionDTO jobDefinitionDTO) {
        return fromJobDefinition(jobDefinitionDTO, "");
    }

    public static JobDocument fromJobDefinition(JobDefinitionDTO jobDefinitionDTO, String actionId) {
        return JobDocument.builder()
                .jobName(jobDefinitionDTO.getJobName())
                .jobDescription(jobDefinitionDTO.getJobDescription())
                .configurations(jobDefinitionDTO.getConfigurations())
                .jobContent(jobDefinitionDTO.getJobContent())
                .jobCategory(jobDefinitionDTO.getJobCategory())
                .createdAt(DateTimeUtils.getCurrentEpochTimeInSecond())
                .actionId(actionId)
                .build();
    }

    public static JobDefinitionDTO toJobDefinition(JobDocument jobDocument) {
        return JobDefinitionDTO.builder()
                .jobName(jobDocument.getJobName())
                .jobDescription(jobDocument.getJobDescription())
                .configurations(jobDocument.getConfigurations())
                .jobContent(jobDocument.getJobContent())
                .jobCategory(jobDocument.getJobCategory())
                .createdAt(jobDocument.getCreatedAt())
                .build();
    }
}
