package com.hoatv.springboot.common.configurations;

import com.hoatv.fwk.common.services.CheckedConsumer;
import com.hoatv.fwk.common.services.HttpClientFactory;
import com.hoatv.fwk.common.ultilities.ObjectUtils;
import com.hoatv.metric.mgmt.annotations.MetricProvider;
import com.hoatv.metric.mgmt.annotations.MetricRegistry;
import com.hoatv.metric.mgmt.services.MetricMgmtService;
import com.hoatv.metric.mgmt.services.MetricProviderRegistry;
import com.hoatv.system.health.metrics.MethodStatisticCollector;
import com.hoatv.system.health.metrics.SystemInfoProvider;
import com.hoatv.task.mgmt.annotations.SchedulePoolSettings;
import com.hoatv.task.mgmt.entities.TaskCollection;
import com.hoatv.task.mgmt.services.ScheduleTaskMgmtService;
import com.hoatv.task.mgmt.services.ScheduleTaskRegistryService;
import com.hoatv.task.mgmt.services.TaskFactory;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.apache.commons.collections4.CollectionUtils;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class InitializeConfigurations {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitializeConfigurations.class);

    @Bean
    public MethodStatisticCollector getMethodStatistics() {
        return new MethodStatisticCollector();
    }

    @Bean
    public SystemInfoProvider getSystemInfoProvider() {
        return new SystemInfoProvider();
    }

    @Bean
    public MetricProviderRegistry getMetricProviderRegistry() {
        return new MetricProviderRegistry();
    }

    @Bean
    public MetricMgmtService getMetricMgmtService(MetricProviderRegistry metricProviderRegistry) {
        return new MetricMgmtService(metricProviderRegistry);
    }

    @Bean
    public ServletListenerRegistrationBean<ServletContextListener> servletListener() {
        ServletListenerRegistrationBean<ServletContextListener> srb = new ServletListenerRegistrationBean<>();
        srb.setListener(new ApplicationServletContextListener());
        return srb;
    }

    @Bean(destroyMethod = "destroy")
    public ScheduleTaskRegistryService getScheduleTaskRegistryService() {
        return new ScheduleTaskRegistryService();
    }

    @Bean
    public CommandLineRunner getCommandLineRunner(ApplicationContext ctx) {
        return args -> {
            LOGGER.info("Let's inspect the beans provided by Spring Boot");
            String[] metricProviderBeanNames = ctx.getBeanNamesForAnnotation(MetricProvider.class);
            String[] metricRegistryBeanNames = ctx.getBeanNamesForAnnotation(MetricRegistry.class);
            String[] schedulePoolSettingBeanNames = ctx.getBeanNamesForAnnotation(SchedulePoolSettings.class);

            List<Object> metricProviders = Stream.of(metricProviderBeanNames).map(ctx::getBean).collect(Collectors.toList());
            List<Object> metricRegistries = Stream.of(metricRegistryBeanNames).map(ctx::getBean).collect(Collectors.toList());
            List<Object> poolSettings = Stream.of(schedulePoolSettingBeanNames).map(ctx::getBean).collect(Collectors.toList());
            ObjectUtils.checkThenThrow(metricRegistries.size() != 1, "Required at least one of Metric Registry annotation");
            Object metricRegistry = metricRegistries.stream().findFirst().orElseThrow();
            ObjectUtils.checkThenThrow(!(metricRegistry instanceof MetricProviderRegistry), "This must be an instance of MetricProviderRegistry");
            MetricProviderRegistry metricProviderRegistry = (MetricProviderRegistry) metricRegistry;
            metricProviderRegistry.loadFromObjects(metricProviders);

            ScheduleTaskRegistryService scheduleTaskExecutorService = ctx.getBean(ScheduleTaskRegistryService.class);

            CheckedConsumer<Object> scheduleTaskExecutorService1 = applicationObj -> {
                SchedulePoolSettings schedulePoolSettings = applicationObj.getClass().getAnnotation(SchedulePoolSettings.class);
                ScheduleTaskMgmtService taskMgmtExecutorV1 = TaskFactory.INSTANCE.newScheduleTaskMgmtService(schedulePoolSettings);

                Predicate<Field> isContainScheduleTaskMgmtServiceField =
                        field -> "scheduleTaskMgmtService".equals(field.getName());
                CheckedConsumer<Field> scheduleTaskSettingConsumer = field -> field.set(applicationObj, taskMgmtExecutorV1);
                Set<Field> fields = ReflectionUtils.getFields(applicationObj.getClass(), isContainScheduleTaskMgmtServiceField);
                fields.forEach(scheduleTaskSettingConsumer);

                // Using set method to set scheduleTaskMgmtService
                if (CollectionUtils.isEmpty(fields)) {
                    Predicate<Method> isContainSetScheduleTaskMgmtServiceMethod =
                            method -> "setScheduleTaskMgmtService".equals(method.getName());
                    CheckedConsumer<Method> setScheduleTaskForInstance =
                            method -> method.invoke(applicationObj, taskMgmtExecutorV1);
                    ReflectionUtils.getMethods(applicationObj.getClass(), isContainSetScheduleTaskMgmtServiceMethod)
                            .forEach(setScheduleTaskForInstance);
                }
                scheduleTaskExecutorService.register(schedulePoolSettings.application(), taskMgmtExecutorV1);
                TaskCollection taskCollection = TaskCollection.fromObject(applicationObj);
                taskMgmtExecutorV1.scheduleTasks(taskCollection, 5000, TimeUnit.MILLISECONDS);
            };
            poolSettings.forEach(scheduleTaskExecutorService1);
        };
    }

    public static class ApplicationServletContextListener implements ServletContextListener {

        @Override
        public void contextDestroyed(ServletContextEvent event) {
            LOGGER.info("Application context is destroyed");
            HttpClientFactory.INSTANCE.destroy();
            TaskFactory.INSTANCE.destroy();
        }

        @Override
        public void contextInitialized(ServletContextEvent event) {
            LOGGER.info("Application context is initialized");
        }
    }
}
