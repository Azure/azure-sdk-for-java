package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

public abstract class QueryTestBase extends DigitalTwinsTestBase {
    @Test
    public abstract void validQuerySucceeds(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws JsonProcessingException;
}
