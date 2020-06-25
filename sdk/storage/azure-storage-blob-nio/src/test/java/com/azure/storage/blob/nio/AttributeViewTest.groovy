package com.azure.storage.blob.nio

import com.azure.storage.blob.BlobClient;

class AttributeViewTest extends APISpec {
    /*
    Get attribute view--all three class types work; invalid class types
    Get attributes--all three class types work. All properties set; invalid class types
    Get attributes string--all valid attributes; invalid attributes; *
    File, directory, virtual directory, no exist
    Set attributes string--all valid attributes; invalid attributes
    Set attributes view
     */
    BlobClient bc

    def setup() {
        cc.create()
        bc = cc.getBlobClient(generateBlobName())
        bc.upload(defaultInputStream.get(), defaultDataSize)
    }

    def cleanup() {
        cc.delete()
    }

    def "AzureBlobFileAttributeView setBlobHttpHeaders"() {

    }

    def "AzureBlobFileAttributeView setMetadata"() {

    }

    def "AzureBlobFileAttributeView setTier"() {

    }
}
