package com.hoatv.action.manager.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hoatv.springboot.common.validation.ValueOfEnum;
import lombok.*;

import javax.validation.constraints.NotEmpty;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class JobDefinitionDTO {

    @JsonProperty("hash")
    private String hash;

    @Setter
    @JsonProperty("name")
    @NotEmpty(message = "Job name cannot be NULL/empty")
    private String jobName;

    @Setter
    @JsonProperty("category")
    @ValueOfEnum(JobCategory.class)
    private String jobCategory;

    @Setter
    @JsonProperty("content")
    @NotEmpty(message = "Job content cannot be NULL/empty")
    private String jobContent;

    @Setter
    @JsonProperty("description")
    private String jobDescription;

    @Setter
    @JsonProperty("isAsync")
    private boolean isAsync;

    @Setter
    @JsonProperty("configurations")
    @NotEmpty(message = "Job configurations cannot be NULL/empty")
    private String configurations;

    @JsonProperty("action_id")
    private String actionId;

    @JsonProperty("created_at")
    private long createdAt;
}
