package com.azure.storage.blob.changefeed

class ChangefeedTest extends APISpec {

    def "Initialization"() {
        setup:
        def cfClient = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient('$blobchangefeed')

        def cf = new Changefeed(cfClient)

        cf.getNextYear().block()
        cf.getNextSegment().block()
    }
}
