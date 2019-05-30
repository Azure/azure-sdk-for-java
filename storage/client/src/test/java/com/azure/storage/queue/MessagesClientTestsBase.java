package com.azure.storage.queue;

import com.azure.core.test.TestBase;
import org.junit.Test;

public abstract class MessagesClientTestsBase extends TestBase {
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
    public abstract void enqueue();

    @Test
    public abstract void dequeue();

    @Test
    public abstract void peek();

    @Test
    public abstract void clear();
}
