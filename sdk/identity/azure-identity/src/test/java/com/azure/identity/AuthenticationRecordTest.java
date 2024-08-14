// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthenticationRecordTest {

    @Test
    public void canSerialize() {
        String json = "{"
            + "\"authority\":\"authority\","
            + "\"homeAccountId\":\"homeAccountId\","
            + "\"tenantId\":\"tenantId\","
            + "\"username\":\"username\","
            + "\"clientId\":\"clientId\""
            + "}";
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
