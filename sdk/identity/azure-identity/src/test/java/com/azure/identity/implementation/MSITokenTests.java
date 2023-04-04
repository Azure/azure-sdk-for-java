// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public class MSITokenTests {
    private OffsetDateTime expected = OffsetDateTime.of(2020, 1, 10, 15, 3, 28, 0, ZoneOffset.UTC);

    private static final SerializerAdapter SERIALIZER = JacksonAdapter.createDefaultSerializerAdapter();

    @Test
    public void canParseLong() {
        MSIToken token = new MSIToken("fake_token", "1578668608", null);
        MSIToken token2 = new MSIToken("fake_token", null, "3599");
        MSIToken token3 = new MSIToken("fake_token", "1578668608", "3599");

        Assert.assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());
        Assert.assertTrue((token2.getExpiresAt().toEpochSecond() - OffsetDateTime.now().toEpochSecond()) > 3500);
        Assert.assertEquals(expected.toEpochSecond(), token3.getExpiresAt().toEpochSecond());
    }

    @Test
    public void canDeserialize() {
        String json = "{\n"
            + "  \"access_token\": \"fake_token\",\n"
            + "  \"refresh_token\": \"\",\n"
            + "  \"expires_in\": \"3599\",\n"
            + "  \"expires_on\": \"1506484173\",\n"
            + "  \"not_before\": \"1506480273\",\n"
            + "  \"resource\": \"https://managementazurecom/\",\n"
            + "  \"token_type\": \"Bearer\"\n"
            + "}";
        MSIToken token;
        try {
            token = SERIALIZER.deserialize(json, MSIToken.class, SerializerEncoding.JSON);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Assert.assertEquals(1506484173, token.getExpiresAt().toEpochSecond());
    }

    @Test
    public void canParseDateTime24Hr() {
        MSIToken token = new MSIToken("fake_token", "01/10/2020 15:03:28 +00:00", null);
        MSIToken token2 = new MSIToken("fake_token", null, "01/10/2020 15:03:28 +00:00");
        MSIToken token3 = new MSIToken("fake_token", "01/10/2020 15:03:28 +00:00",
            "86500");
        MSIToken token4 = new MSIToken("fake_token", null, "43219");

        Assert.assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());
        Assert.assertEquals(expected.toEpochSecond(), token2.getExpiresAt().toEpochSecond());
        Assert.assertEquals(expected.toEpochSecond(), token3.getExpiresAt().toEpochSecond());
        Assert.assertTrue(ChronoUnit.HOURS.between(OffsetDateTime.now(), token4.getExpiresAt()) == 12L);
    }

    @Test
    public void canParseDateTime12Hr() {
        MSIToken token = new MSIToken("fake_token", "1/10/2020 3:03:28 PM +00:00", null);
        MSIToken token2 = new MSIToken("fake_token", null, "1/10/2020 3:03:28 PM +00:00");
        MSIToken token3 = new MSIToken("fake_token", "1/10/2020 3:03:28 PM +00:00",
            "86500");
        MSIToken token4 = new MSIToken("fake_token", null, "86500");

        Assert.assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());
        Assert.assertEquals(expected.toEpochSecond(), token2.getExpiresAt().toEpochSecond());
        Assert.assertEquals(expected.toEpochSecond(), token3.getExpiresAt().toEpochSecond());
        Assert.assertTrue(ChronoUnit.HOURS.between(OffsetDateTime.now(), token4.getExpiresAt()) == 24L);

        token = new MSIToken("fake_token", "12/20/2019 4:58:20 AM +00:00", null);
        token2 = new MSIToken("fake_token", null, "12/20/2019 4:58:20 AM +00:00");
        token3 = new MSIToken("fake_token", "12/20/2019 4:58:20 AM +00:00",
            "105500");
        token4 = new MSIToken("fake_token", null, "105500");
        expected = OffsetDateTime.of(2019, 12, 20, 4, 58, 20, 0, ZoneOffset.UTC);

        Assert.assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());
        Assert.assertEquals(expected.toEpochSecond(), token2.getExpiresAt().toEpochSecond());
        Assert.assertEquals(expected.toEpochSecond(), token3.getExpiresAt().toEpochSecond());
        Assert.assertTrue(ChronoUnit.HOURS.between(OffsetDateTime.now(), token4.getExpiresAt()) == 29L);

        token = new MSIToken("fake_token", "1/1/2020 0:00:00 PM +00:00", null);
        token2 = new MSIToken("fake_token", null, "1/1/2020 0:00:00 PM +00:00");
        token3 = new MSIToken("fake_token", "1/1/2020 0:00:00 PM +00:00",
            "220800");
        token4 = new MSIToken("fake_token", null, "220800");

        expected = OffsetDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        Assert.assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());
        Assert.assertEquals(expected.toEpochSecond(), token2.getExpiresAt().toEpochSecond());
        Assert.assertEquals(expected.toEpochSecond(), token3.getExpiresAt().toEpochSecond());
        Assert.assertTrue(ChronoUnit.HOURS.between(OffsetDateTime.now(), token4.getExpiresAt()) == 61L);
    }
}
