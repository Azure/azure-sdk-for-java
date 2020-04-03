package com.azure.identity.implementation;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.azure.core.util.Configuration;

public class IdentityClientOptionsTest {
    private static final String DEFAULT_AUTHORITY_HOST = "https://login.microsoftonline.com/";
    private static final String ENV_AUTHORITY_HOST = "https://foo.com/";

    @Test
    public void testAuthorithostIsDefault() throws IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException, SecurityException, ClassNotFoundException {
        IdentityClientOptions identityClientOptions = new IdentityClientOptions();
        Assert.assertEquals(DEFAULT_AUTHORITY_HOST, identityClientOptions.getAuthorityHost());
        IdentityClient identityClient = new IdentityClient(null, null, identityClientOptions);
        Field reflectIdentityClientOptions = Class.forName("com.azure.identity.implementation.IdentityClient")
                .getDeclaredField("options");
        reflectIdentityClientOptions.setAccessible(true);
        Assert.assertEquals(identityClientOptions, reflectIdentityClientOptions.get(identityClient));
    }

    @Test
    public void tesNoIdentityClientOptions() throws IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException, SecurityException, ClassNotFoundException {
        IdentityClient identityClient = new IdentityClient(null, null, null);
        Field reflectIdentityClientOptions = Class.forName("com.azure.identity.implementation.IdentityClient")
                .getDeclaredField("options");
        reflectIdentityClientOptions.setAccessible(true);
        IdentityClientOptions identityClientOptions = (IdentityClientOptions) reflectIdentityClientOptions
                .get(identityClient);
        Assert.assertEquals(DEFAULT_AUTHORITY_HOST, identityClientOptions.getAuthorityHost());
    }

    @Test
    public void testAuthorithostIsEnv() {
        Configuration.getGlobalConfiguration().put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, ENV_AUTHORITY_HOST);
        IdentityClientOptions identityClientOptions = new IdentityClientOptions();
        Assert.assertEquals(ENV_AUTHORITY_HOST, identityClientOptions.getAuthorityHost());
    }
}
