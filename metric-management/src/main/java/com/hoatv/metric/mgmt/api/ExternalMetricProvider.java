package com.hoatv.metric.mgmt.api;

import com.hoatv.metric.mgmt.entities.ComplexValue;

import java.util.List;

public interface ExternalMetricProvider {

    List<ComplexValue> getExternalMetricValues();
}
