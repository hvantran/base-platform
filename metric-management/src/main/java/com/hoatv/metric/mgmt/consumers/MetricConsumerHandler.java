package com.hoatv.metric.mgmt.consumers;

public interface MetricConsumerHandler {
    void consume(String application, String category, String metricKey, Object metricValue, String unit);
}