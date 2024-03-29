package com.hoatv.metric.mgmt.services;

import com.hoatv.fwk.common.ultilities.ObjectUtils;
import com.hoatv.fwk.common.ultilities.Pair;
import com.hoatv.metric.mgmt.annotations.Metric;
import com.hoatv.metric.mgmt.annotations.MetricProvider;
import com.hoatv.metric.mgmt.annotations.MetricRegistry;
import com.hoatv.metric.mgmt.entities.MetricCollection;
import com.hoatv.metric.mgmt.entities.MetricEntry;
import lombok.Getter;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@MetricRegistry
public class MetricProviderRegistry {

    private final Map<String, MetricCollection> metricRegistry = new ConcurrentHashMap<>();

    public void loadFromObjects(List<Object> metricInstances) {
        Predicate<Method> metricAnnotated = method -> Objects.nonNull(method.getAnnotation(Metric.class));

        Map<String, MetricCollection> metricPairs = metricInstances.stream().map(instance -> {

            Set<Method> metricMethod = ReflectionUtils.getAllMethods(instance.getClass(), metricAnnotated);
            List<MetricEntry> metricEntries = metricMethod.stream().map(MetricEntry::fromMethod).toList();

            Optional<MetricProvider> metricProviderAnnotation = ObjectUtils.getAnnotation(MetricProvider.class, instance);
            ObjectUtils.checkThenThrow(metricProviderAnnotation.isEmpty(), "MetricProvider annotation is missing");

            String applicationName = metricProviderAnnotation.get().application();
            String category = metricProviderAnnotation.get().category();
            return Pair.of(applicationName + category, new MetricCollection(instance, metricEntries));
        }).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

        metricRegistry.putAll(metricPairs);
    }

    public void addMetricProviders(SimpleEntry<String, Pair<Object, List<Method>>> metricProviderPair) {
        String application = metricProviderPair.getKey();
        Pair<Object, List<Method>> metrics = metricProviderPair.getValue();
        List<Method> methods = metrics.getValue();
        Object instance = metrics.getKey();

        synchronized (metricRegistry) {

            List<MetricEntry> metricEntries = methods.stream().map(MetricEntry::fromMethod).toList();
            MetricCollection metricCollection = metricRegistry.get(application);
            if (Objects.nonNull(metricCollection)) {
                metricCollection.getMetricEntries().addAll(metricEntries);
                return;
            }
            metricRegistry.put(application, new MetricCollection(instance, metricEntries));
        }
    }
}
