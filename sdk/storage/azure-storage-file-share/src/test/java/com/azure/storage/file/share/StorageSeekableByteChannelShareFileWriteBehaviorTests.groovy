// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.storage.file.share.models.FileLastWrittenMode
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
}
