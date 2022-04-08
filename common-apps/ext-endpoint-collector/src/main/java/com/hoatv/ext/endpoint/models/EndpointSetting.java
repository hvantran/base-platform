package com.hoatv.ext.endpoint.models;

import com.hoatv.ext.endpoint.dtos.EndpointSettingVO;
import com.hoatv.ext.endpoint.dtos.EndpointSettingVO.DataGeneratorInfoVO;
import com.hoatv.ext.endpoint.dtos.EndpointSettingVO.Filter;
import com.hoatv.ext.endpoint.dtos.EndpointSettingVO.Input;
import com.hoatv.ext.endpoint.dtos.EndpointSettingVO.RequestInfoVO;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndpointSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String application;

    @Column
    private String taskName;

    @Column(nullable = false)
    private String extEndpoint;

    @Column(nullable = false)
    private String method;

    @Lob
    @Column
    private String data;

    @Column
    private Integer noAttemptTimes;

    @Column
    private Integer noParallelThread;

    @Lob
    @Column(nullable = false)
    private String columnMetadata;

    @Column
    private String generatorMethodName;

    @Column
    private Integer generatorSaltLength;

    @Column
    private String generatorSaltStartWith;

    @Column
    private String successCriteria;

    @OneToOne
    @ToString.Exclude
    private EndpointExecutionResult executionResult;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "endpointSetting")
    @ToString.Exclude
    private Set<EndpointResponse> resultSet = new HashSet<>();

    public EndpointSettingVO toEndpointConfigVO() {
        RequestInfoVO requestInfo = RequestInfoVO.builder()
            .data(data)
            .extEndpoint(extEndpoint)
            .method(method)
            .build();
        DataGeneratorInfoVO dataGeneratorInfo = DataGeneratorInfoVO.builder()
            .generatorMethodName(generatorMethodName)
            .generatorSaltLength(generatorSaltLength)
            .generatorSaltStartWith(generatorSaltStartWith)
            .build();
        Input input = Input.builder()
            .application(application)
            .taskName(taskName)
            .noAttemptTimes(noAttemptTimes)
            .noParallelThread(noParallelThread)
            .columnMetadata(columnMetadata)
            .dataGeneratorInfo(dataGeneratorInfo)
            .requestInfo(requestInfo)
            .build();
        Filter filter = Filter.builder()
            .successCriteria(successCriteria)
            .build();
        return EndpointSettingVO.builder()
            .input(input)
            .filter(filter)
            .build();
    }

    public static EndpointSetting fromEndpointConfigVO(EndpointSettingVO endpointSettingVO) {
        Input input = endpointSettingVO.getInput();
        RequestInfoVO requestInfo = input.getRequestInfo();
        DataGeneratorInfoVO dataGeneratorInfo = input.getDataGeneratorInfo();
        Filter filter = endpointSettingVO.getFilter();
        return EndpointSetting.builder()
                .application(input.getApplication())
                .taskName(input.getTaskName())
                .extEndpoint(requestInfo.getExtEndpoint())
                .method(requestInfo.getMethod())
                .data(requestInfo.getData())
                .noAttemptTimes(input.getNoAttemptTimes())
                .noParallelThread(input.getNoParallelThread())
                .columnMetadata(input.getColumnMetadata())
                .generatorMethodName(dataGeneratorInfo.getGeneratorMethodName())
                .generatorSaltLength(dataGeneratorInfo.getGeneratorSaltLength())
                .generatorSaltStartWith(dataGeneratorInfo.getGeneratorSaltStartWith())
                .successCriteria(filter.getSuccessCriteria())
                .build();
    }
}

