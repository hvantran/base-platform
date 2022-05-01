package com.hoatv.fwk.common.ultilities;

import com.hoatv.fwk.common.services.CheckedSupplier;

public class InstanceUtils {

    private InstanceUtils() {

    }

    public static Object newInstance(Class<?> klass) {
        CheckedSupplier<Object> instanceSup = () -> klass.getDeclaredConstructor().newInstance();
        return instanceSup.get();
    }
}
