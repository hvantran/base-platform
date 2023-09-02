package com.hoatv.metric.mgmt.services;

import com.hoatv.fwk.common.ultilities.ObjectUtils;
import com.hoatv.metric.mgmt.annotations.MetricProvider;
import com.hoatv.metric.mgmt.entities.ComplexValue;
import com.hoatv.metric.mgmt.entities.MetricEntry;
import com.hoatv.metric.mgmt.entities.MetricTag;
import com.hoatv.metric.mgmt.entities.SimpleValue;
import com.hoatv.monitor.mgmt.TimingMetricMonitor;
import com.hoatv.task.mgmt.annotations.ScheduleApplication;
import com.hoatv.task.mgmt.annotations.SchedulePoolSettings;
import com.hoatv.task.mgmt.annotations.ScheduleTask;
import com.hoatv.task.mgmt.annotations.ThreadPoolSettings;
import net.logstash.logback.argument.StructuredArguments;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.lang.reflect.Method;
import java.text.Normalizer;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hoatv.fwk.common.constants.Constants.*;
import static com.hoatv.fwk.common.constants.SystemSettings.*;

@ScheduleApplication(application = METRIC_MANAGEMENT, delay = GLOBAL_METRIC_SCHEDULE_DELAY_IN_MILLIS, period = GLOBAL_METRIC_SCHEDULE_PERIOD_TIME_IN_MILLIS)
@SchedulePoolSettings(application = METRIC_MANAGEMENT, threadPoolSettings = @ThreadPoolSettings(name = METRIC_MANAGEMENT_THREAD_POOL, numberOfThreads = NUMBER_OF_METRIC_THREADS))
public class MetricMgmtService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricMgmtService.class);
    private static final Logger METRIC_LOGGER = LoggerFactory.getLogger(METRIC_LOG_APP_NAME);

    private static final String METRIC_NAME = "metric-name";
    private static final String METRIC_VALUE = "metric-value";
    public static final String METRIC_UNIT = "metric-unit";

    private final MetricProviderRegistry metricProviders;

    public MetricMgmtService(MetricProviderRegistry metricProvider) {
        this.metricProviders = metricProvider;
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
                Method method = metricEntry.getMethod();
                try {
                    String name = metricEntry.getName().toLowerCase().replace(" ", "-");
                    Object value = method.invoke(metricProvider);

                    MDC.put("application", providerAnnotation.application());
                    MDC.put("category", providerAnnotation.category());

                    if (value instanceof SimpleValue simpleValue) {
                        processSimpleValue(metricEntry, name, simpleValue);
                    } else if (value instanceof String metricValue) {
                        processSimpleValue(metricEntry, name, Long.valueOf(metricValue));
                    } else if (value instanceof Long metricValue) {
                        processSimpleValue(metricEntry, name, metricValue);
                    } else if (value instanceof Integer metricValue) {
                        processSimpleValue(metricEntry, name, metricValue.longValue());
                    } else if (value instanceof Collection<?>) {
                        @SuppressWarnings("unchecked")
                        Collection<ComplexValue> complexValues = (Collection<ComplexValue>) value;
                        complexValues.forEach(complexValue -> processComplexValue(metricEntry, name, complexValue));
                    } else {
                        processComplexValue(metricEntry, name, (ComplexValue) value);
                    }
                } catch (Exception exception) {
                    LOGGER.error("Cannot get value from method - {}, instance - {}", method.getName(), metricProvider, exception);
                } finally {
                    MDC.clear();
                }
            }
        });
    }

    private void processSimpleValue(MetricEntry metricEntry, String name, SimpleValue simpleValue) {
        MDC.put(METRIC_NAME, name);
        logMetricRecord(name, simpleValue.getValue(), metricEntry.getUnit());
    }
    private void processSimpleValue(MetricEntry metricEntry, String name, Long simpleValue) {
        MDC.put(METRIC_NAME, name);
        logMetricRecord(name, simpleValue, metricEntry.getUnit());
    }

    private void processComplexValue(MetricEntry metricEntry, String name, ComplexValue complexValue) {
        Collection<MetricTag> metricTags = complexValue.getTags();

        for (MetricTag metricTag : metricTags) {
            String metricUnit = getMetricUnit(metricEntry, metricTag);
            Map<String, String> attributes = metricTag.getAttributes();
            String nameTag = attributes.get("name");
            String metricNameCompute = name;
            MDC.put(METRIC_UNIT, metricUnit);
            if (attributes.isEmpty()) {
                MDC.put(METRIC_NAME, name);
            } else if (Objects.nonNull(nameTag)) {
                Predicate<Map.Entry<String, String>> filterOutName = p -> !"name".equals(p.getKey());
                Map<String, String> newAttributes =
                        attributes.entrySet()
                                .stream()
                                .filter(filterOutName)
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                String nameReplaced = nameTag.toLowerCase().replace(" ", "-");
                String attNames = newAttributes.values().stream().map(MetricMgmtService::deAccent).collect(Collectors.joining("-"));

                StringJoiner stringJoiner = new StringJoiner("-");
                stringJoiner.add(deAccent(nameReplaced));
                if (StringUtils.isNotEmpty(attNames)) {
                    stringJoiner.add(attNames);
                }

                String metricNameFormatted = stringJoiner.toString();
                metricNameCompute = metricNameFormatted;
                MDC.put(METRIC_NAME, metricNameFormatted);
                attributes.forEach(MDC::put);
            } else {
                String metricNameFormatted = name.concat(attributes.toString());
                metricNameCompute = metricNameFormatted;
                MDC.put(METRIC_NAME, metricNameFormatted);
                attributes.forEach(MDC::put);
            }
            logMetricRecord(metricNameCompute, metricTag, metricUnit);
        }
    }

    private void logMetricRecord(String name, Object value, String unit) {
        Map<String, Object> valueMap = getValueMap(value);
        METRIC_LOGGER.info("{} - {} {}", name, value, unit, StructuredArguments.entries(valueMap));
    }

    private String getMetricUnit(MetricEntry metric, Object value) {
        String unit = metric.getUnit();
        if (value instanceof MetricTag metrictag) {
            Map<String, String> attributes = metrictag.getAttributes();
            String unitMetricAttribute = attributes.get("unit");
            if (Objects.nonNull(unitMetricAttribute)) {
                unit = unitMetricAttribute;
                attributes.remove("unit");
            }
        }
        return unit;
    }

    private Map<String, Object> getValueMap(Object value) {
        Map<String, Object> valueMap = new HashMap<>();
        if (value instanceof MetricTag metrictag) {
            Long lValue = Long.parseLong(metrictag.getValue());
            valueMap.put(METRIC_VALUE, lValue);
        } else {
            valueMap.put(METRIC_VALUE, value);
        }
        return valueMap;
    }

    public static String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("").replace("đ", "d");
    }
}