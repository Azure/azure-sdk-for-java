package com.azure.v2.storage.blob;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ContainerClientTest {

    @Test
    @Disabled
    public void testContainterClient() {

        ContainerClient containerClient = new AzureBlobStorageBuilder()
                .url("sas-url")
                .buildContainerClient();

        containerClient.create("container", null, null, null, null, null);

        // This will not work yet as xml serialization is not supported in clientcore.
//        Response<ListBlobsHierarchySegmentResponse> testcontainer = containerClient.listBlobHierarchySegmentNoCustomHeadersWithResponse("testcontainer", null, null, null, null, null, null, null, null);
//        System.out.println(testcontainer.getStatusCode());
//        System.out.println(testcontainer.getValue().getSegment().getBlobItems().get(0));
    }
}
