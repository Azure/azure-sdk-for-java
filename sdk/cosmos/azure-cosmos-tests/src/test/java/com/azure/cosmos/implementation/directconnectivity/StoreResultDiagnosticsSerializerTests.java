// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.UUID;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static com.azure.cosmos.implementation.TestUtils.mockDocumentServiceRequest;
import static org.testng.Assert.fail;

public class StoreResultDiagnosticsSerializerTests {
    private final StoreResultDiagnostics.StoreResultDiagnosticsSerializer serializer;
    private final JsonGenerator jsonGenerator;
    private final SerializerProvider serializerProvider;

    public StoreResultDiagnosticsSerializerTests() throws IOException {
        this.serializer = new StoreResultDiagnostics.StoreResultDiagnosticsSerializer();
        Writer jsonWriter = new StringWriter();
        this.jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
        this.jsonGenerator.setCodec(new ObjectMapper());
        this.serializerProvider = new ObjectMapper().getSerializerProvider();
    }

    //TODO: add more test cases
    @Test(groups = "unit")
    public void storeResultDiagnosticsSerializerTests() throws Exception {
        StoreResponse storeResponse = new StoreResponse(null, 200, new HashMap<>(), null, 0, null);
        StoreResult storeResult = new StoreResult(
                storeResponse,
                null,
                "1",
                1,
                1,
                1.0,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                4,
                2,
                true,
                null,
                1,
                1,
                1,
                null,
                0.3,
                90.0);

        DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();
        RxDocumentServiceRequest request = mockDocumentServiceRequest(clientContext);
        StoreResultDiagnostics storeResultDiagnostics = StoreResultDiagnostics.createStoreResultDiagnostics(storeResult, request);

        try {
            this.serializer.serialize(storeResultDiagnostics, this.jsonGenerator, this.serializerProvider);
        } catch (IOException e) {
            fail("Should serialize successfully");
        }
    }
}
