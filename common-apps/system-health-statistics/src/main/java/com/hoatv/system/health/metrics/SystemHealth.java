package com.hoatv.system.health.metrics;


import com.hoatv.metric.mgmt.annotations.Metric;
import com.hoatv.metric.mgmt.annotations.MetricProvider;
import com.hoatv.metric.mgmt.entities.SimpleValue;
import com.sun.management.OperatingSystemMXBean;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;

import static com.hoatv.fwk.common.constants.Constants.SYSTEM_APPLICATION;

@Component
@MetricProvider(application = SYSTEM_APPLICATION, category = SYSTEM_APPLICATION)
public class SystemHealth {
    private final SimpleValue initHeapSpace = new SimpleValue(0);
    private final SimpleValue usedHeapSpace = new SimpleValue(0);
    private final SimpleValue freeHeapSpace = new SimpleValue(0);
    private final SimpleValue cpuUsage = new SimpleValue(0);
    private final SimpleValue noThreads = new SimpleValue(0);

    @Metric(name = "max-memory", unit = "B")
    public SimpleValue getInitialHeapSpaceMetric() {
        initHeapSpace.setValue(Runtime.getRuntime().maxMemory());
        return initHeapSpace;
    }

    @Metric(name = "used-memory", unit = "B")
    public SimpleValue getUsedHeapSpaceMetric() {
        usedHeapSpace.setValue(Runtime.getRuntime().totalMemory());
        return usedHeapSpace;
    }

    @Metric(name = "free-memory", unit = "B")
    public SimpleValue getFreeHeapSpaceMetric() {
        freeHeapSpace.setValue(Runtime.getRuntime().freeMemory());
        return freeHeapSpace;
    }

    @Metric(name = "cpu-usage")
    public SimpleValue getCPUUsage() {
        OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
        cpuUsage.setValue((long) operatingSystemMXBean.getProcessCpuLoad());
        return cpuUsage;
    }

    @Metric(name = "number-of-threads")
    public SimpleValue getNumberOfThreads() {
        noThreads.setValue(ManagementFactory.getThreadMXBean().getThreadCount());
        return noThreads;
    }

}

