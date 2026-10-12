// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketClientConfiguration;
import com.azure.ai.agents.implementation.realtime.VoiceAgentWebSocketUtils;
import com.azure.ai.agents.models.VoiceAgentTransport;
import com.azure.ai.agents.models.VoiceAgentWebSocketConnectionOptions;
import com.azure.core.http.ProxyOptions;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BetaVoiceAgentWebSocketSessionClientTests {
    @Test
    public void connectionUriUsesSelectedTransport() {
        VoiceAgentWebSocketClientConfiguration configuration = new VoiceAgentWebSocketClientConfiguration(
            URI.create("https://localhost"),
            request -> Mono.just(new com.azure.core.credential.AccessToken("token", OffsetDateTime.now().plusHours(1))),
            "v1", "test-user-agent", null, null);

        URI defaultUri = VoiceAgentWebSocketUtils.buildWebSocketUri(configuration, "agent",
            new VoiceAgentWebSocketConnectionOptions());
        URI websocketUri = VoiceAgentWebSocketUtils.buildWebSocketUri(configuration, "agent",
            new VoiceAgentWebSocketConnectionOptions().setTransport(VoiceAgentTransport.WEBSOCKET));
        URI webRtcUri = VoiceAgentWebSocketUtils.buildWebSocketUri(configuration, "agent",
            new VoiceAgentWebSocketConnectionOptions().setTransport(VoiceAgentTransport.WEBRTC));

        assertEquals("websocket", queryParameter(defaultUri, "transport"));
        assertEquals("websocket", queryParameter(websocketUri, "transport"));
        assertEquals("webrtc", queryParameter(webRtcUri, "transport"));
    }

    @Test
    public void syncTransportHonorsNonProxyHosts() {
        InetSocketAddress proxyAddress = new InetSocketAddress("localhost", 8080);
        ProxyOptions proxyOptions
            = new ProxyOptions(ProxyOptions.Type.HTTP, proxyAddress).setNonProxyHosts("localhost");
        VoiceAgentWebSocketClientConfiguration configuration = new VoiceAgentWebSocketClientConfiguration(
            URI.create("https://localhost"),
            request -> Mono.just(new com.azure.core.credential.AccessToken("token", OffsetDateTime.now().plusHours(1))),
            "v1", "test-user-agent", null, proxyOptions);
        OkHttpClient client = BetaVoiceAgentWebSocketSessionClient.createHttpClient(configuration,
            new VoiceAgentWebSocketConnectionOptions());

        List<Proxy> bypassed = client.proxySelector().select(URI.create("https://LOCALHOST/session"));
        List<Proxy> proxied = client.proxySelector().select(URI.create("https://example.com/session"));

        assertEquals(Proxy.NO_PROXY, bypassed.get(0));
        assertEquals(new Proxy(Proxy.Type.HTTP, proxyAddress), proxied.get(0));
    }

    private static String queryParameter(URI uri, String name) {
        for (String parameter : uri.getQuery().split("&")) {
            String[] pair = parameter.split("=", 2);
            if (name.equals(pair[0])) {
                return pair.length == 2 ? pair[1] : "";
            }
        }
        return null;
    }
}
