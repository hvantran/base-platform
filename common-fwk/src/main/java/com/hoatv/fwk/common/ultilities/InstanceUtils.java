package com.hoatv.fwk.common.ultilities;

import com.hoatv.fwk.common.services.CheckedSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

public class InstanceUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceUtils.class);

    public static Object newInstance(Class klass) {
        CheckedSupplier<Object> instanceSup = () -> klass.getDeclaredConstructor().newInstance();
        return instanceSup.get();
    }
}
