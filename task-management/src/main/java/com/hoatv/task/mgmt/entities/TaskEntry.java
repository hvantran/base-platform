package com.hoatv.task.mgmt.entities;

import lombok.*;

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
}
