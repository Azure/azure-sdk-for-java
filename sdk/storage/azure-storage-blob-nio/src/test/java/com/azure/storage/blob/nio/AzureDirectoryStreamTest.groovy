// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio

import spock.lang.Unroll

import java.nio.file.DirectoryIteratorException
import java.nio.file.Path

class AzureDirectoryStreamTest extends APISpec {
    AzureFileSystem fs

    def setup() {
        fs = createFS(initializeConfigMap())
    }

    @Unroll
    def "List files"() {
        setup:
        if (numFiles > 50 && !liveMode()) {
            return // Skip large data set in record and playback
        }
        def rootName = absolute ? getNonDefaultRootDir(fs) : ""
        def dirName = generateBlobName()
        List<AzureResource> resources = []
        for (int i = 0; i < numFiles; i++) {
            resources.push(new AzureResource(fs.getPath(rootName, dirName, generateBlobName())))
            resources[0].getBlobClient().getBlockBlobClient().commitBlockList(Collections.emptyList())
        }

        when:
        def it = new AzureDirectoryStream((AzurePath) fs.getPath(rootName, dirName), { path -> true }).iterator()

        then:
        if (numFiles > 0) {
            it.hasNext()
            it.hasNext() // Check that repeated hasNext calls returns true and doesn't affect the results of next()
        }
        for (int i = 0; i < numFiles; i++) {
            assert it.hasNext()
            def path = it.next()
            def found = false
            for (AzureResource resource : resources) {
                if (resource.getPath() == path) {
                    found = true
                    resources.remove(resource)
                    break
                }
            }
            assert found
        }
        !it.hasNext()

        when: "Iterating beyond the end throws"
        it.next()

        then:
        thrown(NoSuchElementException)

        where:
        numFiles | absolute | _
        0        | true     | _ // empty iterator
        5        | true     | _ // small number of files
        6000     | true     | _ // requires internally following continuation token. Live only
        5        | false    | _ // Tests listing relative paths goes to default dir
    }

    // If listing results include directories, they should not be recursively listed. Only immediate children are
    // returned.
    @Unroll
    def "List directories"() {
        setup:
        def rootName = getNonDefaultRootDir(fs)
        // The path to list against
        def listResource = new AzureResource(fs.getPath(rootName, generateBlobName()))
        // The only expected result of the listing
        def listResultResource = new AzureResource(listResource.getPath().resolve(generateBlobName()))
        if (!virtual) {
            listResource.putDirectoryBlob(null)
            listResultResource.putDirectoryBlob(null)
        }

        // Put some children under listResultResource. These should not be returned.
        if (!isEmpty) {
            for (int i = 0; i < 3; i++) {
                ((AzurePath) listResultResource.getPath().resolve(generateBlobName())).toBlobClient().getBlockBlobClient()
                    .commitBlockList(Collections.emptyList())
            }
        }

        when:
        def it = new AzureDirectoryStream(listResource.getPath(), { path -> true }).iterator()

        then:
        it.hasNext()
        it.next().toString() == listResultResource.getPath().toString()
        !it.hasNext() // Listing should not be recursive

        where:
        virtual | isEmpty
        true    | false // Can't have empty virtual directory
        false   | false
        false   | true
    }

