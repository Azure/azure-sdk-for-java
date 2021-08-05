// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake

import com.azure.core.credential.AzureSasCredential
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.sas.AccountSasPermission
import com.azure.storage.common.sas.AccountSasResourceType
import com.azure.storage.common.sas.AccountSasService
import com.azure.storage.common.sas.AccountSasSignatureValues
import com.azure.storage.common.sas.SasIpRange
import com.azure.storage.common.sas.SasProtocol
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion
import com.azure.storage.file.datalake.implementation.util.DataLakeSasImplUtil
import com.azure.storage.file.datalake.models.AccessControlType
import com.azure.storage.file.datalake.models.DataLakeAccessPolicy
import com.azure.storage.file.datalake.models.DataLakeSignedIdentifier
import com.azure.storage.file.datalake.models.DataLakeStorageException
import com.azure.storage.file.datalake.models.ListPathsOptions
import com.azure.storage.file.datalake.models.PathAccessControlEntry
import com.azure.storage.file.datalake.models.PathProperties
import com.azure.storage.file.datalake.models.RolePermissions
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
        sasClient = getFileClient(env.dataLakeAccount.credential, fsc.getFileSystemUrl(), pathName)
        sasClient.create()
        sasClient.append(data.defaultInputStream, 0, data.defaultDataSize)
        sasClient.flush(data.defaultDataSize)
    }

    DataLakeServiceSasSignatureValues generateValues(PathSasPermission permission) {
        return new DataLakeServiceSasSignatureValues(namer.getUtcNow().plusDays(1), permission)
            .setStartTime(namer.getUtcNow().minusDays(1))
            .setProtocol(SasProtocol.HTTPS_HTTP)
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
        def key = getOAuthServiceClient().getUserDelegationKey(namer.getUtcNow().minusDays(1), namer.getUtcNow().plusDays(1))
        def keyOid = namer.recordValueFromConfig(key.getSignedObjectId())
        key.setSignedObjectId(keyOid)
        def keyTid = namer.recordValueFromConfig(key.getSignedTenantId())
        key.setSignedTenantId(keyTid)
        return key
    }

    def "file sas permission"() {
        setup:
        def permissions = new PathSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setDeletePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
        if (Constants.SAS_SERVICE_VERSION >= DataLakeServiceVersion.V2019_12_12.version) {
            permissions.setMovePermission(true)
                .setExecutePermission(true)
                .setManageOwnershipPermission(true)
                .setManageAccessControlPermission(true)
        }

        def sasValues = generateValues(permissions)

        when:
        def sas = sasClient.generateSas(sasValues)

        def client = getFileClient(sas, fsc.getFileSystemUrl(), pathName)

        def os = new ByteArrayOutputStream()
        client.read(os)
        def properties = client.getProperties()

        then:
        os.toString() == new String(data.defaultBytes)
        validateSasProperties(properties)
        notThrown(DataLakeStorageException)
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "directory sas permission"() {
        setup:
        def pathName = generatePathName()
        DataLakeDirectoryClient sasClient = getDirectoryClient(env.dataLakeAccount.credential, fsc.getFileSystemUrl(), pathName)
        sasClient.create()
        def permissions = new PathSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setDeletePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
            .setMovePermission(true)
            .setExecutePermission(true)
            .setManageOwnershipPermission(true)
            .setManageAccessControlPermission(true)

        def sasValues = generateValues(permissions)

        when:
        def sas = sasClient.generateSas(sasValues)

        def client = getDirectoryClient(sas, fsc.getFileSystemUrl(), pathName)

        def properties = client.getProperties()

        then:
        notThrown(DataLakeStorageException)
        validateSasProperties(properties)

        when:
        client.createSubdirectory(generatePathName())

        then:
        notThrown(DataLakeStorageException)
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "directory sas permission fail"() {
        setup:
        def pathName = generatePathName()
        DataLakeDirectoryClient sasClient = getDirectoryClient(env.dataLakeAccount.credential, fsc.getFileSystemUrl(), pathName)
        sasClient.create()
        def permissions = new PathSasPermission() /* No read permission. */
            .setWritePermission(true)
            .setDeletePermission(true)
            .setCreatePermission(true)

        def sasValues = generateValues(permissions)

        when:
        def sas = sasClient.generateSas(sasValues)

        def client = getDirectoryClient(sas, fsc.getFileSystemUrl(), pathName)

        client.getProperties()

        then:
        thrown(DataLakeStorageException)
    }

    def "file system sas identifier"() {
        setup:
        def identifier = new DataLakeSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new DataLakeAccessPolicy().setPermissions("racwdl")
                .setExpiresOn(namer.getUtcNow().plusDays(1)))
        fsc.setAccessPolicy(null, Arrays.asList(identifier))

        // Check containerSASPermissions
        def permissions = new FileSystemSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setDeletePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
        if (Constants.SAS_SERVICE_VERSION >= DataLakeServiceVersion.V2020_02_10.version) {
            permissions.setMovePermission(true)
                .setExecutePermission(true)
                .setManageOwnershipPermission(true)
                .setManageAccessControlPermission(true)
        }

        def expiryTime = namer.getUtcNow().plusDays(1)

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
        notThrown(DataLakeStorageException)
    }

    def "file user delegation"() {
        setup:
        def permissions = new PathSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setDeletePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
        if (Constants.SAS_SERVICE_VERSION >= DataLakeServiceVersion.V2019_12_12.version) {
            permissions.setMovePermission(true)
                .setExecutePermission(true)
                .setManageOwnershipPermission(true)
                .setManageAccessControlPermission(true)
        }

        def sasValues = generateValues(permissions)

        when:
        def sas = sasClient.generateUserDelegationSas(sasValues, getUserDelegationInfo())

        def client = getFileClient(sas, fsc.getFileSystemUrl(), pathName)

        def os = new ByteArrayOutputStream()
        client.read(os)
        def properties = client.getProperties()

        then:
        os.toString() == new String(data.defaultBytes)
        validateSasProperties(properties)
        notThrown(DataLakeStorageException)
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "directory user delegation"() {
        setup:
        def pathName = generatePathName()
        DataLakeDirectoryClient sasClient = getDirectoryClient(env.dataLakeAccount.credential, fsc.getFileSystemUrl(), pathName)
        sasClient.create()
        def permissions = new PathSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setDeletePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
            .setMovePermission(true)
            .setExecutePermission(true)
            .setManageOwnershipPermission(true)
            .setManageAccessControlPermission(true)

        def sasValues = generateValues(permissions)

        when:
        def sas = sasClient.generateUserDelegationSas(sasValues, getUserDelegationInfo())

        def client = getDirectoryClient(sas, fsc.getFileSystemUrl(), pathName)

        def properties = client.getProperties()

        then:
        notThrown(DataLakeStorageException)
        validateSasProperties(properties)

        when:
        client.createSubdirectory(generatePathName())

        then:
        notThrown(DataLakeStorageException)

        when:
        fsc = getFileSystemClient(sas, fsc.getFileSystemUrl())
        def it = fsc.listPaths(new ListPathsOptions().setPath(pathName), null).iterator()

        then:
        it.next()
        !it.hasNext()
        notThrown(DataLakeStorageException)
    }

    def "file system user delegation"() {
        setup:
        def permissions = new FileSystemSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setDeletePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
        if (Constants.SAS_SERVICE_VERSION >= DataLakeServiceVersion.V2020_02_10.version) {
            permissions.setMovePermission(true)
                .setExecutePermission(true)
                .setManageOwnershipPermission(true)
                .setManageAccessControlPermission(true)
        }

        def expiryTime = namer.getUtcNow().plusDays(1)

        def key = getOAuthServiceClient().getUserDelegationKey(null, expiryTime)

        def keyOid = namer.recordValueFromConfig(key.getSignedObjectId())
        key.setSignedObjectId(keyOid)

        def keyTid = namer.recordValueFromConfig(key.getSignedTenantId())
        key.setSignedTenantId(keyTid)

        when:
        def sasValues = new DataLakeServiceSasSignatureValues(expiryTime, permissions)
        def sasWithPermissions = fsc.generateUserDelegationSas(sasValues, key)

        def client = getFileSystemClient(sasWithPermissions, fsc.getFileSystemUrl())
        client.listPaths().iterator().hasNext()

        then:
        notThrown(DataLakeStorageException)
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "file user delegation saoid"() {
        setup:
        def saoid = namer.getRandomUuid()
        def pathName = generatePathName()

        def permissions = new PathSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setDeletePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
            .setMovePermission(true)
            .setExecutePermission(true)
            .setManageOwnershipPermission(true)
            .setManageAccessControlPermission(true)
        def expiryTime = namer.getUtcNow().plusDays(1)

        def key = getOAuthServiceClient().getUserDelegationKey(null, expiryTime)
        def keyOid = namer.recordValueFromConfig(key.getSignedObjectId())
        key.setSignedObjectId(keyOid)
        def keyTid = namer.recordValueFromConfig(key.getSignedTenantId())
        key.setSignedTenantId(keyTid)

        when:
        /* Grant userOID on root folder. */
        def rootClient = getDirectoryClient(env.dataLakeAccount.credential, fsc.getFileSystemUrl(), "")
        ArrayList<PathAccessControlEntry> acl = new ArrayList<>();
        PathAccessControlEntry ace = new PathAccessControlEntry()
            .setAccessControlType(AccessControlType.USER)
            .setEntityId(saoid.toString())
            .setPermissions(RolePermissions.parseSymbolic("rwx", false))
        acl.add(ace)
        rootClient.setAccessControlList(acl, null, null)

        def sasValues = new DataLakeServiceSasSignatureValues(expiryTime, permissions)
            .setPreauthorizedAgentObjectId(saoid)
        def sasWithPermissions = rootClient.generateUserDelegationSas(sasValues, key)

        def client = getFileClient(sasWithPermissions, fsc.getFileSystemUrl(), pathName)

        client.create(true)
        client.append(data.defaultInputStream, 0, data.defaultDataSize)
        client.flush(data.defaultDataSize)

        then:
        notThrown(DataLakeStorageException)
        sasWithPermissions.contains("saoid=" + saoid)

        when:
        client = getFileClient(env.dataLakeAccount.credential, fsc.getFileSystemUrl(), pathName)
        def accessControl = client.getAccessControl()

        then:
        notThrown(DataLakeStorageException)
        accessControl.getOwner() == saoid
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "file user delegation suoid"() {
        setup:
        def suoid = namer.getRandomUuid()
        def pathName = generatePathName()

        def permissions = new PathSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setDeletePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
            .setMovePermission(true)
            .setExecutePermission(true)
            .setManageOwnershipPermission(true)
            .setManageAccessControlPermission(true)
        def expiryTime = namer.getUtcNow().plusDays(1)

        def key = getOAuthServiceClient().getUserDelegationKey(null, expiryTime)
        def keyOid = namer.recordValueFromConfig(key.getSignedObjectId())
        key.setSignedObjectId(keyOid)
        def keyTid = namer.recordValueFromConfig(key.getSignedTenantId())
        key.setSignedTenantId(keyTid)

        when: "User is not authorized yet."
        def sasValues = new DataLakeServiceSasSignatureValues(expiryTime, permissions)
            .setAgentObjectId(suoid)
        def sasWithPermissions = sasClient.generateUserDelegationSas(sasValues, key)

        def client = getFileClient(sasWithPermissions, fsc.getFileSystemUrl(), pathName)
        client.create(true)
        client.append(data.defaultInputStream, 0, data.defaultDataSize)
        client.flush(data.defaultDataSize)

        then:
        thrown(DataLakeStorageException)
        sasWithPermissions.contains("suoid=" + suoid)

        when: "User is now authorized."
        /* Grant userOID on root folder. */
        def rootClient = getDirectoryClient(env.dataLakeAccount.credential, fsc.getFileSystemUrl(), "")
        ArrayList<PathAccessControlEntry> acl = new ArrayList<>();
        PathAccessControlEntry ace = new PathAccessControlEntry()
            .setAccessControlType(AccessControlType.USER)
            .setEntityId(suoid.toString())
            .setPermissions(RolePermissions.parseSymbolic("rwx", false))
        acl.add(ace)
        rootClient.setAccessControlList(acl, null, null)

        sasValues = new DataLakeServiceSasSignatureValues(expiryTime, permissions)
            .setAgentObjectId(suoid)
        sasWithPermissions = rootClient.generateUserDelegationSas(sasValues, key)

        client = getFileClient(sasWithPermissions, fsc.getFileSystemUrl(), pathName)

        client.create(true)
        client.append(data.defaultInputStream, 0, data.defaultDataSize)
        client.flush(data.defaultDataSize)

        client = getFileClient(env.dataLakeAccount.credential, fsc.getFileSystemUrl(), pathName)

        then:
        notThrown(DataLakeStorageException)
        sasWithPermissions.contains("suoid=" + suoid)
        client.getAccessControl().getOwner() == suoid

        when: "Use random other suoid. User should not be authorized."
        sasValues = new DataLakeServiceSasSignatureValues(expiryTime, permissions)
            .setAgentObjectId(namer.getRandomUuid())
        sasWithPermissions = rootClient.generateUserDelegationSas(sasValues, key)

        client = getFileClient(sasWithPermissions, fsc.getFileSystemUrl(), pathName)

        client.getProperties()

        then:
        thrown(DataLakeStorageException)
    }

    def "file system user delegation correlation id"() {
        setup:
        def permissions = new FileSystemSasPermission()
            .setListPermission(true)

        def expiryTime = namer.getUtcNow().plusDays(1)

        def key = getOAuthServiceClient().getUserDelegationKey(null, expiryTime)

        def keyOid = namer.recordValueFromConfig(key.getSignedObjectId())
        key.setSignedObjectId(keyOid)

        def keyTid = namer.recordValueFromConfig(key.getSignedTenantId())
        key.setSignedTenantId(keyTid)

        def cid = namer.getRandomUuid()

        when:
        def sasValues = new DataLakeServiceSasSignatureValues(expiryTime, permissions)
            .setCorrelationId(cid)
        def sasWithPermissions = fsc.generateUserDelegationSas(sasValues, key)

        def client = getFileSystemClient(sasWithPermissions, fsc.getFileSystemUrl())
        client.listPaths().iterator().hasNext()

        then:
        sasWithPermissions.contains("scid=" + cid)
        notThrown(DataLakeStorageException)
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    def "file system user delegation correlation id error"() {
        setup:
        def permissions = new FileSystemSasPermission()
            .setListPermission(true)

        def expiryTime = namer.getUtcNow().plusDays(1)

        def key = getOAuthServiceClient().getUserDelegationKey(null, expiryTime)

        def keyOid = namer.recordValueFromConfig(key.getSignedObjectId())
        key.setSignedObjectId(keyOid)

        def keyTid = namer.recordValueFromConfig(key.getSignedTenantId())
        key.setSignedTenantId(keyTid)

        def cid = "invalidcid"

        when:
        def sasValues = new DataLakeServiceSasSignatureValues(expiryTime, permissions)
            .setCorrelationId(cid)
        def sasWithPermissions = fsc.generateUserDelegationSas(sasValues, key)

        def client = getFileSystemClient(sasWithPermissions, fsc.getFileSystemUrl())
        client.listPaths().iterator().hasNext()

        then:
        sasWithPermissions.contains("scid=" + cid)
        thrown(DataLakeStorageException)
    }

    def "account sas file read"() {
        setup:
        def pathName = generatePathName()
        def fc = fsc.getFileClient(pathName)
        fc.create()
        fc.append(data.defaultInputStream, 0, data.defaultDataSize)
        fc.flush(data.defaultDataSize)

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
        def sas = primaryDataLakeServiceClient.generateAccountSas(sasValues)
        def client = getFileClient(sas, fsc.getFileSystemUrl(), pathName).getBlockBlobClient()
        def os = new ByteArrayOutputStream()
        client.download(os)

        then:
        os.toString() == data.defaultText
    }

    def "account sas file delete error"() {
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
        def expiryTime = namer.getUtcNow().plusDays(1)

        when:
        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
        def sas = primaryDataLakeServiceClient.generateAccountSas(sasValues)
        def client = getFileClient(sas, fsc.getFileSystemUrl(), pathName)
        client.delete()

        then:
        thrown(DataLakeStorageException)
    }

    def "account sas create file system error"() {
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
        def sas = primaryDataLakeServiceClient.generateAccountSas(sasValues)
        def sc = getServiceClient(sas, primaryDataLakeServiceClient.getAccountUrl())
        sc.createFileSystem(generateFileSystemName())

        then:
        thrown(DataLakeStorageException)
    }

    def "account sas create file system"() {
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
        def sas = primaryDataLakeServiceClient.generateAccountSas(sasValues)
        def sc = getServiceClient(sas, primaryDataLakeServiceClient.getAccountUrl())
        sc.createFileSystem(generateFileSystemName())

        then:
        notThrown(DataLakeStorageException)
    }

    def "account sas token on endpoint"() {
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
        def sas = primaryDataLakeServiceClient.generateAccountSas(sasValues)
        def fileSystemName = generateFileSystemName()
        def pathName = generatePathName()

        when:
        def sc = getServiceClientBuilder(null, primaryDataLakeServiceClient.getAccountUrl() + "?" + sas, null).buildClient()
        sc.createFileSystem(fileSystemName)

        def fsc = getFileSystemClientBuilder(primaryDataLakeServiceClient.getAccountUrl() + "/" + fileSystemName + "?" + sas).buildClient()
        fsc.listPaths()

        def fc = getFileClient(env.dataLakeAccount.credential, primaryDataLakeServiceClient.getAccountUrl() + "/" + fileSystemName + "/" + pathName + "?" + sas)

        fc.create()

        then:
        notThrown(DataLakeStorageException)
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
        def expected = String.format(expectedStringToSign, env.dataLakeAccount.name)

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

        def util = new DataLakeSasImplUtil(v, "fileSystemName", "pathName", false)
        util.ensureState()
        def sasToken = util.stringToSign(util.getCanonicalName(env.dataLakeAccount.name))

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
        OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null       | null             | null                   | null         | null          | null       | null       | null   || "r\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | "id"       | null             | null                   | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\nid\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null       | new SasIpRange() | null                   | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\nip\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null       | null             | SasProtocol.HTTPS_ONLY | null         | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n" + SasProtocol.HTTPS_ONLY + "\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null       | null             | null                   | "control"    | null          | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\ncontrol\n\n\n\n"
        null                                                      | null       | null             | null                   | null         | "disposition" | null       | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\ndisposition\n\n\n"
        null                                                      | null       | null             | null                   | null         | null          | "encoding" | null       | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\nencoding\n\n"
        null                                                      | null       | null             | null                   | null         | null          | null       | "language" | null   || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\nlanguage\n"
        null                                                      | null       | null             | null                   | null         | null          | null       | null       | "type" || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\ntype"
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_02_10")
    @Unroll
    def "sas impl util string to sign user delegation key"() {
        when:
        def e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

        def p = new PathSasPermission()
        p.setReadPermission(true)

        def v = new DataLakeServiceSasSignatureValues(e, p)
        def expected = String.format(expectedStringToSign, env.dataLakeAccount.name)

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

        v.setCorrelationId(cid)
            .setPreauthorizedAgentObjectId(saoid)
            .setAgentObjectId(suoid)

        def util = new DataLakeSasImplUtil(v, "fileSystemName", "pathName", false)
        util.ensureState()
        def sasToken = util.stringToSign(key, util.getCanonicalName(env.dataLakeAccount.name))

        then:
        sasToken == expected

        /*
        We test string to sign functionality directly related to user delegation sas specific parameters
         */
        where:
        startTime                                                 | keyOid                                 | keyTid                                 | keyStart                                                              | keyExpiry                                                             | keyService | keyVersion   | keyValue                                       | ipRange          | protocol               | cacheControl | disposition   | encoding   | language   | type   | saoid   | suoid   | cid   || expectedStringToSign
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null         | null          | null       | null       | null   | null    | null    | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC) | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null         | null          | null       | null       | null   | null    | null    | null  || "r\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | "11111111-1111-1111-1111-111111111111" | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null         | null          | null       | null       | null   | null    | null    | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n11111111-1111-1111-1111-111111111111\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | "22222222-2222-2222-2222-222222222222" | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null         | null          | null       | null       | null   | null    | null    | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n22222222-2222-2222-2222-222222222222\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC) | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null         | null          | null       | null       | null   | null    | null    | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC) | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null         | null          | null       | null       | null   | null    | null    | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | "b"        | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null         | null          | null       | null       | null   | null    | null    | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\nb\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | "2018-06-17" | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null         | null          | null       | null       | null   | null    | null    | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n2018-06-17\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | new SasIpRange() | null                   | null         | null          | null       | null       | null   | null    | null    | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\nip\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | SasProtocol.HTTPS_ONLY | null         | null          | null       | null       | null   | null    | null    | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n" + SasProtocol.HTTPS_ONLY + "\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | "control"    | null          | null       | null       | null   | null    | null    | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\ncontrol\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null         | "disposition" | null       | null       | null   | null    | null    | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\ndisposition\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null         | null          | "encoding" | null       | null   | null    | null    | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\nencoding\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null         | null          | null       | "language" | null   | null    | null    | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\nlanguage\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null         | null          | null       | null       | "type" | null    | null    | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\ntype"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null         | null          | null       | null       | null   | "saoid" | null    | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\nsaoid\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null         | null          | null       | null       | null   | null    | "suoid" | null  || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\nsuoid\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
        null                                                      | null                                   | null                                   | null                                                                  | null                                                                  | null       | null         | "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=" | null             | null                   | null         | null          | null       | null       | null   | null    | null    | "cid" || "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/fileSystemName/pathName\n\n\n\n\n\n\n\n\ncid\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n"
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
        def sas = primaryDataLakeServiceClient.generateAccountSas(sasValues)
        def pathName = generatePathName()
        fsc.createDirectory(pathName)

        when:
        instrument(new DataLakeFileSystemClientBuilder()
            .endpoint(fsc.getFileSystemUrl())
            .sasToken(sas))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new DataLakeFileSystemClientBuilder()
            .endpoint(fsc.getFileSystemUrl())
            .credential(new AzureSasCredential(sas)))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new DataLakeFileSystemClientBuilder()
            .endpoint(fsc.getFileSystemUrl() + "?" + sas))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new DataLakePathClientBuilder()
            .endpoint(fsc.getFileSystemUrl())
            .pathName(pathName)
            .sasToken(sas))
            .buildDirectoryClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new DataLakePathClientBuilder()
            .endpoint(fsc.getFileSystemUrl())
            .pathName(pathName)
            .credential(new AzureSasCredential(sas)))
            .buildDirectoryClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new DataLakePathClientBuilder()
            .endpoint(fsc.getFileSystemUrl() + "?" + sas)
            .pathName(pathName))
            .buildDirectoryClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new DataLakePathClientBuilder()
            .endpoint(fsc.getFileSystemUrl())
            .pathName(pathName)
            .sasToken(sas))
            .buildFileClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new DataLakePathClientBuilder()
            .endpoint(fsc.getFileSystemUrl())
            .pathName(pathName)
            .credential(new AzureSasCredential(sas)))
            .buildFileClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new DataLakePathClientBuilder()
            .endpoint(fsc.getFileSystemUrl() + "?" + sas)
            .pathName(pathName))
            .buildFileClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new DataLakeServiceClientBuilder()
            .endpoint(fsc.getFileSystemUrl())
            .sasToken(sas))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new DataLakeServiceClientBuilder()
            .endpoint(fsc.getFileSystemUrl())
            .credential(new AzureSasCredential(sas)))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()

        when:
        instrument(new DataLakeServiceClientBuilder()
            .endpoint(fsc.getFileSystemUrl() + "?" + sas))
            .buildClient()
            .getProperties()

        then:
        noExceptionThrown()
    }
}
