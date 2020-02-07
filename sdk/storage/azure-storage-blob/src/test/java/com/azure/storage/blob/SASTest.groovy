// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.storage.blob.models.BlobAccessPolicy
import com.azure.storage.blob.models.BlobRange
import com.azure.storage.blob.models.BlobSignedIdentifier
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.models.UserDelegationKey
import com.azure.storage.blob.sas.BlobContainerSasPermission
import com.azure.storage.blob.sas.BlobSasPermission
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder
import com.azure.storage.common.Utility
import com.azure.storage.common.implementation.StorageImplUtils
import com.azure.storage.common.sas.AccountSasPermission
import com.azure.storage.common.sas.AccountSasResourceType
import com.azure.storage.common.sas.AccountSasService
import com.azure.storage.common.sas.AccountSasSignatureValues
import com.azure.storage.common.sas.SasProtocol
import com.azure.storage.common.StorageSharedKeyCredential

import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.sas.SasIpRange
import spock.lang.Ignore
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class SASTest extends APISpec {

    // TODO (gapra) : Cleanup SAS Tests

    @Unroll
    def "Blob range"() {
        expect:
        if (count == null) {
            assert new BlobRange(offset).toHeaderValue() == result
        } else {
            assert new BlobRange(offset, count).toHeaderValue() == result
        }


        where:
        offset | count || result
        0      | null  || null
        0      | 5     || "bytes=0-4"
        5      | 10    || "bytes=5-14"
    }

    @Unroll
    def "Blob range IA"() {
        when:
        new BlobRange(offset, count)

        then:
        thrown(IllegalArgumentException)

        where:
        offset | count
        -1     | 5
        0      | -1
    }

    def "BlobClient getSnapshot"() {
        setup:
        def data = "test".getBytes()
        def blobName = generateBlobName()
        def bu = cc.getBlobClient(blobName).getBlockBlobClient()
        bu.upload(new ByteArrayInputStream(data), data.length)
        def snapshotId = bu.createSnapshot().getSnapshotId()

        when:
        def snapshotBlob = cc.getBlobClient(blobName, snapshotId).getBlockBlobClient()

        then:
        snapshotBlob.getSnapshotId() == snapshotId
        bu.getSnapshotId() == null
    }

    def "BlobClient isSnapshot"() {
        setup:
        def data = "test".getBytes()
        def blobName = generateBlobName()
        def bu = cc.getBlobClient(blobName).getBlockBlobClient()
        bu.upload(new ByteArrayInputStream(data), data.length)
        def snapshotId = bu.createSnapshot().getSnapshotId()

        when:
        def snapshotBlob = cc.getBlobClient(blobName, snapshotId).getBlockBlobClient()

        then:
        snapshotBlob.isSnapshot()
        !bu.isSnapshot()

    }

    def "serviceSASSignatureValues network test blob"() {
        setup:
        def data = "test".getBytes()
        def blobName = generateBlobName()
        def bu = getBlobClient(primaryCredential, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()
        bu.upload(new ByteArrayInputStream(data), data.length)

        def permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
        def startTime = getUTCNow().minusDays(1)
        def expiryTime = getUTCNow().plusDays(1)
        def ipRange = new SasIpRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255")
        def sasProtocol = SasProtocol.HTTPS_HTTP
        def cacheControl = "cache"
        def contentDisposition = "disposition"
        def contentEncoding = "encoding"
        def contentLanguage = "language"
        def contentType = "type"

        when:
        def sas = new BlobServiceSasSignatureValues()
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .setStartTime(startTime)
            .setProtocol(sasProtocol)
            .setSasIpRange(ipRange)
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType)
            .setBlobName(bu.getBlobName())
            .setContainerName(bu.getContainerName())
            .setSnapshotId(bu.getSnapshotId())
            .generateSasQueryParameters(primaryCredential)
            .encode()

        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()

        def os = new ByteArrayOutputStream()
        client.download(os)
        def properties = client.getProperties()

        then:
        os.toString() == new String(data)
        properties.getCacheControl() == "cache"
        properties.getContentDisposition() == "disposition"
        properties.getContentEncoding() == "encoding"
        properties.getContentLanguage() == "language"
        notThrown(BlobStorageException)
    }

    def "serviceSASSignatureValues network test blob snapshot"() {
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
        def startTime = getUTCNow().minusDays(1)
        def expiryTime = getUTCNow().plusDays(1)
        def ipRange = new SasIpRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255")
        def sasProtocol = SasProtocol.HTTPS_HTTP
        def cacheControl = "cache"
        def contentDisposition = "disposition"
        def contentEncoding = "encoding"
        def contentLanguage = "language"
        def contentType = "type"

        when:
        def sas = new BlobServiceSasSignatureValues()
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .setStartTime(startTime)
            .setProtocol(sasProtocol)
            .setSasIpRange(ipRange)
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType)
            .setBlobName(snapshotBlob.getBlobName())
            .setContainerName(snapshotBlob.getContainerName())
            .setSnapshotId(snapshotBlob.getSnapshotId())
            .generateSasQueryParameters(primaryCredential)
            .encode()

        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName, snapshotId).getBlockBlobClient()

        def os = new ByteArrayOutputStream()
        client.download(os)
        def properties = client.getProperties()

        then:
        os.toString() == new String(data)
        properties.getCacheControl() == "cache"
        properties.getContentDisposition() == "disposition"
        properties.getContentEncoding() == "encoding"
        properties.getContentLanguage() == "language"
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
        def sasWithId = new BlobServiceSasSignatureValues()
            .setIdentifier(identifier.getId())
            .setContainerName(cc.getBlobContainerName())
            .generateSasQueryParameters(primaryCredential)
            .encode()

        def client1 = getContainerClient(sasWithId, cc.getBlobContainerUrl())

        client1.listBlobs().iterator().hasNext()

        def sasWithPermissions = new BlobServiceSasSignatureValues()
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .setContainerName(cc.getBlobContainerName())
            .generateSasQueryParameters(primaryCredential)
            .encode()
        def client2 = getContainerClient(sasWithPermissions, cc.getBlobContainerUrl())

        client2.listBlobs().iterator().hasNext()

        then:
        notThrown(BlobStorageException)
    }

    def "serviceSASSignatureValues network test blob user delegation"() {
        setup:
        def data = "test".getBytes()
        def blobName = generateBlobName()
        def bu = cc.getBlobClient(blobName).getBlockBlobClient()
        bu.upload(new ByteArrayInputStream(data), data.length)

        def permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)

        def startTime = getUTCNow().minusDays(1)
        def expiryTime = getUTCNow().plusDays(1)

        def ipRange = new SasIpRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255")

        def sasProtocol = SasProtocol.HTTPS_HTTP
        def cacheControl = "cache"
        def contentDisposition = "disposition"
        def contentEncoding = "encoding"
        def contentLanguage = "language"
        def contentType = "type"

        def key = getOAuthServiceClient().getUserDelegationKey(null, expiryTime)

        def keyOid = getConfigValue(key.getSignedObjectId())
        key.setSignedObjectId(keyOid)

        def keyTid = getConfigValue(key.getSignedTenantId())
        key.setSignedTenantId(keyTid)
        when:
        def sas = new BlobServiceSasSignatureValues()
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .setStartTime(startTime)
            .setProtocol(sasProtocol)
            .setSasIpRange(ipRange)
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType)
            .setContainerName(bu.getContainerName())
            .setBlobName(bu.getBlobName())
            .setSnapshotId(bu.getSnapshotId())
            .setVersion(key.getSignedVersion())
            .generateSasQueryParameters(key, primaryCredential.getAccountName())
            .encode()

        then:
        sas != null

        when:
        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()

        def os = new ByteArrayOutputStream()
        client.download(os)
        def properties = client.getProperties()

        then:
        os.toString() == new String(data)
        properties.getCacheControl() == "cache"
        properties.getContentDisposition() == "disposition"
        properties.getContentEncoding() == "encoding"
        properties.getContentLanguage() == "language"
        notThrown(BlobStorageException)
    }

    def "BlobServiceSAS network test blob snapshot"() {
        setup:
        def containerName = generateContainerName()
        def blobName = generateBlobName()
        def containerClient = primaryBlobServiceClient.createBlobContainer(containerName)
        def blobClient = containerClient.getBlobClient(blobName).getBlockBlobClient()
        blobClient.upload(defaultInputStream.get(), defaultDataSize) // need something to snapshot
        def snapshotBlob = new SpecializedBlobClientBuilder().blobClient(blobClient.createSnapshot()).buildBlockBlobClient()
        def snapshotId = snapshotBlob.getSnapshotId()

        def permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
        def startTime = getUTCNow().minusDays(1)
        def expiryTime = getUTCNow().plusDays(1)
        def ipRange = new SasIpRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255")
        def sasProtocol = SasProtocol.HTTPS_HTTP
        def cacheControl = "cache"
        def contentDisposition = "disposition"
        def contentEncoding = "encoding"
        def contentLanguage = "language"
        def contentType = "type"

        when:
        def sas = new BlobServiceSasSignatureValues()
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .setStartTime(startTime)
            .setProtocol(sasProtocol)
            .setSasIpRange(ipRange)
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType)
            .setContainerName(snapshotBlob.getContainerName())
            .setBlobName(snapshotBlob.getBlobName())
            .setSnapshotId(snapshotBlob.getSnapshotId())
            .generateSasQueryParameters(primaryCredential)
            .encode()

        and:
        def client = getBlobClient(sas, containerClient.getBlobContainerUrl(), blobName).getAppendBlobClient()

        client.download(new ByteArrayOutputStream())

        then:
        thrown(BlobStorageException)

        when:
        def snapClient = getBlobClient(sas, containerClient.getBlobContainerUrl(), blobName, snapshotId).getAppendBlobClient()

        def data = new ByteArrayOutputStream()
        snapClient.download(data)

        then:
        notThrown(BlobStorageException)
        data.toByteArray() == defaultData.array()

        and:
        def properties = snapClient.getProperties()

        then:
        properties.getCacheControl() == "cache"
        properties.getContentDisposition() == "disposition"
        properties.getContentEncoding() == "encoding"
        properties.getContentLanguage() == "language"

    }

    def "serviceSASSignatureValues network test blob snapshot user delegation"() {
        setup:
        def data = "test".getBytes()
        def blobName = generateBlobName()
        def bu = cc.getBlobClient(blobName).getBlockBlobClient()
        bu.upload(new ByteArrayInputStream(data), data.length)
        def snapshotBlob = new SpecializedBlobClientBuilder().blobClient(bu.createSnapshot()).buildBlockBlobClient()
        def snapshotId = snapshotBlob.getSnapshotId()

        def permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)

        def startTime = getUTCNow().minusDays(1)
        def expiryTime = getUTCNow().plusDays(1)

        def ipRange = new SasIpRange()
            .setIpMin("0.0.0.0")
            .setIpMax("255.255.255.255")

        def sasProtocol = SasProtocol.HTTPS_HTTP
        def cacheControl = "cache"
        def contentDisposition = "disposition"
        def contentEncoding = "encoding"
        def contentLanguage = "language"
        def contentType = "type"

        def key = getOAuthServiceClient().getUserDelegationKey(startTime, expiryTime)

        def keyOid = getConfigValue(key.getSignedObjectId())
        key.setSignedObjectId(keyOid)

        def keyTid = getConfigValue(key.getSignedTenantId())
        key.setSignedTenantId(keyTid)
        when:
        def sas = new BlobServiceSasSignatureValues()
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .setStartTime(startTime)
            .setProtocol(sasProtocol)
            .setSasIpRange(ipRange)
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType)
            .setContainerName(snapshotBlob.getContainerName())
            .setBlobName(snapshotBlob.getBlobName())
            .setSnapshotId(snapshotBlob.getSnapshotId())
            .setVersion(key.getSignedVersion())
            .generateSasQueryParameters(key, primaryCredential.getAccountName())
            .encode()

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

        then:
        notThrown(BlobStorageException)
        os.toString() == new String(data)

        and:
        def properties = client2.getProperties()

        then:
        properties.getCacheControl() == "cache"
        properties.getContentDisposition() == "disposition"
        properties.getContentEncoding() == "encoding"
        properties.getContentLanguage() == "language"
    }

    def "serviceSASSignatureValues network test container user delegation"() {
        setup:
        def permissions = new BlobContainerSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setListPermission(true)

        def expiryTime = getUTCNow().plusDays(1)

        def key = getOAuthServiceClient().getUserDelegationKey(null, expiryTime)

        def keyOid = getConfigValue(key.getSignedObjectId())
        key.setSignedObjectId(keyOid)

        def keyTid = getConfigValue(key.getSignedTenantId())
        key.setSignedTenantId(keyTid)
        when:
        def sasWithPermissions = new BlobServiceSasSignatureValues()
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .setContainerName(cc.getBlobContainerName())
            .generateSasQueryParameters(key, primaryCredential.getAccountName())
            .encode()

        def client = getContainerClient(sasWithPermissions, cc.getBlobContainerUrl())
        client.listBlobs().iterator().hasNext()

        then:
        notThrown(BlobStorageException)
    }

    def "accountSAS network test blob read"() {
        setup:
        def data = "test".getBytes()
        def blobName = generateBlobName()
        def bu = cc.getBlobClient(blobName).getBlockBlobClient()
        bu.upload(new ByteArrayInputStream(data), data.length)

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
        def sas = new AccountSasSignatureValues()
            .setServices(service.toString())
            .setResourceTypes(resourceType.toString())
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .generateSasQueryParameters(primaryCredential)
            .encode()
        def client = getBlobClient(sas, cc.getBlobContainerUrl(), blobName).getBlockBlobClient()
        def os = new ByteArrayOutputStream()
        client.download(os)

        then:
        os.toString() == new String(data)
    }

    def "accountSAS network test blob delete fails"() {
        setup:
        def data = "test".getBytes()
        def blobName = generateBlobName()
        def bu = cc.getBlobClient(blobName).getBlockBlobClient()
        bu.upload(new ByteArrayInputStream(data), data.length)

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
        def sas = new AccountSasSignatureValues()
            .setServices(service.toString())
            .setResourceTypes(resourceType.toString())
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .generateSasQueryParameters(primaryCredential)
            .encode()
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
        def sas = new AccountSasSignatureValues()
            .setServices(service.toString())
            .setResourceTypes(resourceType.toString())
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .generateSasQueryParameters(primaryCredential)
            .encode()
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
        def sas = new AccountSasSignatureValues()
            .setServices(service.toString())
            .setResourceTypes(resourceType.toString())
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .generateSasQueryParameters(primaryCredential)
            .encode()
        def sc = getServiceClient(sas, primaryBlobServiceClient.getAccountUrl())
        sc.createBlobContainer(generateContainerName())

        then:
        notThrown(BlobStorageException)
    }

    def "accountSAS network account sas token on endpoint"() {
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

        def sas = new AccountSasSignatureValues()
            .setServices(service.toString())
            .setResourceTypes(resourceType.toString())
            .setPermissions(permissions)
            .setExpiryTime(expiryTime)
            .generateSasQueryParameters(primaryCredential)
            .encode()
        def containerName = generateContainerName()
        def blobName = generateBlobName()

        when:
        def sc = getServiceClientBuilder(null, primaryBlobServiceClient.getAccountUrl() + "?" + sas, null).buildClient()
        sc.createBlobContainer(containerName)

        def cc = getContainerClientBuilder(primaryBlobServiceClient.getAccountUrl() + "/" + containerName + "?" + sas).buildClient()
        cc.getProperties()

        def bc = getBlobClient(primaryCredential, primaryBlobServiceClient.getAccountUrl() + "/" + containerName + "/" + blobName + "?" + sas)

        def file = getRandomFile(256)
        bc.uploadFromFile(file.toPath().toString())

        then:
        notThrown(BlobStorageException)
    }

    /*
     This test will ensure that each field gets placed into the proper location within the string to sign and that null
     values are handled correctly. We will validate the whole SAS with service calls as well as correct serialization of
     individual parts later.
     */

    @Unroll
    def "serviceSasSignatures string to sign"() {
        when:
        def v = new BlobServiceSasSignatureValues()
        def p = new BlobSasPermission()
        def expected = String.format(expectedStringToSign, primaryCredential.getAccountName())

        p.setReadPermission(true)
        v.setPermissions(p)

        v.setStartTime(startTime)
        def e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        v.setExpiryTime(e)

        v.setContainerName("containerName")
            .setBlobName("blobName")
            .setSnapshotId(snapId)
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

        def token = v.generateSasQueryParameters(primaryCredential)
        then:
        token.getSignature() == primaryCredential.computeHmac256(expected)

        /*
        We don't test the blob or containerName properties because canonicalized resource is always added as at least
        /blob/accountName. We test canonicalization of resources later. Again, this is not to test a fully functional
        sas but the construction of the string to sign.
        Signed resource is tested elsewhere, as we work some minor magic in choosing which value to use.
         */
        where:
        startTime                                                 | identifier | ipRange          | protocol               | snapId   | cacheControl | disposition   | encoding   | language   | type   || expectedStringToSign
        OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null       | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | "id"       | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\nid\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null       | new SasIpRange() | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\nip\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null       | null             | SasProtocol.HTTPS_ONLY | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n" + SasProtocol.HTTPS_ONLY + "\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null       | null             | null                   | "snapId" | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nbs\nsnapId\n\n\n\n\n"
        null                                                      | null       | null             | null                   | null     | "control"    | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\ncontrol\n\n\n\n"
        null                                                      | null       | null             | null                   | null     | null         | "disposition" | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\ndisposition\n\n\n"
        null                                                      | null       | null             | null                   | null     | null         | null          | "encoding" | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\nencoding\n\n"
        null                                                      | null       | null             | null                   | null     | null         | null          | null       | "language" | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\nlanguage\n"
        null                                                      | null       | null             | null                   | null     | null         | null          | null       | null       | "type" || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\ntype"
    }

    @Unroll
    def "serviceSasSignatures string to sign user delegation key"() {
        when:
        def v = new BlobServiceSasSignatureValues()
        def expected = String.format(expectedStringToSign, primaryCredential.getAccountName())

        def p = new BlobSasPermission()
        p.setReadPermission(true)
        v.setPermissions(p)

        v.setStartTime(startTime)
        def e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        v.setExpiryTime(e)

        v.setContainerName("containerName")
            .setBlobName("blobName")
            .setSnapshotId(snapId)
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
        def key = new UserDelegationKey()
            .setSignedObjectId(keyOid)
            .setSignedTenantId(keyTid)
            .setSignedStart(keyStart)
            .setSignedExpiry(keyExpiry)
            .setSignedService(keyService)
            .setSignedVersion(keyVersion)
            .setValue(keyValue)
        def token = v.generateSasQueryParameters(key, primaryCredential.getAccountName())

        then:
        token.getSignature() == StorageImplUtils.computeHMac256(key.getValue(), expected)

        /*
        We test string to sign functionality directly related to user delegation sas specific parameters
         */
        where:
        startTime                                                 | keyOid                                 | keyTid                                 | keyStart                                                              | keyExpiry                                                             | keyService | keyVersion   | keyValue                                       | ipRange          | protocol               | snapId   | cacheControl | disposition   | encoding   | language   | type   || expectedStringToSign
        OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | "11111111-1111-1111-1111-111111111111" | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n11111111-1111-1111-1111-111111111111\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | "22222222-2222-2222-2222-222222222222" | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n22222222-2222-2222-2222-222222222222\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC) | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC) | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | "b"        | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\nb\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | "2018-06-17" | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n2018-06-17\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | new SasIpRange() | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\nip\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | SasProtocol.HTTPS_ONLY | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n" + SasProtocol.HTTPS_ONLY + "\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | "snapId" | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nbs\nsnapId\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | "control"    | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\ncontrol\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | "disposition" | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\ndisposition\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | "encoding" | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\nencoding\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | "language" | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\nlanguage\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | "type" || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\ntype"
    }

    def "serviceSasSignatureValues IA"() {
        setup:
        def v = new BlobServiceSasSignatureValues()
            .setSnapshotId("2018-01-01T00:00:00.0000000Z")

        when:
        v.generateSasQueryParameters(null)

        then:
        def e = thrown(NullPointerException)
        e.getMessage().contains("storageSharedKeyCredential")
    }

    @Unroll
    def "BlobSASPermissions toString"() {
        setup:
        def perms = new BlobSasPermission()
            .setReadPermission(read)
            .setWritePermission(write)
            .setDeletePermission(delete)
            .setCreatePermission(create)
            .setAddPermission(add)

        expect:
        perms.toString() == expectedString

        where:
        read  | write | delete | create | add   || expectedString
        true  | false | false  | false  | false || "r"
        false | true  | false  | false  | false || "w"
        false | false | true   | false  | false || "d"
        false | false | false  | true   | false || "c"
        false | false | false  | false  | true  || "a"
        true  | true  | true   | true   | true  || "racwd"
    }

    @Unroll
    def "BlobSASPermissions parse"() {
        when:
        def perms = BlobSasPermission.parse(permString)

        then:
        perms.hasReadPermission() == read
        perms.hasWritePermission() == write
        perms.hasDeletePermission() == delete
        perms.hasCreatePermission() == create
        perms.hasAddPermission() == add

        where:
        permString || read  | write | delete | create | add
        "r"        || true  | false | false  | false  | false
        "w"        || false | true  | false  | false  | false
        "d"        || false | false | true   | false  | false
        "c"        || false | false | false  | true   | false
        "a"        || false | false | false  | false  | true
        "racwd"    || true  | true  | true   | true   | true
        "dcwra"    || true  | true  | true   | true   | true
    }

    def "BlobSASPermissions parse IA"() {
        when:
        BlobSasPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "ContainerSASPermissions toString"() {
        setup:
        def perms = new BlobContainerSasPermission()
            .setReadPermission(read)
            .setWritePermission(write)
            .setDeletePermission(delete)
            .setCreatePermission(create)
            .setAddPermission(add)
            .setListPermission(list)

        expect:
        perms.toString() == expectedString

        where:
        read  | write | delete | create | add   | list  || expectedString
        true  | false | false  | false  | false | false || "r"
        false | true  | false  | false  | false | false || "w"
        false | false | true   | false  | false | false || "d"
        false | false | false  | true   | false | false || "c"
        false | false | false  | false  | true  | false || "a"
        false | false | false  | false  | false | true  || "l"
        true  | true  | true   | true   | true  | true  || "racwdl"
    }

    @Unroll
    def "ContainerSASPermissions parse"() {
        when:
        def perms = BlobContainerSasPermission.parse(permString)

        then:
        perms.hasReadPermission() == read
        perms.hasWritePermission() == write
        perms.hasDeletePermission() == delete
        perms.hasCreatePermission() == create
        perms.hasAddPermission() == add
        perms.hasListPermission() == list

        where:
        permString || read  | write | delete | create | add   | list
        "r"        || true  | false | false  | false  | false | false
        "w"        || false | true  | false  | false  | false | false
        "d"        || false | false | true   | false  | false | false
        "c"        || false | false | false  | true   | false | false
        "a"        || false | false | false  | false  | true  | false
        "l"        || false | false | false  | false  | false | true
        "racwdl"   || true  | true  | true   | true   | true  | true
        "dcwrla"   || true  | true  | true   | true   | true  | true
    }

    def "ContainerSASPermissions parse IA"() {
        when:
        BlobContainerSasPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "IPRange toString"() {
        setup:
        def ip = new SasIpRange()
            .setIpMin(min)
            .setIpMax(max)

        expect:
        ip.toString() == expectedString

        where:
        min  | max  || expectedString
        "a"  | "b"  || "a-b"
        "a"  | null || "a"
        null | "b"  || ""
    }

    @Unroll
    def "IPRange parse"() {
        when:
        def ip = SasIpRange.parse(rangeStr)

        then:
        ip.getIpMin() == min
        ip.getIpMax() == max

        where:
        rangeStr || min | max
        "a-b"    || "a" | "b"
        "a"      || "a" | null
        ""       || ""  | null
    }

    @Unroll
    def "SASProtocol parse"() {
        expect:
        SasProtocol.parse(protocolStr) == protocol

        where:
        protocolStr  || protocol
        "https"      || SasProtocol.HTTPS_ONLY
        "https,http" || SasProtocol.HTTPS_HTTP
    }

    @Unroll
    def "ServiceSASSignatureValues assertGenerateOk"() {
        when:
        def serviceSASSignatureValues = new BlobServiceSasSignatureValues()
            .setVersion(BlobServiceVersion.getLatest().getVersion())
            .setContainerName("containerName")
            .setBlobName("blobName")
            .setSnapshotId(snapshotId)
            .setPermissions(BlobContainerSasPermission.parse("rl"))

        serviceSASSignatureValues.generateSasQueryParameters(new StorageSharedKeyCredential("n", "key"))

        then:

        thrown(IllegalArgumentException)

        where:
        snapshotId | _
        null       | _
        "id"       | _
    }

    /*
     This test will ensure that each field gets placed into the proper location within the string to sign and that null
     values are handled correctly. We will validate the whole SAS with service calls as well as correct serialization of
     individual parts later.
     */

    @Unroll
    def "accountSasSignatures string to sign"() {
        when:
        def v = new AccountSasSignatureValues()
        def p = new AccountSasPermission()
            .setReadPermission(true)
        v.setPermissions(p)
            .setServices("b")
            .setResourceTypes("o")
            .setStartTime(startTime)
            .setExpiryTime(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
        if (ipRange != null) {
            def ipR = new SasIpRange()
            ipR.setIpMin("ip")
            v.setSasIpRange(ipR)
        }
        v.setProtocol(protocol)

        def token = v.generateSasQueryParameters(primaryCredential)

        then:
        token.getSignature() == primaryCredential.computeHmac256(String.format(expectedStringToSign, primaryCredential.getAccountName()))

        where:
        startTime                                                 | ipRange          | protocol               || expectedStringToSign
        OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null             | null                   || "%s" + "\nr\nb\no\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n"
        null                                                      | new SasIpRange() | null                   || "%s" + "\nr\nb\no\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\nip\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n"
        null                                                      | null             | SasProtocol.HTTPS_ONLY || "%s" + "\nr\nb\no\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n\n" + SasProtocol.HTTPS_ONLY + "\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\n"
    }

    @Unroll
    def "accountSasSignatureValues IA"() {
        setup:
        def v = new AccountSasSignatureValues()
            .setPermissions(AccountSasPermission.parse(permissions))
            .setServices(service)
            .setResourceTypes(resourceType)
            .setExpiryTime(expiryTime)
            .setVersion(version)

        when:
        v.generateSasQueryParameters(creds)

        then:
        def e = thrown(NullPointerException)
        e.getMessage().contains(parameter)

        where:
        permissions | service | resourceType | expiryTime                                                | version | creds             || parameter
        "c"         | null    | "c"          | OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | "v"     | primaryCredential || "services"
        "c"         | "b"     | null         | OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | "v"     | primaryCredential || "resourceTypes"
        "c"         | "b"     | "c"          | null                                                      | "v"     | primaryCredential || "expiryTime"
        "c"         | "b"     | "c"          | OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | "v"     | null              || "storageSharedKeyCredentials"
    }

    def "accountSasSignatureValues null"() {
        when:
        setup:
        def v = new AccountSasSignatureValues()

        when:
        v.setPermissions(null)


        then:
        def e = thrown(NullPointerException)
        e.getMessage().contains("permissions")
    }

    def "accountSasSignatureValues null permission"() {
        when:
        AccountSasPermission.parse(null)

        then:
        thrown(NullPointerException)
    }

    @Unroll
    def "AccountSASPermissions toString"() {
        setup:
        def perms = new AccountSasPermission()
        perms.setReadPermission(read)
            .setWritePermission(write)
            .setDeletePermission(delete)
            .setListPermission(list)
            .setAddPermission(add)
            .setCreatePermission(create)
            .setUpdatePermission(update)
            .setProcessMessages(process)

        expect:
        perms.toString() == expectedString

        where:
        read  | write | delete | list  | add   | create | update | process || expectedString
        true  | false | false  | false | false | false  | false  | false   || "r"
        false | true  | false  | false | false | false  | false  | false   || "w"
        false | false | true   | false | false | false  | false  | false   || "d"
        false | false | false  | true  | false | false  | false  | false   || "l"
        false | false | false  | false | true  | false  | false  | false   || "a"
        false | false | false  | false | false | true   | false  | false   || "c"
        false | false | false  | false | false | false  | true   | false   || "u"
        false | false | false  | false | false | false  | false  | true    || "p"
        true  | true  | true   | true  | true  | true   | true   | true    || "rwdlacup"
    }

    @Unroll
    def "AccountSASPermissions parse"() {
        when:
        def perms = AccountSasPermission.parse(permString)

        then:
        perms.hasReadPermission() == read
        perms.hasWritePermission() == write
        perms.hasDeletePermission() == delete
        perms.hasListPermission() == list
        perms.hasAddPermission() == add
        perms.hasCreatePermission() == create
        perms.hasUpdatePermission() == update
        perms.hasProcessMessages() == process

        where:
        permString || read  | write | delete | list  | add   | create | update | process
        "r"        || true  | false | false  | false | false | false  | false  | false
        "w"        || false | true  | false  | false | false | false  | false  | false
        "d"        || false | false | true   | false | false | false  | false  | false
        "l"        || false | false | false  | true  | false | false  | false  | false
        "a"        || false | false | false  | false | true  | false  | false  | false
        "c"        || false | false | false  | false | false | true   | false  | false
        "u"        || false | false | false  | false | false | false  | true   | false
        "p"        || false | false | false  | false | false | false  | false  | true
        "rwdlacup" || true  | true  | true   | true  | true  | true   | true   | true
        "lwrupcad" || true  | true  | true   | true  | true  | true   | true   | true
    }

    def "AccountSASPermissions parse IA"() {
        when:
        AccountSasPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "AccountSASResourceType toString"() {
        setup:
        def resourceTypes = new AccountSasResourceType()
            .setService(service)
            .setContainer(container)
            .setObject(object)

        expect:
        resourceTypes.toString() == expectedString

        where:
        service | container | object || expectedString
        true    | false     | false  || "s"
        false   | true      | false  || "c"
        false   | false     | true   || "o"
        true    | true      | true   || "sco"
    }

    @Unroll
    def "AccountSASResourceType parse"() {
        when:
        def resourceTypes = AccountSasResourceType.parse(resourceTypeString)

        then:
        resourceTypes.isService() == service
        resourceTypes.isContainer() == container
        resourceTypes.isObject() == object

        where:
        resourceTypeString || service | container | object
        "s"                || true    | false     | false
        "c"                || false   | true      | false
        "o"                || false   | false     | true
        "sco"              || true    | true      | true
    }

    @Unroll
    def "AccountSASResourceType IA"() {
        when:
        AccountSasResourceType.parse("scq")

        then:
        thrown(IllegalArgumentException)
    }

    // TODO : Figure out how to properly port this test over since I changed it to a common sas params
    @Ignore
    def "BlobURLParts"() {
        setup:
        def parts = new BlobUrlParts()
        parts.setScheme("http")
            .setHost("host")
            .setContainerName("container")
            .setBlobName("blob")
            .setSnapshot("snapshot")
        def sasValues = new BlobServiceSasSignatureValues()
            .setPermissions(new BlobSasPermission().setReadPermission(true))
            .setContainerName("containerName")
            .setBlobName("blobName")
            .setExpiryTime(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
        parts.setSasQueryParameters(sasValues.generateSasQueryParameters(primaryCredential))

        when:
        def splitParts = parts.toUrl().toString().split("\\?")

        then:
        splitParts.size() == 2 // Ensure that there is only one question mark even when sas and snapshot are present
        splitParts[0] == "http://host/container/blob"
        splitParts[1].contains("snapshot=snapshot")
        splitParts[1].contains("sp=r")
        splitParts[1].contains("sig=")
        splitParts[1].split("&").size() == 6 // snapshot & sv & sr & sp & sig
    }

    def "URLParser"() {
        when:
        def parts = BlobUrlParts.parse(new URL("http://host/container/" + originalBlobName + "?snapshot=snapshot&sv=" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "&sr=c&sp=r&sig=Ee%2BSodSXamKSzivSdRTqYGh7AeMVEk3wEoRZ1yzkpSc%3D"))

        then:
        parts.getScheme() == "http"
        parts.getHost() == "host"
        parts.getBlobContainerName() == "container"
        parts.getBlobName() == finalBlobName
        parts.getSnapshot() == "snapshot"
        parts.getSasQueryParameters().getPermissions() == "r"
        parts.getSasQueryParameters().getVersion() == Constants.HeaderConstants.TARGET_STORAGE_VERSION
        parts.getSasQueryParameters().getResource() == "c"
        parts.getSasQueryParameters().getSignature() == Utility.urlDecode("Ee%2BSodSXamKSzivSdRTqYGh7AeMVEk3wEoRZ1yzkpSc%3D")

        where:
        originalBlobName       | finalBlobName
        "blob"                 | "blob"
        "path/to]a blob"       | "path/to]a blob"
        "path%2Fto%5Da%20blob" | "path/to]a blob"
        "斑點"                 | "斑點"
        "%E6%96%91%E9%BB%9E"   | "斑點"
    }
}
