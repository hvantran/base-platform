package com.hoatv.metric.mgmt.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MetricCollection {

    private Object object;

    private List<MetricEntry> metricEntries;
}
