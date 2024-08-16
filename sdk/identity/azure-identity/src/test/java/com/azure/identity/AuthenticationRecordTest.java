// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthenticationRecordTest {
    String json = "{"
        + "\"authority\":\"authority\","
        + "\"homeAccountId\":\"homeAccountId\","
        + "\"tenantId\":\"tenantId\","
        + "\"username\":\"username\","
        + "\"clientId\":\"clientId\""
        + "}";
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

    @Test
    public void canSerializeAsync() {
        InputStream stream = new ByteArrayInputStream(json.getBytes());
        StepVerifier.create(AuthenticationRecord.deserializeAsync(stream))
            .expectNextMatches(record -> {
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
                return true;
            }).verifyComplete();
    }
}
