// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.catalog;

import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.List;
import java.util.Map;

public class ListGlossaries {
    public static void main(String[] args) {
        GlossaryClient client = new PurviewCatalogClientBuilder()
            .endpoint(System.getenv("ATLAS_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildGlossaryClient();
        BinaryData binaryData = client.listGlossaries(null);
        List<?> glossaries = binaryData.toObject(List.class);
        System.out.println(glossaries);
        Map<?, ?> map = (Map<?, ?>) glossaries.get(0);
        List<?> terms = (List<?>) map.get("terms");
        System.out.println(terms);
    }
}
