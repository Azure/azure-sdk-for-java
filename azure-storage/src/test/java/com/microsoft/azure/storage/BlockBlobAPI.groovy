package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.BlockBlobURL
import com.microsoft.azure.storage.blob.models.BlockListType
import io.reactivex.Flowable

class BlockBlobAPI extends APISpec{
    BlockBlobURL bu

    def setup(){
        bu = cu.createBlockBlobURL(generateBlobName())
        bu.upload(Flowable.just(defaultData), defaultText.length(), null, null, null)
                .blockingGet()
    }

    def "Block blob stage block"() {
        expect:
        bu.stageBlock(new String(Base64.encoder.encode("0000".bytes)), Flowable.just(defaultData),
                defaultData.remaining(), null).blockingGet().statusCode() == 201
    }

    def "Block blob commit block list"(){
        setup:
        String blockID = new String(Base64.encoder.encode("0000".bytes))
        bu.stageBlock(blockID, Flowable.just(defaultData), defaultData.remaining(), null).blockingGet()
        ArrayList<String> ids = new ArrayList<>()
        ids.add(blockID)

        expect:
        bu.commitBlockList(ids, null, null, null).blockingGet().statusCode() == 201
    }

    def "Block blob get block list"() {
        setup:
        String blockID = new String(Base64.encoder.encode("0000".bytes))
        bu.stageBlock(blockID, Flowable.just(defaultData), defaultData.remaining(), null).blockingGet()
        ArrayList<String> ids = new ArrayList<>()
        ids.add(blockID)

        expect:
        bu.getBlockList(BlockListType.ALL, null).blockingGet()
                .body().uncommittedBlocks().get(0).name().equals(blockID)
    }

    def "Block blob upload"() {
        expect:
        bu.upload(Flowable.just(defaultData), defaultData.remaining(),
                null, null, null).blockingGet().statusCode() == 201
    }
}
