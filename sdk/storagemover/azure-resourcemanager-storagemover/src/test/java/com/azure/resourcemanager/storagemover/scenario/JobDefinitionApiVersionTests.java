// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storagemover.scenario;

import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.storagemover.models.AzureKeyVaultSmbCredentials;
import com.azure.resourcemanager.storagemover.models.AzureStorageBlobContainerEndpointProperties;
import com.azure.resourcemanager.storagemover.models.AzureStorageSmbFileShareEndpointProperties;
import com.azure.resourcemanager.storagemover.models.Connection;
import com.azure.resourcemanager.storagemover.models.ConnectionProperties;
import com.azure.resourcemanager.storagemover.models.ConnectionStatus;
import com.azure.resourcemanager.storagemover.models.CopyMode;
import com.azure.resourcemanager.storagemover.models.EndpointKind;
import com.azure.resourcemanager.storagemover.models.Frequency;
import com.azure.resourcemanager.storagemover.models.JobDefinition;
import com.azure.resourcemanager.storagemover.models.JobType;
import com.azure.resourcemanager.storagemover.models.ScheduleInfo;
import com.azure.resourcemanager.storagemover.models.SchedulerTime;
import com.azure.resourcemanager.storagemover.models.SmbMountEndpointProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JobDefinitionApiVersionTests extends StorageMoverManagementTestBase {
    private static final String PARTNER_SUBSCRIPTION_ID = "11111111-1111-1111-1111-111111111111";
    private static final String PARTNER_TENANT_ID = "22222222-2222-2222-2222-222222222222";
    private static final String PARTNER_ACCOUNT_ID = "/subscriptions/" + PARTNER_SUBSCRIPTION_ID
        + "/resourceGroups/partner/providers/Microsoft.Storage/storageAccounts/partneraccount";

    @Override
    protected Region testRegion() {
        return Region.fromName("eastus2euap");
    }

    @Test
    public void createWithHourlySchedule() {
        String moverName = createParents("hourly");
        for (String kind : Arrays.asList("Source", "Target")) {
            storageMoverManager.endpoints()
                .define(kind.toLowerCase(java.util.Locale.ROOT))
                .withExistingStorageMover(resourceGroupName, moverName)
                .withProperties(new AzureStorageSmbFileShareEndpointProperties()
                    .withStorageAccountResourceId(FAKE_STORAGE_ACCOUNT_ID)
                    .withFileShareName(kind.toLowerCase(java.util.Locale.ROOT) + "share")
                    .withEndpointKind(EndpointKind.fromString(kind)))
                .create();
        }

        OffsetDateTime start = scheduleStartDate();
        ScheduleInfo schedule = new ScheduleInfo().withFrequency(Frequency.HOURLY)
            .withRepeatInterval("4h")
            .withIsActive(true)
            .withExecutionTime(new SchedulerTime().withHour(9))
            .withStartDate(start)
            .withEndDate(start.plusDays(30));
        JobDefinition job = job(moverName, "hourly", JobType.CLOUD_TO_CLOUD).withSchedule(schedule).create();
        Assertions.assertEquals(Frequency.HOURLY, job.schedule().frequency());
        Assertions.assertEquals("4h", job.schedule().repeatInterval());
        Assertions.assertEquals(Boolean.TRUE, job.schedule().isActive());
        job = job.refresh();
        Assertions.assertEquals(Frequency.HOURLY, job.schedule().frequency());
        Assertions.assertEquals("4h", job.schedule().repeatInterval());

        job = job.update().withSchedule(new ScheduleInfo().withRepeatInterval("6h")).apply();
        Assertions.assertEquals(Frequency.HOURLY, job.schedule().frequency());
        Assertions.assertEquals("6h", job.schedule().repeatInterval());
        Assertions.assertEquals(Boolean.TRUE, job.schedule().isActive());
        job = job.update().withSchedule(new ScheduleInfo().withIsActive(false)).apply();
        Assertions.assertEquals(Boolean.FALSE, job.schedule().isActive());
        Assertions.assertEquals("6h", job.schedule().repeatInterval());

        schedule.withRepeatInterval("5h");
        assertInvalidProperties(
            () -> job(moverName, "hourly-invalid", JobType.CLOUD_TO_CLOUD).withSchedule(schedule).create());
        storageMoverManager.jobDefinitions().delete(resourceGroupName, moverName, "project", "hourly");
    }

    @Test
    public void crossTenantJobValidation() {
        String moverName = createParents("cross-tenant");
        storageMoverManager.endpoints()
            .define("source")
            .withExistingStorageMover(resourceGroupName, moverName)
            .withProperties(
                new AzureStorageBlobContainerEndpointProperties().withStorageAccountResourceId(FAKE_STORAGE_ACCOUNT_ID)
                    .withBlobContainerName("testcontainer")
                    .withEndpointKind(EndpointKind.SOURCE)
                    .withEnableCrossTenantTransfer(true)
                    .withAllowedStorageAccounts(Collections.singletonList(PARTNER_ACCOUNT_ID)))
            .create();
        String partnerEndpointId = "/subscriptions/" + PARTNER_SUBSCRIPTION_ID
            + "/resourceGroups/partner/providers/Microsoft.StorageMover/storageMovers/partner/endpoints/target";
        String localEndpointId = getResourceGroup().id() + "/providers/Microsoft.StorageMover/storageMovers/"
            + moverName + "/endpoints/target";

        assertInvalidProperties(
            () -> job(moverName, "missing-tenant", JobType.CLOUD_TO_CLOUD).withIsCrossTenantJob(true)
                .withCrossTenantEndpointResourceId(partnerEndpointId)
                .create());
        assertInvalidProperties(
            () -> job(moverName, "missing-resource", JobType.CLOUD_TO_CLOUD).withIsCrossTenantJob(true)
                .withCrossTenantEndpointTenantId(PARTNER_TENANT_ID)
                .create());
        assertInvalidProperties(() -> job(moverName, "missing-flag", JobType.CLOUD_TO_CLOUD)
            .withCrossTenantEndpointTenantId(PARTNER_TENANT_ID)
            .withCrossTenantEndpointResourceId(partnerEndpointId)
            .create());
        assertInvalidProperties(
            () -> job(moverName, "not-cloud-to-cloud", JobType.ON_PREM_TO_CLOUD).withIsCrossTenantJob(true)
                .withCrossTenantEndpointTenantId(PARTNER_TENANT_ID)
                .withCrossTenantEndpointResourceId(partnerEndpointId)
                .create());
        assertInvalidProperties(
            () -> job(moverName, "same-subscription", JobType.CLOUD_TO_CLOUD).withIsCrossTenantJob(true)
                .withCrossTenantEndpointTenantId(PARTNER_TENANT_ID)
                .withCrossTenantEndpointResourceId(localEndpointId)
                .create());
        Assertions.assertFalse(
            storageMoverManager.jobDefinitions().list(resourceGroupName, moverName, "project").iterator().hasNext());
    }

    @Test
    public void createAgentlessJobDefinition() {
        String moverName = createParents("agentless");
        storageMoverManager.endpoints()
            .define("source")
            .withExistingStorageMover(resourceGroupName, moverName)
            .withProperties(new SmbMountEndpointProperties().withHost("10.0.0.1")
                .withShareName("testshare")
                .withCredentials(new AzureKeyVaultSmbCredentials()
                    .withUsernameUri("https://examples-azureKeyVault.vault.azure.net/secrets/examples-username")
                    .withPasswordUri("https://examples-azureKeyVault.vault.azure.net/secrets/examples-password")))
            .create();
        storageMoverManager.endpoints()
            .define("target")
            .withExistingStorageMover(resourceGroupName, moverName)
            .withProperties(
                new AzureStorageSmbFileShareEndpointProperties().withStorageAccountResourceId(FAKE_STORAGE_ACCOUNT_ID)
                    .withFileShareName("testfileshare"))
            .create();
        assertInvalidProperties(
            () -> job(moverName, "agentless", JobType.ON_PREM_TO_CLOUD_AGENT_LESS).withSourceSubpath("/")
                .withTargetSubpath("/")
                .create());

        String agentlessPrivateLinkServiceId = "/subscriptions/" + syntheticsSubscriptionId
            + "/resourceGroups/E2E-Management-RGsyn/providers/Microsoft.Network/privateLinkServices/"
            + "E2E-VPC-PLS-E2ETest.Test.ECY";
        Connection connection = storageMoverManager.connections()
            .define("agentless")
            .withExistingStorageMover(resourceGroupName, moverName)
            .withProperties(new ConnectionProperties().withPrivateLinkServiceId(agentlessPrivateLinkServiceId)
                .withDescription("ConnectionForAgentlessJob"))
            .create();
        try {
            Assertions.assertTrue(Arrays.asList(ConnectionStatus.PENDING, ConnectionStatus.APPROVED)
                .contains(connection.properties().connectionStatus()));
            List<String> connections = Collections.singletonList(connection.id());
            JobDefinition job = job(moverName, "agentless", JobType.ON_PREM_TO_CLOUD_AGENT_LESS).withSourceSubpath("/")
                .withTargetSubpath("/")
                .withConnections(connections)
                .create();
            Assertions.assertEquals(JobType.ON_PREM_TO_CLOUD_AGENT_LESS, job.jobType());
            Assertions.assertEquals(connections, job.connections());
            Assertions.assertTrue(job.agentName() == null || job.agentName().isEmpty());
            job = job.refresh();
            Assertions.assertEquals(JobType.ON_PREM_TO_CLOUD_AGENT_LESS, job.jobType());
            Assertions.assertEquals(connections, job.connections());
            storageMoverManager.jobDefinitions().delete(resourceGroupName, moverName, "project", "agentless");
        } finally {
            storageMoverManager.connections().delete(resourceGroupName, moverName, "agentless");
        }
    }

    private String createParents(String prefix) {
        String moverName = generateRandomResourceName("sm-" + prefix + "-", 24);
        storageMoverManager.storageMovers()
            .define(moverName)
            .withRegion(testRegion())
            .withExistingResourceGroup(resourceGroupName)
            .create();
        storageMoverManager.projects()
            .define("project")
            .withExistingStorageMover(resourceGroupName, moverName)
            .create();
        return moverName;
    }

    private JobDefinition.DefinitionStages.WithCreate job(String moverName, String name, JobType type) {
        return storageMoverManager.jobDefinitions()
            .define(name)
            .withExistingProject(resourceGroupName, moverName, "project")
            .withCopyMode(CopyMode.ADDITIVE)
            .withSourceName("source")
            .withTargetName("target")
            .withJobType(type);
    }

    private static void assertInvalidProperties(Runnable action) {
        ManagementException exception = Assertions.assertThrows(ManagementException.class, action::run);
        Assertions.assertEquals(400, exception.getResponse().getStatusCode());
        Assertions.assertEquals("InvalidRequestPropertyValue", exception.getValue().getCode());
    }
}
