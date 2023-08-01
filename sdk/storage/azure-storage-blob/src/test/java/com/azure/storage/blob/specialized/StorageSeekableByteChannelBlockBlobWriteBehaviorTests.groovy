package com.azure.storage.blob.specialized

import com.azure.storage.blob.models.AccessTier
import com.azure.storage.blob.models.BlobHttpHeaders
import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.options.BlockBlobCommitBlockListOptions

import com.azure.storage.blob.options.BlockBlobStageBlockOptions
import com.azure.storage.common.test.shared.TestDataFactory
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import spock.lang.Specification
import spock.lang.Unroll

class StorageSeekableByteChannelBlockBlobWriteBehaviorTests extends Specification {
    def data = TestDataFactory.getInstance()

    StorageSeekableByteChannelBlockBlobWriteBehavior getSimpleBehavior(BlockBlobClient client) {
       return new StorageSeekableByteChannelBlockBlobWriteBehavior(client, null, null, null, null, null,
           StorageSeekableByteChannelBlockBlobWriteBehavior.WriteMode.OVERWRITE, null)
    }

    def testStringMap() {
        [foo: "bar", fizz: "buzz"] as Map<String, String>
    }

    @Unroll
    def "WriteBehavior write calls to client correctly"() {
        given: "BlockBlobClient"
        def client = Mockito.mock(BlockBlobClient.class)

        and: "Block blob behavior"
        def behavior = new StorageSeekableByteChannelBlockBlobWriteBehavior(client, null, null, null, null, conditions,
            StorageSeekableByteChannelBlockBlobWriteBehavior.WriteMode.OVERWRITE, null)

        when: "WriteBehavior.write() called"
        behavior.write(data.defaultData, offset)

        and: "mockito verification capture"
        def optionsCaptor = ArgumentCaptor.forClass(BlockBlobStageBlockOptions)
        Mockito.verify(client, Mockito.times(1)).stageBlockWithResponse(optionsCaptor.capture(), Mockito.isNull(), Mockito.isNull())

        then: "Expected BlockBlobClient upload parameters given"
        optionsCaptor.getValue().getLeaseId() == (conditions == null ? null : "foo")

        where:
        offset | conditions
        0      | null
        50     | null
        0      | new BlobRequestConditions().setLeaseId("foo")
    }

    @Unroll
    def "WriteBehavior commits with correct settings"() {
        given:
        def client = Mockito.mock(BlockBlobClient.class)
        def behavior = new StorageSeekableByteChannelBlockBlobWriteBehavior(client, headers, metadata, tags, tier,
            conditions, StorageSeekableByteChannelBlockBlobWriteBehavior.WriteMode.OVERWRITE, null)

        when: "Three writes"
        behavior.write(data.defaultData, 0)
        behavior.write(data.defaultData, 0)
        behavior.write(data.defaultData, 0)

        and: "WriteBehavior.commit() called"
        behavior.commit(0)

        and: "mockito verification capture"
        def optionsCaptor = ArgumentCaptor.forClass(BlockBlobCommitBlockListOptions)
        Mockito.verify(client, Mockito.times(3)).stageBlockWithResponse(Mockito.any(), Mockito.any(), Mockito.any())
        Mockito.verify(client, Mockito.times(1)).commitBlockListWithResponse(optionsCaptor.capture(), Mockito.isNull(), Mockito.isNull())

        then: "Expected commit options"
        optionsCaptor.getValue().getHeaders() == headers
        optionsCaptor.getValue().getMetadata() == metadata
        optionsCaptor.getValue().getTags() == tags
        optionsCaptor.getValue().getTier() == tier
        optionsCaptor.getValue().getRequestConditions() == conditions
        optionsCaptor.getValue().getBase64BlockIds().size() == 3

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
        def client = Mockito.mock(BlockBlobClient.class)
        def behavior = new StorageSeekableByteChannelBlockBlobWriteBehavior(client, null, null, null, null, null,
            StorageSeekableByteChannelBlockBlobWriteBehavior.WriteMode.OVERWRITE, blocks)

        when: "Three writes"
        behavior.write(data.defaultData, 0)
        behavior.write(data.defaultData, 0)
        behavior.write(data.defaultData, 0)

        and: "WriteBehavior.commit() called"
        behavior.commit(0)

        and: "mockito verification capture"
        def optionsCaptor = ArgumentCaptor.forClass(BlockBlobCommitBlockListOptions)
        Mockito.verify(client, Mockito.times(3)).stageBlockWithResponse(Mockito.any(), Mockito.any(), Mockito.any())
        Mockito.verify(client, Mockito.times(1)).commitBlockListWithResponse(optionsCaptor.capture(), Mockito.isNull(), Mockito.isNull())

        then: "Expected three writes and appropriate blocklist"
        optionsCaptor.getValue().getBase64BlockIds().size() == 3
        optionsCaptor.getValue().getBase64BlockIds().intersect(blocks).isEmpty()
    }

