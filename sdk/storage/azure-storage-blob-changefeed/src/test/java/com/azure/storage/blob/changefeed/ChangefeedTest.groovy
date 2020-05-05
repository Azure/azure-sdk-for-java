package com.azure.storage.blob.changefeed

import reactor.test.StepVerifier

class ChangefeedTest extends APISpec {

    def "cf"() {
//        setup:
//        primaryBlobServiceClient.getBlobContainerClient(BlobChangefeedAsyncClient.CHANGEFEED_CONTAINER_NAME)
//        .getBlobClient("log/00/2020/03/28/0500/00000.avro")
//        .downloadToFile("C:\\Users\\gapra\\Desktop\\changefeed_small.avro")
        when:
        def sv = StepVerifier.create(
            new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
            .buildAsyncClient().getEvents()
        )
        then:
        sv.expectNextCount(1652)
            .verifyComplete()
    }
}
