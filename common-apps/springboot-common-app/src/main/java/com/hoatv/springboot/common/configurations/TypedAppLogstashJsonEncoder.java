package com.hoatv.springboot.common.configurations;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.ContextAware;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.LogstashFormatter;
import net.logstash.logback.composite.AbstractCompositeJsonFormatter;
import net.logstash.logback.composite.JsonProvider;
import net.logstash.logback.composite.loggingevent.MdcJsonProvider;
import net.logstash.logback.encoder.LogstashEncoder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * TypedAppLogstashJsonEncoder class is used to dynamic type of logging events on the fly
 */
public class TypedAppLogstashJsonEncoder extends LogstashEncoder {

    @Override
    protected AbstractCompositeJsonFormatter<ILoggingEvent> createFormatter() {
        return new TypedAppLogstashFormatter(this);
    }

    protected TypedAppLogstashFormatter getFormatter() {
        return (TypedAppLogstashFormatter) super.getFormatter();
    }

    public static class TypeAppMdcJsonProvider extends MdcJsonProvider {

        private static Map<String, String> getMdcProperties(ILoggingEvent event) {
            Map<String, String> loggerContextProperties = event.getLoggerContextVO()
                    .getPropertyMap();
            Map<String, String> currentMdcProperties = event.getMDCPropertyMap();

            Map<String, String> mdcProperties = new HashMap<>();
            mdcProperties.putAll(currentMdcProperties);
            String appName = loggerContextProperties.get("app_name");
            String isoDate = loggerContextProperties.get("iso_date");
            String defaultType = String.format("%s-%s", appName, isoDate);
            mdcProperties.putIfAbsent("type", defaultType);
            return mdcProperties;
        }

        @Override
        public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
            Map<String, String> mdcProperties = getMdcProperties(event);
            if (mdcProperties != null && !mdcProperties.isEmpty()) {

                boolean hasWrittenStart = false;

                for (Map.Entry<String, String> entry : mdcProperties.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null
                            && (getIncludeMdcKeyNames().isEmpty() || getIncludeMdcKeyNames().contains(entry.getKey()))
                            && (getExcludeMdcKeyNames().isEmpty() || !getExcludeMdcKeyNames().contains(entry.getKey()))) {

                        String fieldName = getMdcKeyFieldNames().get(entry.getKey());
                        if (fieldName == null) {
                            fieldName = entry.getKey();
                        }
                        if (!hasWrittenStart && getFieldName() != null) {
                            generator.writeObjectFieldStart(getFieldName());
                            hasWrittenStart = true;
                        }
                        generator.writeFieldName(fieldName);
                        generator.writeObject(entry.getValue());
                    }
                }
                if (hasWrittenStart) {
                    generator.writeEndObject();
                }
            }
        }
    }

    public static class TypedAppLogstashFormatter extends LogstashFormatter {
        public TypedAppLogstashFormatter(ContextAware declaredOrigin) {
            super(declaredOrigin);

            Optional<JsonProvider<ILoggingEvent>> oldProvider = getProviders().getProviders()
                    .stream()
                    .filter(o -> o.getClass() == MdcJsonProvider.class)
                    .findFirst();

            if (oldProvider.isPresent()) {
                getProviders().removeProvider(oldProvider.get());
                getProviders().addProvider(new TypeAppMdcJsonProvider());
            }
        }
    }
}
