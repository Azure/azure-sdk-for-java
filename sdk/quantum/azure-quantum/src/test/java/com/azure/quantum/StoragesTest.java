package com.azure.quantum;

import com.azure.core.http.rest.Response;
import com.azure.quantum.models.BlobDetails;
import com.azure.quantum.models.SasUriResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.azure.quantum.TestUtils.getClient;

public class StoragesTest {

    @Test
    public void sasUriWithResponseAsyncTest() {
        Storages storages = new Storages(getClient());
        BlobDetails details = new BlobDetails().setContainerName("containerName").setBlobName("blobName");
        Response<SasUriResponse> response = storages.sasUriWithResponseAsync(details).block();

        assertEquals(200, response.getStatusCode());
        assertEquals("sasUri", response.getValue().getSasUri());
    }

}
