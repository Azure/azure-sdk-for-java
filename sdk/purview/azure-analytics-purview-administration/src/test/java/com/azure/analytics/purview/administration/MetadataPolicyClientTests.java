// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.administration;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

public class MetadataPolicyClientTests extends PurviewAccountClientTestBase {
    private MetadataPolicyClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> new PurviewMetadataClientBuilder()
            .endpoint(getEndpoint())
            .pipeline(httpPipeline)
            .buildMetadataPolicyClient());
    }

    @Test
    public void testListPolicy() {
        List<BinaryData> response = client.listAll(null, Context.NONE).stream().collect(Collectors.toList());
        System.out.println(response);
        Assertions.assertTrue(response.size() > 0);
    }

    @Test
    public void testUpdatePolicy() {
        List<BinaryData> listResponse = client.listAll(null, Context.NONE).stream().collect(Collectors.toList());
        BinaryData item = listResponse.iterator().next();

        JsonReader jsonReader = Json.createReader(new StringReader(item.toString()));
        JsonObject policy = jsonReader.readObject();
        String policyId = policy.getString("id");

        RequestOptions requestOptions = new RequestOptions()
            .addHeader("Content-Type", "application/json")
            .setBody(item);

        BinaryData response = client.updateWithResponse(policyId, requestOptions, Context.NONE).getValue();
        System.out.println(response);
    }
}
