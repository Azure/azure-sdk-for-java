// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.primitives;

import org.junit.Test;

import com.microsoft.azure.servicebus.ClientSettings;
import com.microsoft.azure.servicebus.TestUtils;
import com.microsoft.azure.servicebus.security.ManagedIdentityTokenProvider;
import com.microsoft.azure.servicebus.security.SharedAccessSignatureTokenProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConnectionStringBuilderTests {
    @Test
    public void connectionStringBuilderTest() {
        String connectionString = "Endpoint=sb://test.servicebus.windows.net/;SharedAccessSignatureToken=SharedAccessSignature sr=amqp%3A%2F%2test.servicebus.windows.net%2topic";
        ConnectionStringBuilder builder = new ConnectionStringBuilder(connectionString);

        assertEquals("SharedAccessSignature sr=amqp%3A%2F%2test.servicebus.windows.net%2topic", builder.getSharedAccessSignatureToken());
        assertEquals(connectionString, builder.toString());
    }

    @Test
    public void invalidAadTokenConnectionStringTest() {
        TestUtils.assertThrows(IllegalArgumentException.class, () -> {
            String connecitionString = "Endpoint=sb://test.servicebus.windows.net/;Authentication=Managed Identity;SHAREDACCESSKEYNAME=val2";
            new ConnectionStringBuilder(connecitionString);
        });
        
        TestUtils.assertThrows(IllegalArgumentException.class, () -> {
            String connecitionString = "Endpoint=sb://test.servicebus.windows.net/;Authentication=Managed Identity;SharedAccessSignatureToken=val2";
            new ConnectionStringBuilder(connecitionString);
        });
    }
    
    @Test
    public void tokenProviderFromConnectionStringTest() {
        String connecitionString = "Endpoint=sb://test.servicebus.windows.net/;Authentication=Managed Identity";
        ClientSettings settings = Util.getClientSettingsFromConnectionStringBuilder(new ConnectionStringBuilder(connecitionString));
        assertTrue(settings.getTokenProvider() instanceof ManagedIdentityTokenProvider);

        // Should accept "ManagedIdentity" without the space as well
        connecitionString = "Endpoint=sb://test.servicebus.windows.net/;Authentication=ManagedIdentity";
        settings = Util.getClientSettingsFromConnectionStringBuilder(new ConnectionStringBuilder(connecitionString));
        assertTrue(settings.getTokenProvider() instanceof ManagedIdentityTokenProvider);
        
        // Should be case insensitive
        connecitionString = "Endpoint=sb://test.servicebus.windows.net/;AUTHENTICATION=managedidentity";
        settings = Util.getClientSettingsFromConnectionStringBuilder(new ConnectionStringBuilder(connecitionString));
        assertTrue(settings.getTokenProvider() instanceof ManagedIdentityTokenProvider);
        
        connecitionString = "Endpoint=sb://test.servicebus.windows.net/;SHAREDACCESSKEYNAME=keyname;SharedAccessKey=key";
        settings = Util.getClientSettingsFromConnectionStringBuilder(new ConnectionStringBuilder(connecitionString));
        assertTrue(settings.getTokenProvider() instanceof SharedAccessSignatureTokenProvider);
    }
}
