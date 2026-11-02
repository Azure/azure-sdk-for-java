// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storagemover;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.resourcemanager.storagemover.models.AzureKeyVaultSmbCredentials;
import com.azure.resourcemanager.storagemover.models.AzureStorageBlobContainerEndpointProperties;
import com.azure.resourcemanager.storagemover.models.AzureStorageBlobContainerEndpointUpdateProperties;
import com.azure.resourcemanager.storagemover.models.AzureStorageSmbFileShareEndpointProperties;
import com.azure.resourcemanager.storagemover.models.AzureStorageSmbFileShareEndpointUpdateProperties;
import com.azure.resourcemanager.storagemover.models.CopyMode;
import com.azure.resourcemanager.storagemover.models.DataIntegrityValidation;
import com.azure.resourcemanager.storagemover.models.Endpoint;
import com.azure.resourcemanager.storagemover.models.EndpointBaseProperties;
import com.azure.resourcemanager.storagemover.models.EndpointBaseUpdateParameters;
import com.azure.resourcemanager.storagemover.models.EndpointBaseUpdateProperties;
import com.azure.resourcemanager.storagemover.models.EndpointKind;
import com.azure.resourcemanager.storagemover.models.Frequency;
import com.azure.resourcemanager.storagemover.models.JobDefinition;
import com.azure.resourcemanager.storagemover.models.JobRunResourceId;
import com.azure.resourcemanager.storagemover.models.JobType;
import com.azure.resourcemanager.storagemover.models.NfsMountEndpointProperties;
import com.azure.resourcemanager.storagemover.models.NfsMountSourceType;
import com.azure.resourcemanager.storagemover.models.NfsVersion;
import com.azure.resourcemanager.storagemover.models.ScheduleInfo;
import com.azure.resourcemanager.storagemover.models.SmbMountEndpointProperties;
import com.azure.resourcemanager.storagemover.models.SmbMountSourceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ApiContractTests {
    private static final String SUBSCRIPTION_ID = "00000000-0000-0000-0000-000000000000";
    private static final String MOVER_ID = "/subscriptions/" + SUBSCRIPTION_ID
        + "/resourceGroups/resource-group/providers/Microsoft.StorageMover/storageMovers/mover";
    private static final String STORAGE_ACCOUNT_ID = "/subscriptions/" + SUBSCRIPTION_ID
        + "/resourceGroups/resource-group/providers/Microsoft.Storage/storageAccounts/account";
    private static final String PARTNER_STORAGE_ACCOUNT_ID
        = "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/partner"
            + "/providers/Microsoft.Storage/storageAccounts/partneraccount";
    private static final String PARTNER_ENDPOINT_ID
        = "/subscriptions/11111111-1111-1111-1111-111111111111/resourceGroups/partner"
            + "/providers/Microsoft.StorageMover/storageMovers/partner/endpoints/target";
    private static final String PARTNER_TENANT_ID = "22222222-2222-2222-2222-222222222222";
    private static final String CONNECTION_ID = MOVER_ID + "/connections/connection";
    private static final String JOB_PATH = "/projects/project/jobDefinitions/job";
    private static final OffsetDateTime SYNCED_UNTIL = OffsetDateTime.parse("2026-05-01T00:00:00Z");
    private static final String JOB_BODY
        = "{\"properties\":{\"copyMode\":\"Additive\",\"sourceName\":\"source\",\"targetName\":\"target\","
            + "\"jobType\":\"CloudToCloud\",\"isCrossTenantJob\":true,\"crossTenantEndpointTenantId\":\""
            + PARTNER_TENANT_ID + "\",\"crossTenantEndpointResourceId\":\"" + PARTNER_ENDPOINT_ID
            + "\",\"syncMode\":\"SnapshotBased\",\"moverSyncedUntil\":\"2026-05-01T00:00:00Z\","
            + "\"schedule\":{\"frequency\":\"Hourly\",\"repeatInterval\":\"PT1H\",\"isActive\":true}}}";

    @ParameterizedTest
    @MethodSource("endpointCases")
    public void endpointCreateMatchesPythonContract(String name, EndpointBaseProperties properties, String body) {
        StorageMoverManager manager = manager(request -> {
            assertRequest(request, HttpMethod.PUT, "/endpoints/" + name, body);
            return Mono.just(resourceResponse(request, body));
        });

        Endpoint endpoint = manager.endpoints()
            .define(name)
            .withExistingStorageMover("resource-group", "mover")
            .withProperties(properties)
            .create();
        Assertions.assertEquals(name, endpoint.name());
        Assertions.assertEquals(properties.endpointType(), endpoint.properties().endpointType());
    }

    private static Stream<Arguments> endpointCases() {
        return Stream.of(
            Arguments.of("blob-cross-tenant",
                new AzureStorageBlobContainerEndpointProperties().withStorageAccountResourceId(STORAGE_ACCOUNT_ID)
                    .withBlobContainerName("container")
                    .withEndpointKind(EndpointKind.TARGET)
                    .withEnableCrossTenantTransfer(true)
                    .withAllowedStorageAccounts(Collections.singletonList(PARTNER_STORAGE_ACCOUNT_ID)),
                "{\"properties\":{\"endpointType\":\"AzureStorageBlobContainer\",\"storageAccountResourceId\":\""
                    + STORAGE_ACCOUNT_ID + "\",\"blobContainerName\":\"container\",\"endpointKind\":\"Target\","
                    + "\"enableCrossTenantTransfer\":true,\"allowedStorageAccounts\":[\"" + PARTNER_STORAGE_ACCOUNT_ID
                    + "\"]}}"),
            Arguments.of("smb-cross-tenant",
                new AzureStorageSmbFileShareEndpointProperties().withStorageAccountResourceId(STORAGE_ACCOUNT_ID)
                    .withFileShareName("share")
                    .withEndpointKind(EndpointKind.TARGET)
                    .withEnableCrossTenantTransfer(true)
                    .withAllowedStorageAccounts(Collections.singletonList(PARTNER_STORAGE_ACCOUNT_ID)),
                "{\"properties\":{\"endpointType\":\"AzureStorageSmbFileShare\",\"storageAccountResourceId\":\""
                    + STORAGE_ACCOUNT_ID + "\",\"fileShareName\":\"share\",\"endpointKind\":\"Target\","
                    + "\"enableCrossTenantTransfer\":true,\"allowedStorageAccounts\":[\"" + PARTNER_STORAGE_ACCOUNT_ID
                    + "\"]}}"),
            Arguments.of("nfs-fsx",
                new NfsMountEndpointProperties().withHost("10.0.0.5")
                    .withExport("/export")
                    .withEndpointKind(EndpointKind.SOURCE)
                    .withNfsVersion(NfsVersion.NFSV4_1)
                    .withSourceType(NfsMountSourceType.FSXEFS),
                "{\"properties\":{\"endpointType\":\"NfsMount\",\"host\":\"10.0.0.5\",\"export\":\"/export\","
                    + "\"endpointKind\":\"Source\",\"nfsVersion\":\"NFSv4_1\",\"sourceType\":\"FSX-EFS\"}}"),
            Arguments.of("smb-fsx",
                new SmbMountEndpointProperties().withHost("10.0.0.6")
                    .withShareName("share")
                    .withEndpointKind(EndpointKind.SOURCE)
                    .withSourceType(SmbMountSourceType.FSXSMB)
                    .withCredentials(new AzureKeyVaultSmbCredentials()
                        .withUsernameUri("https://vault.vault.azure.net/secrets/username")
                        .withPasswordUri("https://vault.vault.azure.net/secrets/password")),
                "{\"properties\":{\"endpointType\":\"SmbMount\",\"host\":\"10.0.0.6\",\"shareName\":\"share\","
                    + "\"endpointKind\":\"Source\",\"sourceType\":\"FSX-SMB\",\"credentials\":{"
                    + "\"type\":\"AzureKeyVaultSmb\",\"usernameUri\":\"https://vault.vault.azure.net/secrets/username\","
                    + "\"passwordUri\":\"https://vault.vault.azure.net/secrets/password\"}}}"));
    }

    @ParameterizedTest
    @MethodSource("endpointUpdateCases")
    public void endpointUpdatePreservesExplicitFalseAndEmptyAllowList(EndpointBaseUpdateProperties properties,
        String endpointType) {
        String body = "{\"properties\":{\"endpointType\":\"" + endpointType
            + "\",\"enableCrossTenantTransfer\":false,\"allowedStorageAccounts\":[]}}";
        StorageMoverManager manager = manager(request -> {
            assertRequest(request, HttpMethod.PATCH, "/endpoints/endpoint", body);
            return Mono.just(resourceResponse(request, body));
        });
        Assertions.assertNotNull(manager.serviceClient()
            .getEndpoints()
            .update("resource-group", "mover", "endpoint",
                new EndpointBaseUpdateParameters().withProperties(properties)));
    }

    private static Stream<Arguments> endpointUpdateCases() {
        return Stream.of(
            Arguments.of(new AzureStorageBlobContainerEndpointUpdateProperties().withEnableCrossTenantTransfer(false)
                .withAllowedStorageAccounts(Collections.emptyList()), "AzureStorageBlobContainer"),
            Arguments.of(new AzureStorageSmbFileShareEndpointUpdateProperties().withEnableCrossTenantTransfer(false)
                .withAllowedStorageAccounts(Collections.emptyList()), "AzureStorageSmbFileShare"));
    }

    @Test
    public void crossTenantJobCreateMatchesPythonContract() {
        StorageMoverManager manager = manager(request -> {
            assertRequest(request, HttpMethod.PUT, JOB_PATH, JOB_BODY);
            return Mono.just(resourceResponse(request, JOB_BODY));
        });
        JobDefinition job = manager.jobDefinitions()
            .define("job")
            .withExistingProject("resource-group", "mover", "project")
            .withCopyMode(CopyMode.ADDITIVE)
            .withSourceName("source")
            .withTargetName("target")
            .withJobType(JobType.CLOUD_TO_CLOUD)
            .withIsCrossTenantJob(true)
            .withCrossTenantEndpointTenantId(PARTNER_TENANT_ID)
            .withCrossTenantEndpointResourceId(PARTNER_ENDPOINT_ID)
            .withSyncMode("SnapshotBased")
            .withMoverSyncedUntil(SYNCED_UNTIL)
            .withSchedule(
                new ScheduleInfo().withFrequency(Frequency.HOURLY).withRepeatInterval("PT1H").withIsActive(true))
            .create();
        Assertions.assertTrue(job.isCrossTenantJob());
        Assertions.assertEquals(PARTNER_ENDPOINT_ID, job.crossTenantEndpointResourceId());
        Assertions.assertEquals(PARTNER_TENANT_ID, job.crossTenantEndpointTenantId());
        Assertions.assertEquals("SnapshotBased", job.syncMode());
        Assertions.assertEquals(SYNCED_UNTIL, job.moverSyncedUntil());
    }

    @Test
    public void agentlessJobCreateMatchesPythonContract() {
        String body = "{\"properties\":{\"copyMode\":\"Additive\",\"sourceName\":\"source\",\"targetName\":\"target\","
            + "\"jobType\":\"OnPremToCloudAgentLess\",\"connections\":[\"" + CONNECTION_ID + "\"]}}";
        StorageMoverManager manager = manager(request -> {
            assertRequest(request, HttpMethod.PUT, JOB_PATH, body);
            return Mono.just(resourceResponse(request, body));
        });
        JobDefinition job = manager.jobDefinitions()
            .define("job")
            .withExistingProject("resource-group", "mover", "project")
            .withCopyMode(CopyMode.ADDITIVE)
            .withSourceName("source")
            .withTargetName("target")
            .withJobType(JobType.ON_PREM_TO_CLOUD_AGENT_LESS)
            .withConnections(Collections.singletonList(CONNECTION_ID))
            .create();
        Assertions.assertEquals(JobType.ON_PREM_TO_CLOUD_AGENT_LESS, job.jobType());
        Assertions.assertEquals(Collections.singletonList(CONNECTION_ID), job.connections());
    }

    @Test
    public void jobCreatePreservesMd5Contract() {
        String body = "{\"properties\":{\"copyMode\":\"Additive\",\"sourceName\":\"source\",\"targetName\":\"target\","
            + "\"dataIntegrityValidation\":\"SaveVerifyFileMD5\"}}";
        StorageMoverManager manager = manager(request -> {
            assertRequest(request, HttpMethod.PUT, JOB_PATH, body);
            return Mono.just(resourceResponse(request, body));
        });
        JobDefinition job = manager.jobDefinitions()
            .define("job")
            .withExistingProject("resource-group", "mover", "project")
            .withCopyMode(CopyMode.ADDITIVE)
            .withSourceName("source")
            .withTargetName("target")
            .withDataIntegrityValidation(DataIntegrityValidation.SAVE_VERIFY_FILE_MD5)
            .create();
        Assertions.assertEquals(DataIntegrityValidation.SAVE_VERIFY_FILE_MD5, job.dataIntegrityValidation());
    }

    @Test
    public void jobUpdateSendsOnlyMutableProperties() {
        String body = "{\"properties\":{\"connections\":[\"" + CONNECTION_ID + "\"],\"syncMode\":\"FullScan\","
            + "\"moverSyncedUntil\":\"2026-05-02T00:00:00Z\","
            + "\"schedule\":{\"frequency\":\"Hourly\",\"repeatInterval\":\"PT2H\",\"isActive\":false}}}";
        AtomicInteger requests = new AtomicInteger();
        StorageMoverManager manager = manager(request -> {
            if (requests.getAndIncrement() == 0) {
                assertRequest(request, HttpMethod.GET, JOB_PATH, null);
                return Mono.just(resourceResponse(request, JOB_BODY));
            }
            assertRequest(request, HttpMethod.PATCH, JOB_PATH, body);
            return Mono.just(resourceResponse(request, body));
        });

        JobDefinition job = manager.jobDefinitions().get("resource-group", "mover", "project", "job");
        JobDefinition updated = job.update()
            .withConnections(Collections.singletonList(CONNECTION_ID))
            .withSyncMode("FullScan")
            .withMoverSyncedUntil(SYNCED_UNTIL.plusDays(1))
            .withSchedule(
                new ScheduleInfo().withFrequency(Frequency.HOURLY).withRepeatInterval("PT2H").withIsActive(false))
            .apply();
        Assertions.assertEquals(2, requests.get());
        Assertions.assertEquals("FullScan", updated.syncMode());
        Assertions.assertEquals(SYNCED_UNTIL.plusDays(1), updated.moverSyncedUntil());
        Assertions.assertEquals(Boolean.FALSE, updated.schedule().isActive());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void reconcileIsSingleBodylessPost(boolean resourceMethod) {
        AtomicInteger posts = new AtomicInteger();
        String jobRunId = MOVER_ID + JOB_PATH + "/jobRuns/run";
        StorageMoverManager manager = manager(request -> {
            if (request.getHttpMethod() == HttpMethod.GET) {
                Assertions.assertTrue(resourceMethod);
                assertRequest(request, HttpMethod.GET, JOB_PATH, null);
                return Mono.just(resourceResponse(request, JOB_BODY));
            }
            assertRequest(request, HttpMethod.POST, JOB_PATH + "/reconcileJob", null);
            posts.incrementAndGet();
            return Mono.just(new MockHttpResponse(request, 200,
                ("{\"jobRunResourceId\":\"" + jobRunId + "\"}").getBytes(StandardCharsets.UTF_8)));
        });
        JobRunResourceId result = resourceMethod
            ? manager.jobDefinitions().get("resource-group", "mover", "project", "job").reconcileJob()
            : manager.jobDefinitions()
                .reconcileJobWithResponse("resource-group", "mover", "project", "job", Context.NONE)
                .getValue();
        Assertions.assertEquals(jobRunId, result.jobRunResourceId());
        Assertions.assertEquals(1, posts.get());
    }

    private static StorageMoverManager manager(HttpClient httpClient) {
        return StorageMoverManager.configure()
            .withHttpClient(httpClient)
            .authenticate(ignored -> Mono.just(new AccessToken("fake-token", OffsetDateTime.MAX)),
                new AzureProfile("", SUBSCRIPTION_ID, AzureCloud.AZURE_PUBLIC_CLOUD));
    }

    private static void assertRequest(HttpRequest request, HttpMethod method, String path, String body) {
        Assertions.assertEquals(method, request.getHttpMethod());
        Assertions.assertEquals("management.azure.com", request.getUrl().getHost());
        Assertions.assertEquals(MOVER_ID + path, request.getUrl().getPath());
        Assertions.assertEquals("api-version=2026-05-01", request.getUrl().getQuery());
        if (body == null) {
            Assertions.assertNull(request.getBodyAsBinaryData());
        } else {
            Assertions.assertEquals(BinaryData.fromString(body).toObject(Map.class),
                request.getBodyAsBinaryData().toObject(Map.class));
        }
    }

    private static MockHttpResponse resourceResponse(HttpRequest request, String body) {
        String path = request.getUrl().getPath();
        String name = path.substring(path.lastIndexOf('/') + 1);
        String response = "{\"id\":\"" + path + "\",\"name\":\"" + name + "\"," + body.substring(1);
        return new MockHttpResponse(request, 200, response.getBytes(StandardCharsets.UTF_8));
    }
}
