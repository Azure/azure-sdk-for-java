package com.azure.storage.blob.specialized

import com.azure.storage.blob.models.AccessTier
import com.azure.storage.blob.models.BlobHttpHeaders
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.options.BlockBlobCommitBlockListOptions
import com.azure.storage.blob.options.BlockBlobSeekableByteChannelWriteMode
import com.azure.storage.blob.options.BlockBlobStageBlockOptions
import com.azure.storage.common.test.shared.TestDataFactory
import spock.lang.Specification
import spock.lang.Unroll

class StorageSeekableByteChannelBlockBlobWriteBehaviorTests extends Specification {
    def data = TestDataFactory.getInstance()

    StorageSeekableByteChannelBlockBlobWriteBehavior getSimpleBehavior(BlockBlobClient client) {
       return new StorageSeekableByteChannelBlockBlobWriteBehavior(client, null, null, null, null, null,
           BlockBlobSeekableByteChannelWriteMode.OVERWRITE, null)
    }

    def testStringMap() {
        [foo: "bar", fizz: "buzz"] as Map<String, String>
    }

    @Unroll
    def "WriteBehavior write calls to client correctly"() {
        given:
        BlockBlobClient client = Mock()
        def behavior = new StorageSeekableByteChannelBlockBlobWriteBehavior(client, null, null, null, null, conditions,
            BlockBlobSeekableByteChannelWriteMode.OVERWRITE, null)

        when: "WriteBehavior.write() called"
        behavior.write(data.defaultData, offset)

        then: "Expected BlockBlobClient upload parameters given"
        1 * client.stageBlockWithResponse(
            { BlockBlobStageBlockOptions options -> options.getLeaseId() == (conditions == null ? null : "foo") },
            null, null)

        where:
        offset | conditions
        0      | null
        50     | null
        0      | new BlobRequestConditions().setLeaseId("foo")
    }

    @Unroll
    def "WriteBehavior commits with correct settings"() {
        given:
        BlockBlobClient client = Mock()
        def behavior = new StorageSeekableByteChannelBlockBlobWriteBehavior(client, headers, metadata, tags, tier,
            conditions, BlockBlobSeekableByteChannelWriteMode.OVERWRITE, null)

        when: "Three writes"
        behavior.write(data.defaultData, 0)
        behavior.write(data.defaultData, 0)
        behavior.write(data.defaultData, 0)

        and: "WriteBehavior.commit() called"
        behavior.commit(0)

        then: "Expected three writes and appropriate commit"
        3 * client.stageBlockWithResponse(_, _, _)
        1 * client.commitBlockListWithResponse(
            { BlockBlobCommitBlockListOptions options ->
                options.getHeaders() == headers &&
                options.getMetadata() == metadata &&
                options.getTags() == tags &&
                options.getTier() == tier &&
                options.getRequestConditions() == conditions &&
                options.getBase64BlockIds().size() == 3
            },
            null, null)

        where:
        headers               | metadata        | tags            | tier           | conditions
        null                  | null            | null            | null           | null
        new BlobHttpHeaders() | null            | null            | null           | null
        null                  | testStringMap() | null            | null           | null
        null                  | null            | testStringMap() | null           | null
        null                  | null            | null            | AccessTier.HOT | null
        null                  | null            | null            | null           | new BlobRequestConditions()
        null                  | null            | null            | null           | null
    }

    def "WriteBehavior commit overwrite"() {
        given: "existing blocks"
        def blocks = ["1", "2", "3", "4", "5"]

        and: "behavior set to overwrite"
        BlockBlobClient client = Mock()
        def behavior = new StorageSeekableByteChannelBlockBlobWriteBehavior(client, null, null, null, null, null,
            BlockBlobSeekableByteChannelWriteMode.OVERWRITE, blocks)

        when: "Three writes"
        behavior.write(data.defaultData, 0)
        behavior.write(data.defaultData, 0)
        behavior.write(data.defaultData, 0)

        and: "WriteBehavior.commit() called"
        behavior.commit(0)

        then: "Expected three writes and appropriate blocklist"
        3 * client.stageBlockWithResponse(_, _, _)
        1 * client.commitBlockListWithResponse(
            { BlockBlobCommitBlockListOptions options ->
                options.getBase64BlockIds().size() == 3 && options.getBase64BlockIds().intersect(blocks).isEmpty()
            },
            null, null)
    }

    def "WriteBehavior commit append"() {
        given: "existing blocks"
        def blocks = ["1", "2", "3", "4", "5"]

        and: "behavior set to append"
        BlockBlobClient client = Mock()
        def behavior = new StorageSeekableByteChannelBlockBlobWriteBehavior(client, null, null, null, null, null,
            BlockBlobSeekableByteChannelWriteMode.APPEND, blocks)

        when: "Three writes"
        behavior.write(data.defaultData, 0)
        behavior.write(data.defaultData, 0)
        behavior.write(data.defaultData, 0)

        and: "WriteBehavior.commit() called"
        behavior.commit(0)

        then: "Expected three writes and appropriate blocklist"
        3 * client.stageBlockWithResponse(_, _, _)
        1 * client.commitBlockListWithResponse(
            { BlockBlobCommitBlockListOptions options ->
                options.getBase64BlockIds().size() == blocks.size() + 3 && options.getBase64BlockIds()[0..blocks.size()-1] == blocks
            },
            null, null)
    }

    def "WriteBehavior commit prepend"() {
        given: "existing blocks"
        def blocks = ["1", "2", "3", "4", "5"]

        and: "behavior set to append"
        BlockBlobClient client = Mock()
        def behavior = new StorageSeekableByteChannelBlockBlobWriteBehavior(client, null, null, null, null, null,
            BlockBlobSeekableByteChannelWriteMode.PREPEND, blocks)

        when: "Three writes"
        behavior.write(data.defaultData, 0)
        behavior.write(data.defaultData, 0)
        behavior.write(data.defaultData, 0)

        and: "WriteBehavior.commit() called"
        behavior.commit(0)

        then: "Expected three writes and appropriate blocklist"
        3 * client.stageBlockWithResponse(_, _, _)
        1 * client.commitBlockListWithResponse(
            { BlockBlobCommitBlockListOptions options ->
                options.getBase64BlockIds().size() == blocks.size() + 3 && options.getBase64BlockIds()[3..-1] == blocks
            },
            null, null)
    }

    def "WriteBehavior Seek unsupported"(){
        given:
        def behavior = getSimpleBehavior(Mock(BlockBlobClient))

        when:
        behavior.assertCanSeek(10)

        then:
        thrown(UnsupportedOperationException)
    }

    def "WriteBehavior truncate unsupported"(){
        given:
        def behavior = getSimpleBehavior(Mock(BlockBlobClient))

        when:
        behavior.resize(10)

        then:
        thrown(UnsupportedOperationException)
    }
}
