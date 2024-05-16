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

package com.microsoft.windowsazure.services.media.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URLEncoder;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.entityoperations.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityListOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityUpdateOperation;
import com.microsoft.windowsazure.services.media.implementation.atom.ContentType;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.atom.LinkType;
import com.microsoft.windowsazure.services.media.implementation.content.AssetType;
import com.microsoft.windowsazure.services.media.implementation.content.Constants;
import com.microsoft.windowsazure.services.media.implementation.content.JobType;

/**
 * Tests for the methods and factories of the Asset entity.
 */
public class ChannelEntityTest {
    static final String sampleAssetId = "nb:cid:UUID:1151b8bd-9ada-4e7f-9787-8dfa49968eab";
    private final String expectedUri = String.format("Assets('%s')",
            URLEncoder.encode(sampleAssetId, "UTF-8"));

    public ChannelEntityTest() throws Exception {
    }

    @Test
    public void assetCreateReturnsDefaultCreatePayload() {
        AssetType payload = (AssetType) Asset.create().getRequestContents();

        assertNotNull(payload);
        assertNull(payload.getId());
        assertNull(payload.getState());
        assertNull(payload.getCreated());
        assertNull(payload.getLastModified());
        assertNull(payload.getAlternateId());
        assertNull(payload.getName());
        assertNull(payload.getOptions());
    }

    @Test
    public void assetCreateCanSetAssetName() {
        String name = "assetCreateCanSetAssetName";

        Asset.Creator creator = Asset.create().setName(
                "assetCreateCanSetAssetName");

        AssetType payload = (AssetType) creator.getRequestContents();

        assertNotNull(payload);
        assertNull(payload.getId());
        assertNull(payload.getState());
        assertNull(payload.getCreated());
        assertNull(payload.getLastModified());
        assertNull(payload.getAlternateId());
        assertEquals(name, payload.getName());
        assertNull(payload.getOptions());
    }

    @Test
    public void assetGetReturnsExpectedUri() throws Exception {
        String expectedUri = String.format("Assets('%s')",
                URLEncoder.encode(sampleAssetId, "UTF-8"));

        EntityGetOperation<AssetInfo> getter = Asset.get(sampleAssetId);

        assertEquals(expectedUri, getter.getUri());
    }

    @Test
    public void assetListReturnsExpectedUri() {
        EntityListOperation<AssetInfo> lister = Asset.list();

        assertEquals("Assets", lister.getUri());
        assertNotNull(lister.getQueryParameters());
        assertEquals(0, lister.getQueryParameters().size());
    }

    @Test
    public void assetListCanTakeQueryParameters() {
        EntityListOperation<AssetInfo> lister = Asset.list().setTop(10)
                .setSkip(2);

        assertEquals("10", lister.getQueryParameters().getFirst("$top"));
        assertEquals("2", lister.getQueryParameters().getFirst("$skip"));
        assertEquals(2, lister.getQueryParameters().size());
    }

    @Test
    public void assetListCanTakeQueryParametersChained() {
        EntityListOperation<AssetInfo> lister = Asset.list().setTop(10)
                .setSkip(2).set("filter", "something");

        assertEquals("10", lister.getQueryParameters().getFirst("$top"));
        assertEquals("2", lister.getQueryParameters().getFirst("$skip"));
        assertEquals("something", lister.getQueryParameters()
                .getFirst("filter"));
        assertEquals(3, lister.getQueryParameters().size());
    }

    @Test
    public void assetUpdateReturnsExpectedUri() throws Exception {
        EntityUpdateOperation updater = Asset.update(sampleAssetId);
        assertEquals(expectedUri, updater.getUri());
    }

    @Test
    public void assetUpdateCanSetNameAndAltId() throws Exception {

        String expectedName = "newAssetName";
        String expectedAltId = "newAltId";

        EntityUpdateOperation updater = Asset.update(sampleAssetId)
                .setName(expectedName).setAlternateId(expectedAltId);

        AssetType payload = (AssetType) updater.getRequestContents();

        assertEquals(expectedName, payload.getName());
        assertEquals(expectedAltId, payload.getAlternateId());
    }

    @Test
    public void assetDeleteReturnsExpectedUri() throws Exception {
        EntityDeleteOperation deleter = Asset.delete(sampleAssetId);

        assertEquals(expectedUri, deleter.getUri());
    }

    private static final String expectedOutputAsset = "Job(someJobId)/OutputAssets";
    private static final String expectedInputAsset = "Job(someJobId)/InputAssets";

    @Test
    public void listForLinkReturnsExpectedUri() throws Exception {
        JobInfo fakeJob = createJob();

        EntityListOperation<AssetInfo> lister = Asset.list(fakeJob
                .getInputAssetsLink());

        assertEquals(lister.getUri(), expectedInputAsset);
    }

    private JobInfo createJob() {
        EntryType fakeJobEntry = new EntryType();
        addEntryLink(fakeJobEntry, Constants.ODATA_DATA_NS
                + "/related/OutputMediaAssets", expectedOutputAsset,
                "application/atom+xml;type=feed", "OutputAssets");
        addEntryLink(fakeJobEntry, Constants.ODATA_DATA_NS
                + "/related/InputMediaAssets", expectedInputAsset,
                "application/atom+xml;type=feed", "InputAssets");

        JobType payload = new JobType().setId("SomeId").setName("FakeJob");
        addEntryContent(fakeJobEntry, payload);

        return new JobInfo(fakeJobEntry, payload);
    }

    private void addEntryLink(EntryType entry, String rel, String href,
            String type, String title) {
        LinkType link = new LinkType();
        link.setRel(rel);
        link.setHref(href);
        link.setType(type);
        link.setTitle(title);

        JAXBElement<LinkType> linkElement = new JAXBElement<LinkType>(
                new QName("link", Constants.ATOM_NS), LinkType.class, link);
        entry.getEntryChildren().add(linkElement);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private ContentType addEntryContent(EntryType entry, Object content) {
        ContentType contentWrapper = new ContentType();
        contentWrapper.getContent().add(
                new JAXBElement(Constants.ODATA_PROPERTIES_ELEMENT_NAME,
                        content.getClass(), content));

        entry.getEntryChildren().add(
                new JAXBElement<ContentType>(
                        Constants.ATOM_CONTENT_ELEMENT_NAME, ContentType.class,
                        contentWrapper));
        return contentWrapper;
    }
}
