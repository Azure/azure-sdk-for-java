// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.models.AuthenticationRecord;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthenticationRecordTest {
    String json = "{" + "\"authority\":\"authority\"," + "\"homeAccountId\":\"homeAccountId\","
        + "\"tenantId\":\"tenantId\"," + "\"username\":\"username\"," + "\"clientId\":\"clientId\"" + "}";

    @Test
    public void canSerialize() {
        InputStream stream = new ByteArrayInputStream(json.getBytes());
        AuthenticationRecord record = AuthenticationRecord.deserialize(stream);
        assertEquals("authority", record.getAuthority());
        assertEquals("homeAccountId", record.getHomeAccountId());
        assertEquals("tenantId", record.getTenantId());
        assertEquals("username", record.getUsername());
        assertEquals("clientId", record.getClientId());
        OutputStream outStream = new ByteArrayOutputStream();
        record.serialize(outStream);
        try {
            outStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String serialized = outStream.toString();
        assertEquals(json, serialized);
    }
}
