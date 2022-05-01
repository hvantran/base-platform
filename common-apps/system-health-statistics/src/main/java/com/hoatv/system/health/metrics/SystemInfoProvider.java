package com.hoatv.system.health.metrics;


import com.hoatv.metric.mgmt.annotations.Metric;
import com.hoatv.metric.mgmt.annotations.MetricProvider;
import com.hoatv.metric.mgmt.entities.SimpleValue;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

import static com.hoatv.fwk.common.constants.Constants.SYSTEM_APPLICATION;

@MetricProvider(application = SYSTEM_APPLICATION, category = SYSTEM_APPLICATION)
public class SystemInfoProvider {
    private final SimpleValue initHeapSpace = new SimpleValue(0);
    private final SimpleValue usedHeapSpace = new SimpleValue(0);
    private final SimpleValue freeHeapSpace = new SimpleValue(0);
    private final SimpleValue cpuUsage = new SimpleValue(0);
    private final SimpleValue noThreads = new SimpleValue(0);
    private static final int MB = 1024 * 1024;
    private final Runtime runtime = Runtime.getRuntime();

    @Metric(name = "max-memory", unit = "MB")
    public SimpleValue getInitialHeapSpaceMetric() {
        initHeapSpace.setValue(runtime.maxMemory() / MB);
        return initHeapSpace;
    }

    @Metric(name = "used-memory", unit = "MB")
    public SimpleValue getUsedHeapSpaceMetric() {
        usedHeapSpace.setValue((runtime.totalMemory() - runtime.freeMemory())/ MB);
        return usedHeapSpace;
    }

    @Metric(name = "free-memory", unit = "MB")
    public SimpleValue getFreeHeapSpaceMetric() {
        freeHeapSpace.setValue(runtime.freeMemory() / MB);
        return freeHeapSpace;
    }

    @Metric(name = "cpu-usage")
    public SimpleValue getCPUUsage() {
        OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        cpuUsage.setValue((long) operatingSystemMXBean.getProcessCpuLoad());
        return cpuUsage;
    }

    @Metric(name = "number-of-threads")
    public SimpleValue getNumberOfThreads() {
        noThreads.setValue(ManagementFactory.getThreadMXBean().getThreadCount());
        return noThreads;
    }

}

