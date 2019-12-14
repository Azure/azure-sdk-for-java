// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake

import com.azure.storage.blob.implementation.util.BlobSasImplUtil
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.implementation.StorageImplUtils
import com.azure.storage.common.sas.*
import com.azure.storage.file.datalake.implementation.models.StorageErrorException
import com.azure.storage.file.datalake.models.DataLakeAccessPolicy
import com.azure.storage.file.datalake.models.DataLakeSignedIdentifier
import com.azure.storage.file.datalake.models.FileRange
import com.azure.storage.file.datalake.models.PathProperties
import com.azure.storage.file.datalake.models.UserDelegationKey
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues
import com.azure.storage.file.datalake.sas.FileSystemSasPermission
import com.azure.storage.file.datalake.sas.PathSasPermission
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

class SASTest extends APISpec {

    DataLakeFileClient sasClient
    String pathName

    def setup() {
        pathName = generatePathName()
        sasClient = getFileClient(primaryCredential, fsc.getFileSystemUrl(), pathName)
        sasClient.create()
        sasClient.append(defaultInputStream.get(), 0, defaultDataSize)
        sasClient.flush(defaultDataSize)
    }

    @Unroll
    def "File range"() {
        expect:
        if (count == null) {
            assert new FileRange(offset).toHeaderValue() == result
        } else {
            assert new FileRange(offset, count).toHeaderValue() == result
        }


        where:
        offset | count || result
        0      | null  || null
        0      | 5     || "bytes=0-4"
        5      | 10    || "bytes=5-14"
    }

    @Unroll
    def "File range IA"() {
        when:
        new FileRange(offset, count)

        then:
        thrown(IllegalArgumentException)

        where:
        offset | count
        -1     | 5
        0      | -1
    }

    DataLakeServiceSasSignatureValues generateValues(PathSasPermission permission) {
        return new DataLakeServiceSasSignatureValues(getUTCNow().plusDays(1), permission)
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

    def validateSasProperties(PathProperties properties) {
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

    def "serviceSASSignatureValues network test file"() {
        setup:
        def permissions = new PathSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)

        def sasValues = generateValues(permissions)

        when:
        def sas = sasClient.generateSas(sasValues)

        def client = getFileClient(sas, fsc.getFileSystemUrl(), pathName)

        def os = new ByteArrayOutputStream()
        client.read(os)
        def properties = client.getProperties()

        then:
        os.toString() == new String(defaultData.array())
        validateSasProperties(properties)
        notThrown(BlobStorageException)
    }

    def "serviceSASSignatureValues network test file system"() {
        setup:
        def identifier = new DataLakeSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new DataLakeAccessPolicy().setPermissions("racwdl")
                .setExpiresOn(getUTCNow().plusDays(1)))
        fsc.setAccessPolicy(null, Arrays.asList(identifier))

        // Check containerSASPermissions
        def permissions = new FileSystemSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setListPermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setListPermission(true)

        def expiryTime = getUTCNow().plusDays(1)

        when:
        def sasValues = new DataLakeServiceSasSignatureValues(identifier.getId())
        def sasWithId = fsc.generateSas(sasValues)

        def client1 = getFileSystemClient(sasWithId, fsc.getFileSystemUrl())

        // Wait 30 seconds as it may take time for the access policy to take effect.
        sleepIfLive(30000)

        client1.listPaths().iterator().hasNext()

        sasValues = new DataLakeServiceSasSignatureValues(expiryTime, permissions)
        def sasWithPermissions = fsc.generateSas(sasValues)
        def client2 = getFileSystemClient(sasWithPermissions, fsc.getFileSystemUrl())

        client2.listPaths().iterator().hasNext()

        then:
        notThrown(BlobStorageException)
    }

    def "serviceSASSignatureValues network test file user delegation"() {
        setup:
        def permissions = new PathSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)

        def sasValues = generateValues(permissions)

        when:
        def sas = sasClient.generateUserDelegationSas(sasValues, getUserDelegationInfo())

