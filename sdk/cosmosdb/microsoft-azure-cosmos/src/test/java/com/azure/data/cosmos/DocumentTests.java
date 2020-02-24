// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Document;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static com.azure.data.cosmos.BridgeInternal.setTimestamp;
import static org.assertj.core.api.Assertions.assertThat;

public class DocumentTests {

    @Test(groups = { "unit" })
    public void timestamp()  {
        Document d = new Document();
        OffsetDateTime time = OffsetDateTime.of(2019, 8, 6, 12, 53, 29, 0, ZoneOffset.UTC);
        setTimestamp(d, time);
        assertThat(d.timestamp()).isEqualTo(time);
    }
}
