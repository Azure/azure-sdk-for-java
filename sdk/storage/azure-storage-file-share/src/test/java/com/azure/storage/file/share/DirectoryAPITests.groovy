// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion

import com.azure.storage.file.share.models.ShareErrorCode
import com.azure.storage.file.share.models.ShareFileHttpHeaders
import com.azure.storage.file.share.models.NtfsFileAttributes
import com.azure.storage.file.share.models.ShareFileItem
import com.azure.storage.file.share.models.ShareRequestConditions
import com.azure.storage.file.share.models.ShareSnapshotInfo
import com.azure.storage.file.share.models.ShareStorageException
import com.azure.storage.file.share.options.ShareCreateOptions
import com.azure.storage.file.share.options.ShareDirectoryCreateOptions
import com.azure.storage.file.share.options.ShareFileRenameOptions
import com.azure.storage.file.share.options.ShareListFilesAndDirectoriesOptions
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.stream.Collectors

class DirectoryAPITests extends APISpec {
    ShareDirectoryClient primaryDirectoryClient
    ShareClient shareClient
    String directoryPath
    String shareName
    static Map<String, String> testMetadata
    FileSmbProperties smbProperties
    static def filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL"

    def setup() {
        shareName = namer.getRandomName(60)
        directoryPath = namer.getRandomName(60)
        shareClient = shareBuilderHelper(shareName).buildClient()
        shareClient.create()
        primaryDirectoryClient = directoryBuilderHelper(shareName, directoryPath).buildDirectoryClient()
        testMetadata = Collections.singletonMap("testmetadata", "value")
        smbProperties = new FileSmbProperties().setNtfsFileAttributes(EnumSet.<NtfsFileAttributes>of(NtfsFileAttributes.NORMAL))
    }

    def "Get directory URL"() {
        given:
        def accountName = StorageSharedKeyCredential.fromConnectionString(environment.primaryAccount.connectionString).getAccountName()
        def expectURL = String.format("https://%s.file.core.windows.net/%s/%s", accountName, shareName, directoryPath)

        when:
        def directoryURL = primaryDirectoryClient.getDirectoryUrl()

        then:
        expectURL == directoryURL
    }

    def "Get share snapshot URL"() {
        given:
        def accountName = StorageSharedKeyCredential.fromConnectionString(environment.primaryAccount.connectionString).getAccountName()
        def expectURL = String.format("https://%s.file.core.windows.net/%s/%s", accountName, shareName, directoryPath)

        when:
        ShareSnapshotInfo shareSnapshotInfo = shareClient.createSnapshot()
        expectURL = expectURL + "?sharesnapshot=" + shareSnapshotInfo.getSnapshot()
        ShareDirectoryClient newDirClient = shareBuilderHelper(shareName).snapshot(shareSnapshotInfo.getSnapshot())
            .buildClient().getDirectoryClient(directoryPath)
        def directoryURL = newDirClient.getDirectoryUrl()

        then:
        expectURL == directoryURL

        when:
        def snapshotEndpoint = String.format("https://%s.file.core.windows.net/%s/%s?sharesnapshot=%s", accountName, shareName, directoryPath, shareSnapshotInfo.getSnapshot())
        ShareDirectoryClient client = getDirectoryClient(StorageSharedKeyCredential.fromConnectionString(environment.primaryAccount.connectionString), snapshotEndpoint)

        then:
        client.getDirectoryUrl() == snapshotEndpoint
    }

    def "Get sub directory client"() {
        given:
        def subDirectoryClient = primaryDirectoryClient.getSubdirectoryClient("testSubDirectory")

        expect:
        subDirectoryClient instanceof ShareDirectoryClient
    }

    def "Get file client"() {
        given:
        def fileClient = primaryDirectoryClient.getFileClient("testFile")

        expect:
        fileClient instanceof ShareFileClient
    }

    def "Exists"() {
        when:
        primaryDirectoryClient.create()

        then:
        primaryDirectoryClient.exists()
    }

    def "Does not exist"() {
        expect:
        !primaryDirectoryClient.exists()
    }

