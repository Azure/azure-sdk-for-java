// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio

class AzureDirectoryStreamTest extends APISpec {
    def fs
    def setup() {
        fs = createFS(initializeConfigMap())
    }

    def "List files"() {
        setup:
        def rootName = getNonDefaultRootDir(fs)
        def dirName = generateBlobName()
        List<AzureResource> resources = []
        for (int i=0; i<3; i++) {
            resources.push(new AzureResource(fs.getPath(rootName, dirName, generateBlobName())))
            resources[0].getBlobClient().getBlockBlobClient().commitBlockList(Collections.emptyList())
        }

        when:
        def it = new AzureDirectoryStream(fs.getPath(rootName, dirName), {path -> true}).iterator()

        then:
        for (int i=0; i<3; i++) {
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
    }
    // Files
    // Directories
    // Root and relative
    // Require paging in pagedIterable
    // Directory children not returned
    // Various levels of depth
    // Test getting two iterators
    // Test iterating after closing
    // Test next() after end
    // Test hasNext reads ahead?
    // Test multiple hasNext calls in a row
    // Test multiple levels of depth
    // Test non default directory
    // Test empty
    // Test filter
    // Filter exception
}
