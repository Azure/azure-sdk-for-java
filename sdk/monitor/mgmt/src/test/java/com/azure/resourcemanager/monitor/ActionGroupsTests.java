// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.monitor.models.ActionGroup;
import com.azure.resourcemanager.resources.core.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ActionGroupsTests extends MonitorManagementTest {
    private String rgName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("jMonitor_", 18);

        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void canCRUDActionGroups() throws Exception {

        ActionGroup ag =
            monitorManager
                .actionGroups()
                .define("simpleActionGroup")
                .withNewResourceGroup(rgName, Region.AUSTRALIA_SOUTHEAST)
                .defineReceiver("first")
                .withPushNotification("azurepush@outlook.com")
                .withEmail("justemail@outlook.com")
                .withSms("1", "4255655665")
                .withVoice("1", "2062066050")
                .withWebhook("https://www.rate.am")
                .attach()
                .defineReceiver("second")
                .withEmail("secondemail@outlook.com")
                .withWebhook("https://www.spyur.am")
                .attach()
                .create();
        Assertions.assertNotNull(ag);
        Assertions.assertEquals("simpleAction", ag.shortName());
        Assertions.assertNotNull(ag.pushNotificationReceivers());
        Assertions.assertEquals(1, ag.pushNotificationReceivers().size());
        Assertions.assertNotNull(ag.smsReceivers());
        Assertions.assertEquals(1, ag.smsReceivers().size());
        Assertions.assertNotNull(ag.voiceReceivers());
        Assertions.assertEquals(1, ag.voiceReceivers().size());
        Assertions.assertNotNull(ag.emailReceivers());
        Assertions.assertEquals(2, ag.emailReceivers().size());
        Assertions.assertNotNull(ag.webhookReceivers());
        Assertions.assertEquals(2, ag.webhookReceivers().size());
        Assertions.assertTrue(ag.emailReceivers().get(0).name().startsWith("first"));
        Assertions.assertTrue(ag.emailReceivers().get(1).name().startsWith("second"));

        ag
            .update()
            .defineReceiver("third")
            .withWebhook("https://www.news.am")
            .attach()
            .updateReceiver("first")
            .withoutSms()
            .parent()
            .withoutReceiver("second")
            .apply();

        Assertions.assertEquals(2, ag.webhookReceivers().size());
        Assertions.assertEquals(1, ag.emailReceivers().size());
        Assertions.assertEquals(0, ag.smsReceivers().size());

        ActionGroup agGet = monitorManager.actionGroups().getById(ag.id());
        Assertions.assertEquals("simpleAction", agGet.shortName());
        Assertions.assertEquals(2, agGet.webhookReceivers().size());
        Assertions.assertEquals(1, agGet.emailReceivers().size());
        Assertions.assertEquals(0, agGet.smsReceivers().size());

        monitorManager
            .actionGroups()
            .enableReceiver(agGet.resourceGroupName(), agGet.name(), agGet.emailReceivers().get(0).name());

        PagedIterable<ActionGroup> agListByRg = monitorManager.actionGroups().listByResourceGroup(rgName);
        Assertions.assertNotNull(agListByRg);
        Assertions.assertEquals(1, TestUtilities.getSize(agListByRg));

        PagedIterable<ActionGroup> agList = monitorManager.actionGroups().list();
        Assertions.assertNotNull(agListByRg);
        Assertions.assertTrue(TestUtilities.getSize(agListByRg) > 0);

        monitorManager.actionGroups().deleteById(ag.id());
        agListByRg = monitorManager.actionGroups().listByResourceGroup(rgName);
        Assertions.assertEquals(0, TestUtilities.getSize(agListByRg));

        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }
}
