package com.hoatv.task.mgmt.entities;

import com.hoatv.fwk.common.services.BiCheckedFunction;
import com.hoatv.fwk.common.services.CheckedFunction;
import com.hoatv.task.mgmt.annotations.ScheduleApplication;
import com.hoatv.task.mgmt.annotations.ScheduleTask;
import lombok.*;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.Callable;

import static com.hoatv.fwk.common.ultilities.ObjectUtils.checkThenThrow;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class TaskEntry {

    @NonNull
    private String name;

    @NonNull
    private String applicationName;

    @NonNull
    private Callable<?> taskHandler;

    @NonNull
    private long delayInMillis;

    @NonNull
    private long periodInMillis;

    /**
     * Generate a TaskEntry from Callable instance with provide a ScheduleApplication
     * @param scheduleApplication
     * @return A TaskEntry
     */
    public static CheckedFunction<Object, TaskEntry> fromObject(ScheduleApplication scheduleApplication) {
        return taskHandlerObj -> {
            checkThenThrow(!(taskHandlerObj instanceof Callable<?>),
                    "Schedule task class must be implemented Callable");
            ScheduleTask annotation = taskHandlerObj.getClass().getAnnotation(ScheduleTask.class);
            Callable<?> callable = (Callable<?>) taskHandlerObj;
            long period = annotation.period() != 0 ? annotation.period() : scheduleApplication.period();
            long delay = annotation.delay() != 0 ? annotation.delay() : scheduleApplication.delay();
            return new TaskEntry(annotation.name(), annotation.application(), callable, delay, period);
        };
    }

    /**
     * Generate a TaskEntry from a Callable instance with provide task name and application
     * @param taskName
     * @param applicationName
     * @return A TaskEntry
     */
    public static CheckedFunction<Object, TaskEntry> fromObject(String taskName, String applicationName) {
        return taskHandlerObj -> {
            Callable<?> callable = (Callable<?>) taskHandlerObj;
            return new TaskEntry(taskName, applicationName, callable, 0, 0);
        };
    }

    /**
     * Generate a TaskEntry from a Method with provide task name, application, instance class, and destination method
     * @param taskName
     * @param applicationName
     * @return A TaskEntry
     */
    public static BiCheckedFunction<Object, Method, TaskEntry> fromMethod(String taskName, String applicationName,
            Object... methodArgs) {
        return (instance, method) -> {
            Callable<?> callable = () -> method.invoke(instance, methodArgs);
            return new TaskEntry(taskName, applicationName, callable, 0, 0);
        };
    }


    /**
     * Generate a TaskEntry from a Method with provide instance class with ScheduleApplication annotation, and destination method
     * @return A TaskEntry
     */
    public static BiCheckedFunction<Object, Method, TaskEntry> fromMethod(Object... methodArgs) {
        return (instance, method) -> {
            ScheduleApplication scheduleApplication = instance.getClass().getAnnotation(ScheduleApplication.class);
            checkThenThrow(scheduleApplication == null, "ScheduleApplication should be annotated in instance");
            Optional<TaskEntry> taskEntry = getScheduleTask(instance, method).map(annotation -> {
                Callable<?> callable = () -> method.invoke(instance, methodArgs);
                long period = annotation.period() > 0 ? annotation.period() : scheduleApplication.period();
                long delay = annotation.delay() > 0 ? annotation.delay() : scheduleApplication.delay();
                return new TaskEntry(annotation.name(), scheduleApplication.application(), callable, delay, period);
            });

            checkThenThrow(taskEntry.isEmpty(), "ScheduleTask should be annotated in instance method");
            return taskEntry.get();
        };
    }

    public static Optional<ScheduleTask> getScheduleTask(Object instance, Method method) {
        ScheduleTask annotation = method.getAnnotation(ScheduleTask.class);
        if (annotation != null) {
            return Optional.of(annotation);
        }
        Class<?> superclass = instance.getClass().getSuperclass();
        try {
            return Optional.of(superclass
                    .getDeclaredMethod(method.getName())
                    .getAnnotation(ScheduleTask.class));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    public static BiCheckedFunction<Object, Method, TaskEntry> fromMethodWithParams(String name, long period,
            long delay, Object... methodArgs) {
        return (instance, method) -> {
            ScheduleApplication scheduleApplication = instance.getClass().getAnnotation(ScheduleApplication.class);
            checkThenThrow(scheduleApplication == null, "ScheduleApplication should be annotated in instance");
            Callable<?> callable = () -> method.invoke(instance, methodArgs);
            long periodParam = period > 0 ? period : scheduleApplication.period();
            long delayParam = delay > 0 ? delay : scheduleApplication.delay();
            return new TaskEntry(name, scheduleApplication.application(), callable, delayParam, periodParam);
        };
    }
}
