package com.hoatv.action.manager.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;


@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ActionDefinitionDTO {

    @JsonProperty("hash")
    private String hash;

    @Setter
    @JsonProperty("name")
    @NotEmpty(message = "Action name cannot be NULL/empty")
    private String actionName;

    @Setter
    @JsonProperty("description")
    private String actionDescription;

    @Setter
    @JsonProperty("configurations")
    @NotEmpty(message = "Action configurations cannot be NULL/empty")
    private String configurations;

    @JsonProperty("createdAt")
    private long createdAt;

    @Valid
    @JsonProperty("relatedJobs")
    @NotEmpty(message = "Jobs cannot be NULL/empty")
    private List<JobDefinitionDTO> jobs;
}
