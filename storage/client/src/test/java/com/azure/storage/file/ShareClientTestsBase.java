package com.azure.storage.file;

import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.core.test.TestBase;
import org.junit.Rule;
import org.junit.rules.TestName;

public abstract class ShareClientTestsBase extends TestBase {
    private final ServiceLogger logger = new ServiceLogger(ShareClientTestsBase.class);

    String shareName;

    @Rule
    public TestName testName = new TestName();

    @Override
    public String testName() {
        return testName.getMethodName();
    }

    String getShareName() {
        return testResourceNamer.randomName("share", 16).toLowerCase();
    }
}
