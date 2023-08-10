package com.hoatv.springboot.common.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("@annotation(com.hoatv.monitor.mgmt.LoggingMonitor)")
    public Object logMethodExecution(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String methodName = proceedingJoinPoint.getSignature().getName();
        long start = System.currentTimeMillis();
        LOGGER.debug("Processing method {} - {}", methodName, proceedingJoinPoint.getArgs());
        Object returnValue = proceedingJoinPoint.proceed();
        long end = System.currentTimeMillis();
        LOGGER.info("Method {} execution time: {} ms", methodName, (end - start));
        return returnValue;
    }
}
