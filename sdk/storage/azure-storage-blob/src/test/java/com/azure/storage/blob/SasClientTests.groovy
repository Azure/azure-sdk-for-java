package com.azure.storage.blob

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
import com.azure.storage.common.sas.AccountSasPermission
import com.azure.storage.common.sas.AccountSasResourceType
import com.azure.storage.common.sas.AccountSasService
import com.azure.storage.common.sas.AccountSasSignatureValues
import com.azure.storage.common.sas.SasIpRange
import com.azure.storage.common.sas.SasProtocol

class SasClientTests extends APISpec {

    BlockBlobClient sasClient
    String blobName

    def setup() {
        blobName = generateBlobName()
        sasClient = getBlobClient(primaryCredential, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()
        sasClient.upload(new ByteArrayInputStream(defaultData.array()), defaultDataSize)
    }

    def "network test blob sas"() {
        setup:
        def permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)

        def sasValues = generateValues(permissions)

        when:
        def sas = sasClient.generateSas(sasValues)

        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()

        def os = new ByteArrayOutputStream()
        client.download(os)
        def properties = client.getProperties()

        then:
        os.toString() == new String(defaultData.array())
        validateSasProperties(properties)
        notThrown(BlobStorageException)
    }

    def "network test blob snapshot"() {
        setup:

        def data = "test".getBytes()
        def blobName = generateBlobName()
        def bu = getBlobClient(primaryCredential, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()
        bu.upload(new ByteArrayInputStream(data), data.length)
        def snapshotId = bu.createSnapshot().getSnapshotId()
        def snapshotBlob = cc.getBlobClient(blobName, snapshotId).getBlockBlobClient()

        def permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)

        def sasValues = generateValues(permissions)

        when:
        def sas = snapshotBlob.generateSas(sasValues)

        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName, snapshotId).getBlockBlobClient()

        def os = new ByteArrayOutputStream()
        client.download(os)
        def properties = client.getProperties()

        then:
        os.toString() == new String(data)
        validateSasProperties(properties)
    }

    def "serviceSASSignatureValues network test container"() {
        setup:
        def identifier = new BlobSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new BlobAccessPolicy().setPermissions("racwdl")
                .setExpiresOn(getUTCNow().plusDays(1)))
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
        def expiryTime = getUTCNow().plusDays(1)

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

    def "network test blob user delegation"() {
        setup:
        def permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)

        def sasValues = generateValues(permissions)

        when:
        def sas = sasClient.generateUserDelegationSas(sasValues, getUserDelegationInfo())

        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()

        def os = new ByteArrayOutputStream()
        client.download(os)
        def properties = client.getProperties()

        then:
        os.toString() == new String(defaultData.array())
        validateSasProperties(properties)
        notThrown(BlobStorageException)
    }

    def "BlobServiceSAS network test blob snapshot"() {
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
        os.toByteArray() == defaultData.array()

        then:
        validateSasProperties(properties)
    }

    def "network test blob snapshot user delegation"() {
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
        os.toString() == new String(defaultData.array())
        validateSasProperties(properties)
    }

    def "network test container user delegation"() {
        setup:
        def permissions = new BlobContainerSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
        def expiryTime = getUTCNow().plusDays(1)

        when:
        def sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions)
        def sasWithPermissions = cc.generateUserDelegationSas(sasValues, getUserDelegationInfo())

        def client = getContainerClient(sasWithPermissions, cc.getBlobContainerUrl())
        client.listBlobs().iterator().hasNext()

        then:
        notThrown(BlobStorageException)
    }

    def "accountSAS network test blob read"() {
        setup:
        def service = new AccountSasService()
            .setBlobAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
        def expiryTime = getUTCNow().plusDays(1)

        when:
        def sasValues = new AccountSasSignatureValues(expiryTime, service, resourceType, permissions)
        def sas = primaryBlobServiceClient.generateSas(sasValues)
        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()
        def os = new ByteArrayOutputStream()
        client.download(os)

        then:
        os.toString() == new String(defaultData.array())
    }

    def "accountSAS network test blob delete fails new API"() {
        setup:
        def service = new AccountSasService()
            .setBlobAccess(true)
        def resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true)
        def permissions = new AccountSasPermission()
            .setReadPermission(true)
        def expiryTime = getUTCNow().plusDays(1)

        when:
        def sasValues = new AccountSasSignatureValues(expiryTime, service, resourceType, permissions)
        def sas = primaryBlobServiceClient.generateSas(sasValues)
        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()
        client.delete()

        then:
        thrown(BlobStorageException)
    }

    def "accountSAS network create container fails"() {
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
        def expiryTime = getUTCNow().plusDays(1)

        when:
        def sasValues = new AccountSasSignatureValues(expiryTime, service, resourceType, permissions)
        def sas = primaryBlobServiceClient.generateSas(sasValues)
        def sc = getServiceClient(sas, primaryBlobServiceClient.getAccountUrl())
        sc.createBlobContainer(generateContainerName())

        then:
        thrown(BlobStorageException)
    }

    def "accountSAS network create container succeeds"() {
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
        def expiryTime = getUTCNow().plusDays(1)

        when:
        def sasValues = new AccountSasSignatureValues(expiryTime, service, resourceType, permissions)
        def sas = primaryBlobServiceClient.generateSas(sasValues)
        def sc = getServiceClient(sas, primaryBlobServiceClient.getAccountUrl())
        sc.createBlobContainer(generateContainerName())

        then:
        notThrown(BlobStorageException)
    }

    BlobServiceSasSignatureValues generateValues(BlobSasPermission permission) {
        return new BlobServiceSasSignatureValues(getUTCNow().plusDays(1), permission)
        .setStartTime(getUTCNow().minusDays(1))
        .setProtocol(SasProtocol.HTTPS_HTTP)
        .setSasIpRange(new SasIpRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255"))
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
        def key = getOAuthServiceClient().getUserDelegationKey(getUTCNow().minusDays(1), getUTCNow().plusDays(1))
        def keyOid = getConfigValue(key.getSignedObjectId())
        key.setSignedObjectId(keyOid)
        def keyTid = getConfigValue(key.getSignedTenantId())
        key.setSignedTenantId(keyTid)
        return key
    }

}
