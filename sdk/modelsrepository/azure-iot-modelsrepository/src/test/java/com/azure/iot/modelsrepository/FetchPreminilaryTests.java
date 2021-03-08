package com.azure.iot.modelsrepository;

import org.junit.jupiter.api.Test;

import java.util.Map;

public class FetchPreminilaryTests {

    @Test
    public void dtmiToPathTest() {
        ModelsRepositoryAsyncClient client = new ModelsRepositoryClientBuilder().repositoryEndpoint("https://devicemodels.azure.com").buildAsyncClient();

        Map<String, String> map = client.GetModels("dtmi:com:example:Thermostat;1").block();
        System.out.println(map.values());
    }
}
