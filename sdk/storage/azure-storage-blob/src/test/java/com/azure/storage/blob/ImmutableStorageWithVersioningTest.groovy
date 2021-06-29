// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.credential.TokenCredential
import com.azure.core.http.HttpHeaders
import com.azure.core.http.HttpMethod
import com.azure.core.http.HttpPipeline
import com.azure.core.http.HttpPipelineBuilder
import com.azure.core.http.HttpRequest
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy
import com.azure.core.test.TestMode
import com.azure.identity.EnvironmentCredentialBuilder
import com.azure.storage.blob.models.BlobErrorCode
import com.azure.storage.blob.models.BlobImmutabilityPolicy
import com.azure.storage.blob.models.BlobImmutabilityPolicyMode
import com.azure.storage.blob.models.BlobListDetails
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.LeaseStateType
import com.azure.storage.blob.models.ListBlobContainersOptions
import com.azure.storage.blob.models.ListBlobsOptions
import com.azure.storage.blob.models.ParallelTransferOptions
import com.azure.storage.blob.models.PublicAccessType
import com.azure.storage.blob.options.AppendBlobCreateOptions
import com.azure.storage.blob.options.BlobBeginCopyOptions
import com.azure.storage.blob.options.BlobBreakLeaseOptions
import com.azure.storage.blob.options.BlobCopyFromUrlOptions
import com.azure.storage.blob.options.BlobParallelUploadOptions
import com.azure.storage.blob.options.BlockBlobCommitBlockListOptions
import com.azure.storage.blob.options.BlockBlobSimpleUploadOptions
import com.azure.storage.blob.options.PageBlobCreateOptions
import com.azure.storage.blob.sas.BlobContainerSasPermission
import com.azure.storage.blob.sas.BlobSasPermission
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues
import com.azure.storage.common.sas.AccountSasPermission
import com.azure.storage.common.sas.AccountSasResourceType
import com.azure.storage.common.sas.AccountSasService
import com.azure.storage.common.sas.AccountSasSignatureValues
import com.azure.storage.common.test.shared.extensions.LiveOnly
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion
import com.fasterxml.jackson.databind.ObjectMapper
import reactor.core.publisher.Flux
import spock.lang.ResourceLock
import spock.lang.Unroll

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.temporal.ChronoUnit

@ResourceLock("ManagementPlaneThrottling")
@RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2020_10_02")
class ImmutableStorageWithVersioningTest extends APISpec {

    private BlobContainerClient vlwContainer;
    private BlobClient vlwBlob
    private String accountName = env.primaryAccount.name
    private String containerName
    private String resourceGroupName = "XClient"
    private String subscriptionId = "ba45b233-e2ef-4169-8808-49eb0d8eba0d"
    private String apiVersion = "2021-04-01"
    private TokenCredential credential = new EnvironmentCredentialBuilder().build()
    private BearerTokenAuthenticationPolicy credentialPolicy = new BearerTokenAuthenticationPolicy(credential, "https://management.azure.com/.default")

    def setup() {
        containerName = generateContainerName()

        if (env.testMode != TestMode.PLAYBACK) {
            String url = String.format("https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/"
                + "Microsoft.Storage/storageAccounts/%s/blobServices/default/containers/%s?api-version=%s", subscriptionId,
                resourceGroupName, accountName, containerName, apiVersion)
            HttpPipeline httpPipeline = new HttpPipelineBuilder()
                .policies(credentialPolicy)
                .httpClient(getHttpClient())
                .build()

            def immutableStorageWithVersioning = new ImmutableStorageWithVersioning()
            immutableStorageWithVersioning.enabled = true
            def properties = new Properties()
            properties.immutableStorageWithVersioning = immutableStorageWithVersioning
            def body = new Body()
            body.id = String.format("/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Storage/storageAccounts/"
                + "%s/blobServices/default/containers/%s", subscriptionId, resourceGroupName, accountName, containerName)
            body.name = containerName
            body.type = "Microsoft.Storage/storageAccounts/blobServices/containers"
            body.properties = properties

            String serializedBody = new ObjectMapper().writeValueAsString(body)

            httpPipeline.send(new HttpRequest(HttpMethod.PUT, new URL(url), new HttpHeaders(),
                Flux.just(ByteBuffer.wrap(serializedBody.getBytes(StandardCharsets.UTF_8)))))
                .block()
        }

        vlwContainer = primaryBlobServiceClient.getBlobContainerClient(containerName)
        vlwBlob = vlwContainer.getBlobClient(generateBlobName())
        vlwBlob.upload(new ByteArrayInputStream(new byte[0]), 0)
    }

