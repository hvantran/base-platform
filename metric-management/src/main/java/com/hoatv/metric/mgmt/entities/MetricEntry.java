package com.hoatv.metric.mgmt.entities;

import com.hoatv.fwk.common.ultilities.ObjectUtils;
import com.hoatv.metric.mgmt.annotations.Metric;
import lombok.Builder;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.Objects;

@Getter
@Builder
public class MetricEntry {

    private String name;

    private String unit;

    private Method method;

    public static MetricEntry fromMethod(Method method) {
        Metric annotation = method.getAnnotation(Metric.class);
        ObjectUtils.checkThenThrow(Objects::isNull, annotation, "Metric must be annotated in method");
        return MetricEntry.builder()
                .method(method)
                .name(annotation.name())
                .unit(annotation.unit()).build();
    }
}
