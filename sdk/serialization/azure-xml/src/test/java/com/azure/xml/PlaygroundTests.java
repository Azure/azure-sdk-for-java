// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import com.azure.xml.implementation.DefaultXmlReader;
import com.azure.xml.implementation.DefaultXmlWriter;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PlaygroundTests {
    private static final String SIMPLE_XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<SignedIdentifiers>"
        + "<SignedIdentifier>" + "<Id>MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=</Id>" + "<AccessPolicy>"
        + "<Start>2009-09-28T08:49:37Z</Start>" + "<Expiry>2009-09-29T08:49:37Z</Expiry>"
        + "<Permission>rwd</Permission>" + "</AccessPolicy>" + "</SignedIdentifier>" + "</SignedIdentifiers>";

    private static final String COMPLEX_XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        + "<entry xmlns=\"http://www.w3.org/2005/Atom\">"
        + "<id>https://shivangiservicebus.servicebus.windows.net/$namespaceinfo?api-version=2021-05</id>"
        + "<title>ShivangiServiceBus</title>" + "<updated>2020-07-02T09:53:19Z</updated>" + "<author>"
        + "<name>ShivangiServiceBus</name>" + "</author>"
        + "<link rel=\"self\" href=\"https://shivangiservicebus.servicebus.windows.net/$namespaceinfo?api-version=2021-05\"/>"
        + "<content type=\"application/xml\">"
        + "<NamespaceInfo xmlns=\"http://schemas.microsoft.com/netservices/2010/10/servicebus/connect\">"
        + "<Alias>MyServiceBusFallback</Alias>" + "<CreatedTime>2020-04-09T08:38:55.807Z</CreatedTime>"
        + "<MessagingSKU>Premium</MessagingSKU>" + "<MessagingUnits>1</MessagingUnits>"
        + "<ModifiedTime>2020-06-12T06:34:38.383Z</ModifiedTime>" + "<Name>ShivangiServiceBus</Name>"
        + "<NamespaceType>Messaging</NamespaceType>" + "</NamespaceInfo>" + "</content>" + "</entry>";

    @Test
    public void toXmlSimple() throws IOException, XMLStreamException {
        AccessPolicy accessPolicy = new AccessPolicy().setStartsOn(OffsetDateTime.parse("2009-09-28T08:49:37Z"))
            .setExpiresOn(OffsetDateTime.parse("2009-09-29T08:49:37Z"))
            .setPermissions("rwd");

        SignedIdentifier signedIdentifier = new SignedIdentifier().setId("MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=")
            .setAccessPolicy(accessPolicy);

        SignedIdentifiersWrapper wrapper = new SignedIdentifiersWrapper(Collections.singletonList(signedIdentifier));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (XmlWriter xmlWriter = DefaultXmlWriter.toStream(byteArrayOutputStream)) {
            xmlWriter.writeStartDocument();
            wrapper.toXml(xmlWriter);
        }
        String actualXml = byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());

        assertEquals(SIMPLE_XML, actualXml);
    }

    @Test
    public void fromXmlSimple() throws IOException, XMLStreamException {
        AccessPolicy accessPolicy = new AccessPolicy().setStartsOn(OffsetDateTime.parse("2009-09-28T08:49:37Z"))
            .setExpiresOn(OffsetDateTime.parse("2009-09-29T08:49:37Z"))
            .setPermissions("rwd");

        SignedIdentifier signedIdentifier = new SignedIdentifier().setId("MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=")
            .setAccessPolicy(accessPolicy);

        SignedIdentifiersWrapper wrapper = new SignedIdentifiersWrapper(Collections.singletonList(signedIdentifier));

        try (XmlReader xmlReader = DefaultXmlReader.fromString(SIMPLE_XML)) {
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

    @Test
    public void toXmlComplex() throws IOException, XMLStreamException {
        ResponseAuthor responseAuthor = new ResponseAuthor().setName("ShivangiServiceBus");

        ResponseLink responseLink = new ResponseLink()
            .setHref("https://shivangiservicebus.servicebus.windows.net/$namespaceinfo?api-version=2021-05")
            .setRel("self");

        NamespaceProperties namespaceProperties = new NamespaceProperties().setAlias("MyServiceBusFallback")
            .setCreatedTime(OffsetDateTime.parse("2020-04-09T08:38:55.807Z"))
            .setMessagingSku(MessagingSku.PREMIUM)
            .setMessagingUnits(1)
            .setModifiedTime(OffsetDateTime.parse("2020-06-12T06:34:38.383Z"))
            .setName("ShivangiServiceBus")
            .setNamespaceType(NamespaceType.MESSAGING);

        NamespacePropertiesEntryContent namespacePropertiesEntryContent
            = new NamespacePropertiesEntryContent().setType("application/xml")
                .setNamespaceProperties(namespaceProperties);

        NamespacePropertiesEntry namespacePropertiesEntry = new NamespacePropertiesEntry()
            .setId("https://shivangiservicebus.servicebus.windows.net/$namespaceinfo?api-version=2021-05")
            .setTitle("ShivangiServiceBus")
            .setUpdated(OffsetDateTime.parse("2020-07-02T09:53:19Z"))
            .setAuthor(responseAuthor)
            .setLink(responseLink)
            .setContent(namespacePropertiesEntryContent);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (XmlWriter xmlWriter = DefaultXmlWriter.toStream(byteArrayOutputStream)) {
            xmlWriter.writeStartDocument();
            namespacePropertiesEntry.toXml(xmlWriter);
        }
        String actualXml = byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());

        assertEquals(COMPLEX_XML, actualXml);
    }

    @Test
    public void fromXmlComplex() throws IOException, XMLStreamException {
        ResponseAuthor responseAuthor = new ResponseAuthor().setName("ShivangiServiceBus");

        ResponseLink responseLink = new ResponseLink()
            .setHref("https://shivangiservicebus.servicebus.windows.net/$namespaceinfo?api-version=2021-05")
            .setRel("self");

        NamespaceProperties namespaceProperties = new NamespaceProperties().setAlias("MyServiceBusFallback")
            .setCreatedTime(OffsetDateTime.parse("2020-04-09T08:38:55.807Z"))
            .setMessagingSku(MessagingSku.PREMIUM)
            .setMessagingUnits(1)
            .setModifiedTime(OffsetDateTime.parse("2020-06-12T06:34:38.383Z"))
            .setName("ShivangiServiceBus")
            .setNamespaceType(NamespaceType.MESSAGING);

        NamespacePropertiesEntryContent namespacePropertiesEntryContent
            = new NamespacePropertiesEntryContent().setType("application/xml")
                .setNamespaceProperties(namespaceProperties);

        NamespacePropertiesEntry namespacePropertiesEntry = new NamespacePropertiesEntry()
            .setId("https://shivangiservicebus.servicebus.windows.net/$namespaceinfo?api-version=2021-05")
            .setTitle("ShivangiServiceBus")
            .setUpdated(OffsetDateTime.parse("2020-07-02T09:53:19Z"))
            .setAuthor(responseAuthor)
            .setLink(responseLink)
            .setContent(namespacePropertiesEntryContent);

        try (XmlReader xmlReader = DefaultXmlReader.fromString(COMPLEX_XML)) {
            NamespacePropertiesEntry actualEntry = NamespacePropertiesEntry.fromXml(xmlReader);

            assertNotNull(actualEntry);

            assertEquals(namespacePropertiesEntry.getId(), actualEntry.getId());
            assertEquals(namespacePropertiesEntry.getTitle(), actualEntry.getTitle());
            assertEquals(namespacePropertiesEntry.getUpdated(), actualEntry.getUpdated());

            ResponseAuthor expectedAuthor = namespacePropertiesEntry.getAuthor();
            ResponseAuthor actualAuthor = actualEntry.getAuthor();

            assertEquals(expectedAuthor.getName(), actualAuthor.getName());

            ResponseLink expectedLink = namespacePropertiesEntry.getLink();
            ResponseLink actualLink = actualEntry.getLink();

            assertEquals(expectedLink.getHref(), actualLink.getHref());
            assertEquals(expectedLink.getRel(), actualLink.getRel());

            NamespacePropertiesEntryContent expectedContent = namespacePropertiesEntry.getContent();
            NamespacePropertiesEntryContent actualContent = actualEntry.getContent();

            assertEquals(expectedContent.getType(), actualContent.getType());

            NamespaceProperties expectedProperties = expectedContent.getNamespaceProperties();
            NamespaceProperties actualProperties = actualContent.getNamespaceProperties();

            assertEquals(expectedProperties.getAlias(), actualProperties.getAlias());
            assertEquals(expectedProperties.getCreatedTime(), actualProperties.getCreatedTime());
            assertEquals(expectedProperties.getMessagingSku(), actualProperties.getMessagingSku());
            assertEquals(expectedProperties.getMessagingUnits(), actualProperties.getMessagingUnits());
            assertEquals(expectedProperties.getModifiedTime(), actualProperties.getModifiedTime());
            assertEquals(expectedProperties.getName(), actualProperties.getName());
            assertEquals(expectedProperties.getNamespaceType(), actualProperties.getNamespaceType());
        }
    }
}
