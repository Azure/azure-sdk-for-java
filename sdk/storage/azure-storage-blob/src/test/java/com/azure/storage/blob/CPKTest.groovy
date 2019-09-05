package com.azure.storage.blob

class CPKTest extends APISpec {
    ContainerClient cpkContainer

    def setup() {
        bc = cc.getBlockBlobClient(generateBlobName())
        bc.upload(defaultInputStream.get(), defaultDataSize)
    }
}
