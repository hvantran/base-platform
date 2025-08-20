package com.hoatv.metric.mgmt.consumers;

import static com.hoatv.fwk.common.constants.Constants.METRIC_LOG_APP_NAME;
import static com.hoatv.fwk.common.ultilities.StringCommonUtils.deAccent;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.hoatv.fwk.common.ultilities.StringCommonUtils;
import com.hoatv.metric.mgmt.annotations.MetricConsumer;
import com.hoatv.metric.mgmt.entities.ComplexValue;
import com.hoatv.metric.mgmt.entities.MetricTag;
import com.hoatv.metric.mgmt.entities.SimpleValue;
import net.logstash.logback.argument.StructuredArguments;

@MetricConsumer
public class LoggingMetricConsumer implements MetricConsumerHandler {
    private static final Logger METRIC_LOGGER = LoggerFactory.getLogger(METRIC_LOG_APP_NAME);

    private static final String METRIC_NAME = "metric-name";
    private static final String METRIC_VALUE = "metric-value";
    public static final String METRIC_UNIT = "metric-unit";
    public static final String UNIT = "unit";
    public static final String NAME_PROPERTY = "name";

    @Override
    public void consume(String application, String category, String name, Object value, String unit) {
        try {
            MDC.put("application", application);
            MDC.put("category", category);

            switch (value) {
                case SimpleValue simpleValue -> processSimpleValue(unit, name, simpleValue);
                case String metricValue -> {
                    try {
                        Long metricValueLong = NumberUtils.createLong(metricValue);
                        processSimpleValue(unit, name, metricValueLong);
                    } catch (NumberFormatException exception) {
                        Double metricValueDouble = NumberUtils.createDouble(metricValue);
                        processSimpleValue(unit, name, metricValueDouble);
                    }
                }
                case Long metricValue -> processSimpleValue(unit, name, metricValue);
                case Integer metricValue -> processSimpleValue(unit, name, metricValue.longValue());
                case Double metricValue -> processSimpleValue(unit, name, metricValue);
                case Collection<?> objects -> {
                    @SuppressWarnings("unchecked")
                    Collection<ComplexValue> complexValues = (Collection<ComplexValue>) value;
                    complexValues.forEach(complexValue -> processComplexValue(unit, name, complexValue));
                }
                case null, default -> processComplexValue(unit, name, (ComplexValue) value);
            }
        } finally {
            MDC.clear();
        }
    }

    private void processSimpleValue(String unit, String name, SimpleValue simpleValue) {
        MDC.put(METRIC_NAME, name);
        logMetricRecord(name, simpleValue.getValue(), unit);
    }

    private void processSimpleValue(String unit, String name, Long simpleValue) {
        MDC.put(METRIC_NAME, name);
        logMetricRecord(name, simpleValue, unit);
    }

    private void processSimpleValue(String unit, String name, Double simpleValue) {
        MDC.put(METRIC_NAME, name);
        logMetricRecord(name, simpleValue, unit);
    }

    private void processComplexValue(String unit, String name, ComplexValue complexValue) {
        Collection<MetricTag> metricTags = complexValue.getTags();

        for (MetricTag metricTag : metricTags) {
            String metricUnit = getMetricUnit(unit, metricTag);
            Map<String, String> attributes = metricTag.getAttributes();
            String nameTag = attributes.get(NAME_PROPERTY);
            String metricNameCompute = name;
            MDC.put(METRIC_UNIT, metricUnit);
            if (attributes.isEmpty()) {
                MDC.put(METRIC_NAME, name);
            } else if (Objects.nonNull(nameTag)) {
                Predicate<Map.Entry<String, String>> filterOutName = p -> !NAME_PROPERTY.equals(p.getKey());
                Map<String, String> newAttributes =
                        attributes.entrySet()
                                .stream()
                                .filter(filterOutName)
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                String nameReplaced = nameTag.toLowerCase().replace(" ", "-");
                String attNames = newAttributes.values().stream().map(StringCommonUtils::deAccent).collect(Collectors.joining("-"));

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

    private String getMetricUnit(String unit, Object value) {
        if (value instanceof MetricTag metrictag) {
            Map<String, String> attributes = metrictag.getAttributes();
            String unitMetricAttribute = attributes.get(UNIT);
            if (Objects.nonNull(unitMetricAttribute)) {
                unit = unitMetricAttribute;
                attributes.remove(UNIT);
            }
        }
        return unit;
    }

    private Map<String, Object> getValueMap(Object value) {
        if (value instanceof MetricTag metrictag) {
            String metricValue = metrictag.getValue();
            try {
                Long metricValueLong = NumberUtils.createLong(metricValue);
                return Map.of(METRIC_VALUE, metricValueLong);
            } catch (NumberFormatException exception) {
                Double metricValueDouble = NumberUtils.createDouble(metricValue);
                return Map.of(METRIC_VALUE, metricValueDouble);
            }
        }
        return Map.of(METRIC_VALUE, value);
    }
}
