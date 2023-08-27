package com.hoatv.springboot.common.aspects;

import com.hoatv.fwk.common.ultilities.ObjectStringSubstitutor;
import com.hoatv.monitor.mgmt.LoggingMonitor;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("@annotation(com.hoatv.monitor.mgmt.LoggingMonitor)")
    public Object logMethodExecution(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        long start = System.currentTimeMillis();
        Object[] methodArguments = proceedingJoinPoint.getArgs();
        logMethodDescription(method, methodArguments);

        LOGGER.debug("Processing method {} - {}", methodName, methodArguments);
        Object returnValue = proceedingJoinPoint.proceed();
        long end = System.currentTimeMillis();
        LOGGER.info("Method {} execution time: {} ms", methodName, (end - start));
        return returnValue;
    }

    private void logMethodDescription(Method method, Object[] methodArguments) {

        LoggingMonitor loggingMonitor = method.getAnnotation(LoggingMonitor.class);
        String descriptionFormat = loggingMonitor.description();
        if (StringUtils.isEmpty(descriptionFormat)) {
            return;
        }

        Map<String, Object> argumentMapper = IntStream.range(0, methodArguments.length)
                .mapToObj(index -> new SimpleEntry<>("argument" + index, methodArguments[index]))
                .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
        ObjectStringSubstitutor objectStringSubstitutor = new ObjectStringSubstitutor(argumentMapper, "{", "}");
        String description = objectStringSubstitutor.replace(descriptionFormat);
        LOGGER.info("{}", description);
    }
}
