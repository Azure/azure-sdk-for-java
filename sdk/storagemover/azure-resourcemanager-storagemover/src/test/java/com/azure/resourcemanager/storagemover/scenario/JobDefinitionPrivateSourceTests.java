// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storagemover.scenario;

import com.azure.core.management.Region;
import com.azure.resourcemanager.network.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.storage.models.BlobContainer;
import com.azure.resourcemanager.storage.models.PublicAccess;
import com.azure.resourcemanager.storagemover.models.AzureMultiCloudConnectorEndpointProperties;
import com.azure.resourcemanager.storagemover.models.AzureStorageBlobContainerEndpointProperties;
import com.azure.resourcemanager.storagemover.models.Connection;
import com.azure.resourcemanager.storagemover.models.ConnectionProperties;
import com.azure.resourcemanager.storagemover.models.CopyMode;
import com.azure.resourcemanager.storagemover.models.DataIntegrityValidation;
import com.azure.resourcemanager.storagemover.models.Endpoint;
import com.azure.resourcemanager.storagemover.models.EndpointKind;
import com.azure.resourcemanager.storagemover.models.JobDefinition;
import com.azure.resourcemanager.storagemover.models.JobRun;
import com.azure.resourcemanager.storagemover.models.JobRunResourceId;
import com.azure.resourcemanager.storagemover.models.JobRunStatus;
import com.azure.resourcemanager.storagemover.models.JobType;
import com.azure.resourcemanager.storagemover.models.ManagedServiceIdentity;
import com.azure.resourcemanager.storagemover.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.storagemover.models.Project;
import com.azure.resourcemanager.storagemover.models.StorageMover;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Mirrors the {@code StartC2CJobWithPrivateSourceTest} scenario (row #31 of
 * the cross-language matrix). Drives the full 12-step private-source
 * Cloud-to-Cloud flow end-to-end:
 *
 * <ol>
 *   <li>Self-provision storage mover + project in {@code westcentralus} (the
 *       per-test resource group is already in that region via
 *       {@link #testRegion()}).</li>
 *   <li>Create a per-test target blob container under
 *       {@link #TEST_STORAGE_ACCOUNT_NAME} so parallel runs do not race over
 *       the same container.</li>
 *   <li>Create the Storage Mover {@link Connection} referencing
 *       {@link #PRIVATE_LINK_SERVICE_ID}; capture the auto-created PE id.</li>
 *   <li>Locate the matching PE-connection on the PLS (poll up to 150s).</li>
 *   <li>PATCH the PE-connection {@code Approved}.</li>
 *   <li>Poll the Storage Mover Connection until {@code connectionStatus =
 *       Approved} (the RP propagates the PLS-side approval asynchronously —
 *       up to 5 min).</li>
 *   <li>Create the target Blob endpoint with explicit
 *       {@link ManagedServiceIdentityType#SYSTEM_ASSIGNED}; capture
 *       {@code identity.principalId}.</li>
 *   <li>Assign {@code Storage Blob Data Contributor} to the MSI on the
 *       container scope (retry on {@code PrincipalNotFound}).</li>
 *   <li>Create the source MCC endpoint over
 *       {@link #AWS_PRIVATE_S3_BUCKET_ID}.</li>
 *   <li>Create the C2C {@link JobDefinition}
 *       (copyMode={@link CopyMode#ADDITIVE}, agentless, with the connection in
 *       {@code connections}).</li>
 *   <li>{@code startJob} → poll the resulting {@link JobRun} on a 30s cadence
 *       for up to 30 minutes; expect terminal {@link JobRunStatus#SUCCEEDED}
 *       in 3-5 minutes.</li>
 *   <li>Tear down (role assignment → connection → container) with a 60s
 *       grace period between connection delete and container delete so the
 *       PLS releases the slot before the next test claims it.</li>
 * </ol>
 *
 * <p>Lives in its own class (separate from {@link JobDefinitionJobRunTests}) so
 * the per-test resource group can be pinned to {@code westcentralus} via
 * {@link #testRegion()}; the other job-definition test runs in
 * {@link #DEFAULT_REGION}.
 */
public class JobDefinitionPrivateSourceTests extends StorageMoverManagementTestBase {

    /** Job-run terminal states (the polling loop exits on any of these). */
    private static final Set<JobRunStatus> TERMINAL_STATES = Collections.unmodifiableSet(
        new HashSet<>(java.util.Arrays.asList(JobRunStatus.SUCCEEDED, JobRunStatus.FAILED, JobRunStatus.CANCELED)));

    @Override
    protected Region testRegion() {
        return WEST_CENTRAL_US;
    }

    @Test
    public void startC2CJobWithPrivateSource() {
        String storageMoverName = generateRandomResourceName("stomover-c2cps-", 24);
        String projectName = generateRandomResourceName("project-c2cps-", 24);
        String connectionName = generateRandomResourceName("conn-c2cps-", 24);
        String sourceEndpointName = generateRandomResourceName("mccep-", 24);
        String targetEndpointName = generateRandomResourceName("blobep-", 24);
        String jobDefinitionName = generateRandomResourceName("jobdef-c2cps-", 24);
        // Container name must be lowercase per Azure Storage naming rules.
        String containerName = generateRandomResourceName("tc", 24).toLowerCase(Locale.ROOT);

        String roleAssignmentName = null;
        boolean containerCreated = false;

        StorageMover sm = storageMoverManager.storageMovers()
            .define(storageMoverName)
            .withRegion(testRegion())
            .withExistingResourceGroup(resourceGroupName)
            .create();

        Project project = storageMoverManager.projects()
            .define(projectName)
            .withExistingStorageMover(resourceGroupName, sm.name())
            .create();
        Assertions.assertNotNull(project);

        try {
            // Step 2: per-test target container in cpmoveraccount.
            BlobContainer container = storageManager(XDATAMOVE_SYNTHETICS_SUB_ID).blobContainers()
                .defineContainer(containerName)
                .withExistingBlobService(TEST_STORAGE_ACCOUNT_RG, TEST_STORAGE_ACCOUNT_NAME)
                .withPublicAccess(PublicAccess.NONE)
                .create();
            containerCreated = true;
            String containerScope = container.id();

            // Step 3: create the Storage Mover Connection bound to the PLS.
            Connection connection = storageMoverManager.connections()
                .define(connectionName)
                .withExistingStorageMover(resourceGroupName, sm.name())
                .withProperties(new ConnectionProperties().withDescription("C2C private source")
                    .withPrivateLinkServiceId(PRIVATE_LINK_SERVICE_ID))
                .create();
            Assertions.assertNotNull(connection.properties());
            String privateEndpointResourceId = connection.properties().privateEndpointResourceId();
            Assertions.assertNotNull(privateEndpointResourceId,
                "Storage Mover RP did not return privateEndpointResourceId on the Connection create response");

            // Steps 4-5: locate the PE on the PLS and approve it.
            PrivateEndpointConnectionInner peConnection = findPrivateEndpointConnection(privateEndpointResourceId);
            approvePrivateEndpointConnection(peConnection.name());

            // Step 6: wait for the Storage Mover Connection to flip Approved.
            waitForConnectionApproved(sm.name(), connectionName);

            // Step 7: target Blob endpoint with explicit SystemAssigned MSI.
            Endpoint blobEndpoint = storageMoverManager.endpoints()
                .define(targetEndpointName)
                .withExistingStorageMover(resourceGroupName, sm.name())
                .withProperties(new AzureStorageBlobContainerEndpointProperties()
                    .withStorageAccountResourceId(TEST_STORAGE_ACCOUNT_ID)
                    .withBlobContainerName(containerName))
                .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
                .create();
            Assertions.assertNotNull(blobEndpoint.identity(), "blob endpoint create did not return an identity block");
            String principalId = blobEndpoint.identity().principalId();
            Assertions.assertNotNull(principalId, "blob endpoint identity.principalId was null");

            // Step 8: RBAC assignment with PrincipalNotFound retry.
            roleAssignmentName = assignBlobDataContributorWithRetry(principalId, containerScope);

            // Step 9: source MCC endpoint over the private bucket.
            Endpoint mccEndpoint
                = storageMoverManager.endpoints()
                    .define(sourceEndpointName)
                    .withExistingStorageMover(resourceGroupName, sm.name())
                    .withProperties(new AzureMultiCloudConnectorEndpointProperties()
                        .withMultiCloudConnectorId(MULTI_CLOUD_CONNECTOR_ID)
                        .withAwsS3BucketId(AWS_PRIVATE_S3_BUCKET_ID)
                        .withEndpointKind(EndpointKind.SOURCE))
                    .create();

            // Step 10: C2C JobDefinition (agentless, with the connection).
            JobDefinition jobDefinition = storageMoverManager.jobDefinitions()
                .define(jobDefinitionName)
                .withExistingProject(resourceGroupName, sm.name(), project.name())
                .withCopyMode(CopyMode.ADDITIVE)
                .withSourceName(mccEndpoint.name())
                .withTargetName(blobEndpoint.name())
                .withDescription("C2C with private source")
                .withJobType(JobType.CLOUD_TO_CLOUD)
                .withSourceSubpath("/")
                .withTargetSubpath("/")
                .withConnections(Collections.singletonList(connection.id()))
                .withDataIntegrityValidation(DataIntegrityValidation.NONE)
                .create();
            Assertions.assertEquals(jobDefinitionName, jobDefinition.name());
            Assertions.assertEquals(CopyMode.ADDITIVE, jobDefinition.copyMode());

            // Step 11: startJob → poll up to 30 min on a 30s cadence.
            JobRunResourceId startResult = jobDefinition.startJob();
            Assertions.assertNotNull(startResult);
            String jobRunFullId = startResult.jobRunResourceId();
            Assertions.assertNotNull(jobRunFullId, "startJob returned a null jobRunResourceId");
            String jobRunName = jobRunFullId.substring(jobRunFullId.lastIndexOf('/') + 1);

            JobRunStatus finalStatus = null;
            for (int attempt = 0; attempt < 60; attempt++) {
                JobRun run = storageMoverManager.jobRuns()
                    .get(resourceGroupName, sm.name(), project.name(), jobDefinitionName, jobRunName);
                JobRunStatus status = run.status();
                if (status != null && TERMINAL_STATES.contains(status)) {
                    finalStatus = status;
                    break;
                }
                sleep(Duration.ofSeconds(30));
            }
            Assertions.assertEquals(JobRunStatus.SUCCEEDED, finalStatus,
                "expected job run to finish Succeeded but got " + finalStatus);
        } finally {
            // Step 12: ordered cleanup. Each step is best-effort so an earlier
            // failure does not mask the underlying test error.
            if (roleAssignmentName != null) {
                try {
                    authorizationManager(XDATAMOVE_SYNTHETICS_SUB_ID).roleAssignments()
                        .deleteById("/subscriptions/" + XDATAMOVE_SYNTHETICS_SUB_ID + "/resourceGroups/"
                            + TEST_STORAGE_ACCOUNT_RG + "/providers/Microsoft.Storage/storageAccounts/"
                            + TEST_STORAGE_ACCOUNT_NAME + "/blobServices/default/containers/" + containerName
                            + "/providers/Microsoft.Authorization/roleAssignments/" + roleAssignmentName);
                } catch (RuntimeException ignored) {
                    // best effort
                }
            }
            try {
                storageMoverManager.connections().delete(resourceGroupName, sm.name(), connectionName);
            } catch (RuntimeException ignored) {
                // best effort — RG deletion in cleanUpResources will pick up stragglers
            }
            // Give the PLS time to release the slot before the next test claims
            // it. .NET hit NoValidConnectionFound on back-to-back runs without this.
            sleep(Duration.ofSeconds(60));
            if (containerCreated) {
                try {
                    storageManager(XDATAMOVE_SYNTHETICS_SUB_ID).blobContainers()
                        .delete(TEST_STORAGE_ACCOUNT_RG, TEST_STORAGE_ACCOUNT_NAME, containerName);
                } catch (RuntimeException ignored) {
                    // best effort
                }
            }
        }
    }
}
