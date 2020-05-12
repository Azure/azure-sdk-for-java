package com.azure.storage.blob.changefeed

import reactor.test.StepVerifier

class ChangefeedNetworkTest extends APISpec {

    def "cf"() {
        when:
        def sv = StepVerifier.create(
            new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
                .buildAsyncClient().getEvents()
        )
        then:
        sv.expectNextCount(2000)
            .verifyComplete()
    }

    def "real cf paged 50"() {
        setup:
        BlobChangefeedPagedFlux pagedFlux = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildAsyncClient().getEvents()

        when:
        def sv = StepVerifier.create(
            pagedFlux.byPage(50)
        )
        then:
        sv.expectNextCount(34).verifyComplete()
    }

    def "real cf actually paged"() {
        setup:

        BlobChangefeedAsyncClient client = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildAsyncClient()

        BlobChangefeedPagedFlux pagedFlux = client.getEvents("{\"endTime\":\"+999999999-12-31T23:59:59.999999999-18:00\",\"segmentTime\":\"2020-03-23T22:00Z\",\"shardCursors\":{\"log/00/2020/03/23/2200/\":{\"chunkPath\":\"log/00/2020/03/23/2200/00000.avro\",\"blockOffset\":199372,\"objectBlockIndex\":28}},\"shardPath\":\"log/00/2020/03/23/2200/\"}")
        def sv = StepVerifier.create(
            pagedFlux
        )

        sv.expectNextCount(1152).verifyComplete()
    }
}
