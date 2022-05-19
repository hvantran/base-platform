package com.hoatv.system.health.metrics;

import com.hoatv.metric.mgmt.annotations.Metric;
import com.hoatv.metric.mgmt.annotations.MetricProvider;
import com.hoatv.metric.mgmt.entities.ComplexValue;
import com.hoatv.metric.mgmt.entities.MetricTag;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.hoatv.fwk.common.constants.MetricProviders.MetricCategories.METHOD_EXECUTION_TIME;
import static com.hoatv.fwk.common.constants.MetricProviders.SYSTEM_APPLICATION;

@MetricProvider(application = SYSTEM_APPLICATION, category = METHOD_EXECUTION_TIME)
public class MethodStatisticCollector {

    private static class SummaryMethodExecution {
        private static final String METRIC_PATTERN = "%s-%s";

        private long totalExecutionTime;
        private long maxExecutionTime;
        private long avgExecutionTime;
        private long minExecutionTime;
        private final AtomicInteger numberOfExecution;
        private final String methodName;
        private final String unit;

        public SummaryMethodExecution(String methodName, String unit) {
            this.unit = unit;
            this.methodName = methodName;
            this.numberOfExecution = new AtomicInteger(0);
        }

        public void addExecutionTime(long executionTime) {
            this.totalExecutionTime += executionTime;
            this.numberOfExecution.incrementAndGet();
            if (executionTime > this.maxExecutionTime) {
                this.maxExecutionTime = executionTime;
            }
            if (this.minExecutionTime == 0 || executionTime < this.minExecutionTime) {
                this.minExecutionTime = executionTime;
            }
            this.avgExecutionTime = this.totalExecutionTime / this.numberOfExecution.get();
        }

        public List<ComplexValue> getComplexValues() {
            String avgExecutionTimeMetricName = String.format(METRIC_PATTERN, "avg", methodName);
            ComplexValue avgExecutionTimeMetric = getComplexValue(avgExecutionTimeMetricName, this.unit, this.avgExecutionTime);

            String minExecutionTimeMetricName = String.format(METRIC_PATTERN, "min", methodName);
            ComplexValue minExecutionTimeMetric = getComplexValue(minExecutionTimeMetricName, this.unit, this.minExecutionTime);

            String maxExecutionTimeMetricName = String.format(METRIC_PATTERN, "max", methodName);
            ComplexValue maxExecutionTimeMetric = getComplexValue(maxExecutionTimeMetricName, this.unit, this.maxExecutionTime);

            String totalExecutionTimeMetricName = String.format(METRIC_PATTERN, "total", methodName);
            ComplexValue totalExecutionTimeMetric = getComplexValue(totalExecutionTimeMetricName, this.unit, this.totalExecutionTime);

            String numberOfExecutionMetricName = String.format(METRIC_PATTERN, "count", methodName);
            ComplexValue numberOfExecutionMetric = getComplexValue(numberOfExecutionMetricName, "", this.numberOfExecution.get());

            return List.of(avgExecutionTimeMetric, minExecutionTimeMetric, maxExecutionTimeMetric, totalExecutionTimeMetric, numberOfExecutionMetric);
        }

        private ComplexValue getComplexValue(String metricName, String unit, long value) {
            ComplexValue complexValue = new ComplexValue();
            MetricTag metricTag = new MetricTag(String.valueOf(value));
            metricTag.getAttributes().put("name", metricName);
            metricTag.getAttributes().put("unit", unit);
            complexValue.setTags(Collections.singletonList(metricTag));
            return complexValue;
        }
    }

    private final Map<String, SummaryMethodExecution> statistics = new ConcurrentHashMap<>();

    public void addMethodStatistics(String methodName, String unit, long executionTime) {
        statistics.putIfAbsent(methodName, new SummaryMethodExecution(methodName, unit));
        statistics.get(methodName).addExecutionTime(executionTime);
    }

    @Metric(name = "Method statistics", unit="ms")
    public List<ComplexValue> getMethodStatistics() {
        return statistics.entrySet().stream()
                .flatMap(methodStatistic -> methodStatistic.getValue().getComplexValues().stream())
                .collect(Collectors.toList());
    }
}
