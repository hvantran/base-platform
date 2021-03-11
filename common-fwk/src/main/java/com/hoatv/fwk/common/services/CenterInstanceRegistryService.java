package com.hoatv.fwk.common.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum CenterInstanceRegistryService {

    INSTANCE;

    private Map<String, Object> mouseRegistry = new ConcurrentHashMap<>();

    public Object getInstance(String application) {
        return mouseRegistry.get(application);
    }

    public void setInstance(String application, Object object) {
        mouseRegistry.put(application, object);
    }
}
