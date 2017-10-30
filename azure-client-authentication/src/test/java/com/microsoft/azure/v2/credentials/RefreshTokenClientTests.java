package com.microsoft.azure.v2.credentials;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.microsoft.azure.v2.credentials.http.MockHttpClient;
import com.microsoft.rest.v2.http.HttpRequest;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.Assert.*;

public class RefreshTokenClientTests {
    @Test
    public void refreshToken() throws IOException {
        final MockHttpClient httpClient = new MockHttpClient();
        final RefreshTokenClient client = new RefreshTokenClient("http://my.base.url", httpClient);

        client.refreshToken("mockTenant", "mockClientId", "mockResource", "mockRefreshToken", false);

        assertEquals(1, httpClient.requests().size());
        final HttpRequest request = httpClient.requests().get(0);
        assertEquals("POST", request.httpMethod());
        assertEquals("com.microsoft.azure.v2.credentials.RefreshTokenClient$RefreshTokenService.refreshToken", request.callerMethod());
        assertEquals("http://my.base.url/mockTenant/oauth2/token", request.url());
        assertEquals("client_id=mockClientId&grant_type=refresh_token&resource=mockResource&refresh_token=mockRefreshToken", CharStreams.toString(new InputStreamReader(request.body().createInputStream(), Charsets.UTF_8)));
    }
}
