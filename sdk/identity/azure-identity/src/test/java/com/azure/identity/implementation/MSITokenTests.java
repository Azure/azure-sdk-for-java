// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

public class MSITokenTests {
    private OffsetDateTime expected = OffsetDateTime.of(2020, 1, 10, 15, 3, 28, 0, ZoneOffset.UTC);

    private static final SerializerAdapter SERIALIZER = JacksonAdapter.createDefaultSerializerAdapter();

    @Test
    public void canParseLong() {
        MSIToken token = new MSIToken("fake_token", "1578668608", null, null);
        MSIToken token2 = new MSIToken("fake_token", null, "3599", null);
        MSIToken token3 = new MSIToken("fake_token", "1578668608", "3599", null);

        Assert.assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());
        Assert.assertTrue((token2.getExpiresAt().toEpochSecond() - OffsetDateTime.now().toEpochSecond()) > 3500);
        Assert.assertEquals(expected.toEpochSecond(), token3.getExpiresAt().toEpochSecond());
    }

    @Test
    public void canParseRefreshes() {
        try (MockedStatic<OffsetDateTime> offsetDateTimeMockedStatic = mockStatic(OffsetDateTime.class, CALLS_REAL_METHODS)) {

            offsetDateTimeMockedStatic.when(() -> OffsetDateTime.now((ZoneId) any())).thenReturn(expected);
            offsetDateTimeMockedStatic.when(() -> OffsetDateTime.now((Clock) any())).thenReturn(expected);
            offsetDateTimeMockedStatic.when(OffsetDateTime::now).thenReturn(expected);

            OffsetDateTime now = OffsetDateTime.now();
            OffsetDateTime expiration = now.plusHours(12);
            OffsetDateTime expirationMinusOneHour = expiration.minusHours(1);
            Duration expirationMinusOneHourSeconds = Duration.between(expirationMinusOneHour, expected);
            OffsetDateTime expirationMinusElevenHours = expiration.minusHours(11);
            Duration expirationMinusElevenHoursSeconds = Duration.between(expirationMinusElevenHours, expected);

            MSIToken expirationMinusOneHourToken = new MSIToken("fake_token",
                String.valueOf(expirationMinusOneHour.toEpochSecond()),
                String.valueOf(expirationMinusOneHourSeconds.getSeconds()), null);

            MSIToken expirationMinus11HoursToken = new MSIToken("fake_token",
                String.valueOf(expirationMinusElevenHours.toEpochSecond()),
                String.valueOf(expirationMinusElevenHoursSeconds.getSeconds()), null);

            MSIToken hasRefresh = new MSIToken("fake_token",
                String.valueOf(expirationMinusElevenHours.toEpochSecond()),
                String.valueOf(expirationMinusElevenHoursSeconds.getSeconds()),
                String.valueOf(240));

            Assert.assertEquals(now.plusSeconds(240).toEpochSecond(), hasRefresh.getRefreshAtSeconds());
            Assert.assertEquals(now.plusHours(1).toEpochSecond(), expirationMinus11HoursToken.getRefreshAtSeconds());
            long expected = Duration.between(now, expirationMinusOneHour).getSeconds() / 2;
            Assert.assertEquals(now.plusSeconds(expected).toEpochSecond(), expirationMinusOneHourToken.getRefreshAtSeconds());

        }

    }

    @Test
    public void canDeserializeWithRefreshIn() {
        String json = "{\n"
            + "  \"access_token\": \"fake_token\",\n"
            + "  \"refresh_token\": \"\",\n"
            + "  \"expires_in\": \"3599\",\n"
            + "  \"expires_on\": \"1506484173\",\n"
            + "  \"refresh_in\": \"3600\",\n"
            + "  \"not_before\": \"1506480273\",\n"
            + "  \"resource\": \"https://managementazurecom/\",\n"
            + "  \"token_type\": \"Bearer\"\n"
            + "}";

        try (MockedStatic<OffsetDateTime> offsetDateTimeMockedStatic = mockStatic(OffsetDateTime.class, CALLS_REAL_METHODS)) {

            offsetDateTimeMockedStatic.when(() -> OffsetDateTime.now((ZoneId) any())).thenReturn(expected);
            offsetDateTimeMockedStatic.when(() -> OffsetDateTime.now((Clock) any())).thenReturn(expected);
            offsetDateTimeMockedStatic.when(OffsetDateTime::now).thenReturn(expected);
            {
                MSIToken token;
                try {
                    token = SERIALIZER.deserialize(json, MSIToken.class, SerializerEncoding.JSON);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Assert.assertEquals(1506484173, token.getExpiresAt().toEpochSecond());
                Assert.assertEquals(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(3600).toEpochSecond(), token.getRefreshAtSeconds());
            }
        }
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
        MSIToken token = new MSIToken("fake_token", "01/10/2020 15:03:28 +00:00", null, null);
        MSIToken token2 = new MSIToken("fake_token", null, "01/10/2020 15:03:28 +00:00", null);
        MSIToken token3 = new MSIToken("fake_token", "01/10/2020 15:03:28 +00:00",
            "86500", null);
        MSIToken token4 = new MSIToken("fake_token", null, "43219", null);

        Assert.assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());
        Assert.assertEquals(expected.toEpochSecond(), token2.getExpiresAt().toEpochSecond());
        Assert.assertEquals(expected.toEpochSecond(), token3.getExpiresAt().toEpochSecond());
        Assert.assertTrue(ChronoUnit.HOURS.between(OffsetDateTime.now(), token4.getExpiresAt()) == 12L);
    }

    @Test
    public void canParseDateTime12Hr() {
        MSIToken token = new MSIToken("fake_token", "1/10/2020 3:03:28 PM +00:00", null, null);
        MSIToken token2 = new MSIToken("fake_token", null, "1/10/2020 3:03:28 PM +00:00", null);
        MSIToken token3 = new MSIToken("fake_token", "1/10/2020 3:03:28 PM +00:00",
            "86500", null);
        MSIToken token4 = new MSIToken("fake_token", null, "86500", null);

        Assert.assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());
        Assert.assertEquals(expected.toEpochSecond(), token2.getExpiresAt().toEpochSecond());
        Assert.assertEquals(expected.toEpochSecond(), token3.getExpiresAt().toEpochSecond());
        Assert.assertTrue(ChronoUnit.HOURS.between(OffsetDateTime.now(), token4.getExpiresAt()) == 24L);

        token = new MSIToken("fake_token", "12/20/2019 4:58:20 AM +00:00", null, null);
        token2 = new MSIToken("fake_token", null, "12/20/2019 4:58:20 AM +00:00", null);
        token3 = new MSIToken("fake_token", "12/20/2019 4:58:20 AM +00:00",
            "105500", null);
        token4 = new MSIToken("fake_token", null, "105500", null);
        expected = OffsetDateTime.of(2019, 12, 20, 4, 58, 20, 0, ZoneOffset.UTC);

        Assert.assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());
        Assert.assertEquals(expected.toEpochSecond(), token2.getExpiresAt().toEpochSecond());
        Assert.assertEquals(expected.toEpochSecond(), token3.getExpiresAt().toEpochSecond());
        Assert.assertTrue(ChronoUnit.HOURS.between(OffsetDateTime.now(), token4.getExpiresAt()) == 29L);

        token = new MSIToken("fake_token", "1/1/2020 0:00:00 PM +00:00", null, null);
        token2 = new MSIToken("fake_token", null, "1/1/2020 0:00:00 PM +00:00", null);
        token3 = new MSIToken("fake_token", "1/1/2020 0:00:00 PM +00:00",
            "220800", null);
        token4 = new MSIToken("fake_token", null, "220800", null);

        expected = OffsetDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        Assert.assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());
        Assert.assertEquals(expected.toEpochSecond(), token2.getExpiresAt().toEpochSecond());
        Assert.assertEquals(expected.toEpochSecond(), token3.getExpiresAt().toEpochSecond());
        Assert.assertTrue(ChronoUnit.HOURS.between(OffsetDateTime.now(), token4.getExpiresAt()) == 61L);
    }
}