    private final class Body {
        public String id
        public String name
        public String type
        public Properties properties
    }
    private final class Properties {
        public ImmutableStorageWithVersioning immutableStorageWithVersioning
    }
    private final class ImmutableStorageWithVersioning {
        public boolean enabled
    }

    def cleanup() {

        if (env.testMode != TestMode.PLAYBACK) {
            HttpPipeline httpPipeline = new HttpPipelineBuilder()
                .policies(credentialPolicy)
                .httpClient(getHttpClient())
                .build()
            def cleanupClient = new BlobServiceClientBuilder()
                .httpClient(getHttpClient())
                .credential(env.primaryAccount.credential)
                .endpoint(env.primaryAccount.blobEndpoint)
                .buildClient()

            def options = new ListBlobContainersOptions().setPrefix(namer.getResourcePrefix())
            for (def container : cleanupClient.listBlobContainers(options, null)) {
                def containerClient = cleanupClient.getBlobContainerClient(container.getName())

                if (container.getProperties().getLeaseState() == LeaseStateType.LEASED) {
                    createLeaseClient(containerClient).breakLeaseWithResponse(new BlobBreakLeaseOptions().setBreakPeriod(Duration.ofSeconds(0)), null, null)
                }
                if (container.getProperties().isImmutableStorageWithVersioningEnabled()) {
                    options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveImmutabilityPolicy(true).setRetrieveLegalHold(true))
                    for (def blob: containerClient.listBlobs(options, null)) {
                        def blobClient = containerClient.getBlobClient(blob.getName())
                        if (blob.getProperties().hasLegalHold()) {
                            blobClient.setLegalHold(false)
                        }
                        if (blob.getProperties().getImmutabilityPolicy().getPolicyMode() != null) {
                            sleepIfRecord(5 * 1000)
                            blobClient.deleteImmutabilityPolicy()
                        }
                        blobClient.delete()
                    }
                }

                String url = String.format("https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/"
                    + "Microsoft.Storage/storageAccounts/%s/blobServices/default/containers/%s?api-version=%s", subscriptionId,
                    resourceGroupName, accountName, container.getName(), apiVersion)
                httpPipeline.send(new HttpRequest(HttpMethod.DELETE, new URL(url), new HttpHeaders(), Flux.empty()))
                    .block()
            }
        }

    }

    def "set immutability policy min"() {
        setup:
        def expiryTime = getNamer().getUtcNow().plusSeconds(2)
        def immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED)

        // The service rounds Immutability Policy Expiry to the nearest second.
        def expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS)

        when:
        def response = vlwBlob.setImmutabilityPolicy(immutabilityPolicy)

        then:
        expectedImmutabilityPolicyExpiry == response.getExpiryTime()
        BlobImmutabilityPolicyMode.UNLOCKED == response.getPolicyMode()
    }

    @Unroll
    def "set immutability policy"() {
        setup:
        def expiryTime = getNamer().getUtcNow().plusSeconds(2)
        def immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(policyMode)

        // The service rounds Immutability Policy Expiry to the nearest second.
        def expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS)

        when: "set immutability policy"
        def response = vlwBlob.setImmutabilityPolicyWithResponse(immutabilityPolicy, null, null, null).getValue()

        then:
        expectedImmutabilityPolicyExpiry == response.getExpiryTime()
        policyMode == response.getPolicyMode()

        when: "get properties"
        response = vlwBlob.getProperties()

        then:
        expectedImmutabilityPolicyExpiry == response.getImmutabilityPolicy().getExpiryTime()
        policyMode.toString() == response.getImmutabilityPolicy().getPolicyMode().toString()

        when: "list blob"
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveImmutabilityPolicy(true).setRetrieveLegalHold(true))
        response = vlwContainer.listBlobs(options, null).iterator()

        then:
        def blob = response.next()
        !response.hasNext()
        blob.getName() == vlwBlob.getBlobName()
        expectedImmutabilityPolicyExpiry == blob.getProperties().getImmutabilityPolicy().getExpiryTime()
        policyMode.toString() == blob.getProperties().getImmutabilityPolicy().getPolicyMode().toString()


        where:
        policyMode                          || _
        BlobImmutabilityPolicyMode.UNLOCKED || _
    }

    @Unroll
    def "set immutability policy AC"() {
        setup:
        def bac = new BlobRequestConditions()
            .setIfUnmodifiedSince(unmodified)
        def expiryTime = getNamer().getUtcNow().plusSeconds(2)
        def immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED)

        when:
        def response = vlwBlob.setImmutabilityPolicyWithResponse(immutabilityPolicy, bac, null, null)

        then:
        response.getStatusCode() == 200

        where:
        unmodified || _
        null       || _
        newDate    || _
    }

    def "set immutability policy AC fail"() {
        setup:
        def bac = new BlobRequestConditions()
            .setIfUnmodifiedSince(oldDate)
        def expiryTime = getNamer().getUtcNow().plusSeconds(2)
        def immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED)

        when:
        vlwBlob.setImmutabilityPolicyWithResponse(immutabilityPolicy, bac, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.CONDITION_NOT_MET
    }

    @Unroll
    def "set immutability policy AC IA"() {
        setup:
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseId)
            .setTagsConditions(tags)
            .setIfMatch(ifMatch)
            .setIfNoneMatch(ifNoneMatch)
            .setIfModifiedSince(ifModifiedSince)
        def expiryTime = getNamer().getUtcNow().plusSeconds(2)
        def immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED)

        when:
        vlwBlob.setImmutabilityPolicyWithResponse(immutabilityPolicy, bac, null, null)

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage() == String.format("%s does not support the %s request condition(s) for parameter 'requestConditions'.", "setImmutabilityPolicy(WithResponse)", wrongCondition)

        where:
        leaseId     | tags              | ifMatch   | ifNoneMatch   | ifModifiedSince    || wrongCondition
        "leaseId"   | null              | null      | null          | null               || "LeaseId"
        null        | "tagsConditions"  | null      | null          | null               || "TagsConditions"
        null        | null              | "ifMatch" | null          | null               || "IfMatch"
        null        | null              | null      | "ifNoneMatch" | null               || "IfNoneMatch"
        null        | null              | null      | null          | oldDate            || "IfModifiedSince"
        "leaseId"   | "tagsConditions"  | "ifMatch" | "ifNoneMatch" | oldDate            || "LeaseId, TagsConditions, IfModifiedSince, IfMatch, IfNoneMatch"
    }

    def "set immutability policy error"() {
        setup:
        def blob = vlwContainer.getBlobClient(generateBlobName())
        def expiryTime = getNamer().getUtcNow().plusSeconds(2)
        def immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED)

        when:
        blob.setImmutabilityPolicyWithResponse(immutabilityPolicy, null, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND
    }

    def "set immutability policy IA"() {
        setup:
        def expiryTime = getNamer().getUtcNow().plusSeconds(2)
        def immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.MUTABLE)

        when:
        vlwBlob.setImmutabilityPolicyWithResponse(immutabilityPolicy, null, null, null)

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage() == "immutabilityPolicy.policyMode must be Locked or Unlocked"
    }

    def "delete immutability policy min"() {
        setup:
        def expiryTime = getNamer().getUtcNow().plusSeconds(2)
        def immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED)
        vlwBlob.setImmutabilityPolicy(immutabilityPolicy)

        when:
        vlwBlob.deleteImmutabilityPolicy()

        then:
        def properties = vlwBlob.getProperties()
        properties.getImmutabilityPolicy().getPolicyMode() == null
        properties.getImmutabilityPolicy().getExpiryTime() == null
    }

    def "delete immutability policy"() {
        setup:
        def expiryTime = getNamer().getUtcNow().plusSeconds(2)
        def immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED)
        vlwBlob.setImmutabilityPolicy(immutabilityPolicy)

        when:
        vlwBlob.deleteImmutabilityPolicyWithResponse(null, null)

        then:
        def properties = vlwBlob.getProperties()
        properties.getImmutabilityPolicy().getPolicyMode() == null
        properties.getImmutabilityPolicy().getExpiryTime() == null
    }

    def "delete immutability policy error"() {
        setup:
        def blob = vlwContainer.getBlobClient(generateBlobName())

        when:
        blob.deleteImmutabilityPolicyWithResponse(null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND
    }

    @Unroll
    def "set legal hold min"() {
        when:
        def response = vlwBlob.setLegalHold(legalHold)

        then:
        response.hasLegalHold() == legalHold

        where:
        legalHold || _
        true      || _
        false     || _
    }

    @Unroll
    def "set legal hold"() {
        when: "set legal hold"
        def response = vlwBlob.setLegalHoldWithResponse(legalHold, null, null)

        then:
        response.getValue().hasLegalHold() == legalHold

        when: "get properties"
        response = vlwBlob.getProperties()

        then:
        legalHold == response.hasLegalHold()

        when: "list blob"
        def options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveImmutabilityPolicy(true).setRetrieveLegalHold(true))
        response = vlwContainer.listBlobs(options, null).iterator()

        then:
        def blob = response.next()
        !response.hasNext()
        blob.getName() == vlwBlob.getBlobName()
        legalHold == blob.getProperties().hasLegalHold()

        where:
        legalHold || _
        true      || _
        false     || _
    }

    def "set legal hold error"() {
        setup:
        def blob = vlwContainer.getBlobClient(generateBlobName())

        when:
        blob.setLegalHoldWithResponse(false, null, null)

        then:
        def e = thrown(BlobStorageException)
        e.getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND
    }

    def "container properties"() {
        when:
        def response = vlwContainer.getProperties()

        then:
        response.isImmutableStorageWithVersioningEnabled()

        when:
        response = vlwContainer.getServiceClient().listBlobContainers(new ListBlobContainersOptions().setPrefix(vlwContainer.getBlobContainerName()), null).iterator()

        then:
        def container = response.next()
        !response.hasNext()
        container.getProperties().isImmutableStorageWithVersioningEnabled()
    }

    def "append blob create"() {
        setup:
        def appendBlob = vlwContainer.getBlobClient(generateBlobName()).getAppendBlobClient()
        def expiryTime = getNamer().getUtcNow().plusSeconds(2)
        // The service rounds Immutability Policy Expiry to the nearest second.
        def expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS)
        def immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED)

        when:
        appendBlob.createWithResponse(new AppendBlobCreateOptions()
            .setImmutabilityPolicy(immutabilityPolicy)
            .setLegalHold(true), null, null)

        then:
        def response = appendBlob.getProperties()
        expectedImmutabilityPolicyExpiry == response.getImmutabilityPolicy().getExpiryTime()
        BlobImmutabilityPolicyMode.UNLOCKED.toString() == response.getImmutabilityPolicy().getPolicyMode().toString()
        response.hasLegalHold()
    }

    def "page blob create"() {
        setup:
        def pageBlob = vlwContainer.getBlobClient(generateBlobName()).getPageBlobClient()
        def expiryTime = getNamer().getUtcNow().plusSeconds(2)
        // The service rounds Immutability Policy Expiry to the nearest second.
        def expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS)
        def immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED)

        when:
        pageBlob.createWithResponse(new PageBlobCreateOptions(512)
            .setImmutabilityPolicy(immutabilityPolicy)
            .setLegalHold(true), null, null)

        then:
        def response = pageBlob.getProperties()
        expectedImmutabilityPolicyExpiry == response.getImmutabilityPolicy().getExpiryTime()
        BlobImmutabilityPolicyMode.UNLOCKED.toString() == response.getImmutabilityPolicy().getPolicyMode().toString()
        response.hasLegalHold()
    }

    def "block blob commit block list"() {
        setup:
        def blockBlob = vlwBlob.getBlockBlobClient()
        def expiryTime = getNamer().getUtcNow().plusSeconds(2)
        // The service rounds Immutability Policy Expiry to the nearest second.
        def expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS)
        def immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED)

        when:
        blockBlob.commitBlockListWithResponse(new BlockBlobCommitBlockListOptions(new ArrayList<String>())
            .setImmutabilityPolicy(immutabilityPolicy)
            .setLegalHold(true), null, null)

        then:
        def response = blockBlob.getProperties()
        expectedImmutabilityPolicyExpiry == response.getImmutabilityPolicy().getExpiryTime()
        BlobImmutabilityPolicyMode.UNLOCKED.toString() == response.getImmutabilityPolicy().getPolicyMode().toString()
        response.hasLegalHold()
    }

    def "block blob upload"() {
        setup:
        def blockBlob = vlwBlob.getBlockBlobClient()
        def expiryTime = getNamer().getUtcNow().plusSeconds(2)
        // The service rounds Immutability Policy Expiry to the nearest second.
        def expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS)
        def immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED)

        when:
        blockBlob.uploadWithResponse(new BlockBlobSimpleUploadOptions(data.defaultFlux, data.defaultDataSize)
            .setImmutabilityPolicy(immutabilityPolicy)
            .setLegalHold(true), null, null)

        then:
        def response = blockBlob.getProperties()
        expectedImmutabilityPolicyExpiry == response.getImmutabilityPolicy().getExpiryTime()
        BlobImmutabilityPolicyMode.UNLOCKED.toString() == response.getImmutabilityPolicy().getPolicyMode().toString()
        response.hasLegalHold()
    }

    @Unroll
    @LiveOnly
    def "blob upload"() {
        setup:
        def expiryTime = getNamer().getUtcNow().plusSeconds(2)
        // The service rounds Immutability Policy Expiry to the nearest second.
        def expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS)
        def immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED)

        when:
        vlwBlob.uploadWithResponse(new BlobParallelUploadOptions(data.defaultFlux)
            .setParallelTransferOptions(new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(blockSize))
            .setImmutabilityPolicy(immutabilityPolicy)
            .setLegalHold(true), null, null)

        then:
        def response = vlwBlob.getProperties()
        expectedImmutabilityPolicyExpiry == response.getImmutabilityPolicy().getExpiryTime()
        BlobImmutabilityPolicyMode.UNLOCKED.toString() == response.getImmutabilityPolicy().getPolicyMode().toString()
        response.hasLegalHold()

        where:
        blockSize || _
        1         || _ // Tests multi-part upload
        null      || _ // Tests single shot upload
    }

    def "sync copy"() {
        setup:
        vlwContainer.setAccessPolicy(PublicAccessType.CONTAINER, null)
        def destination = vlwContainer.getBlobClient(generateBlobName()).getBlockBlobClient()
        def expiryTime = getNamer().getUtcNow().plusSeconds(2)
        // The service rounds Immutability Policy Expiry to the nearest second.
        def expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS)
        def immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED)

        when:
        destination.copyFromUrlWithResponse(new BlobCopyFromUrlOptions(vlwBlob.getBlobUrl())
            .setImmutabilityPolicy(immutabilityPolicy)
            .setLegalHold(true), null, null)

        then:
        def response = destination.getProperties()
        expectedImmutabilityPolicyExpiry == response.getImmutabilityPolicy().getExpiryTime()
        BlobImmutabilityPolicyMode.UNLOCKED.toString() == response.getImmutabilityPolicy().getPolicyMode().toString()
        response.hasLegalHold()

        cleanup:
        vlwContainer.setAccessPolicy(null, null)
    }

    def "copy"() {
        setup:
        def destination = vlwContainer.getBlobClient(generateBlobName()).getBlockBlobClient()
        def expiryTime = getNamer().getUtcNow().plusSeconds(2)
        // The service rounds Immutability Policy Expiry to the nearest second.
        def expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS)
        def immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED)

        when:
        def poller = destination.beginCopy(new BlobBeginCopyOptions(vlwBlob.getBlobUrl())
            .setImmutabilityPolicy(immutabilityPolicy)
            .setLegalHold(true).setPollInterval(getPollingDuration(1000)))
        poller.waitForCompletion()

        then:
        def response = destination.getProperties()
        expectedImmutabilityPolicyExpiry == response.getImmutabilityPolicy().getExpiryTime()
        BlobImmutabilityPolicyMode.UNLOCKED.toString() == response.getImmutabilityPolicy().getPolicyMode().toString()
        response.hasLegalHold()
    }

    /* SAS tests */
    def "account sas"() {
        setup:
        def expiryTime = namer.getUtcNow().plusDays(1)
        def permissions = AccountSasPermission.parse("rwdxlacuptfi")
        def service = new AccountSasService().setBlobAccess(true)
        def resource = new AccountSasResourceType().setObject(true).setContainer(true)
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resource)
        expiryTime = getNamer().getUtcNow().plusSeconds(2)
        // The service rounds Immutability Policy Expiry to the nearest second.
        def expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS)
        def immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED)
        def sas = primaryBlobServiceClient.generateAccountSas(sasValues)
        def client = getBlobClient(sas, vlwContainer.getBlobContainerUrl(), vlwBlob.getBlobName())

        when:
        def response = client.setImmutabilityPolicy(immutabilityPolicy)

        then:
        expectedImmutabilityPolicyExpiry == response.getExpiryTime()
        BlobImmutabilityPolicyMode.UNLOCKED.toString() == response.getPolicyMode().toString()

        when:
        response = client.setLegalHold(false)

        then:
        !response.hasLegalHold()
    }

    def "container sas"() {
        setup:
        def expiryTime = namer.getUtcNow().plusDays(1)
        def permissions = BlobContainerSasPermission.parse("racwdxltmei")
        def sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions)
        expiryTime = getNamer().getUtcNow().plusSeconds(2)
        // The service rounds Immutability Policy Expiry to the nearest second.
        def expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS)
        def immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED)
        def sas = vlwContainer.generateSas(sasValues)
        def client = getBlobClient(sas, vlwContainer.getBlobContainerUrl(), vlwBlob.getBlobName())

        when:
        def response = client.setImmutabilityPolicy(immutabilityPolicy)

        then:
        expectedImmutabilityPolicyExpiry == response.getExpiryTime()
        BlobImmutabilityPolicyMode.UNLOCKED.toString() == response.getPolicyMode().toString()

        when:
        response = client.setLegalHold(false)

        then:
        !response.hasLegalHold()
    }

    def "blob sas"() {
        setup:
        def expiryTime = namer.getUtcNow().plusDays(1)
        def permissions = BlobSasPermission.parse("racwdxtlmei")
        def sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions)
        expiryTime = getNamer().getUtcNow().plusSeconds(2)
        // The service rounds Immutability Policy Expiry to the nearest second.
        def expectedImmutabilityPolicyExpiry = expiryTime.truncatedTo(ChronoUnit.SECONDS)
        def immutabilityPolicy = new BlobImmutabilityPolicy()
            .setExpiryTime(expiryTime)
            .setPolicyMode(BlobImmutabilityPolicyMode.UNLOCKED)
        def sas = vlwBlob.generateSas(sasValues)
        def client = getBlobClient(sas, vlwContainer.getBlobContainerUrl(), vlwBlob.getBlobName())

        when:
        def response = client.setImmutabilityPolicy(immutabilityPolicy)

        then:
        expectedImmutabilityPolicyExpiry == response.getExpiryTime()
        BlobImmutabilityPolicyMode.UNLOCKED.toString() == response.getPolicyMode().toString()

        when:
        response = client.setLegalHold(false)

        then:
        !response.hasLegalHold()
    }

}
