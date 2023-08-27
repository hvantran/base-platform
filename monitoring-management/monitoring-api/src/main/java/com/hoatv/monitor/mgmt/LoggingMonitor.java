package com.hoatv.monitor.mgmt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public @interface LoggingMonitor {

    /**
     * The summary of the method.
     * It supports the display parameters of the execution method with ${argument<index>} syntax and also supports getting the property of the object by using getter methods.
     * Example:
     * "Hello ${argument0}" on method with the first argument is Nick. The result is "Hello Nick"
     * "Hello ${argument0.getMessage()}" on method with the first argument is User("Nick"). The result is "Hello Nick"
     * @return
     */
    String description() default "";
}