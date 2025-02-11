// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.administration;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.json.models.JsonObject;
import com.azure.json.models.JsonString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

public class MetadataPolicyClientTests extends PurviewAccountClientTestBase {
    private MetadataPolicyClient client;

    @Override
    protected void beforeTest() {
        client = purviewMetadataClientBuilderSetUp().buildClient();
    }

    @Test
    public void testListPolicy() {
        List<BinaryData> response = client.listAll(null).stream().collect(Collectors.toList());
        System.out.println(response);
        Assertions.assertFalse(response.isEmpty());
    }

    @Test
    public void testUpdatePolicy() {
        List<BinaryData> listResponse = client.listAll(null).stream().collect(Collectors.toList());
        BinaryData item = listResponse.iterator().next();

        JsonObject policy = item.toObject(JsonObject.class);
        String policyId = ((JsonString) policy.getProperty("id")).getValue();

        RequestOptions requestOptions
            = new RequestOptions().addHeader(HttpHeaderName.CONTENT_TYPE, "application/json").setBody(item);

        BinaryData response = client.updateWithResponse(policyId, requestOptions).getValue();
        System.out.println(response);
    }
}
