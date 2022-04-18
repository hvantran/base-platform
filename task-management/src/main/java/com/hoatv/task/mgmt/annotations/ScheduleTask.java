package com.hoatv.task.mgmt.annotations;

import java.lang.annotation.*;


@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ScheduleTask {

    String application() default "";

    String name();

    long delay() default 0;

    long period() default 0;

}
