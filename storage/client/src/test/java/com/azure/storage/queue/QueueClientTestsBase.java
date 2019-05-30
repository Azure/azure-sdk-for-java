package com.azure.storage.queue;

import com.azure.core.test.TestBase;
import org.junit.Test;

public abstract class QueueClientTestsBase extends TestBase {
    @Override
    protected String testName() {
        return null;
    }

    @Override
    protected void beforeTest() {
    }

    @Override
    protected void afterTest() {
    }

    @Test
    public abstract void createQueue();

    @Test
    public abstract void deleteQueue();

    @Test
    public abstract void getProperties();

    @Test
    public abstract void setMetadata();

    @Test
    public abstract void getAccessPolicy();

    @Test
    public abstract void setAccessPolicy();
}
