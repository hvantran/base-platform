package com.hoatv.metric.mgmt.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SimpleValue {

    private Object value;

    @Override
    public String toString() {
        return "value=" + value;
    }
}
