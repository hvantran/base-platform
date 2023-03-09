package com.hoatv.springboot.common.aspects;

import com.hoatv.system.health.metrics.MethodStatisticCollector;
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

    private MethodStatisticCollector methodStatisticCollector;

    public TimingAspect(MethodStatisticCollector methodStatisticCollector) {
        this.methodStatisticCollector = methodStatisticCollector;
    }

    @Around("@annotation(com.hoatv.monitor.mgmt.TimingMetricMonitor)")
    public Object logMethodExecutionTime(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object returnValue = proceedingJoinPoint.proceed();
        long end = System.currentTimeMillis();
        String methodName = proceedingJoinPoint.getSignature().getName();
        LOGGER.info("Method {} execution time: {} ms", methodName, (end - start));
        methodStatisticCollector.addMethodStatistics(methodName, "ms", (end - start));
        return returnValue;
    }
}
