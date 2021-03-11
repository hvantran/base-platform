package com.hoatv.task.mgmt.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TaskCollection {

    private String applicationName;

    private List<TaskEntry> taskEntries;
}
