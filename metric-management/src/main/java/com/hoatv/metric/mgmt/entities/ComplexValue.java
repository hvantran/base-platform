package com.hoatv.metric.mgmt.entities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@RequiredArgsConstructor
public class ComplexValue {

    private Collection<MetricTag> tags;
}
