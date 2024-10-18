package com.hoatv.task.mgmt.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskFactoryTest {

    @Test
    void destroy() {
        TaskFactory.INSTANCE.getTaskMgmtService(1, 1000, "sample");
        TaskFactory.INSTANCE.getTaskMgmtServiceV1(1, 1000, "sample1");
        TaskFactory.INSTANCE.destroy();
        assertEquals(0, TaskFactory.INSTANCE.serviceRegistry.size());
        assertEquals(0, TaskFactory.INSTANCE.serviceRegistryV1.size());
    }

    @Test
    void testDestroy() {
        TaskFactory.INSTANCE.getTaskMgmtService(1, 1000, "sample");
        TaskFactory.INSTANCE.destroy("sample");
        assertEquals(0, TaskFactory.INSTANCE.serviceRegistry.size());
        assertEquals(0, TaskFactory.INSTANCE.serviceRegistryV1.size());
    }
}