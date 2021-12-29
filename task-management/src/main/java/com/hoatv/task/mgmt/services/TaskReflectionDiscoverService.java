package com.hoatv.task.mgmt.services;

import com.hoatv.fwk.common.services.BiCheckedFunction;
import com.hoatv.fwk.common.services.CheckedFunction;
import com.hoatv.fwk.common.ultilities.InstanceUtils;
import com.hoatv.task.mgmt.annotations.ScheduleApplication;
import com.hoatv.task.mgmt.annotations.SchedulePoolSettings;
import com.hoatv.task.mgmt.annotations.ScheduleTask;
import com.hoatv.task.mgmt.entities.TaskCollection;
import com.hoatv.task.mgmt.entities.TaskEntry;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class TaskReflectionDiscoverService {

    private static final Logger APP_LOGGER = LoggerFactory.getLogger(TaskReflectionDiscoverService.class);

    public Set<Class<?>> getSchedulePoolSettings(String scanPackage) {
        Reflections reflections = new Reflections(scanPackage);
        return reflections.getTypesAnnotatedWith(SchedulePoolSettings.class);
    }

    public List<TaskCollection> getTaskCollection(String scanPackage) {

        Reflections reflections = new Reflections(scanPackage);
        Class<ScheduleApplication> scheduleAppClass = ScheduleApplication.class;

        Set<Class<?>> scheduleAppClasses = reflections.getTypesAnnotatedWith(scheduleAppClass);
        Set<Class<?>> taskHandlerClasses = reflections.getTypesAnnotatedWith(ScheduleTask.class);

        CheckedFunction<Object, TaskCollection> mappingFunction = getTaskCollectionFromApplication(taskHandlerClasses);

        return scheduleAppClasses.stream()
                .map(InstanceUtils::newInstance)
                .map(mappingFunction).collect(Collectors.toList());
    }

    private CheckedFunction<Object, TaskCollection> getTaskCollectionFromApplication(
            Set<Class<?>> taskHandlerClasses) {

        BiPredicate<String, Class<?>> classPredicate = filterApplicationTask();

        return applicationObj -> {
            ScheduleApplication scheduleApplication = applicationObj.getClass().getAnnotation(ScheduleApplication.class);
            String application = scheduleApplication.application();

            CheckedFunction<Class<?>, TaskEntry> taskEntryOnClasses = getScheduleClassTaskEntry(scheduleApplication);
            BiCheckedFunction<Object, Method, TaskEntry> taskEntryOnMethods = getScheduleMethodTaskEntry();
            TaskCollection taskCollection = new TaskCollection(application, new ArrayList<>());
            List<TaskEntry> taskEntries = taskCollection.getTaskEntries();

            List<TaskEntry> taskHandlerOnClasses = taskHandlerClasses.stream()
                    .filter(taskHandlerClass -> classPredicate.test(application, taskHandlerClass))
                    .map(taskEntryOnClasses).collect(Collectors.toList());
            taskEntries.addAll(taskHandlerOnClasses);
            APP_LOGGER.info("Number of schedule tasks for application: {} on classes - {}", application, taskHandlerOnClasses.size());

            Method[] methods = applicationObj.getClass().getMethods();
            List<TaskEntry> taskHandlerOnMethods = Arrays.stream(methods)
                    .filter(method -> Objects.nonNull(method.getAnnotation(ScheduleTask.class)))
                    .map(method -> taskEntryOnMethods.apply(applicationObj, method))
                    .collect(Collectors.toList());
            APP_LOGGER.info("Number of schedule tasks for application: {} on its method - {}", application, taskHandlerOnMethods.size());
            taskEntries.addAll(taskHandlerOnMethods);
            return taskCollection;
        };
    }


    private CheckedFunction<Class<?>, TaskEntry> getScheduleClassTaskEntry(ScheduleApplication scheduleApplication) {
        return taskHandlerClass -> {
            ScheduleTask annotation = taskHandlerClass.getAnnotation(ScheduleTask.class);
            Callable<?> callable = (Callable<?>) InstanceUtils.newInstance(taskHandlerClass);
            long period = annotation.period() != 0 ? annotation.period() : scheduleApplication.period();
            long delay = annotation.delay() != 0 ? annotation.delay() : scheduleApplication.delay();
            return new TaskEntry(annotation.name(), annotation.application(), callable, delay, period);
        };
    }

    private BiPredicate<String, Class<?>> filterApplicationTask() {
        return (application, taskHandlerClass) -> {
            ScheduleTask annotation = taskHandlerClass.getAnnotation(ScheduleTask.class);
            return application.equals(annotation.application());
        };
    }

    private BiCheckedFunction<Object, Method, TaskEntry> getScheduleMethodTaskEntry() {
        return (instance, method) -> {
            ScheduleTask annotation = method.getAnnotation(ScheduleTask.class);
            ScheduleApplication scheduleApplication = instance.getClass().getAnnotation(ScheduleApplication.class);
            Callable<?> callable = () -> method.invoke(instance);
            long period = annotation.period() != 0 ? annotation.period() : scheduleApplication.period();
            long delay = annotation.delay() != 0 ? annotation.delay() : scheduleApplication.delay();
            return new TaskEntry(annotation.name(), scheduleApplication.application(), callable, delay, period);
        };
    }
}
