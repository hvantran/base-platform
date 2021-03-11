package com.hoatv.metric.mgmt.entities;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
public class MetricTag {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.##");

    private Map<String, String> attributes = new HashMap<>();

    @NonNull
    private String value;

    @Override
    public String toString() {
        if (NumberUtils.isParsable(value)) {
            return "tags=" + attributes + ", value=" + DECIMAL_FORMAT.format(Double.parseDouble(value));
        }
        return "tags=" + attributes + ", value=" + value;
    }

}
