/**
 * Copyright 2012 Microsoft Corporation
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

import static org.junit.Assert.*;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Test;

public class AssetInfoTest {

    @Test
    public void testGetSetId() {
        // Arrange
        String expectedId = "expectedId";
        AssetInfo assetInfo = new AssetInfo();

        // Act 
        String actualId = assetInfo.setId(expectedId).getId();

        // Assert
        assertEquals(expectedId, actualId);

    }

    @Test
    public void testGetSetState() {
        // Arrange
        AssetState expectedState = AssetState.Published;
        AssetInfo assetInfo = new AssetInfo();

        // Act
        AssetState actualState = assetInfo.setState(expectedState).getState();

        // Assert
        assertEquals(expectedState, actualState);
    }

    @Test
    public void testGetSetCreated() throws Exception {
        // Arrange
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar expectedCreated = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

        AssetInfo assetInfo = new AssetInfo();

        // Act 
        XMLGregorianCalendar actualCreated = assetInfo.setCreated(expectedCreated).getCreated();

        // Assert
        assertEquals(expectedCreated, actualCreated);

    }

    @Test
    public void testGetSetLastModified() throws Exception {
        // Arrange

        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar expectedLastModified = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        AssetInfo assetInfo = new AssetInfo();

        // Act
        XMLGregorianCalendar actualLastModified = assetInfo.setLastModified(expectedLastModified).getLastModified();

        // Assert
        assertEquals(expectedLastModified, actualLastModified);
    }

    @Test
    public void testGetSetAlternateId() {
        // Arrange
        String expectedAlternateId = "testAlternateId";
        AssetInfo assetInfo = new AssetInfo();

        // Act
        String actualAlternateId = assetInfo.setAlternateId(expectedAlternateId).getAlternateId();

        // Assert
        assertEquals(expectedAlternateId, actualAlternateId);
    }

    @Test
    public void testGetSetName() {
        // Arrange
        String expectedName = "testName";
        AssetInfo assetInfo = new AssetInfo();

        // Act
        String actualName = assetInfo.setName(expectedName).getName();

        // Assert
        assertEquals(expectedName, actualName);
    }

    @Test
    public void testGetSetOptions() {
        // Arrange
        EncryptionOption expectedOptions = EncryptionOption.None;
        AssetInfo assetInfo = new AssetInfo();

        // Act
        EncryptionOption actualOptions = assetInfo.setOptions(expectedOptions).getOptions();

        // Assert
        assertEquals(expectedOptions, actualOptions);
    }

}