        def client = getFileClient(sas, fsc.getFileSystemUrl(), pathName)

        def os = new ByteArrayOutputStream()
        client.read(os)
        def properties = client.getProperties()

        then:
        os.toString() == new String(defaultData.array())
        validateSasProperties(properties)
        notThrown(BlobStorageException)
    }

    def "serviceSASSignatureValues network test file system user delegation"() {
        setup:
        def permissions = new FileSystemSasPermission()
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
        def sasValues = new DataLakeServiceSasSignatureValues(expiryTime, permissions)
        def sasWithPermissions = fsc.generateUserDelegationSas(sasValues, key)

        def client = getFileSystemClient(sasWithPermissions, fsc.getFileSystemUrl())
        client.listPaths().iterator().hasNext()

        then:
        notThrown(BlobStorageException)
    }

    def "accountSAS network test file read"() {
        setup:
        def pathName = generatePathName()
        def fc = fsc.getFileClient(pathName)
        fc.create()
        fc.append(defaultInputStream.get(), 0, defaultDataSize)
        fc.flush(defaultDataSize)

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
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryDataLakeServiceClient.generateAccountSas(sasValues)
        def client = getFileClient(sas, fsc.getFileSystemUrl(), pathName).getBlockBlobClient()
        def os = new ByteArrayOutputStream()
        client.download(os)

        then:
        os.toString() == defaultText
    }

    def "accountSAS network test file delete fails"() {
        setup:
        def pathName = generatePathName()
        def fc = fsc.getFileClient(pathName)
        fc.create()

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
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryDataLakeServiceClient.generateAccountSas(sasValues)
        def client = getFileClient(sas, fsc.getFileSystemUrl(), pathName)
        client.delete()

        then:
        thrown(StorageErrorException)
    }

    def "accountSAS network create file system fails"() {
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
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryDataLakeServiceClient.generateAccountSas(sasValues)
        def sc = getServiceClient(sas, primaryDataLakeServiceClient.getAccountUrl())
        sc.createFileSystem(generateFileSystemName())

        then:
        thrown(BlobStorageException)
    }

    def "accountSAS network create file system succeeds"() {
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
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryDataLakeServiceClient.generateAccountSas(sasValues)
        def sc = getServiceClient(sas, primaryDataLakeServiceClient.getAccountUrl())
        sc.createFileSystem(generateFileSystemName())

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

        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryDataLakeServiceClient.generateAccountSas(sasValues)
        def fileSystemName = generateFileSystemName()
        def pathName = generatePathName()

        when:
        def sc = getServiceClientBuilder(null, primaryDataLakeServiceClient.getAccountUrl() + "?" + sas, null).buildClient()
        sc.createFileSystem(fileSystemName)

        def fsc = getFileSystemClientBuilder(primaryDataLakeServiceClient.getAccountUrl() + "/" + fileSystemName + "?" + sas).buildClient()
        fsc.listPaths()

        def fc = getFileClient(primaryCredential, primaryDataLakeServiceClient.getAccountUrl() + "/" + fileSystemName + "/" + pathName + "?" + sas)

        fc.create()

        then:
        notThrown(Exception)
    }

    /*
     This test will ensure that each field gets placed into the proper location within the string to sign and that null
     values are handled correctly. We will validate the whole SAS with service calls as well as correct serialization of
     individual parts later.
     */

    @Unroll
    def "sas impl util string to sign"() {
        when:
        def e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

        def p = new PathSasPermission()
        p.setReadPermission(true)

        def v
        if (identifier != null) {
            v = new DataLakeServiceSasSignatureValues(identifier)
        } else {
            v = new DataLakeServiceSasSignatureValues(e, p)
        }
        def expected = String.format(expectedStringToSign, primaryCredential.getAccountName())

        v.setPermissions(p)

        v.setStartTime(startTime)
        v.setExpiryTime(e)

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

        def util = new BlobSasImplUtil(Transforms.toBlobSasValues(v), "fileSystemName", "pathName", null)
        util.ensureState()
        def sasToken = util.stringToSign(util.getCanonicalName(primaryCredential.getAccountName()))

        then:
        sasToken == expected

        /*
        We don't test the blob or containerName properties because canonicalized resource is always added as at least
        /blob/accountName. We test canonicalization of resources later. Again, this is not to test a fully functional
        sas but the construction of the string to sign.
        Signed resource is tested elsewhere, as we work some minor magic in choosing which value to use.
         */
        where:
        startTime                                                 | identifier | ipRange          | protocol               | cacheControl | disposition   | encoding   | language   | type   || expectedStringToSign
        OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null       | null             | null                   | null         | null          | null       | null       | null   || "r\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | "id"       | null             | null                   | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\nid\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null       | new SasIpRange() | null                   | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\nip\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null       | null             | SasProtocol.HTTPS_ONLY | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n" + SasProtocol.HTTPS_ONLY + "\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null       | null             | null                   | "control"    | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\ncontrol\n\n\n\n"
        null                                                      | null       | null             | null                   | null         | "disposition" | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\ndisposition\n\n\n"
        null                                                      | null       | null             | null                   | null         | null          | "encoding" | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\nencoding\n\n"
        null                                                      | null       | null             | null                   | null         | null          | null       | "language" | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\nlanguage\n"
        null                                                      | null       | null             | null                   | null         | null          | null       | null       | "type" || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\ntype"
    }

    @Unroll
    def "sas impl util string to sign user delegation key"() {
        when:
        def e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

        def p = new PathSasPermission()
        p.setReadPermission(true)

        def v = new DataLakeServiceSasSignatureValues(e, p)
        def expected = String.format(expectedStringToSign, primaryCredential.getAccountName())

        p.setReadPermission(true)
        v.setPermissions(p)

        v.setStartTime(startTime)
        v.setExpiryTime(e)

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

        def util = new BlobSasImplUtil(Transforms.toBlobSasValues(v), "fileSystemName", "pathName", null)
        util.ensureState()
        def sasToken = util.stringToSign(Transforms.toBlobUserDelegationKey(key), util.getCanonicalName(primaryCredential.getAccountName()))

        then:
        sasToken == expected

        /*
        We test string to sign functionality directly related to user delegation sas specific parameters
         */
        where:
        startTime                                                 | keyOid                                 | keyTid                                 | keyStart                                                              | keyExpiry                                                             | keyService | keyVersion   | keyValue                                       | ipRange          | protocol               | snapId   | cacheControl | disposition   | encoding   | language   | type   || expectedStringToSign
        OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | "11111111-1111-1111-1111-111111111111" | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n11111111-1111-1111-1111-111111111111\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | "22222222-2222-2222-2222-222222222222" | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n22222222-2222-2222-2222-222222222222\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC) | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC) | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | "b"        | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\nb\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | "2018-06-17" | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n2018-06-17\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | new SasIpRange() | null                   | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\nip\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | SasProtocol.HTTPS_ONLY | null     | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n" + SasProtocol.HTTPS_ONLY + "\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | "control"    | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\ncontrol\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | "disposition" | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\ndisposition\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | "encoding" | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\nencoding\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | "language" | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\nlanguage\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null     | null         | null          | null       | null       | "type" || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n" + Constants.HeaderConstants.TARGET_STORAGE_VERSION + "\nb\n\n\n\n\n\ntype"
    }

    @Unroll
    def "PathSasPermission toString"() {
        setup:
        def perms = new PathSasPermission()
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
    def "PathSasPermission parse"() {
        when:
        def perms = PathSasPermission.parse(permString)

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

    def "PathSasPermission parse IA"() {
        when:
        PathSasPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "FileSystemSasPermission toString"() {
        setup:
        def perms = new FileSystemSasPermission()
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
    def "FileSystemSasPermission parse"() {
        when:
        def perms = FileSystemSasPermission.parse(permString)

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

    def "FileSystemSasPermission parse IA"() {
        when:
        FileSystemSasPermission.parse("rwaq")

        then:
        thrown(IllegalArgumentException)
    }

}
