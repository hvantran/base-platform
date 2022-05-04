package com.hoatv.webflux;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtTaskReportVO {

    private long startTime;
    private String attemptValue;
    private long endTime;
    private String executionResult;

    public ExtTaskReportVO(long startTime) {
        this.startTime = startTime;
    }

    public long getElapsedTime() {
        return endTime - startTime;
    }
}
