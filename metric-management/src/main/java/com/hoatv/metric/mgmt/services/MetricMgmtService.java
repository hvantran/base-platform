package com.hoatv.metric.mgmt.services;

import com.hoatv.fwk.common.ultilities.ObjectUtils;
import com.hoatv.metric.mgmt.annotations.MetricProvider;
import com.hoatv.metric.mgmt.entities.MetricEntry;
import com.hoatv.monitor.mgmt.TimingMetricMonitor;
import com.hoatv.task.mgmt.annotations.ScheduleApplication;
import com.hoatv.task.mgmt.annotations.SchedulePoolSettings;
import com.hoatv.task.mgmt.annotations.ScheduleTask;
import com.hoatv.task.mgmt.annotations.ThreadPoolSettings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static com.hoatv.fwk.common.constants.Constants.*;
import static com.hoatv.fwk.common.constants.SystemSettings.*;
import static com.hoatv.fwk.common.ultilities.StringCommonUtils.deAccent;

@ScheduleApplication(application = METRIC_MANAGEMENT, delay = GLOBAL_METRIC_SCHEDULE_DELAY_IN_MILLIS, period = GLOBAL_METRIC_SCHEDULE_PERIOD_TIME_IN_MILLIS)
@SchedulePoolSettings(application = METRIC_MANAGEMENT, threadPoolSettings = @ThreadPoolSettings(name = METRIC_MANAGEMENT_THREAD_POOL, numberOfThreads = NUMBER_OF_METRIC_THREADS))
public class MetricMgmtService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricMgmtService.class);

    private final MetricProviderRegistry metricProviders;
    private final MetricConsumerRegistry consumerRegistry;

    public MetricMgmtService(MetricProviderRegistry providerRegistry, MetricConsumerRegistry consumerRegistry) {
        this.metricProviders = providerRegistry;
        this.consumerRegistry = consumerRegistry;
    }

    @TimingMetricMonitor
    @ScheduleTask(name = GLOBAL_METRIC_NAME)
    public void collectMetricData() {
        metricProviders.getMetricRegistry().forEach((application, metricCollection) -> {
            Object metricProvider = metricCollection.getObject();
            List<MetricEntry> metricEntries = metricCollection.getMetricEntries();
            Optional<MetricProvider> providerAnnotationOp = ObjectUtils.getAnnotation(MetricProvider.class, metricProvider);
            ObjectUtils.checkThenThrow(providerAnnotationOp.isEmpty(), "Metric provider must be annotated with @MetricProvider");
            MetricProvider providerAnnotation = providerAnnotationOp.get();

            for (MetricEntry metricEntry : metricEntries) {
                Method method          = metricEntry.getMethod();
                String category        = providerAnnotation.category();
                String metricNameLower = metricEntry.getName().toLowerCase();
                String name            = deAccent(metricNameLower).replace(" ", "-");
                String unit            = metricEntry.getUnit();
                Object value;
                try {
                    value = method.invoke(metricProvider);
                }
                catch (IllegalAccessException | InvocationTargetException e) {
                    LOGGER.error("Cannot get value from method - {}, instance - {}", method.getName(), metricProvider, e);
                    continue;
                }
                consumerRegistry.forEach(consumer -> {
                    String consumerName = consumer.getClass().getSimpleName();
                    try {
                        consumer.consume(application, category, name, value, unit);
                    } catch (Exception exception) {
                        LOGGER.error("Unable to forward metric to consumer: {}", consumerName, exception);
                    }
                });
            }
        });
    }
}