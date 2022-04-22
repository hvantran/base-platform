package com.hoatv.ext.endpoint.models;

import lombok.*;
import org.apache.commons.lang3.time.DurationFormatUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class EndpointExecutionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ToString.Exclude
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "endpointSettingId", referencedColumnName = "id")
    private EndpointSetting endpointSetting;

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime endedAt;

    @Column
    private Integer numberOfTasks;

    @Column
    private Integer numberOfCompletedTasks;

    @Column
    private Integer percentComplete;

    @Column
    private String elapsedTime;

    @PrePersist
    public void prePersist() {
        startedAt = LocalDateTime.now();
        percentComplete = 0;
        numberOfCompletedTasks = 0;
    }

    @PreUpdate
    public void preUpdate() {
        if (Objects.nonNull(endedAt)) {
            long elapsedTimeMillis = ChronoUnit.MILLIS.between(startedAt, endedAt);
            elapsedTime = DurationFormatUtils.formatDuration(elapsedTimeMillis, "HH:mm:ss.S");
        }
    }
}
