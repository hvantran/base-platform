package com.hoatv.metric.mgmt.services;

import com.hoatv.metric.mgmt.entities.ComplexValue;
import com.hoatv.metric.mgmt.entities.MetricTag;
import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;


@Getter
public class MetricService {

    private final Map<String, ComplexValue> metrics = new ConcurrentHashMap<>();

    public ComplexValue getMetric(String name) {
        return metrics.get(name);
    }

    public List<ComplexValue> getRegexMetrics(String regex) {
        return metrics.entrySet().stream()
                .filter(p -> Pattern.matches(regex, p.getKey()))
                .map(Map.Entry::getValue)
                .toList();
    }

    public void removeMetric(String name) {
        metrics.remove(name);
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
