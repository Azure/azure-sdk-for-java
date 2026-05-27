// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storagemover.scenario;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.network.models.PrivateLinkServiceConnectionState;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storagemover.StorageMoverManager;
import com.azure.resourcemanager.storagemover.models.Connection;
import com.azure.resourcemanager.storagemover.models.ConnectionStatus;
import com.azure.resourcemanager.test.ResourceManagerTestProxyTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
     * Region where the cross-sub private-link / cpmoveraccount / AWS private
     * bucket fixtures live. The Storage Mover {@code cpmoveraccount} and the
     * {@code test-pls-wcs} PrivateLinkService are deployed in {@code
     * westcentralus}. Private-link tests therefore self-provision their
     * resource group in that region so PE traffic stays in-region.
     */
    protected static final Region WEST_CENTRAL_US = Region.US_WEST_CENTRAL;

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
     * Cross-language shared fixture subscription
     * ({@code XDataMove-Synthetics}). Hosts the private-link service, the
     * private AWS S3 bucket, and the {@code cpmoveraccount} storage account
     * used by the C2C-with-private-source scenario.
     */
    protected static final String XDATAMOVE_SYNTHETICS_SUB_ID = "b6b34ad8-ca89-4f85-beb7-c2ec13702dac";

    /**
     * Well-known resource ids used by the {@code AzureMultiCloudConnector}
     * endpoint scenarios. Mirrors the constants in
     * {@code StorageMoverManagementTestBase.cs} from .NET.
     */
    protected static final String MULTI_CLOUD_CONNECTOR_ID
        = "/subscriptions/" + XDATAMOVE_SYNTHETICS_SUB_ID + "/resourceGroups/E2E-Management-RGsyn"
            + "/providers/Microsoft.HybridConnectivity/publicCloudConnectors/e2e-sm-rp-connector";

    protected static final String AWS_S3_BUCKET_ID = "/subscriptions/" + XDATAMOVE_SYNTHETICS_SUB_ID
        + "/resourceGroups/aws_640698235822/providers/Microsoft.AWSConnector/s3Buckets/e2e-sm-rp-bucket";

    /**
     * AWS S3 bucket reachable only over the private endpoint (rows #31/#32 of
     * the cross-language matrix).
     */
    protected static final String AWS_PRIVATE_S3_BUCKET_ID = "/subscriptions/" + XDATAMOVE_SYNTHETICS_SUB_ID
        + "/resourceGroups/aws_640698235822/providers/Microsoft.AWSConnector/s3Buckets/e2e-sm-rp-private-bucket";

    /**
     * PrivateLinkService that fronts the AWS private bucket. The Connection
     * resource references this id; PE-approval helpers live in this base.
     */
    protected static final String PRIVATE_LINK_SERVICE_RG = "E2E-Management-RGsyn";
    protected static final String PRIVATE_LINK_SERVICE_NAME = "test-pls-wcs";
    protected static final String PRIVATE_LINK_SERVICE_ID
        = "/subscriptions/" + XDATAMOVE_SYNTHETICS_SUB_ID + "/resourceGroups/" + PRIVATE_LINK_SERVICE_RG
            + "/providers/Microsoft.Network/privateLinkServices/" + PRIVATE_LINK_SERVICE_NAME;

    /**
     * Storage account holding the per-test target blob containers used by the
     * C2C-with-private-source scenario.
     */
    protected static final String TEST_STORAGE_ACCOUNT_RG = "CP_Mover_IN_WCUS";
    protected static final String TEST_STORAGE_ACCOUNT_NAME = "cpmoveraccount";
    protected static final String TEST_STORAGE_ACCOUNT_ID
        = "/subscriptions/" + XDATAMOVE_SYNTHETICS_SUB_ID + "/resourceGroups/" + TEST_STORAGE_ACCOUNT_RG
            + "/providers/Microsoft.Storage/storageAccounts/" + TEST_STORAGE_ACCOUNT_NAME;

    /**
     * Built-in {@code Storage Blob Data Contributor} role definition GUID
     * (subscription-scope role-definition id is composed by the helper).
     */
    protected static final String STORAGE_BLOB_DATA_CONTRIBUTOR_ROLE_ID = "ba92f5b4-2d11-453d-a403-e96b0029c9fe";

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
     * The HTTP pipeline shared across all managers in a test run (set by
     * {@link #initializeClients}). Cross-subscription managers reuse this
     * pipeline so requests flow through the same recorder / playback proxy.
     */
    private HttpPipeline sharedHttpPipeline;
    private AzureProfile sharedTestProfile;

    /** Cached cross-sub managers (one of each is sufficient for the suite). */
    private NetworkManager xDataMoveNetworkManager;
    private AuthorizationManager xDataMoveAuthorizationManager;
    private StorageManager xDataMoveStorageManager;

    /**
     * Provider registration policy. Has to be a member field because the
     * pipeline (and therefore the policy) must exist before the
     * {@link ResourceManager} that supplies the provider list does — see the
     * two-phase wiring in {@link #buildHttpPipeline} / {@link #initializeClients}.
     */
    private final ProviderRegistrationPolicy providerRegistrationPolicy = new ProviderRegistrationPolicy();

    /**
     * Region the per-test resource group is created in. Subclasses override to
     * pin themselves to a non-default region (e.g. private-link scenarios that
     * must run in {@code westcentralus}).
     *
     * @return the region for the per-test resource group.
     */
    protected Region testRegion() {
        return DEFAULT_REGION;
    }

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
        // Cross-sub principalIds and PE names are server-generated per-run and
        // must be redacted in body too — URL sub-id sanitization is already
        // handled by the framework's default sanitizers.
        interceptorManager.addSanitizers(Arrays.asList(
            new TestProxySanitizer("$..startDate", null, REDACTED_SCHEDULE_DATE, TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("$..endDate", null, REDACTED_SCHEDULE_DATE, TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("$..principalId", null, "00000000-0000-0000-0000-000000000000",
                TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("$..tenantId", null, "00000000-0000-0000-0000-000000000000",
                TestProxySanitizerType.BODY_KEY)));
        super.beforeTest();
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));

        this.sharedHttpPipeline = httpPipeline;
        this.sharedTestProfile = profile;

        // StorageMoverManager has no public 2-arg constructor, so the generic
        // buildManager() helper from the base does not work — use the static
        // authenticate factory instead.
        storageMoverManager = StorageMoverManager.authenticate(httpPipeline, profile);
        resourceManager = ResourceManager.authenticate(httpPipeline, profile).withDefaultSubscription();

        // Now that ResourceManager exists we can hand its provider collection to
        // the policy that was already wired into the pipeline.
        providerRegistrationPolicy.setProviders(resourceManager.providers());

        resourceGroupName = generateRandomResourceName("rg-storagemover-", 30);
        resourceManager.resourceGroups().define(resourceGroupName).withRegion(testRegion()).create();
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

    /**
     * Sleeps for the given duration in live / record mode but skips the sleep
     * during playback (the recording captures the post-wait state). Wraps
     * {@link ResourceManagerUtils#sleep(Duration)} for clarity at call sites.
     *
     * @param duration how long to sleep in live mode.
     */
    protected void sleep(Duration duration) {
        ResourceManagerUtils.sleep(duration);
    }

    /**
     * Lazily authenticates a {@link NetworkManager} bound to the supplied
     * subscription, reusing the shared HTTP pipeline so cross-sub calls are
     * captured by the test recorder.
     *
     * @param subscriptionId target subscription id.
     * @return a cached {@link NetworkManager} for the subscription.
     */
    protected NetworkManager networkManager(String subscriptionId) {
        if (xDataMoveNetworkManager == null) {
            xDataMoveNetworkManager = NetworkManager.authenticate(sharedHttpPipeline, profileFor(subscriptionId));
        }
        return xDataMoveNetworkManager;
    }

    /**
     * Lazily authenticates an {@link AuthorizationManager} bound to the
     * supplied subscription, reusing the shared HTTP pipeline.
     *
     * @param subscriptionId target subscription id.
     * @return a cached {@link AuthorizationManager} for the subscription.
     */
    protected AuthorizationManager authorizationManager(String subscriptionId) {
        if (xDataMoveAuthorizationManager == null) {
            xDataMoveAuthorizationManager
                = AuthorizationManager.authenticate(sharedHttpPipeline, profileFor(subscriptionId));
        }
        return xDataMoveAuthorizationManager;
    }

    /**
     * Lazily authenticates a {@link StorageManager} bound to the supplied
     * subscription, reusing the shared HTTP pipeline.
     *
     * @param subscriptionId target subscription id.
     * @return a cached {@link StorageManager} for the subscription.
     */
    protected StorageManager storageManager(String subscriptionId) {
        if (xDataMoveStorageManager == null) {
            xDataMoveStorageManager = StorageManager.authenticate(sharedHttpPipeline, profileFor(subscriptionId));
        }
        return xDataMoveStorageManager;
    }

    private AzureProfile profileFor(String subscriptionId) {
        AzureEnvironment env = sharedTestProfile != null ? sharedTestProfile.getEnvironment() : AzureEnvironment.AZURE;
        String tenantId = sharedTestProfile != null ? sharedTestProfile.getTenantId() : null;
        return new AzureProfile(tenantId, subscriptionId, env);
    }

    /**
     * Locates the PE-connection on the PLS whose backing private endpoint
     * matches the supplied id. The Storage Mover RP provisions the PE
     * asynchronously, so the lookup polls up to {@code 10 * 15s = 150s} before
     * giving up.
     *
     * @param privateEndpointResourceId fully-qualified private endpoint ARM id
     *     captured from {@link Connection#properties()}.
     * @return the matching {@link PrivateEndpointConnectionInner}.
     * @throws IllegalStateException if no matching connection appears within
     *     the polling window.
     */
    protected PrivateEndpointConnectionInner findPrivateEndpointConnection(String privateEndpointResourceId) {
        Objects.requireNonNull(privateEndpointResourceId, "privateEndpointResourceId");
        String targetName = lastSegment(privateEndpointResourceId);
        for (int attempt = 0; attempt < 10; attempt++) {
            for (PrivateEndpointConnectionInner conn : networkManager(XDATAMOVE_SYNTHETICS_SUB_ID).serviceClient()
                .getPrivateLinkServices()
                .listPrivateEndpointConnections(PRIVATE_LINK_SERVICE_RG, PRIVATE_LINK_SERVICE_NAME)) {
                if (conn.privateEndpoint() != null
                    && targetName.equalsIgnoreCase(lastSegment(conn.privateEndpoint().id()))) {
                    return conn;
                }
            }
            sleep(Duration.ofSeconds(15));
        }
        throw new IllegalStateException(
            "no PrivateEndpointConnection on " + PRIVATE_LINK_SERVICE_NAME + " matched " + privateEndpointResourceId);
    }

    private static String lastSegment(String resourceId) {
        if (resourceId == null) {
            return null;
        }
        int idx = resourceId.lastIndexOf('/');
        return idx < 0 ? resourceId : resourceId.substring(idx + 1);
    }

    /**
     * Approves the supplied PE-connection on the PLS.
     *
     * @param peConnectionName name of the PE-connection to approve.
     * @return the updated {@link PrivateEndpointConnectionInner}.
     */
    protected PrivateEndpointConnectionInner approvePrivateEndpointConnection(String peConnectionName) {
        PrivateEndpointConnectionInner parameters = new PrivateEndpointConnectionInner()
            .withPrivateLinkServiceConnectionState(new PrivateLinkServiceConnectionState().withStatus("Approved")
                .withDescription("")
                .withActionsRequired("None"));
        return networkManager(XDATAMOVE_SYNTHETICS_SUB_ID).serviceClient()
            .getPrivateLinkServices()
            .updatePrivateEndpointConnection(PRIVATE_LINK_SERVICE_RG, PRIVATE_LINK_SERVICE_NAME, peConnectionName,
                parameters);
    }

    /**
     * Polls the Storage Mover Connection resource until its
     * {@link ConnectionStatus} reads {@link ConnectionStatus#APPROVED}. There
     * is a 1-5 minute lag between PLS-side approval and the RP propagating the
     * status, so we wait up to {@code 10 * 30s = 5 min}.
     *
     * @param storageMoverName parent storage mover name.
     * @param connectionName connection name.
     * @return the connection once status is {@code Approved}.
     * @throws IllegalStateException if status does not reach {@code Approved}
     *     within the polling window.
     */
    protected Connection waitForConnectionApproved(String storageMoverName, String connectionName) {
        for (int attempt = 0; attempt < 10; attempt++) {
            Connection refreshed
                = storageMoverManager.connections().get(resourceGroupName, storageMoverName, connectionName);
            if (refreshed.properties() != null
                && ConnectionStatus.APPROVED.equals(refreshed.properties().connectionStatus())) {
                return refreshed;
            }
            sleep(Duration.ofSeconds(30));
        }
        throw new IllegalStateException("Connection " + connectionName + " did not reach Approved within 5 minutes");
    }

    /**
     * Assigns {@code Storage Blob Data Contributor} to the supplied principal
     * id on the given scope. MSI propagation can lag for several seconds after
     * an Endpoint create returns, so we retry up to {@code 10 * 6s = 60s} on
     * {@code PrincipalNotFound}. {@code RoleAssignmentExists} is treated as
     * idempotent success.
     *
     * @param principalId MSI principal id from the endpoint create response.
     * @param scope full ARM scope (e.g. a container resource id).
     * @return the role-assignment GUID (the {@code name} of the role
     *     assignment) for cleanup; never {@code null}.
     */
    protected String assignBlobDataContributorWithRetry(String principalId, String scope) {
        String roleAssignmentName = generateRandomUuid();
        ManagementException lastError = null;
        for (int attempt = 0; attempt < 10; attempt++) {
            try {
                authorizationManager(XDATAMOVE_SYNTHETICS_SUB_ID).roleAssignments()
                    .define(roleAssignmentName)
                    .forObjectId(principalId)
                    .withBuiltInRole(BuiltInRole.STORAGE_BLOB_DATA_CONTRIBUTOR)
                    .withScope(scope)
                    .create();
                return roleAssignmentName;
            } catch (ManagementException ex) {
                String code = ex.getValue() != null ? ex.getValue().getCode() : null;
                if ("RoleAssignmentExists".equalsIgnoreCase(code)) {
                    return roleAssignmentName;
                }
                if (!"PrincipalNotFound".equalsIgnoreCase(code)) {
                    throw ex;
                }
                lastError = ex;
                sleep(Duration.ofSeconds(6));
            }
        }
        throw new IllegalStateException("role assignment never propagated for principal " + principalId, lastError);
    }

    private static Region parseRegionOrDefault(String name, Region fallback) {
        if (CoreUtils.isNullOrEmpty(name)) {
            return fallback;
        }
        Region parsed = Region.fromName(name);
        return parsed != null ? parsed : Region.create(name, name);
    }
}
