package com.azure.ai.openai.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AzureCognitiveSearchChatExtensionConfiguration {

    @JsonProperty(value = "endpoint")
    private String endpoint;

    @JsonProperty(value = "key")
    private String key;

    @JsonProperty(value = "indexName")
    private String indexName;

    @JsonCreator
    public AzureCognitiveSearchChatExtensionConfiguration(
        @JsonProperty(value = "endpoint") String endpoint,
        @JsonProperty(value = "key") String key,
        @JsonProperty(value = "indexName") String indexName)
    {
        this.endpoint = endpoint;
        this.key = key;
        this.indexName = indexName;
    }
}
