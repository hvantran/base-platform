package com.hoatv.system.health.metrics;

import com.hoatv.metric.mgmt.annotations.Metric;
import com.hoatv.metric.mgmt.annotations.MetricProvider;
import com.hoatv.metric.mgmt.entities.ComplexValue;
import com.hoatv.metric.mgmt.entities.MetricTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.hoatv.fwk.common.constants.Constants.SYSTEM_APPLICATION;

@MetricProvider(application = "Method Statistics Provider", category = SYSTEM_APPLICATION)
public class MethodStatistics {

    private final Map<String, Double> statistics = new ConcurrentHashMap<>();

    public void computeMethodExecutionTime(String methodName, double executionTime) {
        statistics.putIfAbsent(methodName, executionTime);
        statistics.computeIfPresent(methodName, (k, v) -> (v + executionTime)/2);
    }

    @Metric(name = "Method statistics")
    public List<ComplexValue> getExternalMetricValues() {
        return statistics.entrySet().stream().map(methodStatistic -> {
                    String methodName = methodStatistic.getKey();
                    Double executionTime = methodStatistic.getValue();
                    ComplexValue complexValue = new ComplexValue();
                    List<MetricTag> metricTags = new ArrayList<>();
                    MetricTag metricTag = new MetricTag(executionTime.toString());
                    metricTag.getAttributes().put("name", methodName + "_execution_time");
                    metricTags.add(metricTag);
                    complexValue.setTags(metricTags);
            return complexValue;
        }).collect(Collectors.toList());
    }
}
