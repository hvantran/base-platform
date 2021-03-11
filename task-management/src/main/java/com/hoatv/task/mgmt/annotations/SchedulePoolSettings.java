package com.hoatv.task.mgmt.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SchedulePoolSettings {

    String application() default "";

    ThreadPoolSettings threadPoolSettings();
}
