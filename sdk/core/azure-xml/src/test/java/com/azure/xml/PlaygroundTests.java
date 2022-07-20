// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PlaygroundTests {
    @Test
    public void toXml() throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><SignedIdentifiers><SignedIdentifier><Id>MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=</Id><AccessPolicy><Start>2009-09-28T08:49:37Z</Start><Expiry>2009-09-29T08:49:37Z</Expiry><Permission>rwd</Permission></AccessPolicy></SignedIdentifier></SignedIdentifiers>";

        AccessPolicy accessPolicy = new AccessPolicy()
            .setStartsOn(OffsetDateTime.parse("2009-09-28T08:49:37Z"))
            .setExpiresOn(OffsetDateTime.parse("2009-09-29T08:49:37Z"))
            .setPermissions("rwd");

        SignedIdentifier signedIdentifier = new SignedIdentifier()
            .setId("MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=")
            .setAccessPolicy(accessPolicy);

        SignedIdentifiersWrapper wrapper = new SignedIdentifiersWrapper(Collections.singletonList(signedIdentifier));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (XmlWriter xmlWriter = DefaultXmlWriter.fromOutputStream(byteArrayOutputStream)) {
            xmlWriter.writeStartDocument();
            wrapper.toXml(xmlWriter);
        }
        String actualXml = byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());

        assertEquals(xml, actualXml);
    }

    @Test
    public void fromXml() throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><SignedIdentifiers><SignedIdentifier><Id>MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=</Id><AccessPolicy><Start>2009-09-28T08:49:37Z</Start><Expiry>2009-09-29T08:49:37Z</Expiry><Permission>rwd</Permission></AccessPolicy></SignedIdentifier></SignedIdentifiers>";

        AccessPolicy accessPolicy = new AccessPolicy()
            .setStartsOn(OffsetDateTime.parse("2009-09-28T08:49:37Z"))
            .setExpiresOn(OffsetDateTime.parse("2009-09-29T08:49:37Z"))
            .setPermissions("rwd");

        SignedIdentifier signedIdentifier = new SignedIdentifier()
            .setId("MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=")
            .setAccessPolicy(accessPolicy);

        SignedIdentifiersWrapper wrapper = new SignedIdentifiersWrapper(Collections.singletonList(signedIdentifier));

        try (XmlReader xmlReader = DefaultXmlReader.fromString(xml)) {
            SignedIdentifiersWrapper actualWrapper = SignedIdentifiersWrapper.fromXml(xmlReader);

            assertNotNull(actualWrapper);
            assertEquals(wrapper.items().size(), actualWrapper.items().size());

            for (int i = 0; i < wrapper.items().size(); i++) {
                SignedIdentifier expectedIdentifier = wrapper.items().get(i);
                SignedIdentifier actualIdentifier = wrapper.items().get(i);

                assertEquals(expectedIdentifier.getId(), actualIdentifier.getId());

                AccessPolicy expectedPolicy = expectedIdentifier.getAccessPolicy();
                AccessPolicy actualPolicy = actualIdentifier.getAccessPolicy();

                assertEquals(expectedPolicy.getStartsOn(), actualPolicy.getStartsOn());
                assertEquals(expectedPolicy.getExpiresOn(), actualPolicy.getExpiresOn());
                assertEquals(expectedPolicy.getPermissions(), actualPolicy.getPermissions());
            }
        }
    }
}
