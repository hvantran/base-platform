package com.hoatv.springboot.common.aspects;

import com.hoatv.springboot.common.configurations.InitializeConfigurations;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TimingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitializeConfigurations.class);

    @Around("@annotation(com.hoatv.monitor.mgmt.TimingMonitor)")
    public void logMethodExecutionTime(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        proceedingJoinPoint.proceed();
        long end = System.currentTimeMillis();
        LOGGER.info("Method {} execution time: {} ms", proceedingJoinPoint.getSignature().getName(), (end - start));
    }
}
