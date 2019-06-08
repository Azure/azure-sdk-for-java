// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.eventhubs.implementation.ClientConstants;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

@RunWith(Theories.class)
public class CredentialInfoTest {
    private static final String NAMESPACE_NAME = "dummyNamespaceName";
    private static final String DEFAULT_DOMAIN_NAME = "servicebus.windows.net/";
    private static final String ENTITY_PATH = "dummyEntityPath";
    private static final String SHARED_ACCESS_KEY_NAME = "dummySasKeyName";
    private static final String SHARED_ACCESS_KEY = "dummySasKey";

    private static final String ENDPOINT = getURI(ClientConstants.ENDPOINT_FORMAT, NAMESPACE_NAME, DEFAULT_DOMAIN_NAME).toString();

    private static final String CONNECTION_STRING = String.format(
        "Endpoint=%s;SharedAccessKeyName=%s;SharedAccessKey=%s;EntityPath=%s",
        ENDPOINT, SHARED_ACCESS_KEY_NAME, SHARED_ACCESS_KEY, ENTITY_PATH);

    @DataPoints
    public static String[] connectionStrings() {
        return new String[]{null, "", "wrongConnStr"};
    }

    @Theory
    @Test(expected = IllegalArgumentException.class)
    public void invalidConnStrBuilder(String connStr) {
        CredentialInfo.from(connStr);
    }

    @Theory
    @Test(expected = IllegalArgumentException.class)
    public void invalidConnStrBuilderWithEventHubPath(String connStr) {
        CredentialInfo.from(connStr, ENTITY_PATH);
    }

    @Test
    public void propertySetter() {
        final CredentialInfo credentialInfo = CredentialInfo.from(CONNECTION_STRING);
        validateCredentialInfo(credentialInfo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullEventHubPath() {
        final CredentialInfo credentialInfo = CredentialInfo.from(CONNECTION_STRING, null);
    }

    @Test
    public void customEventHubPath() {
        final CredentialInfo credentialInfo = CredentialInfo.from(CONNECTION_STRING, ENTITY_PATH);
        validateCredentialInfo(credentialInfo);
    }

    private static URI getURI(String endpointFormat, String namespaceName, String domainName) {
        try {
            return new URI(String.format(Locale.US, endpointFormat, namespaceName, domainName));
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException(String.format(Locale.US,
                "Invalid namespace name: %s", namespaceName), exception);
        }
    }

    private static void validateCredentialInfo(CredentialInfo credentialInfo) {
        Assert.assertEquals(ENDPOINT, credentialInfo.endpoint().toString());
        Assert.assertEquals(SHARED_ACCESS_KEY, credentialInfo.sharedAccessKey());
        Assert.assertEquals(SHARED_ACCESS_KEY_NAME, credentialInfo.sharedAccessKeyName());
        Assert.assertEquals(ENTITY_PATH, credentialInfo.eventHubPath());
    }
}
