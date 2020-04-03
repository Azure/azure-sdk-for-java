package com.azure.identity.implementation;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.azure.core.util.Configuration;

public class IdentityClientOptionsTest {

    @Test
    public void testDefaultAuthorityHost() {
        String defaultAuthorityHost = "https://login.microsoftonline.com/";        
        IdentityClientOptions identityClientOptions = new IdentityClientOptions();
        Assert.assertEquals(defaultAuthorityHost, identityClientOptions.getAuthorityHost());
    }

    @Test
    public void testEnvAuthorityHost() {
        String envAuthorityHost = "https://foo.com/";
        Configuration.getGlobalConfiguration().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, envAuthorityHost);
        IdentityClientOptions identityClientOptions = new IdentityClientOptions();
        Assert.assertEquals(envAuthorityHost, identityClientOptions.getAuthorityHost());
    }

    @Test
    public void testCustomAuthorityHost() {
        String authorityHost = "https://custom.com/";        
        IdentityClientOptions identityClientOptions = new IdentityClientOptions();
        identityClientOptions.setAuthorityHost(authorityHost);
        Assert.assertEquals(authorityHost, identityClientOptions.getAuthorityHost());
    }

}