    def "WriteBehavior commit append"() {
        given: "existing blocks"
        def blocks = ["1", "2", "3", "4", "5"]

        and: "behavior set to append"
        def client = Mockito.mock(BlockBlobClient.class)
        def behavior = new StorageSeekableByteChannelBlockBlobWriteBehavior(client, null, null, null, null, null,
            StorageSeekableByteChannelBlockBlobWriteBehavior.WriteMode.APPEND, blocks)

        when: "Three writes"
        behavior.write(data.defaultData, 0)
        behavior.write(data.defaultData, 0)
        behavior.write(data.defaultData, 0)

        and: "WriteBehavior.commit() called"
        behavior.commit(0)

        and: "mockito verification capture"
        def optionsCaptor = ArgumentCaptor.forClass(BlockBlobCommitBlockListOptions)
        Mockito.verify(client, Mockito.times(3)).stageBlockWithResponse(Mockito.any(), Mockito.any(), Mockito.any())
        Mockito.verify(client, Mockito.times(1)).commitBlockListWithResponse(optionsCaptor.capture(), Mockito.isNull(), Mockito.isNull())

        then: "Expected three writes and appropriate blocklist"
        optionsCaptor.getValue().getBase64BlockIds().size() == blocks.size() + 3
        optionsCaptor.getValue().getBase64BlockIds()[0..blocks.size()-1] == blocks
    }

    def "WriteBehavior commit prepend"() {
        given: "existing blocks"
        def blocks = ["1", "2", "3", "4", "5"]

        and: "behavior set to prepend"
        def client = Mockito.mock(BlockBlobClient.class)
        def behavior = new StorageSeekableByteChannelBlockBlobWriteBehavior(client, null, null, null, null, null,
            StorageSeekableByteChannelBlockBlobWriteBehavior.WriteMode.PREPEND, blocks)

        when: "Three writes"
        behavior.write(data.defaultData, 0)
        behavior.write(data.defaultData, 0)
        behavior.write(data.defaultData, 0)

        and: "WriteBehavior.commit() called"
        behavior.commit(0)

        and: "mockito verification capture"
        def optionsCaptor = ArgumentCaptor.forClass(BlockBlobCommitBlockListOptions)
        Mockito.verify(client, Mockito.times(3)).stageBlockWithResponse(Mockito.any(), Mockito.any(), Mockito.any())
        Mockito.verify(client, Mockito.times(1)).commitBlockListWithResponse(optionsCaptor.capture(), Mockito.isNull(), Mockito.isNull())

        then: "Expected three writes and appropriate blocklist"
        optionsCaptor.getValue().getBase64BlockIds().size() == blocks.size() + 3
        optionsCaptor.getValue().getBase64BlockIds()[3..-1] == blocks
    }

    def "WriteBehavior Seek unsupported"(){
        given:
        def behavior = getSimpleBehavior(Mockito.mock(BlockBlobClient.class))

        when:
        behavior.assertCanSeek(10)

        then:
        thrown(UnsupportedOperationException)
    }

    def "WriteBehavior truncate unsupported"(){
        given:
        def behavior = getSimpleBehavior(Mockito.mock(BlockBlobClient.class))

        when:
        behavior.resize(10)

        then:
        thrown(UnsupportedOperationException)
    }
}
