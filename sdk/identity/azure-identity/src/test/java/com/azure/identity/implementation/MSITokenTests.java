// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import org.junit.Assert;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class MSITokenTests {
    private OffsetDateTime expected = OffsetDateTime.of(2020, 1, 10, 15, 3, 28, 0, ZoneOffset.UTC);

    @Test
    public void canParseLong() {
        MSIToken token = new MSIToken("fake_token", "1578668608");
        Assert.assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());
    }

    @Test
    public void canParseDateTime24Hr() {
        MSIToken token = new MSIToken("fake_token", "01/10/2020 15:03:28 +00:00");
        Assert.assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());
    }

    @Test
    public void canParseDateTime12Hr() {
        MSIToken token = new MSIToken("fake_token", "1/10/2020 3:03:28 PM +00:00");
        Assert.assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());

        token = new MSIToken("fake_token", "12/20/2019 4:58:20 AM +00:00");
        expected = OffsetDateTime.of(2019, 12, 20, 4, 58, 20, 0, ZoneOffset.UTC);
        Assert.assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());

        token = new MSIToken("fake_token", "1/1/2020 0:00:00 PM +00:00");
        expected = OffsetDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        Assert.assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());
    }
}
