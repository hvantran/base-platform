package com.hoatv.system.health.metrics;

import com.hoatv.metric.mgmt.annotations.Metric;
import com.hoatv.metric.mgmt.annotations.MetricProvider;
import com.hoatv.metric.mgmt.entities.ComplexValue;
import com.hoatv.metric.mgmt.entities.MetricTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.hoatv.fwk.common.constants.Constants.SYSTEM_APPLICATION;

@MetricProvider(application = "Method Statistics Provider", category = SYSTEM_APPLICATION)
public class MethodStatisticCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodStatisticCollector.class);

    private final Map<String, Long> statistics = new ConcurrentHashMap<>();

    public void computeMethodExecutionTime(String methodName, long executionTime) {
        LOGGER.debug("Method {} execution time: {} ms", methodName, executionTime);
        statistics.putIfAbsent(methodName, executionTime);
        statistics.computeIfPresent(methodName, (k, v) -> v > executionTime ? v : executionTime);
    }

    @Metric(name = "Method statistics", unit="ms")
    public List<ComplexValue> getMethodStatistics() {
        return statistics.entrySet().stream().map(methodStatistic -> {
                    String methodName = methodStatistic.getKey();
                    long executionTime = methodStatistic.getValue();
                    ComplexValue complexValue = new ComplexValue();
                    List<MetricTag> metricTags = new ArrayList<>();
                    MetricTag metricTag = new MetricTag(String.valueOf(executionTime));
                    metricTag.getAttributes().put("name", methodName);
                    metricTags.add(metricTag);
                    complexValue.setTags(metricTags);
            return complexValue;
        }).collect(Collectors.toList());
    }
}
