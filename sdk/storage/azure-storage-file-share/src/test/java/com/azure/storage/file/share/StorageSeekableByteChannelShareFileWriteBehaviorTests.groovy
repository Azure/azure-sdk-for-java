// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.storage.common.implementation.Constants
import com.azure.storage.file.share.models.FileLastWrittenMode
import com.azure.storage.file.share.models.ShareFileProperties
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions
import com.azure.storage.file.share.models.ShareRequestConditions
import spock.lang.Unroll

class StorageSeekableByteChannelShareFileWriteBehaviorTests extends APISpec {
    @Unroll
    def "WriteBehavior write calls to client correctly"() {
        given:
        ShareFileClient client = Mock()
        def behavior = new StorageSeekableByteChannelShareFileWriteBehavior(client, conditions, lastWrittenMode)

        when: "WriteBehavior.write() called"
        behavior.write(getData().getDefaultData(), offset)

        then: "Expected ShareFileClient upload parameters given"
        1 * client.uploadRangeWithResponse(
            { ShareFileUploadRangeOptions options -> options.getOffset() == offset &&
                options.getRequestConditions() == conditions && options.getLastWrittenMode() == lastWrittenMode &&
                options.getDataStream().getBytes() == getData().getDefaultBytes() },
            null, null)

        where:
        offset | conditions                   | lastWrittenMode
        0      | null                         | null
        50     | null                         | null
        0      | new ShareRequestConditions() | null
        0      | null                         | FileLastWrittenMode.PRESERVE
    }

    def "WriteBehavior canSeek anywhere in file range"() {
        given:
        ShareFileClient client = Mock()
        def behavior = new StorageSeekableByteChannelShareFileWriteBehavior(client, null, null)

        when: "WriteBehavior.canSeek() is called"
        boolean result = behavior.canSeek(position)

        then: "Expected behavior"
        result == expected
        1 * client.getProperties() >> new ShareFileProperties(null, null, null, null, fileSize as Long, null, null,
            null, null, null, null, null, null, null, null, null, null, null)

        where:
        fileSize     | position         | expected
        Constants.KB | 0                | true
        Constants.KB | 500              | true
        Constants.KB | Constants.KB     | true
        Constants.KB | Constants.KB + 1 | false
        Constants.KB | -1               | false
    }

    def "WriteBehavior truncate unsupported"(){
        given:
        def behavior = new StorageSeekableByteChannelShareFileWriteBehavior(Mock(ShareFileClient), null, null)

        when:
        behavior.resize(10)

        then:
        thrown(UnsupportedOperationException)
    }
}
