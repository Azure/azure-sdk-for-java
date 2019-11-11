/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.services.media.implementation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.atom.ContentType;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.atom.LinkType;
import com.microsoft.windowsazure.services.media.implementation.content.Constants;
import com.microsoft.windowsazure.services.media.implementation.content.MediaProcessorType;
import com.microsoft.windowsazure.services.media.models.LinkInfo;
import com.microsoft.windowsazure.services.media.models.MediaProcessorInfo;

/**
 * Testing retrieval of links from ATOM entities
 * 
 */
public class LinkRetrievalTest {
    private static QName linkName = new QName("link", Constants.ATOM_NS);
    private MediaProcessorInfo info;
    private LinkType link1;
    private LinkType link2;

    @Before
    public void setup() {
        EntryType entry = new EntryType();

        link1 = new LinkType();
        link1.setTitle("someLink");
        link1.setRel("Related/something");
        link1.setHref("some/uri/somewhere");

        link2 = new LinkType();
        link2.setTitle("someOtherLink");
        link2.setRel("Related/else");
        link2.setHref("some/other/href/somewhere");

        entry.getEntryChildren().add(
                new JAXBElement<LinkType>(linkName, LinkType.class, link1));
        entry.getEntryChildren().add(
                new JAXBElement<LinkType>(linkName, LinkType.class, link2));

        MediaProcessorType payload = new MediaProcessorType().setId("DummyId")
                .setName("Dummy Name").setVersion("0.0.0").setVendor("Contoso")
                .setSku("sku skiddo").setDescription("For testing links only");

        ContentType contentElement = new ContentType();
        contentElement.getContent().add(
                new JAXBElement<MediaProcessorType>(
                        Constants.ODATA_PROPERTIES_ELEMENT_NAME,
                        MediaProcessorType.class, payload));

        entry.getEntryChildren().add(
                new JAXBElement<ContentType>(
                        Constants.ATOM_CONTENT_ELEMENT_NAME, ContentType.class,
                        contentElement));

        info = new MediaProcessorInfo(entry, payload);
    }

    @Test
    public void canRetrieveSingleLinkFromEntity() {
        assertTrue(info.hasLink(link1.getRel()));
    }

    @Test
    public void getFalseWhenLinkIsntThere() {
        assertFalse(info.hasLink("noSuchLink"));
    }

    @Test
    public void canRetrieveEntireLinkByRel() {
        LinkInfo<?> link = info.getLink(link2.getRel());

        assertLinksEqual(link2, link);
    }

    @Test
    public void getNullWhenLinkIsntThere() {
        assertNull(info.getLink("noSuchLink"));
    }

    private static void assertLinksEqual(LinkType expected, LinkInfo<?> actual) {
        assertEquals(expected.getHref(), actual.getHref());
    }
}
