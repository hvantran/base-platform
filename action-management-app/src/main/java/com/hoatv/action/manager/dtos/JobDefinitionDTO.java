package com.hoatv.action.manager.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

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
    private String jobName;

    @Setter
    @JsonProperty("category")
    private JobCategory jobCategory;

    @Setter
    @JsonProperty("content")
    private String jobContent;

    @Setter
    @JsonProperty("description")
    private String jobDescription;

    @Setter
    @JsonProperty("configurations")
    private String configurations;

    @JsonProperty("action_id")
    private String actionId;

    @JsonProperty("created_at")
    private long createdAt;
}
