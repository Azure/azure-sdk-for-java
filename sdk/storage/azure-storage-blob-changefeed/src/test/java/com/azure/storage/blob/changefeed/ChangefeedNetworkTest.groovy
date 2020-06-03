package com.azure.storage.blob.changefeed


import reactor.test.StepVerifier
import spock.lang.Ignore

class ChangefeedNetworkTest extends APISpec {

    @Ignore
    def "min"() {
        when:
        def sv = StepVerifier.create(
            new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
                .buildAsyncClient().getEvents()
        )
        then:
        sv.expectNextCount(472) /* Note this number should be adjusted to verify the number of events expected. */
            .verifyComplete()
    }

    @Ignore
    def "byPage size"() {
        setup:
        BlobChangefeedPagedFlux pagedFlux = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildAsyncClient().getEvents()

        when:
        def sv = StepVerifier.create(
            pagedFlux.byPage(50)
        )
        then:
        sv.expectNextCount(10).verifyComplete()
    }

    @Ignore
    def "byPage continuationToken"() {
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
