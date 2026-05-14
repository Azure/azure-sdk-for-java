// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storagemover.scenario;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.storagemover.StorageMoverManager;
import com.azure.resourcemanager.test.ResourceManagerTestProxyTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import org.junit.jupiter.api.Assertions;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base class for Storage Mover scenario tests.
 *
 * <p>Mirrors the cross-language test suite layout used by .NET
 * ({@code Azure.ResourceManager.StorageMover/tests/Scenario}) and Python
 * ({@code azure-mgmt-storagemover/tests}). Each concrete subclass focuses on
 * one operation group.
 */
public abstract class StorageMoverManagementTestBase extends ResourceManagerTestProxyTestBase {

    /**
     * Default region. Overridable via {@code AZURE_TEST_LOCATION} (e.g. when a
     * subscription does not have {@code eastus} enabled).
     */
    protected static final Region DEFAULT_REGION
        = parseRegionOrDefault(Configuration.getGlobalConfiguration().get("AZURE_TEST_LOCATION"), Region.US_EAST);

    /**
     * Placeholder value the {@code $..startDate} / {@code $..endDate} body-key
     * sanitizers rewrite all schedule dates to. Both the stored recording and
     * the live request body during playback get this value, so playback matches
     * succeed regardless of when the test is run.
     */
    private static final String REDACTED_SCHEDULE_DATE = "2030-01-01T00:00:00Z";

