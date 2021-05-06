package com.hoatv.task.mgmt.entities;

import com.hoatv.fwk.common.services.BiCheckedFunction;
import com.hoatv.fwk.common.services.CheckedFunction;
import com.hoatv.fwk.common.ultilities.ObjectUtils;
import com.hoatv.task.mgmt.annotations.ScheduleApplication;
import com.hoatv.task.mgmt.annotations.ScheduleTask;
import lombok.*;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

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

    public static CheckedFunction<Object, TaskEntry> fromObject(ScheduleApplication scheduleApplication) {
        return taskHandlerObj -> {
            ObjectUtils.checkThenThrow(!(taskHandlerObj instanceof Callable<?>), "Schedule task class must be implemented Callable");
            ScheduleTask annotation = taskHandlerObj.getClass().getAnnotation(ScheduleTask.class);
            Callable<?> callable = (Callable<?>) taskHandlerObj;
            long period = annotation.period() != 0 ? annotation.period() : scheduleApplication.period();
            long delay = annotation.delay() != 0 ? annotation.delay() : scheduleApplication.delay();
            return new TaskEntry(annotation.name(), annotation.application(), callable, delay, period);
        };
    }

    public static BiCheckedFunction<Object, Method, TaskEntry> fromMethod(Object... args) {
        return (instance, method) -> {
            ScheduleApplication scheduleApplication = instance.getClass().getAnnotation(ScheduleApplication.class);
            ObjectUtils.checkThenThrow(scheduleApplication == null, "ScheduleApplication should be annotated in instance");
            ScheduleTask annotation = method.getAnnotation(ScheduleTask.class);
            ObjectUtils.checkThenThrow(annotation == null, "ScheduleTask should be annotated in instance method");
            Callable<?> callable = () -> method.invoke(instance);
            long period = annotation.period() != 0 ? annotation.period() : scheduleApplication.period();
            long delay = annotation.delay() != 0 ? annotation.delay() : scheduleApplication.delay();
            return new TaskEntry(annotation.name(), scheduleApplication.application(), callable, delay, period);
        };
    }
}
