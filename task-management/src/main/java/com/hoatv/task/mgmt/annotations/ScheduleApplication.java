package com.hoatv.task.mgmt.annotations;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ScheduleApplication {

    String application();

    long delay() default 0;

    long period() default 0;

}
