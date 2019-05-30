package com.azure.storage.queue;

import com.azure.core.test.TestBase;
import org.junit.Test;

public abstract class MessageIdClientTestsBase extends TestBase {
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
    public abstract void update();

    @Test
    public abstract void delete();
}
