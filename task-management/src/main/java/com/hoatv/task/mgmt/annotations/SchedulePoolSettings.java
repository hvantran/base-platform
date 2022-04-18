package com.hoatv.task.mgmt.annotations;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SchedulePoolSettings {

    String application() default "";

    ThreadPoolSettings threadPoolSettings();
}
