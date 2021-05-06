package com.azure.purview.catalog;

import com.azure.analytics.purview.catalog.GlossaryBaseClient;
import com.azure.analytics.purview.catalog.PurviewCatalogServiceRestAPIDocumentBuilder;
import com.azure.core.experimental.http.DynamicResponse;
import com.azure.identity.EnvironmentCredentialBuilder;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.stream.Collectors;

public class GlossaryBaseClientTests {
    @Test
    public void testGetGlossaries() {
        GlossaryBaseClient client = new PurviewCatalogServiceRestAPIDocumentBuilder()
            .endpoint(System.getenv("ATLAS_ENDPOINT"))
            .credential(new EnvironmentCredentialBuilder().build())
            .buildGlossaryBaseClient();

        DynamicResponse res = client.getGlossaries().send();
        if (res.getStatusCode() / 100 != 2) {
            System.out.println("Error code" + res.getStatusCode() + ": " + res.getBody());
        } else {
            JsonReader jsonReader = Json.createReader(new StringReader(res.getBody().toString()));
            JsonArray glossaries = jsonReader.readArray();
            glossaries.forEach(value -> {
                JsonObject glossary = value.asJsonObject();
                System.out.println(glossary.getString("name"));
                System.out.print("Terms: [");
                System.out.print(glossary.getJsonArray("terms").stream()
                    .map(value2 -> value2.asJsonObject().getString("displayText"))
                    .collect(Collectors.joining(", ")));
                System.out.println("]");
            });
        }
    }
}
