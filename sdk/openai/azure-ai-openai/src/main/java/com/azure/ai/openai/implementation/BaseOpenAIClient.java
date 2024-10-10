package com.azure.ai.openai.implementation;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.serializer.SerializerAdapter;

public interface BaseOpenAIClient {
    String getEndpoint();
    HttpPipeline getHttpPipeline();
    SerializerAdapter getSerializerAdapter();
}
