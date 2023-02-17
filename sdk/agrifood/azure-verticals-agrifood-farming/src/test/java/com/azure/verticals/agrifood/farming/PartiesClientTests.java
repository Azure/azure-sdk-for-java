// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.verticals.agrifood.farming;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class PartiesClientTests extends TestBase {
    private final String defaultEndpoint = "https://REDACTED.farmbeats.azure.net";

    private PartiesAsyncClient createClient() {
        PartiesClientBuilder builder =
            new PartiesClientBuilder()
                .endpoint(Configuration.getGlobalConfiguration().get("FARMBEATS_ENDPOINT", defaultEndpoint))
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        if (getTestMode() == TestMode.PLAYBACK) {
            builder.httpClient(interceptorManager.getPlaybackClient())
                .credential(request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)));
        } else if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build());
        } else if (getTestMode() == TestMode.LIVE) {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }
        return builder.buildAsyncClient();
    }

    @Test
    public void testList() {
        PartiesAsyncClient client = createClient();
        Response<BinaryData> response = client.getWithResponse("contoso-party", new RequestOptions()).block();
        assertNotNull(response.getValue());
    }
}
