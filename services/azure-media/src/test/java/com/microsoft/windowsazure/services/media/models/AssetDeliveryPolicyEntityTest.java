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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.content.AssetDeliveryPolicyRestType;

/**
 * Tests for access policy entity
 * 
 */
public class AssetDeliveryPolicyEntityTest {

    private final String sampleAdpId = "nb:adpid:UUID:92b0f6ba-3c9f-49b6-a5fa-2a8703b04ecd";

    public AssetDeliveryPolicyEntityTest() throws Exception {
    }

    @Test
    public void restTypeRoundTripTest() throws Exception {
        // Arrange
        String expectedName = "restTypeRoundTripTest";
        String expectedEnvelopeBaseKeyAcquisitionUrl = "expectedEnvelopeBaseKeyAcquisitionUrl";
        String expectedEnvelopeEncryptionIV = "expectedEnvelopeEncryptionIV";
        String expectedEnvelopeEncryptionIVAsBase64 = "expectedEnvelopeEncryptionIVAsBase64";
        String expectedEnvelopeKeyAcquisitionUrl = "expectedEnvelopeKeyAcquisitionUrl";
        String expectedPlayReadyCustomAttributes = "expectedPlayReadyCustomAttributes";
        String expectedPlayReadyLicenseAcquisitionUrl = "expectedPlayReadyLicenseAcquisitionUrl";
        AssetDeliveryPolicyType expectedAssetDeliveryPolicyType = AssetDeliveryPolicyType.DynamicCommonEncryption;
        Date expectedDate = new Date();
        AssetDeliveryPolicyRestType adp = new AssetDeliveryPolicyRestType();
        adp.setId(sampleAdpId);
        adp.setCreated(expectedDate);
        adp.setName(expectedName);
        adp.setAssetDeliveryProtocol(AssetDeliveryProtocol.bitsFromProtocols(EnumSet.of(AssetDeliveryProtocol.Dash, AssetDeliveryProtocol.Hds, AssetDeliveryProtocol.HLS, AssetDeliveryProtocol.SmoothStreaming)));
        Map<AssetDeliveryPolicyConfigurationKey, String> assetDeliveryConfiguration
            = new HashMap<AssetDeliveryPolicyConfigurationKey, String>();
        assetDeliveryConfiguration.put(AssetDeliveryPolicyConfigurationKey.EnvelopeBaseKeyAcquisitionUrl, expectedEnvelopeBaseKeyAcquisitionUrl);
        assetDeliveryConfiguration.put(AssetDeliveryPolicyConfigurationKey.EnvelopeEncryptionIV, expectedEnvelopeEncryptionIV);
        assetDeliveryConfiguration.put(AssetDeliveryPolicyConfigurationKey.EnvelopeEncryptionIVAsBase64, expectedEnvelopeEncryptionIVAsBase64);
        assetDeliveryConfiguration.put(AssetDeliveryPolicyConfigurationKey.EnvelopeKeyAcquisitionUrl, expectedEnvelopeKeyAcquisitionUrl);
        assetDeliveryConfiguration.put(AssetDeliveryPolicyConfigurationKey.PlayReadyCustomAttributes, expectedPlayReadyCustomAttributes);
        assetDeliveryConfiguration.put(AssetDeliveryPolicyConfigurationKey.PlayReadyLicenseAcquisitionUrl, expectedPlayReadyLicenseAcquisitionUrl);
        adp.setAssetDeliveryConfiguration(assetDeliveryConfiguration);
        adp.setAssetDeliveryPolicyType(expectedAssetDeliveryPolicyType.getCode());
        
        JAXBContext context = JAXBContext.newInstance(AssetDeliveryPolicyRestType.class);
        JAXBContext context2 = JAXBContext.newInstance(AssetDeliveryPolicyRestType.class);
        Marshaller marshaller = context.createMarshaller();
        Unmarshaller unmarshaller = context2.createUnmarshaller();
        StringWriter writer = new StringWriter();
        QName qName = new QName("test.schema", "AssetDeliveryPolicyRestType");
        JAXBElement<AssetDeliveryPolicyRestType> root = new JAXBElement<AssetDeliveryPolicyRestType>(qName, AssetDeliveryPolicyRestType.class, adp);
        // Act
        marshaller.marshal(root, writer);
        String xml = writer.toString();  

        JAXBElement<AssetDeliveryPolicyRestType> root2 = (JAXBElement<AssetDeliveryPolicyRestType>) unmarshaller.unmarshal(new StreamSource(new StringReader(xml)), AssetDeliveryPolicyRestType.class);
        AssetDeliveryPolicyRestType results = root2.getValue();
        
        // Assert
        assertNotNull(results);
        Map<AssetDeliveryPolicyConfigurationKey, String> resultADC = results.getAssetDeliveryConfiguration();
        assertEquals(resultADC.get(AssetDeliveryPolicyConfigurationKey.EnvelopeBaseKeyAcquisitionUrl), expectedEnvelopeBaseKeyAcquisitionUrl);
        assertEquals(resultADC.get(AssetDeliveryPolicyConfigurationKey.EnvelopeEncryptionIV), expectedEnvelopeEncryptionIV);
        assertEquals(resultADC.get(AssetDeliveryPolicyConfigurationKey.EnvelopeEncryptionIVAsBase64), expectedEnvelopeEncryptionIVAsBase64);
        assertEquals(resultADC.get(AssetDeliveryPolicyConfigurationKey.EnvelopeKeyAcquisitionUrl), expectedEnvelopeKeyAcquisitionUrl);
        assertEquals(resultADC.get(AssetDeliveryPolicyConfigurationKey.PlayReadyCustomAttributes), expectedPlayReadyCustomAttributes);
        assertEquals(resultADC.get(AssetDeliveryPolicyConfigurationKey.PlayReadyLicenseAcquisitionUrl), expectedPlayReadyLicenseAcquisitionUrl);
        assertEquals(results.getAssetDeliveryPolicyType().intValue(), expectedAssetDeliveryPolicyType.getCode());
        assertEquals(results.getCreated(), expectedDate);        
        assertEquals(results.getName(), expectedName);
        assertEquals(results.getId(), sampleAdpId);
    }

}
