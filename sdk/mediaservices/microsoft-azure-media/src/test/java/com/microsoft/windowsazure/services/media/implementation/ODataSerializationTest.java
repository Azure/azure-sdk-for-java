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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.junit.Assert;
import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.atom.ContentType;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.AssetType;
import com.microsoft.windowsazure.services.media.implementation.content.Constants;
import com.microsoft.windowsazure.services.media.models.AssetInfo;

public class ODataSerializationTest {

    private final String sampleFeedOneAsset = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<feed xml:base=\"https://wamsbayclus001rest-hs.cloudapp.net/api/\" xmlns=\"http://www.w3.org/2005/Atom\" xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\">\n"
            + "  <id>https://wamsbayclus001rest-hs.cloudapp.net/api/Assets</id>\n"
            + "  <title type=\"text\">Assets</title>\n"
            + "  <updated>2012-08-28T18:35:15Z</updated>\n"
            + "  <link rel=\"self\" title=\"Assets\" href=\"Assets\" />\n"
            + "  <entry>\n"
            + "    <id>https://wamsbayclus001rest-hs.cloudapp.net/api/Assets('nb%3Acid%3AUUID%3A1f6c7bb4-8013-486e-b4c9-2e4a6842b9a6')</id>\n"
            + "    <category term=\"Microsoft.Cloud.Media.Vod.Rest.Data.Models.Asset\" scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\" />\n"
            + "    <link rel=\"edit\" title=\"Asset\" href=\"Assets('nb%3Acid%3AUUID%3A1f6c7bb4-8013-486e-b4c9-2e4a6842b9a6')\" />\n"
            + "    <link rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/Locators\" type=\"application/atom+xml;type=feed\" title=\"Locators\" href=\"Assets('nb%3Acid%3AUUID%3A1f6c7bb4-8013-486e-b4c9-2e4a6842b9a6')/Locators\" />\n"
            + "    <link rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/ContentKeys\" type=\"application/atom+xml;type=feed\" title=\"ContentKeys\" href=\"Assets('nb%3Acid%3AUUID%3A1f6c7bb4-8013-486e-b4c9-2e4a6842b9a6')/ContentKeys\" />\n"
            + "    <link rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/Files\" type=\"application/atom+xml;type=feed\" title=\"Files\" href=\"Assets('nb%3Acid%3AUUID%3A1f6c7bb4-8013-486e-b4c9-2e4a6842b9a6')/Files\" />\n"
            + "    <link rel=\"http://schemas.microsoft.com/ado/2007/08/dataservices/related/ParentAssets\" type=\"application/atom+xml;type=feed\" title=\"ParentAssets\" href=\"Assets('nb%3Acid%3AUUID%3A1f6c7bb4-8013-486e-b4c9-2e4a6842b9a6')/ParentAssets\" />\n"
            + "    <title />\n"
            + "    <updated>2012-08-28T18:35:15Z</updated>\n"
            + "    <author>\n"
            + "      <name />\n"
            + "    </author>\n"
            + "    <m:action metadata=\"https://wamsbayclus001rest-hs.cloudapp.net/api/$metadata#WindowsAzureMediaServices.Publish\" title=\"Publish\" target=\"https://wamsbayclus001rest-hs.cloudapp.net/api/Assets('nb%3Acid%3AUUID%3A1f6c7bb4-8013-486e-b4c9-2e4a6842b9a6')/Publish\" />\n"
            + "    <content type=\"application/xml\">\n"
            + "      <m:properties>\n"
            + "        <d:Id>nb:cid:UUID:1f6c7bb4-8013-486e-b4c9-2e4a6842b9a6</d:Id>\n"
            + "        <d:State m:type=\"Edm.Int32\">0</d:State>\n"
            + "        <d:Created m:type=\"Edm.DateTime\">2012-08-28T18:34:06.123</d:Created>\n"
            + "        <d:LastModified m:type=\"Edm.DateTime\">2012-08-28T18:34:06.123</d:LastModified>\n"
            + "        <d:AlternateId m:null=\"true\" />\n"
            + "        <d:Name>testAsset</d:Name>\n"
            + "        <d:Options m:type=\"Edm.Int32\">0</d:Options>\n"
            + "      </m:properties>\n"
            + "    </content>\n"
            + "  </entry>\n"
            + "</feed>";

    @Test
    public void canUnmarshallAssetFromFeed() throws Exception {
        ODataAtomUnmarshaller um = new ODataAtomUnmarshaller();
        InputStream input = new ByteArrayInputStream(
                sampleFeedOneAsset.getBytes("UTF-8"));
        List<AssetInfo> entries = um.unmarshalFeed(input, AssetInfo.class);
        Assert.assertEquals(1, entries.size());
        Assert.assertEquals("nb:cid:UUID:1f6c7bb4-8013-486e-b4c9-2e4a6842b9a6",
                entries.get(0).getId());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void canMarshalEntryFromJavaObject() throws Exception {
        AssetType a = new AssetType();
        a.setName("testNewAsset");
        a.setOptions(0);
        a.setAlternateId("some other id");

        JAXBContext context = JAXBContext.newInstance(EntryType.class,
                AssetType.class);
        Marshaller m = context.createMarshaller();

        EntryType e = new EntryType();
        ContentType c = new ContentType();
        c.getContent().add(
                new JAXBElement(Constants.ODATA_PROPERTIES_ELEMENT_NAME,
                        AssetType.class, a));
        e.getEntryChildren().add(
                new JAXBElement(Constants.ATOM_CONTENT_ELEMENT_NAME,
                        ContentType.class, c));

        m.marshal(new JAXBElement(new QName(Constants.ATOM_NS, "entry"),
                EntryType.class, e), System.out);

    }
}
