// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch

import com.azure.storage.blob.batch.options.BlobBatchSetBlobAccessTierOptions
import com.azure.storage.blob.models.AccessTier
import spock.lang.Specification

class OptionsBagTest extends Specification {

    def "SetAccessTier constructor"() {
        setup:
        def url = "https://account.blob.core.windows.net/container/blob"
        def container = "container"
        def blob = "blob"

        when:
        def options1 = new BlobBatchSetBlobAccessTierOptions(url, AccessTier.ARCHIVE)
        def options2 = new BlobBatchSetBlobAccessTierOptions(container, blob, AccessTier.ARCHIVE)

        then:
        options1.getBlobUrl() == url
        options2.getBlobUrl() == url

        options1.getBlobName() == blob
        options2.getBlobName() == blob

        options1.getBlobContainerName() == container
        options2.getBlobContainerName() == container

        options1.getBlobIdentifier() == "container/blob"
        options2.getBlobIdentifier() == "container/blob"
    }

    def "SetAccessTier snapshot"() {
        setup:
        def url = "https://account.blob.core.windows.net/container/blob?snapshot=snapshot"
        def container = "container"
        def blob = "blob"
        def snapshot = "snapshot"

        when:
        def options1 = new BlobBatchSetBlobAccessTierOptions(url, AccessTier.ARCHIVE)
        def options2 = new BlobBatchSetBlobAccessTierOptions(container, blob, AccessTier.ARCHIVE)
            .setSnapshot(snapshot)

        then:
        options1.getBlobUrl() == url
        options2.getBlobUrl() == url

        options1.getBlobName() == blob
        options2.getBlobName() == blob

        options1.getBlobContainerName() == container
        options2.getBlobContainerName() == container

        options1.getSnapshot() == snapshot
        options2.getSnapshot() == snapshot

        options1.getBlobIdentifier() == "container/blob?snapshot=snapshot"
        options2.getBlobIdentifier() == "container/blob?snapshot=snapshot"
    }

    def "SetAccessTier version"() {
        setup:
        def url = "https://account.blob.core.windows.net/container/blob?versionid=version"
        def container = "container"
        def blob = "blob"
        def version = "version"

        when:
        def options1 = new BlobBatchSetBlobAccessTierOptions(url, AccessTier.ARCHIVE)
        def options2 = new BlobBatchSetBlobAccessTierOptions(container, blob, AccessTier.ARCHIVE)
            .setVersionId(version)

        then:
        options1.getBlobUrl() == url
        options2.getBlobUrl() == url

        options1.getBlobName() == blob
        options2.getBlobName() == blob

        options1.getBlobContainerName() == container
        options2.getBlobContainerName() == container

        options1.getVersionId() == version
        options2.getVersionId() == version

        options1.getBlobIdentifier() == "container/blob?versionid=version"
        options2.getBlobIdentifier() == "container/blob?versionid=version"
    }

    def "SetAccessTier version snapshot error"() {
        setup:
        def url = "https://account.blob.core.windows.net/container/blob?versionid=version&snapshot=snapshot"
        def container = "container"
        def blob = "blob"
        def version = "version"
        def snapshot = "snapshot"
        def options1 = new BlobBatchSetBlobAccessTierOptions(url, AccessTier.ARCHIVE)
        def options2 = new BlobBatchSetBlobAccessTierOptions(container, blob, AccessTier.ARCHIVE)
            .setVersionId(version)
            .setSnapshot(snapshot)

        when:
        options1.getBlobIdentifier()

        then:
        thrown(IllegalArgumentException)

        when:
        options2.getBlobIdentifier()

        then:
        thrown(IllegalArgumentException)
    }

}
