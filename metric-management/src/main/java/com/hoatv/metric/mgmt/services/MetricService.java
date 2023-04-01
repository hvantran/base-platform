package com.hoatv.metric.mgmt.services;

import com.hoatv.metric.mgmt.entities.ComplexValue;
import com.hoatv.metric.mgmt.entities.MetricTag;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


public class MetricService {

    private final Map<String, ComplexValue> metrics = new ConcurrentHashMap<>();

    public Map<String, ComplexValue> getMetrics() {
        return metrics;
    }
    public ComplexValue getMetric(String name) {
        return metrics.get(name);
    }

    public ComplexValue removeMetric(String name) {
        return metrics.remove(name);
    }

    public void setMetric(String name, Collection<MetricTag> metricTags) {
        if (Objects.isNull(metricTags)) {
            return;
        }

        ComplexValue complexValue = metrics.get(name);
        if (Objects.nonNull(complexValue)) {
            complexValue.setTags(metricTags);
            return;
        }
        complexValue = new ComplexValue();
        complexValue.setTags(metricTags);
        metrics.put(name, complexValue);
    }
}
