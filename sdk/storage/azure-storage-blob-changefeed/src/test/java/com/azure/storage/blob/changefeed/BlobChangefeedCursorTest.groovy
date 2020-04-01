package com.azure.storage.blob.changefeed


import com.azure.storage.blob.changefeed.implementation.util.BlobChangefeedCursor

import java.time.OffsetDateTime

class BlobChangefeedCursorTest extends APISpec {

    def "test serialize"() {
        setup:
        BlobChangefeedCursor cursor =
            new BlobChangefeedCursor(OffsetDateTime.now().toString(), OffsetDateTime.now().toString(), "", "", 1)

        String serialized = cursor.serialize()

        System.out.println(serialized)

        BlobChangefeedCursor newCursor = BlobChangefeedCursor.deserialize(serialized)

        System.out.println(cursor.equals(newCursor))

    }

}
