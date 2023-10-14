package com.hoatv.fwk.common.ultilities;

import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.Map;

public class MDCUtils {

    private MDCUtils() {

    }

    public static void includeProperties(Map<String, String> properties, Logger logger, String message, Object... objects) {
        properties.forEach(MDC::put);
        logger.info(message, objects);
    }
}
