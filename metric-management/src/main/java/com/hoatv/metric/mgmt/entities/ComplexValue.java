package com.hoatv.metric.mgmt.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Setter
@RequiredArgsConstructor
public class ComplexValue {

    private Collection<MetricTag> tags;

    public Collection<MetricTag> getTags() {
        return List.copyOf(tags);
    }
}
