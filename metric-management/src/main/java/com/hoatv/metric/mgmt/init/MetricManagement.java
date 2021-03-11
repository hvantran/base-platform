package com.hoatv.metric.mgmt.init;

import com.hoatv.fwk.common.services.CenterInstanceRegistryService;
import com.hoatv.fwk.common.ultilities.InstanceUtils;
import com.hoatv.metric.mgmt.annotations.Metric;
import com.hoatv.metric.mgmt.annotations.MetricProvider;
import com.hoatv.metric.mgmt.entities.ComplexValue;
import com.hoatv.metric.mgmt.entities.MetricTag;
import com.hoatv.metric.mgmt.entities.SimpleValue;
import com.hoatv.task.mgmt.annotations.ScheduleApplication;
import com.hoatv.task.mgmt.annotations.SchedulePoolSettings;
import com.hoatv.task.mgmt.annotations.ScheduleTask;
import com.hoatv.task.mgmt.annotations.ThreadPoolSettings;
import org.apache.commons.lang3.StringUtils;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import static com.hoatv.fwk.common.constants.Constants.*;
import static com.hoatv.fwk.common.constants.SystemSettings.*;

@ScheduleApplication(application = METRIC_MANAGEMENT)
@SchedulePoolSettings(application = METRIC_MANAGEMENT, threadPoolSettings = @ThreadPoolSettings(name = METRIC_MANAGEMENT_THREAD_POOL, numberOfThreads = NUMBER_OF_METRIC_THREADS))
public class MetricManagement {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricManagement.class);
    private static final Logger METRIC_LOGGER = LoggerFactory.getLogger(METRIC_LOG_APP_NAME);
    private final Reflections reflections = new Reflections(DEFAULT_SCAN_PACKAGE);

    @ScheduleTask(name = GLOBAL_METRIC_NAME, delay = GLOBAL_METRIC_SCHEDULE_DELAY_IN_MILLIS, period = GLOBAL_METRIC_SCHEDULE_PERIOD_TIME_IN_MILLIS)
    public void collectMetricData(CenterInstanceRegistryService instanceRegistry) {
        Set<Class<?>> metricProviderClasses = reflections.getTypesAnnotatedWith(MetricProvider.class);
        metricProviderClasses.forEach(metricProvider -> {
            MetricProvider providerAnnotation = metricProvider.getAnnotation(MetricProvider.class);
            String application = providerAnnotation.application();
            Object instance = instanceRegistry.getInstance(application);

            if (Objects.isNull(instance)) {
                LOGGER.warn("Cannot find instance for application {}. Init metric instance directly.", application);
                instance = InstanceUtils.newInstance(metricProvider);
                instanceRegistry.setInstance(application, instance);
            }
            Set<Method> methods = ReflectionUtils.getMethods(metricProvider, method -> Objects.nonNull(method.getAnnotation(Metric.class)));

            for (Method method : methods) {
                try {
                    Metric metric = method.getAnnotation(Metric.class);
                    String name = metric.name().toLowerCase().replace(" ", "-");
                    Object value = method.invoke(instance);

                    MDC.put("application", application);
                    MDC.put("category", providerAnnotation.category());
                    MDC.put("metric-unit", metric.unit());
                    if (value instanceof SimpleValue) {
                        SimpleValue simpleValue = (SimpleValue) value;
                        MDC.put("metric-name", name);
                        MDC.put("metric-value", String.valueOf(simpleValue.getValue()));
                        logMetricRecord(metric, name, simpleValue.getValue());
                        continue;
                    }

                    ComplexValue complexValue = (ComplexValue) value;
                    Collection<MetricTag> metricTags = complexValue.getTags();

                    for (MetricTag metricTag : metricTags) {
                        if (metricTag.getAttributes().isEmpty()) {
                            MDC.put("metric-name", name);
                        } else {
                            MDC.put("metric-name", name.concat(metricTag.getAttributes().toString()));
                            MDC.put("metric-tags", metricTag.getAttributes().toString());
                        }
                        MDC.put("metric-value", metricTag.getValue());
                        logMetricRecord(metric, name, metricTag);
                    }
                } catch (Exception exception) {
                    LOGGER.error("Cannot get value from method - {}, instance - {}", method.getName(), instance, exception);
                } finally {
                    MDC.clear();
                }
            }
        });
    }

    private void logMetricRecord(Metric metric, String name, Object value) {
        if (StringUtils.isEmpty(metric.unit())) {
            METRIC_LOGGER.info("{} - {}", name, value);
        }

        METRIC_LOGGER.info("{} - {} {}", name, value, metric.unit());
    }
}