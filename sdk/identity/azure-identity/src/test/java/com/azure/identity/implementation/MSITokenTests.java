// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import org.junit.Assert;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class MSITokenTests {
    private JacksonAdapter serializer = new JacksonAdapter();
    private OffsetDateTime expected = OffsetDateTime.of(2020, 1, 10, 15, 1, 28, 0, ZoneOffset.UTC);

    @Test
    public void canParseLong() throws Exception {
        String json = "{\"access_token\":\"fake_token\",\"expires_on\":\"1578668608\"}";
        MSIToken token = serializer.deserialize(json, MSIToken.class, SerializerEncoding.JSON);
        Assert.assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());
    }

    @Test
    public void canParseDateTime24Hr() throws Exception {
        String json = "{\"access_token\":\"fake_token\",\"expires_on\":\"01/10/2020 15:03:28 +00:00\"}";
        MSIToken token = serializer.deserialize(json, MSIToken.class, SerializerEncoding.JSON);
        Assert.assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());
    }

    @Test
    public void canParseDateTime12Hr() throws Exception {
        String json = "{\"access_token\":\"fake_token\",\"expires_on\":\"1/10/2020 3:03:28 PM +00:00\"}";
        MSIToken token = serializer.deserialize(json, MSIToken.class, SerializerEncoding.JSON);
        Assert.assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());

        json = "{\"access_token\":\"fake_token\",\"expires_on\":\"12/20/2019 4:58:20 AM +00:00\"}";
        token = serializer.deserialize(json, MSIToken.class, SerializerEncoding.JSON);
        expected = OffsetDateTime.of(2019, 12, 20, 4, 56, 20, 0, ZoneOffset.UTC);
        Assert.assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());

        json = "{\"access_token\":\"fake_token\",\"expires_on\":\"1/1/2020 0:00:00 PM +00:00\"}";
        token = serializer.deserialize(json, MSIToken.class, SerializerEncoding.JSON);
        expected = OffsetDateTime.of(2020, 1, 1, 11, 58, 0, 0, ZoneOffset.UTC);
        Assert.assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());
    }
}