    @Unroll
    def "List files depth"() {
        setup:
        def rootName = getNonDefaultRootDir(fs)
        def listingPath = (AzurePath) fs.getPath(rootName, getPathWithDepth(depth))

        def filePath = new AzureResource(listingPath.resolve(generateBlobName()))
        filePath.getBlobClient().getBlockBlobClient().commitBlockList(Collections.emptyList())

        def concreteDirEmptyPath = new AzureResource(listingPath.resolve(generateBlobName()))
        concreteDirEmptyPath.putDirectoryBlob(null)

        def concreteDirNonEmptyPath = new AzureResource(listingPath.resolve(generateBlobName()))
        concreteDirNonEmptyPath.putDirectoryBlob(null)

        def concreteDirChildPath = new AzureResource(concreteDirNonEmptyPath.getPath().resolve(generateBlobName()))
        concreteDirChildPath.getBlobClient().getBlockBlobClient().commitBlockList(Collections.emptyList())

        def virtualDirPath = new AzureResource(listingPath.resolve(generateBlobName()))
        def virtualDirChildPath = new AzureResource(virtualDirPath.getPath().resolve(generateBlobName()))
        virtualDirChildPath.getBlobClient().getBlockBlobClient().commitBlockList(Collections.emptyList())

        def expectedListResults = [filePath.getPath().toString(), concreteDirEmptyPath.getPath().toString(),
                                   concreteDirNonEmptyPath.getPath().toString(), virtualDirPath.getPath().toString()]

        when:
        def it = new AzureDirectoryStream(listingPath, {path -> true}).iterator()

        then:
        while(it.hasNext()) {
            def next = it.next()
            assert expectedListResults.contains(next.toString())
            expectedListResults.remove(next.toString())
        }
        assert expectedListResults.size() == 0

        where:
        depth | _
        0     | _ // root
        1     | _
        3     | _
    }

    def "Iterator duplicate calls fail"() {
        setup:
        def stream = new AzureDirectoryStream((AzurePath) fs.getPath(generateBlobName()), { path -> true })
        stream.iterator()

        when:
        stream.iterator()

        then:
        thrown(IllegalStateException)
    }

    def "Next hasNext fail after close"() {
        setup:
        def rootName = getNonDefaultRootDir(fs)
        def dirName = generateBlobName()
        List<AzureResource> resources = []
        for (int i = 0; i < 3; i++) {
            resources.push(new AzureResource(fs.getPath(rootName, dirName, generateBlobName())))
            resources[0].getBlobClient().getBlockBlobClient().commitBlockList(Collections.emptyList())
        }
        def stream = new AzureDirectoryStream((AzurePath) fs.getPath(rootName, dirName), { path -> true })
        def it = stream.iterator()

        // There are definitely items we haven't returned from the iterator, but they are inaccessible after closing.
        when:
        stream.close()

        then:
        !it.hasNext()

        when:
        it.next()

        then:
        thrown(NoSuchElementException)
    }

    def "Has next fail after fs close"() {
        setup:
        def path = fs.getPath(generateBlobName())
        putDirectoryBlob(rootNameToContainerClient(getDefaultDir(fs)).getBlobClient(path.getFileName().toString())
            .getBlockBlobClient())
        def stream = fs.provider().newDirectoryStream(path, null)

        when:
        fs.close()
        stream.iterator().hasNext()

        then:
        def e = thrown(DirectoryIteratorException)
        e.getCause().getClass() == IOException.class
    }

    def "Filter"() {
        setup:
        def rootName = getNonDefaultRootDir(fs)
        def dirName = generateBlobName()
        List<AzureResource> resources = []
        for (int i = 0; i < 3; i++) {
            resources.push(new AzureResource(fs.getPath(rootName, dirName, i + generateBlobName())))
            resources[0].getBlobClient().getBlockBlobClient().commitBlockList(Collections.emptyList())
        }
        def stream = new AzureDirectoryStream((AzurePath) fs.getPath(rootName, dirName),
            { Path path -> path.getFileName().toString().startsWith('0') })

        when:
        def it = stream.iterator()

        then:
        it.hasNext()

        when:
        def path = it.next()

        then:
        path.getFileName().toString().startsWith('0')
        !it.hasNext()
    }

    def "Filter exception"() {
        setup:
        def rootName = getNonDefaultRootDir(fs)
        def dirName = generateBlobName()
        List<AzureResource> resources = []
        for (int i = 0; i < 3; i++) {
            resources.push(new AzureResource(fs.getPath(rootName, dirName, i + generateBlobName())))
            resources[0].getBlobClient().getBlockBlobClient().commitBlockList(Collections.emptyList())
        }
        def stream = new AzureDirectoryStream((AzurePath) fs.getPath(rootName, dirName),
            { throw new IOException("Test exception") })

        when:
        stream.iterator().hasNext()

        then:
        def e = thrown(DirectoryIteratorException)
        e.getCause().getMessage() == "Test exception"
    }
}