    /**
     * Returns a {@code startDate} suitable for schedule tests.
     *
     * <p>The Storage Mover RP rejects any {@code endDate} more than one year
     * past wall-clock {@code now()}, so a hard-coded far-future date does not
     * work. The body-key sanitizer registered in {@link #beforeTest} rewrites
     * the value to {@link #REDACTED_SCHEDULE_DATE} in both the stored recording
     * and the live request during playback, keeping the recordings portable.
     *
     * @return a UTC {@link OffsetDateTime} one day in the future, truncated to
     *     the day so the JSON payload is stable.
     */
    protected static OffsetDateTime scheduleStartDate() {
        return OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).truncatedTo(ChronoUnit.DAYS);
    }

    /**
     * Well-known resource ids used by the {@code AzureMultiCloudConnector}
     * endpoint scenarios. Mirrors the constants in
     * {@code StorageMoverManagementTestBase.cs} from .NET.
     */
    protected static final String MULTI_CLOUD_CONNECTOR_ID
        = "/subscriptions/b6b34ad8-ca89-4f85-beb7-c2ec13702dac/resourceGroups/E2E-Management-RGsyn"
            + "/providers/Microsoft.HybridConnectivity/publicCloudConnectors/e2e-sm-rp-connector";

    protected static final String AWS_S3_BUCKET_ID
        = "/subscriptions/b6b34ad8-ca89-4f85-beb7-c2ec13702dac/resourceGroups/aws_640698235822"
            + "/providers/Microsoft.AWSConnector/s3Buckets/e2e-sm-rp-bucket";

    /**
     * Placeholder storage account resource id used when an endpoint requires a
     * storage-account ARM id but the test does not actually hit the storage
     * account. The Storage Mover RP accepts any well-formed id at endpoint
     * metadata level.
     */
    protected static final String FAKE_STORAGE_ACCOUNT_ID
        = "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/fakeRg"
            + "/providers/Microsoft.Storage/storageAccounts/fakeaccount";

    protected StorageMoverManager storageMoverManager;
    protected ResourceManager resourceManager;
    protected String resourceGroupName;

    /**
     * Provider registration policy. Has to be a member field because the
     * pipeline (and therefore the policy) must exist before the
     * {@link ResourceManager} that supplies the provider list does — see the
     * two-phase wiring in {@link #buildHttpPipeline} / {@link #initializeClients}.
     */
    private final ProviderRegistrationPolicy providerRegistrationPolicy = new ProviderRegistrationPolicy();

    @Override
    protected HttpPipeline buildHttpPipeline(TokenCredential credential, AzureProfile profile,
        HttpLogOptions httpLogOptions, List<HttpPipelinePolicy> policies, HttpClient httpClient) {
        List<HttpPipelinePolicy> mergedPolicies = new ArrayList<>();
        if (policies != null) {
            mergedPolicies.addAll(policies);
        }
        // Without auto-registration the first call against a fresh subscription
        // returns "MissingSubscriptionRegistration".
        mergedPolicies.add(providerRegistrationPolicy);
        return HttpPipelineProvider.buildHttpPipeline(credential, profile, null, httpLogOptions, null,
            new RetryPolicy("Retry-After", ChronoUnit.SECONDS), mergedPolicies, httpClient);
    }

    @Override
    protected void beforeTest() {
        // Schedule dates are dynamic ("now + N days") so the RP accepts them,
        // but we sanitize them to a fixed placeholder so recordings replay on
        // any date. The sanitizer is bidirectional: applied both when storing
        // recordings and when matching incoming requests during playback.
        interceptorManager.addSanitizers(Arrays.asList(
            new TestProxySanitizer("$..startDate", null, REDACTED_SCHEDULE_DATE, TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("$..endDate", null, REDACTED_SCHEDULE_DATE, TestProxySanitizerType.BODY_KEY)));
        super.beforeTest();
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));

        // StorageMoverManager has no public 2-arg constructor, so the generic
        // buildManager() helper from the base does not work — use the static
        // authenticate factory instead.
        storageMoverManager = StorageMoverManager.authenticate(httpPipeline, profile);
        resourceManager = ResourceManager.authenticate(httpPipeline, profile).withDefaultSubscription();

        // Now that ResourceManager exists we can hand its provider collection to
        // the policy that was already wired into the pipeline.
        providerRegistrationPolicy.setProviders(resourceManager.providers());

        resourceGroupName = generateRandomResourceName("rg-storagemover-", 30);
        resourceManager.resourceGroups().define(resourceGroupName).withRegion(DEFAULT_REGION).create();
    }

    @Override
    protected void cleanUpResources() {
        if (resourceGroupName != null) {
            try {
                resourceManager.resourceGroups().beginDeleteByName(resourceGroupName);
            } catch (RuntimeException ignored) {
                // Best-effort cleanup; resource-group deletion failures should not
                // mask the underlying test failure.
            }
        }
    }

    /**
     * Asserts that the given operation throws a {@link ManagementException}
     * carrying an HTTP status code in {@code [400, 500)}. Used as the Java
     * analogue of .NET's {@code Assert.IsFalse(await coll.ExistsAsync(name))}
     * pattern (where the SDK does not surface an {@code Exists} helper).
     *
     * @param expectedStatus expected HTTP status (typically 404).
     * @param action the operation that should throw.
     */
    protected static void assertHttpStatus(int expectedStatus, Runnable action) {
        ManagementException ex = Assertions.assertThrows(ManagementException.class, action::run);
        Assertions.assertEquals(expectedStatus, ex.getResponse().getStatusCode(), "expected HTTP " + expectedStatus
            + " but got " + ex.getResponse().getStatusCode() + " — body: " + ex.getValue());
    }

    /**
     * Convenience wrapper for the common 404-expected case.
     *
     * @param action the operation that should produce a 404.
     */
    protected static void assertNotFound(Runnable action) {
        assertHttpStatus(404, action);
    }

    /**
     * Returns a {@link ResourceGroup} representing the per-test resource group
     * created in {@link #initializeClients}.
     *
     * @return the per-test resource group.
     */
    protected ResourceGroup getResourceGroup() {
        return resourceManager.resourceGroups().getByName(resourceGroupName);
    }

    private static Region parseRegionOrDefault(String name, Region fallback) {
        if (CoreUtils.isNullOrEmpty(name)) {
            return fallback;
        }
        Region parsed = Region.fromName(name);
        return parsed != null ? parsed : Region.create(name, name);
    }
}
