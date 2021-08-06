package com.azure.analytics.purview.catalog;

import com.azure.identity.DefaultAzureCredentialBuilder;

public class ReadmeSamples {
    public static void main(String[] args) {
        GlossaryClient client = new PurviewCatalogClientBuilder()
            .endpoint(System.getenv("ATLAS_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildGlossaryClient();
    }
}
