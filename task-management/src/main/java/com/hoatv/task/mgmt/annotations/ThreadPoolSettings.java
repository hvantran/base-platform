package com.hoatv.task.mgmt.annotations;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ThreadPoolSettings {

    String name() default "";

    int numberOfThreads();

    int maxAwaitTerminationMillis() default 5000;
}
