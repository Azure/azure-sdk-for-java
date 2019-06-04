// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public class EventHubClientBuilderTest {
    private static final String END_POINT_FORMAT = "sb://%s.%s";
    private static final String NAMESPACE_NAME = "dummyNamespaceName";
    private static final String DEFAULT_DOMAIN_NAME = "servicebus.windows.net/";

    private static final String ENTITY_PATH = "dummyEntityPath";
    private static final String SHARED_ACCESS_KEY_NAME = "dummySasKeyName";
    private static final String SHARED_ACCESS_KEY = "dummySasKey";
    private static final String END_POINT_STR = getURI(END_POINT_FORMAT, NAMESPACE_NAME, DEFAULT_DOMAIN_NAME).toString();

    private static final String PROXY_HOST = "127.0.0.1";
    private static final String PROXY_PORT = "3128";

    private static final String CORRECT_CONN_STR = String.format("Endpoint=%s;SharedAccessKeyName=%s;SharedAccessKey=%s;EntityPath=%s",
        END_POINT_STR, SHARED_ACCESS_KEY_NAME, SHARED_ACCESS_KEY, ENTITY_PATH);
    private static Proxy proxyAddress = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, Integer.parseInt(PROXY_PORT)));

    private static final CredentialInfo VALID_CREDENTIAL_INFO = CredentialInfo.from(CORRECT_CONN_STR);

    private static URI getURI(String endpointFormat, String namespaceName, String domainName) {
        try {
            return new URI(String.format(Locale.US, endpointFormat, namespaceName, domainName));
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException(String.format(Locale.US,
                "Invalid namespace name: %s", namespaceName), exception);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingConnStrBuilder() {
        EventHubClientBuilder builder = new EventHubClientBuilder();
        builder.build();
    }

    @Test
    public void defaultProxyConfigurationBuilder() {
        EventHubClientBuilder builder = new EventHubClientBuilder();
        builder.credentials(VALID_CREDENTIAL_INFO).build();
    }

    @Test
    public void customNoneProxyConfigurationBuilder() {
        EventHubClientBuilder builder = new EventHubClientBuilder();
        ProxyConfiguration proxyConfig = new ProxyConfiguration(ProxyAuthenticationType.NONE,
            this.proxyAddress, null, null);
        builder.credentials(VALID_CREDENTIAL_INFO)
            .proxyConfiguration(proxyConfig)
            .build();
    }

    // TODO: add test for retry(), scheduler(), timeout(), transportType()
}