    def "Exists error"() {
        setup:
        primaryDirectoryClient = directoryBuilderHelper(shareName, directoryPath)
            .sasToken("sig=dummyToken").buildDirectoryClient()

        when:
        primaryDirectoryClient.exists()

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 403, ShareErrorCode.AUTHENTICATION_FAILED)
    }

    def "Create directory"() {
        expect:
        FileTestHelper.assertResponseStatusCode(primaryDirectoryClient.createWithResponse(null, null, null, null, null), 201)
    }

    def "Create directory error"() {
        given:
        def testShareName = namer.getRandomName(60)

        when:
        directoryBuilderHelper(testShareName, directoryPath).buildDirectoryClient().create()

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND)
    }

    def "Create directory with metadata"() {
        expect:
        FileTestHelper.assertResponseStatusCode(primaryDirectoryClient.createWithResponse(null, null, testMetadata, null, null), 201)
    }

    def "Create directory with file permission"() {
        when:
        def resp = primaryDirectoryClient.createWithResponse(null, filePermission, null, null, null)

        then:
        FileTestHelper.assertResponseStatusCode(resp, 201)
        resp.getValue().getSmbProperties()
        resp.getValue().getSmbProperties().getFilePermissionKey()
        resp.getValue().getSmbProperties().getNtfsFileAttributes()
        resp.getValue().getSmbProperties().getFileLastWriteTime()
        resp.getValue().getSmbProperties().getFileCreationTime()
        resp.getValue().getSmbProperties().getFileChangeTime()
        resp.getValue().getSmbProperties().getParentId()
        resp.getValue().getSmbProperties().getFileId()
    }

    def "Create directory with file permission key"() {
        setup:
        def filePermissionKey = shareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
            .setFilePermissionKey(filePermissionKey)
        when:
        def resp = primaryDirectoryClient.createWithResponse(smbProperties, null, null, null, null)

        then:
        FileTestHelper.assertResponseStatusCode(resp, 201)
        resp.getValue().getSmbProperties()
        resp.getValue().getSmbProperties().getFilePermissionKey()
        resp.getValue().getSmbProperties().getNtfsFileAttributes()
        resp.getValue().getSmbProperties().getFileLastWriteTime()
        resp.getValue().getSmbProperties().getFileCreationTime()
        resp.getValue().getSmbProperties().getFileChangeTime()
        resp.getValue().getSmbProperties().getParentId()
        resp.getValue().getSmbProperties().getFileId()
    }

    def "Create directory with ntfs attributes"() {
        setup:
        def filePermissionKey = shareClient.createPermission(filePermission)
        def attributes = EnumSet.of(NtfsFileAttributes.HIDDEN, NtfsFileAttributes.DIRECTORY)
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
            .setFilePermissionKey(filePermissionKey)
            .setNtfsFileAttributes(attributes)
        when:
        def resp = primaryDirectoryClient.createWithResponse(smbProperties, null, null, null, null)

        then:
        FileTestHelper.assertResponseStatusCode(resp, 201)
        resp.getValue().getSmbProperties()
        resp.getValue().getSmbProperties().getFilePermissionKey()
        resp.getValue().getSmbProperties().getNtfsFileAttributes()
        resp.getValue().getSmbProperties().getFileLastWriteTime()
        resp.getValue().getSmbProperties().getFileCreationTime()
        resp.getValue().getSmbProperties().getFileChangeTime()
        resp.getValue().getSmbProperties().getParentId()
        resp.getValue().getSmbProperties().getFileId()
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2021_06_08")
    def "Create change time"() {
        setup:
        def changeTime = namer.getUtcNow()

        when:
        primaryDirectoryClient.createWithResponse(new FileSmbProperties().setFileChangeTime(changeTime), null, null,
            null, null)

        then:
        compareDatesWithPrecision(primaryDirectoryClient.getProperties().getSmbProperties().getFileChangeTime(), changeTime)
    }

    @Unroll
    def "Create directory permission and key error"() {
        when:
        FileSmbProperties properties = new FileSmbProperties().setFilePermissionKey(filePermissionKey)
        primaryDirectoryClient.createWithResponse(properties, permission, null, null, null)
        then:
        thrown(IllegalArgumentException)
        where:
        filePermissionKey   | permission
        "filePermissionKey" | filePermission
        null                | new String(FileTestHelper.getRandomBuffer(9 * Constants.KB))
    }

    def "Create if not exists directory min"() {
        expect:
        primaryDirectoryClient.createIfNotExists() != null
    }

    def "Create if not exists directory"() {
        expect:
        FileTestHelper.assertResponseStatusCode(primaryDirectoryClient
            .createIfNotExistsWithResponse(new ShareDirectoryCreateOptions(), null, null), 201)
    }

    def "Create if not exists directory error"() {
        given:
        def testShareName = namer.getRandomName(60)

        when:
        directoryBuilderHelper(testShareName, directoryPath).buildDirectoryClient().createIfNotExists()

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.SHARE_NOT_FOUND)
    }

    def "Create if not exists directory that already exists"() {
        setup:
        def options = new ShareDirectoryCreateOptions()
        def primaryDirectoryClient = shareClient.getDirectoryClient(generatePathName())

        when:
        def initialResponse = primaryDirectoryClient.createIfNotExistsWithResponse(options, null, null)
        def secondResponse = primaryDirectoryClient.createIfNotExistsWithResponse(options, null, null)

        then:
        FileTestHelper.assertResponseStatusCode(initialResponse, 201)
        FileTestHelper.assertResponseStatusCode(secondResponse, 409)
    }

    def "Create if not exists directory with metadata"() {
        setup:
        def options = new ShareDirectoryCreateOptions().setMetadata(testMetadata)
        expect:
        FileTestHelper.assertResponseStatusCode(primaryDirectoryClient
            .createIfNotExistsWithResponse(options, null, null), 201)
    }

    def "Create if not exists directory with file permission"() {
        setup:
        def options = new ShareDirectoryCreateOptions().setFilePermission(filePermission)
        when:
        def resp = primaryDirectoryClient.createIfNotExistsWithResponse(options, null, null)

        then:
        FileTestHelper.assertResponseStatusCode(resp, 201)
        resp.getValue().getSmbProperties()
        resp.getValue().getSmbProperties().getFilePermissionKey()
        resp.getValue().getSmbProperties().getNtfsFileAttributes()
        resp.getValue().getSmbProperties().getFileLastWriteTime()
        resp.getValue().getSmbProperties().getFileCreationTime()
        resp.getValue().getSmbProperties().getFileChangeTime()
        resp.getValue().getSmbProperties().getParentId()
        resp.getValue().getSmbProperties().getFileId()
    }

    def "Create if not exists directory with file permission key"() {
        setup:
        def filePermissionKey = shareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
            .setFilePermissionKey(filePermissionKey)
        def options = new ShareDirectoryCreateOptions().setSmbProperties(smbProperties)
        when:
        def resp = primaryDirectoryClient.createIfNotExistsWithResponse(options, null, null)

        then:
        FileTestHelper.assertResponseStatusCode(resp, 201)
        resp.getValue().getSmbProperties()
        resp.getValue().getSmbProperties().getFilePermissionKey()
        resp.getValue().getSmbProperties().getNtfsFileAttributes()
        resp.getValue().getSmbProperties().getFileLastWriteTime()
        resp.getValue().getSmbProperties().getFileCreationTime()
        resp.getValue().getSmbProperties().getFileChangeTime()
        resp.getValue().getSmbProperties().getParentId()
        resp.getValue().getSmbProperties().getFileId()
    }

    def "Create if not exists directory with ntfs attributes"() {
        setup:
        def filePermissionKey = shareClient.createPermission(filePermission)
        def attributes = EnumSet.of(NtfsFileAttributes.HIDDEN, NtfsFileAttributes.DIRECTORY)
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
            .setFilePermissionKey(filePermissionKey)
            .setNtfsFileAttributes(attributes)
        def options = new ShareDirectoryCreateOptions().setSmbProperties(smbProperties)
        when:
        def resp = primaryDirectoryClient.createIfNotExistsWithResponse(options, null, null)

        then:
        FileTestHelper.assertResponseStatusCode(resp, 201)
        resp.getValue().getSmbProperties()
        resp.getValue().getSmbProperties().getFilePermissionKey()
        resp.getValue().getSmbProperties().getNtfsFileAttributes()
        resp.getValue().getSmbProperties().getFileLastWriteTime()
        resp.getValue().getSmbProperties().getFileCreationTime()
        resp.getValue().getSmbProperties().getFileChangeTime()
        resp.getValue().getSmbProperties().getParentId()
        resp.getValue().getSmbProperties().getFileId()
    }

    @Unroll
    def "Create if not exists directory permission and key error"() {
        when:
        FileSmbProperties properties = new FileSmbProperties().setFilePermissionKey(filePermissionKey)
        def options = new ShareDirectoryCreateOptions().setSmbProperties(properties).setFilePermission(permission)
        primaryDirectoryClient.createIfNotExistsWithResponse(options, null, null)
        then:
        thrown(IllegalArgumentException)
        where:
        filePermissionKey   | permission
        "filePermissionKey" | filePermission
        null                | new String(FileTestHelper.getRandomBuffer(9 * Constants.KB))
    }

    def "Delete directory"() {
        given:
        primaryDirectoryClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(primaryDirectoryClient.deleteWithResponse(null, null), 202)
    }

    def "Delete directory error"() {
        when:
        primaryDirectoryClient.delete()

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Delete if exists directory"() {
        given:
        primaryDirectoryClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(primaryDirectoryClient.deleteIfExistsWithResponse(null, null), 202)
    }

    def "Delete if exists directory min"() {
        given:
        primaryDirectoryClient.create()

        expect:
        primaryDirectoryClient.deleteIfExists()
    }

    def "Delete if exists directory that does not exist"() {
        setup:
        primaryDirectoryClient = shareClient.getDirectoryClient(generatePathName())

        when:
        def response = primaryDirectoryClient.deleteIfExistsWithResponse(null, null)

        then:
        !response.getValue()
        response.getStatusCode() == 404
        !primaryDirectoryClient.exists()
    }

    def "Delete if exists directory that was already deleted"() {
        setup:
        primaryDirectoryClient.create()

        when:
        def initialResponse = primaryDirectoryClient.deleteIfExistsWithResponse(null, null)
        def secondResponse = primaryDirectoryClient.deleteIfExistsWithResponse(null, null)

        then:
        initialResponse.getStatusCode() == 202
        secondResponse.getStatusCode() == 404
        initialResponse.getValue()
        !secondResponse.getValue()

    }

    def "Get properties"() {
        given:
        primaryDirectoryClient.create()
        def resp = primaryDirectoryClient.getPropertiesWithResponse(null, null)

        expect:
        FileTestHelper.assertResponseStatusCode(resp, 200)
        resp.getValue().getETag()
        resp.getValue().getSmbProperties()
        resp.getValue().getSmbProperties().getFilePermissionKey()
        resp.getValue().getSmbProperties().getNtfsFileAttributes()
        resp.getValue().getSmbProperties().getFileLastWriteTime()
        resp.getValue().getSmbProperties().getFileCreationTime()
        resp.getValue().getSmbProperties().getFileChangeTime()
        resp.getValue().getSmbProperties().getParentId()
        resp.getValue().getSmbProperties().getFileId()
    }

    def "Get properties error"() {
        when:
        primaryDirectoryClient.getPropertiesWithResponse(null, null)

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Set properties file permission"() {
        given:
        primaryDirectoryClient.create()
        def resp = primaryDirectoryClient.setPropertiesWithResponse(null, filePermission, null, null)
        expect:
        FileTestHelper.assertResponseStatusCode(resp, 200)
        resp.getValue().getSmbProperties()
        resp.getValue().getSmbProperties().getFilePermissionKey()
        resp.getValue().getSmbProperties().getNtfsFileAttributes()
        resp.getValue().getSmbProperties().getFileLastWriteTime()
        resp.getValue().getSmbProperties().getFileCreationTime()
        resp.getValue().getSmbProperties().getFileChangeTime()
        resp.getValue().getSmbProperties().getParentId()
        resp.getValue().getSmbProperties().getFileId()
    }

    def "Set properties file permission key"() {
        given:
        def filePermissionKey = shareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
            .setFilePermissionKey(filePermissionKey)
        primaryDirectoryClient.create()
        def resp = primaryDirectoryClient.setPropertiesWithResponse(smbProperties, null, null, null)

        expect:
        FileTestHelper.assertResponseStatusCode(resp, 200)
        resp.getValue().getSmbProperties()
        resp.getValue().getSmbProperties().getFilePermissionKey()
        resp.getValue().getSmbProperties().getNtfsFileAttributes()
        resp.getValue().getSmbProperties().getFileLastWriteTime()
        resp.getValue().getSmbProperties().getFileCreationTime()
        resp.getValue().getSmbProperties().getFileChangeTime()
        resp.getValue().getSmbProperties().getParentId()
        resp.getValue().getSmbProperties().getFileId()
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2021_06_08")
    def "Set httpHeaders change time"() {
        setup:
        primaryDirectoryClient.create()
        def filePermissionKey = shareClient.createPermission(filePermission)
        def changeTime = namer.getUtcNow()
        smbProperties.setFileChangeTime(namer.getUtcNow())
            .setFilePermissionKey(filePermissionKey)

        when:
        primaryDirectoryClient.setProperties(new FileSmbProperties().setFileChangeTime(changeTime), null)

        then:
        compareDatesWithPrecision(primaryDirectoryClient.getProperties().getSmbProperties().getFileChangeTime(), changeTime)
    }

    @Unroll
    def "Set properties error"() {
        when:
        FileSmbProperties properties = new FileSmbProperties().setFilePermissionKey(filePermissionKey)
        primaryDirectoryClient.create()
        primaryDirectoryClient.setPropertiesWithResponse(properties, permission, null, null)

        then:
        thrown(IllegalArgumentException)

        where:
        filePermissionKey   | permission
        "filePermissionKey" | filePermission
        null                | new String(FileTestHelper.getRandomBuffer(9 * Constants.KB))
    }

    def "Set metadata"() {
        given:
        primaryDirectoryClient.createWithResponse(null, null, testMetadata, null, null)
        def updatedMetadata = Collections.singletonMap("update", "value")

        when:
        def getPropertiesBefore = primaryDirectoryClient.getProperties()
        def setPropertiesResponse = primaryDirectoryClient.setMetadataWithResponse(updatedMetadata, null, null)
        def getPropertiesAfter = primaryDirectoryClient.getProperties()

        then:
        testMetadata == getPropertiesBefore.getMetadata()
        FileTestHelper.assertResponseStatusCode(setPropertiesResponse, 200)
        updatedMetadata == getPropertiesAfter.getMetadata()
    }

    def "Set metadata error"() {
        given:
        primaryDirectoryClient.create()
        def errorMetadata = Collections.singletonMap("", "value")

        when:
        primaryDirectoryClient.setMetadata(errorMetadata)

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.EMPTY_METADATA_KEY)
    }

    @Unroll
    def "List files and directories"() {
        given:
        primaryDirectoryClient.create()

        for (def expectedFile : expectedFiles) {
            primaryDirectoryClient.createFile(expectedFile, 2)
        }

        for (def expectedDirectory : expectedDirectories) {
            primaryDirectoryClient.createSubdirectory(expectedDirectory)
        }

        when:
        def foundFiles = [] as Set
        def foundDirectories = [] as Set
        for (def fileRef : primaryDirectoryClient.listFilesAndDirectories()) {
            if (fileRef.isDirectory()) {
                foundDirectories << fileRef.getName()
            } else {
                foundFiles << fileRef.getName()
            }
        }

        then:
        expectedFiles == foundFiles
        expectedDirectories == foundDirectories

        where:
        expectedFiles          | expectedDirectories
        ["a", "b", "c"] as Set | ["d", "e"] as Set
        ["a", "c", "e"] as Set | ["b", "d"] as Set
    }

    /**
     * The listing hierarchy:
     * share -> dir -> listOp0 (dir) -> listOp3 (file)
     *                               -> listOp4 (file)
     *              -> listOp1 (dir) -> listOp5 (file)
     *                               -> listOp6 (file)
     *              -> listOp2 (file)
     */
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_10_02")
    @Unroll
    def "List files and directories args"() {
        given:
        primaryDirectoryClient.create()
        def nameList = new LinkedList()
        def dirPrefix = namer.getRandomName(60)
        for (int i = 0; i < 2; i++) {
            def subDirClient = primaryDirectoryClient.getSubdirectoryClient(dirPrefix + i)
            subDirClient.create()
            for (int j = 0; j < 2; j++) {
                def num = i * 2 + j + 3
                subDirClient.createFile(dirPrefix + num, 1024)
            }
        }
        primaryDirectoryClient.createFile(dirPrefix + 2, 1024)
        for (int i = 0; i < 3; i++) {
            nameList.add(dirPrefix + i)
        }

        when:
        def fileRefIter = primaryDirectoryClient.listFilesAndDirectories(namer.getResourcePrefix() + extraPrefix, maxResults, null, null).iterator()

        then:
        for (int i = 0; i < numOfResults; i++) {
            Objects.equals(nameList.pop(), fileRefIter.next().getName())
        }
        !fileRefIter.hasNext()

        where:
        extraPrefix   | maxResults | numOfResults
        ""            | null       | 3
        ""            | 1          | 3
        "noOp"        | 3          | 0
    }

    @Unroll
    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_10_02")
    def "List files and directories extended info args"() {
        given:
        primaryDirectoryClient.create()
        def nameList = [] as List<String>
        def dirPrefix = namer.getRandomName(60)
        for (int i = 0; i < 2; i++) {
            def subDirClient = primaryDirectoryClient.getSubdirectoryClient(dirPrefix + i)
            subDirClient.create()
            for (int j = 0; j < 2; j++) {
                def num = i * 2 + j + 3
                subDirClient.createFile(dirPrefix + num, 1024)
            }
        }
        primaryDirectoryClient.createFile(dirPrefix + 2, 1024)
        for (int i = 0; i < 3; i++) {
            nameList << (dirPrefix + i)
        }

        when:
        def options = new ShareListFilesAndDirectoriesOptions()
            .setPrefix(namer.getResourcePrefix())
            .setIncludeExtendedInfo(true)
            .setIncludeTimestamps(timestamps)
            .setIncludeETag(etag)
            .setIncludeAttributes(attributes)
            .setIncludePermissionKey(permissionKey)
        def returnedFileList = primaryDirectoryClient.listFilesAndDirectories(options, null, null).collect()

        then:
        nameList == returnedFileList*.getName()

        where:
        timestamps | etag  | attributes | permissionKey
        false      | false | false      | false
        true       | false | false      | false
        false      | true  | false      | false
        false      | false | true       | false
        false      | false | false      | true
        true       | true  | true       | true
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2020_10_02")
    def "List files and directories extended info results"() {
        given:
        def parentDir = primaryDirectoryClient
        parentDir.create()
        def file = parentDir.createFile(namer.getRandomName(60), 1024)
        def dir = parentDir.createSubdirectory(namer.getRandomName(60))

        when:
        def listResults = parentDir.listFilesAndDirectories(
            new ShareListFilesAndDirectoriesOptions()
                .setIncludeExtendedInfo(true).setIncludeTimestamps(true).setIncludePermissionKey(true).setIncludeETag(true)
                .setIncludeAttributes(true),
            null, null)
            .stream().collect(Collectors.toList())

        then:
        ShareFileItem dirListItem
        ShareFileItem fileListItem
        if (listResults[0].isDirectory()) {
            dirListItem = listResults[0]
            fileListItem = listResults[1]
        } else {
            dirListItem = listResults[1]
            fileListItem = listResults[0]
        }

        new File(dir.getDirectoryPath()).getName() == dirListItem.getName()
        dirListItem.isDirectory()
        dirListItem.getId() && !dirListItem.getId().allWhitespace
        EnumSet.of(NtfsFileAttributes.DIRECTORY) == dirListItem.fileAttributes
        dirListItem.getPermissionKey() && !dirListItem.getPermissionKey().allWhitespace
        dirListItem.getProperties().getCreatedOn()
        dirListItem.getProperties().getLastAccessedOn()
        dirListItem.getProperties().getLastWrittenOn()
        dirListItem.getProperties().getChangedOn()
        dirListItem.getProperties().getLastModified()
        dirListItem.getProperties().getETag() && !dirListItem.getProperties().getETag().allWhitespace

        new File(file.getFilePath()).getName() == fileListItem.getName()
        !fileListItem.isDirectory()
        fileListItem.getId() && !fileListItem.getId().allWhitespace
        EnumSet.of(NtfsFileAttributes.ARCHIVE) == fileListItem.fileAttributes
        fileListItem.getPermissionKey() && !fileListItem.getPermissionKey().allWhitespace
        fileListItem.getProperties().getCreatedOn()
        fileListItem.getProperties().getLastAccessedOn()
        fileListItem.getProperties().getLastWrittenOn()
        fileListItem.getProperties().getChangedOn()
        fileListItem.getProperties().getLastModified()
        fileListItem.getProperties().getETag() && !fileListItem.getProperties().getETag().allWhitespace
    }

    def "List max results by page"() {
        given:
        primaryDirectoryClient.create()
        def nameList = new LinkedList()
        def dirPrefix = namer.getRandomName(60)
        for (int i = 0; i < 2; i++) {
            def subDirClient = primaryDirectoryClient.getSubdirectoryClient(dirPrefix + i)
            subDirClient.create()
            for (int j = 0; j < 2; j++) {
                def num = i * 2 + j + 3
                subDirClient.createFile(dirPrefix + num, 1024)
            }
        }
        primaryDirectoryClient.createFile(dirPrefix + 2, 1024)
        for (int i = 0; i < 3; i++) {
            nameList.add(dirPrefix + i)
        }

        when:
        def fileRefIter = primaryDirectoryClient
            .listFilesAndDirectories(namer.getResourcePrefix(), null, null, null)
            .iterableByPage(1).iterator()

        then:
        for (def page : fileRefIter) {
            assert page.value.size() == 1
        }
    }

    @Unroll
    def "List handles"() {
        given:
        primaryDirectoryClient.create()

        expect:
        primaryDirectoryClient.listHandles(maxResult, recursive, null, null).size() == 0

        where:
        maxResult | recursive
        2         | true
        null      | false
    }

    def "List handles error"() {
        when:
        primaryDirectoryClient.listHandles(null, true, null, null).iterator().hasNext()

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2019_07_07")
    def "Force close handle min"() {
        given:
        primaryDirectoryClient.create()

        when:
        def handlesClosedInfo = primaryDirectoryClient.forceCloseHandle("1")

        then:
        handlesClosedInfo.getClosedHandles() == 0
        handlesClosedInfo.getFailedHandles() == 0
        notThrown(ShareStorageException)
    }

    def "Force close handle invalid handle ID"() {
        given:
        primaryDirectoryClient.create()

        when:
        primaryDirectoryClient.forceCloseHandle("invalidHandleId")

        then:
        thrown(ShareStorageException)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2019_07_07")
    def "Force close all handles min"() {
        given:
        primaryDirectoryClient.create()

        when:
        def handlesClosedInfo  = primaryDirectoryClient.forceCloseAllHandles(false, null, null)

        then:
        notThrown(ShareStorageException)
        handlesClosedInfo.getClosedHandles() == 0
        handlesClosedInfo.getFailedHandles() == 0
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2021_04_10")
    def "Rename min"() {
        setup:
        primaryDirectoryClient.create()

        when:
        primaryDirectoryClient.rename(generatePathName())

        then:
        notThrown(ShareStorageException)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2021_04_10")
    def "Rename with response"() {
        setup:
        primaryDirectoryClient.create()

        when:
        def resp = primaryDirectoryClient.renameWithResponse(new ShareFileRenameOptions(generatePathName()), null, null)

        def renamedClient = resp.getValue()
        renamedClient.getProperties()

        then:
        notThrown(ShareStorageException)

        when:
        primaryDirectoryClient.getProperties()

        then:
        thrown(ShareStorageException)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2021_04_10")
    def "Rename different directory"() {
        setup:
        primaryDirectoryClient.create()
        def dc = shareClient.getDirectoryClient(generatePathName())
        dc.create()
        def destinationPath = dc.getFileClient(generatePathName())

        when:
        def resultClient = primaryDirectoryClient.rename(destinationPath.getFilePath())

        then:
        resultClient.exists()
        !primaryDirectoryClient.exists()
        destinationPath.getFilePath() == resultClient.getDirectoryPath()
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2021_04_10")
    @Unroll
    def "Rename replace if exists"() {
        setup:
        primaryDirectoryClient.create()
        def destination = shareClient.getFileClient(generatePathName())
        destination.create(512)
        def exception = false

        when:
        try {
            primaryDirectoryClient.renameWithResponse(new ShareFileRenameOptions(destination.getFilePath())
                .setReplaceIfExists(replaceIfExists), null, null)
        } catch (ShareStorageException ignored) {
            exception = true
        }

        then:
        replaceIfExists == !exception

        where:
        replaceIfExists | _
        true            | _
        false           | _
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2021_04_10")
    @Unroll
    def "Rename ignore read only"() {
        setup:
        primaryDirectoryClient.create()
        FileSmbProperties props = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))
        def destinationFile = shareClient.getFileClient(generatePathName())
        destinationFile.createWithResponse(512L, null, props, null, null, null, null, null)
        def exception = false

        when:
        try {
            primaryDirectoryClient.renameWithResponse(new ShareFileRenameOptions(destinationFile.getFilePath())
                .setIgnoreReadOnly(ignoreReadOnly).setReplaceIfExists(true), null, null)
        } catch (ShareStorageException ignored) {
            exception = true
        }

        then:
        exception == !ignoreReadOnly

        where:
        ignoreReadOnly  | _
        true            | _
        false           | _
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2021_04_10")
    def "Rename file permission"() {
        setup:
        primaryDirectoryClient.create()
        def filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)"

        when:
        def destClient = primaryDirectoryClient.renameWithResponse(new ShareFileRenameOptions(generatePathName())
            .setFilePermission(filePermission), null, null).getValue()

        then:
        destClient.getProperties().getSmbProperties().getFilePermissionKey() != null
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2021_04_10")
    def "Rename file permission and key set"() {
        setup:
        primaryDirectoryClient.create()
        def filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)"

        when:
        def destClient = primaryDirectoryClient.renameWithResponse(new ShareFileRenameOptions(generatePathName())
            .setFilePermission(filePermission)
            .setSmbProperties(new FileSmbProperties().setFilePermissionKey("filePermissionkey")), null, null).getValue()

        then:
        thrown(ShareStorageException) // permission and key cannot both be set
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2021_04_10")
    def "Rename file smbProperties"() {
        setup:
        primaryDirectoryClient.create()
        def filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)"
        def permissionKey = shareClient.createPermission(filePermission)
        def fileChangeTime = namer.getUtcNow()
        def smbProperties = new FileSmbProperties()
            .setFilePermissionKey(permissionKey)
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.DIRECTORY))
            .setFileCreationTime(namer.getUtcNow().minusDays(5))
            .setFileLastWriteTime(namer.getUtcNow().minusYears(2))
            .setFileChangeTime(fileChangeTime)

        when:
        def destClient = primaryDirectoryClient.renameWithResponse(new ShareFileRenameOptions(generatePathName())
            .setSmbProperties(smbProperties), null, null).getValue()
        def destProperties = destClient.getProperties()

        then:
        destProperties.getSmbProperties().getNtfsFileAttributes() == EnumSet.of(NtfsFileAttributes.DIRECTORY)
        destProperties.getSmbProperties().getFileCreationTime()
        destProperties.getSmbProperties().getFileLastWriteTime()
        compareDatesWithPrecision(destProperties.getSmbProperties().getFileChangeTime(), fileChangeTime)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2021_04_10")
    def "Rename metadata"() {
        given:
        primaryDirectoryClient.create()
        def updatedMetadata = Collections.singletonMap("update", "value")

        when:
        def resp = primaryDirectoryClient.renameWithResponse(new ShareFileRenameOptions(generatePathName())
            .setMetadata(updatedMetadata), null, null)

        def renamedClient = resp.getValue()
        def getPropertiesAfter = renamedClient.getProperties()


        then:
        updatedMetadata == getPropertiesAfter.getMetadata()
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2021_04_10")
    def "Rename error"() {
        setup:
        primaryDirectoryClient = shareClient.getDirectoryClient(generatePathName())

        when:
        primaryDirectoryClient.rename(generatePathName())

        then:
        thrown(ShareStorageException)
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2021_04_10")
    @Unroll
    def "Rename dest AC"() {
        setup:
        primaryDirectoryClient.create()
        def pathName = generatePathName()
        def destFile = shareClient.getFileClient(pathName)
        destFile.create(512)
        leaseID = setupFileLeaseCondition(destFile, leaseID)
        def src = new ShareRequestConditions()
            .setLeaseId(leaseID)

        expect:
        primaryDirectoryClient.renameWithResponse(new ShareFileRenameOptions(pathName)
            .setDestinationRequestConditions(src).setReplaceIfExists(true), null, null).getStatusCode() == 200

        where:
        leaseID         | _
        receivedLeaseID | _
    }

    @RequiredServiceVersion(clazz = ShareServiceVersion.class, min = "V2021_04_10")
    @Unroll
    def "Rename dest AC fail"() {
        setup:
        primaryDirectoryClient.create()
        def pathName = generatePathName()
        def destFile = shareClient.getFileClient(pathName)
        destFile.create(512)
        setupFileLeaseCondition(destFile, leaseID)
        def src = new ShareRequestConditions()
            .setLeaseId(leaseID)

        when:
        primaryDirectoryClient.renameWithResponse(new ShareFileRenameOptions(pathName)
            .setDestinationRequestConditions(src).setReplaceIfExists(true), null, null)

        then:
        thrown(ShareStorageException)

        where:
        leaseID        | _
        garbageLeaseID | _
    }

    def "Create sub directory"() {
        given:
        primaryDirectoryClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createSubdirectoryWithResponse("testCreateSubDirectory", null, null, null, null, null), 201)
    }

    def "Create sub directory invalid name"() {
        given:
        primaryDirectoryClient.create()

        when:
        primaryDirectoryClient.createSubdirectory("test/subdirectory")

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.PARENT_NOT_FOUND)
    }

    def "Create sub directory metadata"() {
        given:
        primaryDirectoryClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createSubdirectoryWithResponse("testCreateSubDirectory", null, null, testMetadata, null, null), 201)
    }

    def "Create sub directory metadata error"() {
        given:
        primaryDirectoryClient.create()

        when:
        primaryDirectoryClient.createSubdirectoryWithResponse("testsubdirectory", null, null, Collections.singletonMap("", "value"), null, null)

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.EMPTY_METADATA_KEY)
    }

    def "Create sub directory file permission"() {
        given:
        primaryDirectoryClient.create()
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createSubdirectoryWithResponse("testCreateSubDirectory", null, filePermission, null, null, null), 201)
    }

    def "Create sub directory file permission key"() {
        given:
        primaryDirectoryClient.create()
        def filePermissionKey = shareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
            .setFilePermissionKey(filePermissionKey)
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createSubdirectoryWithResponse("testCreateSubDirectory", smbProperties, null, null, null, null), 201)
    }

    def "Create if not exists sub directory"() {
        given:
        primaryDirectoryClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createSubdirectoryIfNotExistsWithResponse("testCreateSubDirectory",
                new ShareDirectoryCreateOptions(), null, null), 201)
    }

    def "Create if not exists subdirectory that already exists"() {
        setup:
        def subdirectoryName = generatePathName()
        primaryDirectoryClient = shareClient.getDirectoryClient(generatePathName())
        primaryDirectoryClient.create()
        def initialResponse = primaryDirectoryClient.createSubdirectoryIfNotExistsWithResponse(subdirectoryName,
            new ShareDirectoryCreateOptions(), null, null)

        when:
        def secondResponse = primaryDirectoryClient.createSubdirectoryIfNotExistsWithResponse(subdirectoryName,
            new ShareDirectoryCreateOptions(), null, null)

        then:
        initialResponse.getStatusCode() == 201
        secondResponse.getStatusCode() == 409
    }

    def "Create if not exists sub directory invalid name"() {
        given:
        primaryDirectoryClient.create()

        when:
        primaryDirectoryClient.createSubdirectoryIfNotExists("test/subdirectory")

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.PARENT_NOT_FOUND)
    }

    def "Create if not exists sub directory metadata"() {
        given:
        primaryDirectoryClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createSubdirectoryIfNotExistsWithResponse("testCreateSubDirectory",
                new ShareDirectoryCreateOptions().setMetadata(testMetadata), null, null), 201)
    }

    def "Create if not exists sub directory metadata error"() {
        given:
        primaryDirectoryClient.create()

        when:
        primaryDirectoryClient.createSubdirectoryIfNotExistsWithResponse("testsubdirectory",
            new ShareDirectoryCreateOptions().setMetadata(Collections.singletonMap("", "value")), null, null)

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, ShareErrorCode.EMPTY_METADATA_KEY)
    }

    def "Create if not exists sub directory file permission"() {
        given:
        primaryDirectoryClient.create()
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createSubdirectoryIfNotExistsWithResponse("testCreateSubDirectory",
                new ShareDirectoryCreateOptions().setFilePermission(filePermission), null, null), 201)
    }

    def "Create if not exists sub directory file permission key"() {
        given:
        primaryDirectoryClient.create()
        def filePermissionKey = shareClient.createPermission(filePermission)
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())
            .setFilePermissionKey(filePermissionKey)
        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createSubdirectoryIfNotExistsWithResponse("testCreateSubDirectory",
                new ShareDirectoryCreateOptions().setSmbProperties(smbProperties), null, null), 201)
    }

    def "Delete sub directory"() {
        given:
        def subDirectoryName = "testSubCreateDirectory"
        primaryDirectoryClient.create()
        primaryDirectoryClient.createSubdirectory(subDirectoryName)

        expect:
        FileTestHelper.assertResponseStatusCode(primaryDirectoryClient.deleteSubdirectoryWithResponse(subDirectoryName, null, null), 202)
    }

    def "Delete sub directory error"() {
        given:
        primaryDirectoryClient.create()

        when:
        primaryDirectoryClient.deleteSubdirectory("testsubdirectory")

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Delete if exists sub directory"() {
        given:
        def subDirectoryName = "testSubCreateDirectory"
        primaryDirectoryClient.create()
        primaryDirectoryClient.createSubdirectory(subDirectoryName)

        expect:
        FileTestHelper.assertResponseStatusCode(primaryDirectoryClient.deleteSubdirectoryIfExistsWithResponse(subDirectoryName, null, null), 202)
    }

    def "Delete if exists sub directory min"() {
        given:
        def subDirectoryName = "testSubCreateDirectory"
        primaryDirectoryClient.create()
        primaryDirectoryClient.createSubdirectory(subDirectoryName)

        expect:
        primaryDirectoryClient.deleteSubdirectoryIfExists(subDirectoryName)
    }

    def "Delete if exists sub directory that does not exist"() {
        when:
        def response = primaryDirectoryClient.deleteSubdirectoryIfExistsWithResponse("testsubdirectory", null, null)

        then:
        response.getStatusCode() == 404
        !response.getValue()
    }


    def "Create file"() {
        given:
        primaryDirectoryClient.create()

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createFileWithResponse("testCreateFile", 1024, null, null, null, null, null, null), 201)
    }

    @Unroll
    def "Create file invalid args"() {
        given:
        primaryDirectoryClient.create()

        when:
        primaryDirectoryClient.createFileWithResponse(fileName, maxSize, null, null, null, null, null, null)
        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, statusCode, errMsg)

        where:
        fileName    | maxSize | statusCode | errMsg
        "testfile:" | 1024    | 400        | ShareErrorCode.INVALID_RESOURCE_NAME
        "fileName"  | -1      | 400        | ShareErrorCode.OUT_OF_RANGE_INPUT

    }

    def "Create file maxOverload"() {
        given:
        primaryDirectoryClient.create()
        ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders()
            .setContentType("txt")
        smbProperties.setFileCreationTime(namer.getUtcNow())
            .setFileLastWriteTime(namer.getUtcNow())

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.createFileWithResponse("testCreateFile", 1024, httpHeaders, smbProperties, filePermission, testMetadata, null, null), 201)
    }

    @Unroll
    def "Create file maxOverload invalid args"() {
        given:
        primaryDirectoryClient.create()

        when:
        primaryDirectoryClient.createFileWithResponse(fileName, maxSize, httpHeaders, null, null, metadata, null, null)

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 400, errMsg)

        where:
        fileName    | maxSize | httpHeaders                                           | metadata                              | errMsg
        "testfile:" | 1024    | null                                                  | testMetadata                          | ShareErrorCode.INVALID_RESOURCE_NAME
        "fileName"  | -1      | null                                                  | testMetadata                          | ShareErrorCode.OUT_OF_RANGE_INPUT
        "fileName"  | 1024    | new ShareFileHttpHeaders().setContentMd5(new byte[0]) | testMetadata                          | ShareErrorCode.INVALID_HEADER_VALUE
        "fileName"  | 1024    | null                                                  | Collections.singletonMap("", "value") | ShareErrorCode.EMPTY_METADATA_KEY

    }

    def "Delete file"() {
        given:
        def fileName = "testCreateFile"
        primaryDirectoryClient.create()
        primaryDirectoryClient.createFile(fileName, 1024)

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.deleteFileWithResponse(fileName, null, null), 202)
    }

    def "Delete file error"() {
        given:
        primaryDirectoryClient.create()

        when:
        primaryDirectoryClient.deleteFileWithResponse("testfile", null, null)

        then:
        def e = thrown(ShareStorageException)
        FileTestHelper.assertExceptionStatusCodeAndMessage(e, 404, ShareErrorCode.RESOURCE_NOT_FOUND)
    }

    def "Delete if exists file min"() {
        given:
        def fileName = "testCreateFile"
        primaryDirectoryClient.create()
        primaryDirectoryClient.createFile(fileName, 1024)

        expect:
        primaryDirectoryClient.deleteFileIfExists(fileName)
    }

    def "Delete if exists file"() {
        given:
        def fileName = "testCreateFile"
        primaryDirectoryClient.create()
        primaryDirectoryClient.createFile(fileName, 1024)

        expect:
        FileTestHelper.assertResponseStatusCode(
            primaryDirectoryClient.deleteFileIfExistsWithResponse(fileName, null, null), 202)
    }

    def "Delete if exists file that does not exist"() {
        given:
        primaryDirectoryClient.create()

        when:
        def response = primaryDirectoryClient.deleteFileIfExistsWithResponse("testfile", null, null)

        then:
        response.getStatusCode() == 404
        !response.getValue()
    }

    def "Get snapshot id"() {
        given:
        def snapshot = OffsetDateTime.of(LocalDateTime.of(2000, 1, 1,
            1, 1), ZoneOffset.UTC).toString()

        when:
        def shareSnapshotClient = directoryBuilderHelper(shareName, directoryPath).snapshot(snapshot).buildDirectoryClient()

        then:
        snapshot == shareSnapshotClient.getShareSnapshotId()
    }

    def "Get Share Name"() {
        expect:
        shareName == primaryDirectoryClient.getShareName()
    }

    def "Get Directory Path"() {
        expect:
        directoryPath == primaryDirectoryClient.getDirectoryPath()
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials and auth would fail because we changed a signed header.
    def "Per call policy"() {
        given:
        primaryDirectoryClient.create()

        def directoryClient = directoryBuilderHelper(primaryDirectoryClient.getShareName(), primaryDirectoryClient.getDirectoryPath())
            .addPolicy(getPerCallVersionPolicy()).buildDirectoryClient()

        when:
        def response = directoryClient.getPropertiesWithResponse(null, null)

        then:
        notThrown(ShareStorageException)
        response.getHeaders().getValue("x-ms-version") == "2017-11-09"
    }

    @Unroll
    def "Root directory support"() {
        given:
        // share:/dir1/dir2
        def dir1Name = "dir1"
        def dir2Name = "dir2"
        shareClient.createDirectory(dir1Name).createSubdirectory(dir2Name)
        ShareDirectoryClient rootDirectory = shareClient.getDirectoryClient(rootDirPath)

        expect:
        rootDirectory.exists() // can operate on root directory
        rootDirectory.getSubdirectoryClient(dir1Name).exists() // can operate on a subdirectory

        where:
        _ | rootDirPath
        _ | ""
        _ | "/"
    }

}
