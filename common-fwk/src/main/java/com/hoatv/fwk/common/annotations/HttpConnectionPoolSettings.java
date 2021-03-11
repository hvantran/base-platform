package com.hoatv.fwk.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface HttpConnectionPoolSettings {
    String name() default "";

    int httpClientCorePoolSize();

    int maxWaitMillis() default -1;
}