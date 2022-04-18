package com.hoatv.task.mgmt.entities;

import com.hoatv.fwk.common.services.BiCheckedFunction;
import com.hoatv.fwk.common.ultilities.ObjectUtils;
import com.hoatv.task.mgmt.annotations.ScheduleApplication;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class TaskCollection {
    private static final Logger APP_LOGGER = LoggerFactory.getLogger(TaskCollection.class);

    private String applicationName;

    private List<TaskEntry> taskEntries;

    public static TaskCollection fromObject(Object applicationObj) {

        ScheduleApplication scheduleApplication = applicationObj.getClass().getAnnotation(ScheduleApplication.class);
        ObjectUtils.checkThenThrow(scheduleApplication == null, "ScheduleApplication must be annotated in target object");
        String application = scheduleApplication.application();

        BiCheckedFunction<Object, Method, TaskEntry> taskEntryOnMethods = TaskEntry.fromMethod();
        TaskCollection taskCollection = new TaskCollection(application, new ArrayList<>());
        List<TaskEntry> taskEntries = taskCollection.getTaskEntries();

        Method[] methods = applicationObj.getClass().getMethods();
        List<TaskEntry> taskHandlerOnMethods = Arrays.stream(methods)
                .filter(method -> TaskEntry.getScheduleTask(applicationObj, method).isPresent())
                .map(method -> taskEntryOnMethods.apply(applicationObj, method))
                .collect(Collectors.toList());

        APP_LOGGER.info("Number of schedule tasks for application: {} on its method - {}", application, taskHandlerOnMethods.size());
        taskEntries.addAll(taskHandlerOnMethods);
        return taskCollection;
    }
}
