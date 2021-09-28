// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.credential.AzureSasCredential
import com.azure.core.test.TestMode
import com.azure.core.util.Context
import com.azure.storage.blob.implementation.util.BlobSasImplUtil
import com.azure.storage.blob.models.BlobAccessPolicy
import com.azure.storage.blob.models.BlobProperties
import com.azure.storage.blob.models.BlobSignedIdentifier
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.UserDelegationKey
import com.azure.storage.blob.sas.BlobContainerSasPermission
import com.azure.storage.blob.sas.BlobSasPermission
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues
import com.azure.storage.blob.specialized.BlockBlobClient
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder
import com.azure.storage.common.implementation.AccountSasImplUtil
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.implementation.SasImplUtils
import com.azure.storage.common.implementation.StorageImplUtils
import com.azure.storage.common.sas.AccountSasPermission
import com.azure.storage.common.sas.AccountSasResourceType
import com.azure.storage.common.sas.AccountSasService
import com.azure.storage.common.sas.AccountSasSignatureValues
import com.azure.storage.common.sas.CommonSasQueryParameters
import com.azure.storage.common.sas.SasIpRange
import com.azure.storage.common.sas.SasProtocol
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion
import spock.lang.Retry
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class SasClientTests extends APISpec {

    BlockBlobClient sasClient
    String blobName

    def setup() {
        blobName = generateBlobName()
        sasClient = getBlobClient(env.primaryAccount.credential, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()
        sasClient.upload(data.defaultInputStream, data.defaultDataSize)
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2020_12_06")
    def "blob sas all permissions success"() {
        setup:
        def allPermissions = new BlobSasPermission()
            .setReadPermission(true)
            .setAddPermission(true)
            .setCreatePermission(true)
            .setWritePermission(true)
            .setDeletePermission(true)
            .setDeleteVersionPermission(true)
            .setPermanentDeletePermission(true)
            .setTagsPermission(true)
            .setListPermission(true)
            .setMovePermission(true)
            .setExecutePermission(true)
            .setImmutabilityPolicyPermission(true)

        def sasValues = generateValues(allPermissions)

        when:
        def sas = sasClient.generateSas(sasValues)

        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()

        def os = new ByteArrayOutputStream()
        client.download(os)
        def properties = client.getProperties()

        then:
        notThrown(BlobStorageException)
        os.toString() == data.defaultText
        validateSasProperties(properties)
    }

    def "blob sas read permissions"() {
        setup:
        def permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
        if (Constants.SAS_SERVICE_VERSION >= "2019-12-12") {
            permissions
                .setMovePermission(true)
                .setExecutePermission(true)
        }

        def sasValues = generateValues(permissions)

        when:
        def sas = sasClient.generateSas(sasValues)

        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()

        def os = new ByteArrayOutputStream()
        client.download(os)
        def properties = client.getProperties()

        then:
        notThrown(BlobStorageException)
        os.toString() == data.defaultText
        validateSasProperties(properties)
    }

    def "container sas identifier and permissions"() {
        setup:
        def identifier = new BlobSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new BlobAccessPolicy().setPermissions("racwdl")
                .setExpiresOn(namer.getUtcNow().plusDays(1)))
        cc.setAccessPolicy(null, Arrays.asList(identifier))

        // Check containerSASPermissions
        def permissions = new BlobContainerSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setListPermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
        if (Constants.SAS_SERVICE_VERSION >= "2019-12-12") {
            permissions
                .setMovePermission(true)
                .setExecutePermission(true)
        }
        def expiryTime = namer.getUtcNow().plusDays(1)

        when:
        def sasValues = new BlobServiceSasSignatureValues(identifier.getId())
        def sasWithId = cc.generateSas(sasValues)
        def client1 = getContainerClient(sasWithId, cc.getBlobContainerUrl())
        client1.listBlobs().iterator().hasNext()

        and:
        sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions)
        def sasWithPermissions = cc.generateSas(sasValues)
        def client2 = getContainerClient(sasWithPermissions, cc.getBlobContainerUrl())
        client2.listBlobs().iterator().hasNext()

        then:
        notThrown(BlobStorageException)
    }

    // RBAC replication lag
    @Retry(count = 5, delay = 30, condition = { env.testMode == TestMode.LIVE })
    def "blob sas user delegation"() {
        setup:
        def permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
        if (Constants.SAS_SERVICE_VERSION >= "2019-12-12") {
            permissions
                .setMovePermission(true)
                .setExecutePermission(true)
        }

        def sasValues = generateValues(permissions)

        when:
        def sas = sasClient.generateUserDelegationSas(sasValues, getUserDelegationInfo())

        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()

        def os = new ByteArrayOutputStream()
        client.download(os)
        def properties = client.getProperties()

        then:
        notThrown(BlobStorageException)
        os.toString() == data.defaultText
        validateSasProperties(properties)
    }

    def "blob sas snapshot"() {
        setup:
        def snapshotBlob = new SpecializedBlobClientBuilder().blobClient(sasClient.createSnapshot()).buildBlockBlobClient()
        def snapshotId = snapshotBlob.getSnapshotId()
        def permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
        def sasValues = generateValues(permissions)
        def sas = snapshotBlob.generateSas(sasValues)

        when:
        // base blob with snapshot SAS
        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getAppendBlobClient()
        client.download(new ByteArrayOutputStream())

        then:
        // snapshot-level SAS shouldn't be able to access base blob
        thrown(BlobStorageException)

        when:
        // blob snapshot with snapshot SAS
        def snapClient = getBlobClient(sas, cc.getBlobContainerUrl(), blobName, snapshotId).getAppendBlobClient()

        def os = new ByteArrayOutputStream()
        snapClient.download(os)

        def properties = snapClient.getProperties()

        then:
        notThrown(BlobStorageException)
        os.toByteArray() == data.defaultBytes

        then:
        validateSasProperties(properties)
    }

    // RBAC replication lag
    @Retry(count = 5, delay = 30, condition = { env.testMode == TestMode.LIVE })
    def "blob sas snapshot user delegation"() {
        setup:
        def snapshotBlob = new SpecializedBlobClientBuilder().blobClient(sasClient.createSnapshot()).buildBlockBlobClient()
        def snapshotId = snapshotBlob.getSnapshotId()

        def permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
        def sasValues = generateValues(permissions)
        def sas = snapshotBlob.generateUserDelegationSas(sasValues, getUserDelegationInfo())

        when:
        // base blob with snapshot SAS
        def client1 = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()
        client1.download(new ByteArrayOutputStream())

        then:
        // snapshot-level SAS shouldn't be able to access base blob
        thrown(BlobStorageException)

        when:
        // blob snapshot with snapshot SAS
        def client2 = getBlobClient(sas, cc.getBlobContainerUrl(), blobName, snapshotId).getBlockBlobClient()
        def os = new ByteArrayOutputStream()
        client2.download(os)

        def properties = client2.getProperties()

        then:
        notThrown(BlobStorageException)
        os.toString() == data.defaultText
        validateSasProperties(properties)
    }

    // RBAC replication lag
    @Retry(count = 5, delay = 30, condition = { env.testMode == TestMode.LIVE })
    def "container sas user delegation"() {
        setup:
        def permissions = new BlobContainerSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
        def expiryTime = namer.getUtcNow().plusDays(1)

        when:
        def sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions)
        def sasWithPermissions = cc.generateUserDelegationSas(sasValues, getUserDelegationInfo())

        def client = getContainerClient(sasWithPermissions, cc.getBlobContainerUrl())
        client.listBlobs().iterator().hasNext()

        then:
        notThrown(BlobStorageException)
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "blob sas tags"() {
        setup:
        def permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setTagsPermission(true)

        def sasValues = generateValues(permissions)
        def sas = sasClient.generateSas(sasValues)
        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName)

        when:
        def tags = new HashMap<String, String>()
        tags.put("foo", "bar")
        client.setTags(tags)
        def t = client.getTags()

        then:
        tags == t
        notThrown(BlobStorageException)
    }

    def "blob sas tags fail"() {
        setup:
        def permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            /* No tags permission */

        def sasValues = generateValues(permissions)
        def sas = sasClient.generateSas(sasValues)
        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName)

        when:
        def tags = new HashMap<String, String>()
        tags.put("foo", "bar")
        client.setTags(tags)

        then:
        thrown(BlobStorageException)
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "container sas tags"() {
        setup:
        def permissions = new BlobContainerSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
            .setDeleteVersionPermission(true)
            .setTagsPermission(true)

        def expiryTime = namer.getUtcNow().plusDays(1)
        def sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions)
        def sas = cc.generateSas(sasValues)
        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName)

        when:
        def tags = new HashMap<String, String>()
        tags.put("foo", "bar")
        client.setTags(tags)
        def t = client.getTags()

        then:
        tags == t
        notThrown(BlobStorageException)
    }

    def "container sas tags fail"() {
        setup:
        def permissions = new BlobContainerSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            /* No tags permission. */

        def expiryTime = namer.getUtcNow().plusDays(1)
        def sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions)
        def sas = sasClient.generateSas(sasValues)
        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName)

        when:
        def tags = new HashMap<String, String>()
        tags.put("foo", "bar")
        client.setTags(tags)

        then:
        thrown(BlobStorageException)
    }

    // RBAC replication lag
    @Retry(count = 5, delay = 30, condition = { env.testMode == TestMode.LIVE })
    def "blob user delegation saoid"() {
        setup:
        def permissions = new BlobSasPermission()
            .setReadPermission(true)

        def expiryTime = namer.getUtcNow().plusDays(1)

        def key = getOAuthServiceClient().getUserDelegationKey(null, expiryTime)

        def keyOid = namer.recordValueFromConfig(key.getSignedObjectId())
        key.setSignedObjectId(keyOid)

        def keyTid = namer.recordValueFromConfig(key.getSignedTenantId())
        key.setSignedTenantId(keyTid)

        def saoid = namer.getRandomUuid()

        when:
        def sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions)
            .setPreauthorizedAgentObjectId(saoid)
        def sasWithPermissions = sasClient.generateUserDelegationSas(sasValues, key)

        def client = getBlobClient(sasWithPermissions, cc.getBlobContainerUrl(), blobName)
        client.getProperties()

        then:
        sasWithPermissions.contains("saoid=" + saoid)
        notThrown(BlobStorageException)
    }

    // RBAC replication lag
    @Retry(count = 5, delay = 30, condition = { env.testMode == TestMode.LIVE })
    def "container user delegation correlation id"() {
        setup:
        def permissions = new BlobContainerSasPermission()
            .setListPermission(true)

        def expiryTime = namer.getUtcNow().plusDays(1)

        def key = getOAuthServiceClient().getUserDelegationKey(null, expiryTime)

        def keyOid = namer.recordValueFromConfig(key.getSignedObjectId())
        key.setSignedObjectId(keyOid)

        def keyTid = namer.recordValueFromConfig(key.getSignedTenantId())
        key.setSignedTenantId(keyTid)

        def cid = namer.getRandomUuid()

        when:
        def sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions)
            .setCorrelationId(cid)
        def sasWithPermissions = cc.generateUserDelegationSas(sasValues, key)

        def client = getContainerClient(sasWithPermissions, cc.getBlobContainerUrl())
        client.listBlobs().iterator().hasNext()

        then:
        sasWithPermissions.contains("scid=" + cid)
        notThrown(BlobStorageException)
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2020_02_10")
    def "container user delegation correlation id error"() {
        setup:
        def permissions = new BlobContainerSasPermission()
            .setListPermission(true)

        def expiryTime = namer.getUtcNow().plusDays(1)

        def key = getOAuthServiceClient().getUserDelegationKey(null, expiryTime)

        def keyOid = namer.recordValueFromConfig(key.getSignedObjectId())
        key.setSignedObjectId(keyOid)

        def keyTid = namer.recordValueFromConfig(key.getSignedTenantId())
        key.setSignedTenantId(keyTid)

        def cid = "invalidcid"

        when:
        def sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions)
            .setCorrelationId(cid)
        def sasWithPermissions = cc.generateUserDelegationSas(sasValues, key)

        def client = getContainerClient(sasWithPermissions, cc.getBlobContainerUrl())
        client.listBlobs().iterator().hasNext()

        then:
        sasWithPermissions.contains("scid=" + cid)
        thrown(BlobStorageException)
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    def "account sas tags and filter tags"() {
        setup:
        def service = new AccountSasService()
            .setBlobAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setDeleteVersionPermission(true)
            .setListPermission(true)
            .setUpdatePermission(true)
            .setProcessMessages(true)
            .setFilterTagsPermission(true)
            .setAddPermission(true)
            .setTagsPermission(true)
        def expiryTime = namer.getUtcNow().plusDays(1)
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryBlobServiceClient.generateAccountSas(sasValues)
        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()
        def tags = new HashMap<String, String>()
        tags.put("foo", "bar")

        when:
        client.setTags(tags)

        and:
        def t = client.getTags()

        then:
        tags == t
        notThrown(BlobStorageException)

        when:
        client = getServiceClient(sas, primaryBlobServiceClient.getAccountUrl())
        client.findBlobsByTags("\"foo\"='bar'").iterator().hasNext()

        then:
        notThrown(BlobStorageException)
    }

    def "account sas tags fail"() {
        setup:
        def service = new AccountSasService()
            .setBlobAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setDeleteVersionPermission(true)
            .setListPermission(true)
            .setUpdatePermission(true)
            .setProcessMessages(true)
            .setFilterTagsPermission(true)
            .setAddPermission(true)
        def expiryTime = namer.getUtcNow().plusDays(1)
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryBlobServiceClient.generateAccountSas(sasValues)
        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()
        def tags = new HashMap<String, String>()
        tags.put("foo", "bar")

        when:
        client.setTags(tags)

        then:
        thrown(BlobStorageException)
    }

    def "account sas filter tags fail"() {
        setup:
        def service = new AccountSasService()
            .setBlobAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setDeleteVersionPermission(true)
            .setListPermission(true)
            .setUpdatePermission(true)
            .setProcessMessages(true)
            .setAddPermission(true)
        def expiryTime = namer.getUtcNow().plusDays(1)
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryBlobServiceClient.generateAccountSas(sasValues)
        def client = getServiceClient(sas, primaryBlobServiceClient.getAccountUrl())

        when:
        client.findBlobsByTags("\"foo\"='bar'").iterator().hasNext()

        then:
        thrown(BlobStorageException)
    }

    def "account sas blob read"() {
        setup:
        def service = new AccountSasService()
            .setBlobAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
        def expiryTime = namer.getUtcNow().plusDays(1)

        when:
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryBlobServiceClient.generateAccountSas(sasValues)
        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()
        def os = new ByteArrayOutputStream()
        client.download(os)

        then:
        os.toString() == data.defaultText
    }

    def "account sas blob delete fails"() {
        setup:
        def service = new AccountSasService()
            .setBlobAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
        def expiryTime = namer.getUtcNow().plusDays(1)

        when:
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryBlobServiceClient.generateAccountSas(sasValues)
        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()
        client.delete()

        then:
        thrown(BlobStorageException)
    }

    def "account sas create container fails"() {
        setup:
        def service = new AccountSasService()
            .setBlobAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setCreatePermission(false)
        def expiryTime = namer.getUtcNow().plusDays(1)

        when:
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryBlobServiceClient.generateAccountSas(sasValues)
        def sc = getServiceClient(sas, primaryBlobServiceClient.getAccountUrl())
        sc.createBlobContainer(generateContainerName())

        then:
        thrown(BlobStorageException)
    }

    def "account sas create container succeeds"() {
        setup:
        def service = new AccountSasService()
            .setBlobAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setCreatePermission(true)
        def expiryTime = namer.getUtcNow().plusDays(1)

        when:
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryBlobServiceClient.generateAccountSas(sasValues)
        def sc = getServiceClient(sas, primaryBlobServiceClient.getAccountUrl())
        sc.createBlobContainer(generateContainerName())

        then:
        notThrown(BlobStorageException)
    }

    def "account sas on endpoint"() {
        setup:
        def service = new AccountSasService()
            .setBlobAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setCreatePermission(true)
        def expiryTime = namer.getUtcNow().plusDays(1)
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryBlobServiceClient.generateAccountSas(sasValues)

        when:

        def sc = getServiceClient(primaryBlobServiceClient.getAccountUrl() + "?" + sas)
        sc.createBlobContainer(generateContainerName())

        then:
        notThrown(BlobStorageException)

        when:
        def cc = getContainerClientBuilder(primaryBlobServiceClient.getAccountUrl() + "/" + containerName + "?" + sas).buildClient()
        cc.getProperties()

        then:
        notThrown(BlobStorageException)

        when:
        def bc = getBlobClient(env.primaryAccount.credential, primaryBlobServiceClient.getAccountUrl() + "/" + containerName + "/" + blobName + "?" + sas)
        def file = getRandomFile(256)
        bc.uploadFromFile(file.toPath().toString(), true)

        then:
        notThrown(BlobStorageException)
    }

    def "can use sas to authenticate"() {
        setup:
        def service = new AccountSasService()
            .setBlobAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
        def expiryTime = namer.getUtcNow().plusDays(1)
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryBlobServiceClient.generateAccountSas(sasValues)

        when:
        instrument(new BlobClientBuilder()
            .endpoint(cc.getBlobContainerUrl())
            .blobName(blobName)
            .sasToken(sas))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new BlobClientBuilder()
            .endpoint(cc.getBlobContainerUrl())
            .blobName(blobName)
            .credential(new AzureSasCredential(sas)))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new BlobClientBuilder()
            .endpoint(cc.getBlobContainerUrl() + "?" + sas)
            .blobName(blobName))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new SpecializedBlobClientBuilder()
            .endpoint(cc.getBlobContainerUrl())
            .blobName(blobName)
            .sasToken(sas))
            .buildBlockBlobClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new SpecializedBlobClientBuilder()
            .endpoint(cc.getBlobContainerUrl())
            .blobName(blobName)
            .credential(new AzureSasCredential(sas)))
            .buildBlockBlobClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new SpecializedBlobClientBuilder()
            .endpoint(cc.getBlobContainerUrl() + "?" + sas)
            .blobName(blobName))
            .buildBlockBlobClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new BlobContainerClientBuilder()
            .endpoint(cc.getBlobContainerUrl())
            .sasToken(sas))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new BlobContainerClientBuilder()
            .endpoint(cc.getBlobContainerUrl())
            .credential(new AzureSasCredential(sas)))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new BlobContainerClientBuilder()
            .endpoint(cc.getBlobContainerUrl() + "?" + sas))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new BlobServiceClientBuilder()
            .endpoint(cc.getBlobContainerUrl())
            .sasToken(sas))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new BlobServiceClientBuilder()
            .endpoint(cc.getBlobContainerUrl())
            .credential(new AzureSasCredential(sas)))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new BlobServiceClientBuilder()
            .endpoint(cc.getBlobContainerUrl() + "?" + sas))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()
    }

    BlobServiceSasSignatureValues generateValues(BlobSasPermission permission) {
        return new BlobServiceSasSignatureValues(namer.getUtcNow().plusDays(1), permission)
        .setStartTime(namer.getUtcNow().minusDays(1))
        .setProtocol(SasProtocol.HTTPS_HTTP)
        .setCacheControl("cache")
        .setContentDisposition("disposition")
        .setContentEncoding("encoding")
        .setContentLanguage("language")
        .setContentType("type")
    }

    def validateSasProperties(BlobProperties properties) {
        boolean ret = true
        ret &= properties.getCacheControl() == "cache"
        ret &= properties.getContentDisposition() == "disposition"
        ret &= properties.getContentEncoding() == "encoding"
        ret &= properties.getContentLanguage() == "language"
        return ret
    }

    UserDelegationKey getUserDelegationInfo() {
        def key = getOAuthServiceClient().getUserDelegationKey(namer.getUtcNow().minusDays(1), namer.getUtcNow().plusDays(1))
        def keyOid = namer.recordValueFromConfig(key.getSignedObjectId())
        key.setSignedObjectId(keyOid)
        def keyTid = namer.recordValueFromConfig(key.getSignedTenantId())
        key.setSignedTenantId(keyTid)
        return key
    }

    /*
     This test will ensure that each field gets placed into the proper location within the string to sign and that null
     values are handled correctly. We will validate the whole SAS with service calls as well as correct serialization of
     individual parts later.
     */

    @Unroll
    def "blob sas impl util string to sign"() {
        when:
        def e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        def p = new BlobSasPermission()
        p.setReadPermission(true)
        def v = new BlobServiceSasSignatureValues(e, p)

        def expected = String.format(expectedStringToSign, env.primaryAccount.name)

        v.setStartTime(startTime)

        if (ipRange != null) {
            def ipR = new SasIpRange()
            ipR.setIpMin("ip")
            v.setSasIpRange(ipR)
        }
        v.setIdentifier(identifier)
            .setProtocol(protocol)
            .setCacheControl(cacheControl)
            .setContentDisposition(disposition)
            .setContentEncoding(encoding)
            .setContentLanguage(language)
            .setContentType(type)

        def implUtil = new BlobSasImplUtil(v, "containerName", "blobName", snapId, versionId)

        def sasToken = implUtil.generateSas(env.primaryAccount.credential, Context.NONE)

        def token = BlobUrlParts.parse(cc.getBlobContainerUrl() + "?" + sasToken).getCommonSasQueryParameters()

        then:
        token.getSignature() == env.primaryAccount.credential.computeHmac256(expected)

        /*
        We don't test the blob or containerName properties because canonicalized resource is always added as at least
        /blob/accountName. We test canonicalization of resources later. Again, this is not to test a fully functional
        sas but the construction of the string to sign.
        Signed resource is tested elsewhere, as we work some minor magic in choosing which value to use.
         */
        where:
        startTime                                                 | identifier | ipRange          | protocol               | snapId   | cacheControl | disposition   | encoding   | language   | type   | versionId   || expectedStringToSign
        OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null       | null             | null                   | null     | null         | null          | null       | null       | null   | null        || "r\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | "id"       | null             | null                   | null     | null         | null          | null       | null       | null   | null        || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\nid\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null       | new SasIpRange() | null                   | null     | null         | null          | null       | null       | null   | null        || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\nip\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null       | null             | SasProtocol.HTTPS_ONLY | null     | null         | null          | null       | null       | null   | null        || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n" + SasProtocol.HTTPS_ONLY + "\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null       | null             | null                   | "snapId" | null         | null          | null       | null       | null   | null        || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nbs\nsnapId\n\n\n\n\n"
        null                                                      | null       | null             | null                   | null     | "control"    | null          | null       | null       | null   | null        || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\ncontrol\n\n\n\n"
        null                                                      | null       | null             | null                   | null     | null         | "disposition" | null       | null       | null   | null        || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\ndisposition\n\n\n"
        null                                                      | null       | null             | null                   | null     | null         | null          | "encoding" | null       | null   | null        || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\nencoding\n\n"
        null                                                      | null       | null             | null                   | null     | null         | null          | null       | "language" | null   | null        || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\nlanguage\n"
        null                                                      | null       | null             | null                   | null     | null         | null          | null       | null       | "type" | null        || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\ntype"
        null                                                      | null       | null             | null                   | null     | null         | null          | null       | null       | null   | "versionId" || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nbv\nversionId\n\n\n\n\n"
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2020_02_10")
    @Unroll
    def "blob sas impl util string to sign user delegation key"() {
        when:
        def e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        def p = new BlobSasPermission().setReadPermission(true)
        def v = new BlobServiceSasSignatureValues(e, p)

        def expected = String.format(expectedStringToSign, env.primaryAccount.name)

        v.setStartTime(startTime)

        if (ipRange != null) {
            def ipR = new SasIpRange()
            ipR.setIpMin("ip")
            v.setSasIpRange(ipR)
        }
        v.setProtocol(protocol)
            .setCacheControl(cacheControl)
            .setContentDisposition(disposition)
            .setContentEncoding(encoding)
            .setContentLanguage(language)
            .setContentType(type)
            .setPreauthorizedAgentObjectId(saoid)
            .setCorrelationId(cid)
        def key = new UserDelegationKey()
            .setSignedObjectId(keyOid)
            .setSignedTenantId(keyTid)
            .setSignedStart(keyStart)
            .setSignedExpiry(keyExpiry)
            .setSignedService(keyService)
            .setSignedVersion(keyVersion)
            .setValue(keyValue)

        def implUtil = new BlobSasImplUtil(v, "containerName", "blobName", snapId, versionId)

        def sasToken = implUtil.generateUserDelegationSas(key, env.primaryAccount.name, Context.NONE)

        def token = BlobUrlParts.parse(cc.getBlobContainerUrl() + "?" + sasToken).getCommonSasQueryParameters()

        then:
        token.getSignature() == StorageImplUtils.computeHMac256(key.getValue(), expected)

        /*
        We test string to sign functionality directly related to user delegation sas specific parameters
         */
        where:
        startTime                                                 | keyOid                                 | keyTid                                 | keyStart                                                              | keyExpiry                                                             | keyService | keyVersion   | keyValue                                       | ipRange          | protocol               | snapId   | cacheControl | disposition   | encoding   | language   | type   | versionId   | saoid    | cid   || expectedStringToSign
        OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   | null        | null     | null  || "r\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | "11111111-1111-1111-1111-111111111111" | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   | null        | null     | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n11111111-1111-1111-1111-111111111111\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | "22222222-2222-2222-2222-222222222222" | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   | null        | null     | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n22222222-2222-2222-2222-222222222222\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC) | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   | null        | null     | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC) | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   | null        | null     | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | "b"        | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   | null        | null     | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\nb\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | "2018-06-17" | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   | null        | null     | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n2018-06-17\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | new SasIpRange() | null                   | null     | null         | null          | null       | null       | null   | null        | null     | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\nip\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | SasProtocol.HTTPS_ONLY | null     | null         | null          | null       | null       | null   | null        | null     | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n" + SasProtocol.HTTPS_ONLY + "\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | "snapId" | null         | null          | null       | null       | null   | null        | null     | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nbs\nsnapId\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | "control"    | null          | null       | null       | null   | null        | null     | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\ncontrol\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | "disposition" | null       | null       | null   | null        | null     | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\ndisposition\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | "encoding" | null       | null   | null        | null     | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\nencoding\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | "language" | null   | null        | null     | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\nlanguage\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | "type" | null        | null     | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\ntype"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   | "versionId" | null     | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nbv\nversionId\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   | null        | "saoid"  | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\nsaoid\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   | null        | null     | "cid" || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\ncid\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
    }

    @Unroll
    def "blob sas impl util canonicalized resource"() {
        setup:
        BlobServiceSasSignatureValues v = new BlobServiceSasSignatureValues(expiryTime, new BlobSasPermission())

        BlobSasImplUtil implUtil = new BlobSasImplUtil(v, containerName, blobName, snapId, null)

        expectedStringToSign = String.format(expectedStringToSign,
            Constants.ISO_8601_UTC_DATE_FORMATTER.format(expiryTime),
            env.primaryAccount.name)

        when:
        String token = implUtil.generateSas(env.primaryAccount.credential, Context.NONE)

        def queryParams = new CommonSasQueryParameters(SasImplUtils.parseQueryString(token), true)

        then:
        queryParams.getSignature() == env.primaryAccount.credential.computeHmac256(expectedStringToSign)
        queryParams.getResource() == expectedResource

        where:
        containerName | blobName | snapId | expiryTime           || expectedResource | expectedStringToSign
        "c"           | "b"      | "id"   | OffsetDateTime.now() || "bs"             | "\n\n%s\n" + "/blob/%s/c/b\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nbs\nid\n\n\n\n\n"
        "c"           | "b"      | null   | OffsetDateTime.now() || "b"              | "\n\n%s\n" + "/blob/%s/c/b\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        "c"           | null     | null   | OffsetDateTime.now() || "c"              | "\n\n%s\n" + "/blob/%s/c\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nc\n\n\n\n\n\n"

    }

    @Unroll
    def "account sas impl util string to sign"() {
        when:
        def p = new AccountSasPermission()
            .setReadPermission(true)
        def e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        def s = new AccountSasService().setBlobAccess(true)
        def rt = new AccountSasResourceType().setObject(true)
        def v = new AccountSasSignatureValues(e, p, s, rt)
            .setStartTime(startTime)
        if (ipRange != null) {
            def ipR = new SasIpRange()
            ipR.setIpMin("ip")
            v.setSasIpRange(ipR)
        }
        v.setProtocol(protocol)

        def implUtil = new AccountSasImplUtil(v)

        def sasToken = implUtil.generateSas(env.primaryAccount.credential, Context.NONE)

        def token = BlobUrlParts.parse(cc.getBlobContainerUrl() + "?" + sasToken).getCommonSasQueryParameters()

        then:
        token.getSignature() == env.primaryAccount.credential.computeHmac256(String.format(expectedStringToSign, env.primaryAccount.name))

        where:
        startTime                                                 | ipRange          | protocol               || expectedStringToSign
        OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null             | null                   || "%s" + "\nr\nb\no\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n\n\n" + Constants.SAS_SERVICE_VERSION + "\n"
        null                                                      | new SasIpRange() | null                   || "%s" + "\nr\nb\no\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\nip\n\n" + Constants.SAS_SERVICE_VERSION + "\n"
        null                                                      | null             | SasProtocol.HTTPS_ONLY || "%s" + "\nr\nb\no\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n\n" + SasProtocol.HTTPS_ONLY + "\n" + Constants.SAS_SERVICE_VERSION + "\n"
    }

    /**
     * If this test fails it means that non-deprecated string to sign has new components.
     * In that case we should hardcode version used for deprecated string to sign like we did for user delegation sas.
     */
    def "Remember about string to sign deprecation"() {
        setup:
        def client = sasClient
        def values = new BlobServiceSasSignatureValues(namer.getUtcNow(), new BlobSasPermission())
        values.setContainerName(sasClient.containerName)
        values.setBlobName(sasClient.blobName)

        when:
        def deprecatedStringToSign = values.generateSasQueryParameters(env.primaryAccount.credential).encode()
        def stringToSign = client.generateSas(values)

        then:
        deprecatedStringToSign == stringToSign
    }
}
