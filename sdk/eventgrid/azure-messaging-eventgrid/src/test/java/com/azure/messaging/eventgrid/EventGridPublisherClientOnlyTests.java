// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import com.azure.core.credential.AzureKeyCredential;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventGridPublisherClientOnlyTests {
    // Event Grid endpoint for a topic accepting EventGrid schema events
    private static final String EVENTGRID_ENDPOINT = "AZURE_EVENTGRID_EVENT_ENDPOINT";

    // Event Grid access key for a topic accepting EventGrid schema events
    private static final String EVENTGRID_KEY = "AZURE_EVENTGRID_EVENT_KEY";

    @Test
    public void testGenerateSas() throws UnsupportedEncodingException {
        OffsetDateTime time = OffsetDateTime.of(2021, 3, 3, 16, 48, 0, 0, ZoneOffset.UTC);

        String endpoint = System.getenv(EVENTGRID_ENDPOINT);
        String sasToken1 = EventGridPublisherAsyncClient.generateSas(
            endpoint, new AzureKeyCredential(System.getenv(EVENTGRID_KEY)), time);

        String sasToken2 = EventGridPublisherClient.generateSas(
            System.getenv(EVENTGRID_ENDPOINT), new AzureKeyCredential(System.getenv(EVENTGRID_KEY)), time);

        assertEquals(sasToken1, sasToken2);

        String resKey = "r";
        String expKey = "e";

        Charset charset = StandardCharsets.UTF_8;
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M/d/yyyy h:m:s a");
        String fullEndpoint = String.format("%s?%s=%s", endpoint, "api-version",
            EventGridServiceVersion.getLatest().getVersion());
        String encodedResource = URLEncoder.encode(fullEndpoint, charset.name());
        String encodedExpiration = URLEncoder.encode(time.atZoneSameInstant(ZoneOffset.UTC).format(
            dateTimeFormatter),
            charset.name());
        String unsignedSas = String.format("%s=%s&%s=%s", resKey, encodedResource, expKey, encodedExpiration);

        assertTrue(sasToken1.contains(unsignedSas));
    }
}
