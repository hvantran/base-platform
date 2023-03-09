package com.hoatv.action.manager.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

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
    private String actionName;

    @Setter
    @JsonProperty("description")
    private String actionDescription;

    @Setter
    @JsonProperty("configurations")
    private String configurations;

    @JsonProperty("created_at")
    private long createdAt;

    @JsonProperty("related_jobs")
    private List<JobDefinitionDTO> jobs;
}
