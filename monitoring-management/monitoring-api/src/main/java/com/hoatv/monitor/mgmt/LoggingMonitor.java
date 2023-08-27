package com.hoatv.monitor.mgmt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface LoggingMonitor {

    /**
     * The summary of method. It supports display parameters of execution method
     * Example:
     * "Hello ${argument0}" on method with first argument is Nick. Result is "Hello Nick"
     * @return
     */
    String description() default "";
}
