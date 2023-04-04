// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.Document;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static com.azure.cosmos.BridgeInternal.setTimestamp;
import static org.assertj.core.api.Assertions.assertThat;

public class DocumentTests {

    @Test(groups = { "unit" })
    public void timestamp()  {
        Document d = new Document();
        OffsetDateTime time = OffsetDateTime.of(2019, 8, 6, 12, 53, 29, 0, ZoneOffset.UTC);
        setTimestamp(d, time.toInstant());
        assertThat(d.getTimestamp()).isEqualTo(time.toInstant());
    }
}
