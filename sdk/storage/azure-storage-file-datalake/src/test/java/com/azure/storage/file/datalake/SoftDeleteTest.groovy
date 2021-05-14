// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake

import com.azure.core.test.TestMode
import com.azure.storage.common.Utility
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion
import com.azure.storage.file.datalake.models.DataLakeRetentionPolicy
import com.azure.storage.file.datalake.models.DataLakeServiceProperties
import com.azure.storage.file.datalake.models.DataLakeStorageException
import com.azure.storage.file.datalake.models.PathDeletedItem
import spock.lang.Unroll

import java.util.stream.Collectors

class SoftDeleteTest extends APISpec{

    DataLakeServiceClient softDeleteDataLakeServiceClient
    DataLakeFileSystemClient fileSystemClient

    def setupSpec() {
        if (env.testMode != TestMode.PLAYBACK) {
            // This is to enable soft delete until better way is found. No need for recording.
            def setupClient = new DataLakeServiceClientBuilder()
                .endpoint(env.dataLakeSoftDeleteAccount.dataLakeEndpoint)
                .credential(env.dataLakeSoftDeleteAccount.credential)
                .buildClient()
            setupClient.setProperties(new DataLakeServiceProperties()
                .setDeleteRetentionPolicy(new DataLakeRetentionPolicy().setEnabled(true).setDays(2)))

            sleepIfRecord(30000)
        }
    }

    def setup() {
        softDeleteDataLakeServiceClient = getServiceClient(env.dataLakeSoftDeleteAccount)
        fileSystemClient = softDeleteDataLakeServiceClient.getFileSystemClient(generateFileSystemName())
        fileSystemClient.create()
    }

    def cleanup() {
        fileSystemClient.delete()
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_08_04")
    def "Restore path"() {
        setup:
        def dir = fileSystemClient.getDirectoryClient(generatePathName())
        dir.create()
        dir.delete()

        def file = fileSystemClient.getFileClient(generatePathName())
        file.create()
        file.delete()

        def paths = fileSystemClient.listDeletedPaths().iterator()

        def dirDeletionId = paths.next().getDeletionId()
        def fileDeletionId = paths.next().getDeletionId()

        when:
        def returnedClient = fileSystemClient.undeletePath(dir.getDirectoryName(), dirDeletionId)

        then:
        returnedClient instanceof DataLakeDirectoryClient
        dir.getProperties() != null
        returnedClient.getPathUrl() == dir.getPathUrl()

        when:
        returnedClient = fileSystemClient.undeletePath(file.getFileName(), fileDeletionId)

        then:
        returnedClient instanceof DataLakeFileClient
        file.getProperties() != null
        returnedClient.getPathUrl() == file.getPathUrl()
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_08_04")
    @Unroll
    def "Restore path special characters"() {
        setup:
        name = Utility.urlEncode(name)
        def dir = fileSystemClient.getDirectoryClient("dir" + name)
        dir.create()
        dir.delete()

        def file = fileSystemClient.getFileClient("file" + name)
        file.create()
        file.delete()

        def paths = fileSystemClient.listDeletedPaths().iterator()

        def dirDeletionId = paths.next().getDeletionId()
        def fileDeletionId = paths.next().getDeletionId()

        when:
        def returnedClient = fileSystemClient.undeletePath(Utility.urlEncode(dir.getDirectoryName()), dirDeletionId)

        then:
        returnedClient instanceof DataLakeDirectoryClient
        dir.getProperties() != null

        when:
        returnedClient = fileSystemClient.undeletePath(Utility.urlEncode(file.getFileName()), fileDeletionId)

        then:
        returnedClient instanceof DataLakeFileClient
        file.getProperties() != null

        where:
        name                                                   | _
        "!'();[]@&%=+\$,#äÄöÖüÜß;"                                | _
        "%21%27%28%29%3B%5B%5D%40%26%25%3D%2B%24%2C%23äÄöÖüÜß%3B" | _
        " my cool directory "                                     | _
        "directory"                                               | _
    }

    def "Restore path error"() {
        setup:
        fsc = softDeleteDataLakeServiceClient.getFileSystemClient(generateFileSystemName())

        when:
        fsc.undeletePath("foo", "bar")

        then:
        thrown(DataLakeStorageException)
    }

    def "List deleted paths options maxResults by page"() {
        setup:
        def dir = fileSystemClient.getDirectoryClient(generatePathName())
        dir.create()
        def fc1 = dir.getFileClient(generatePathName())
        fc1.create(true)
        fc1.delete()

        def fc2 = dir.getFileClient(generatePathName())
        fc2.create(true)
        fc2.delete()

        def fc3 = fileSystemClient.getFileClient(generatePathName())
        fc3.create()
        fc3.delete()

        expect:
        def pagedIterable = fileSystemClient.listDeletedPaths();

        def iterableByPage = pagedIterable.iterableByPage(1)
        for (def page : iterableByPage) {
            assert page.value.size() == 1
        }
    }

    def "List deleted paths error"() {
        setup:
        fsc = softDeleteDataLakeServiceClient.getFileSystemClient(generateFileSystemName())

        when:
        fsc.listDeletedPaths().last()

        then:
        thrown(DataLakeStorageException)
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_08_04")
    def "List deleted paths path"() {
        setup:
        def dir = fileSystemClient.getDirectoryClient(generatePathName())
        dir.create()
        def fc1 = dir.getFileClient(generatePathName()) // Create one file under the path
        fc1.create(true)
        fc1.delete()

        def fc2 = fileSystemClient.getFileClient(generatePathName()) // Create another file not under the path
        fc2.create()
        fc2.delete()

        when:
        def deletedBlobs = fileSystemClient.listDeletedPaths(dir.getDirectoryName(), null, null)

        then:
        deletedBlobs.size() == 1
        !deletedBlobs.first().isPrefix()
        deletedBlobs.first().getPath() == dir.getDirectoryName() + "/" + fc1.getFileName()
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_08_04")
    // TODO (gapra): Add more get paths tests (Github issue created)
    def "List deleted paths"() {
        setup:
        def fc1 = fileSystemClient.getFileClient(generatePathName())
        fc1.create(true)
        fc1.delete()

        when:
        List<PathDeletedItem> deletedBlobs = fileSystemClient.listDeletedPaths().stream().collect(Collectors.toList())

        then:
        deletedBlobs.size() == 1
        !deletedBlobs.get(0).isPrefix()
        deletedBlobs.get(0).getPath() == fc1.getFileName()
        deletedBlobs.get(0).getDeletedOn() != null
        deletedBlobs.get(0).getDeletionId() != null
        deletedBlobs.get(0).getRemainingRetentionDays() != null
    }
}
