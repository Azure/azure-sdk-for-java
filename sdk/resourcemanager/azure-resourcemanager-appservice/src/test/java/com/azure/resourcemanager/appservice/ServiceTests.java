package com.azure.resourcemanager.appservice;

import com.azure.core.test.annotation.DoNotRecord;
import org.junit.jupiter.api.Test;

public class ServiceTests extends AppServiceTest {

    @DoNotRecord(skipInPlayback = true)
    @Test
    public void canAccess() {
        appServiceManager.webApps().list().stream().count();
        appServiceManager.appServicePlans().list().stream().count();
        appServiceManager.domains().list().stream().count();
        appServiceManager.certificates().list().stream().count();
        appServiceManager.certificateOrders().list().stream().count();
    }

    @Override
    protected void cleanUpResources() {
        // NOOP
    }
}
