package com.hoatv.springboot.common.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TimingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimingAspect.class);

    @Around("@annotation(com.hoatv.monitor.mgmt.TimingMonitor)")
    public Object logMethodExecutionTime(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object returnValue = proceedingJoinPoint.proceed();
        long end = System.currentTimeMillis();
        LOGGER.info("Method {} execution time: {} ms", proceedingJoinPoint.getSignature().getName(), (end - start));
        return returnValue;
    }
}
