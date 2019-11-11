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

import java.util.List;

import org.junit.Test;

/**
 * Tests for the methods and factories of the ProtectionKey entity.
 */
public class ProtectionKeyEntityTest {

    public ProtectionKeyEntityTest() throws Exception {
    }

    @Test
    public void ProtectionKeyIdReturnsPayloadWithTheRightProtectionKeyType() {
        List<String> contentKeyTypeArray = ProtectionKey
                .getProtectionKeyId(ContentKeyType.StorageEncryption)
                .getQueryParameters().get("contentKeyType");
        String actualContentKeyType = contentKeyTypeArray.get(0);

        assertEquals(ContentKeyType.StorageEncryption.getCode(),
                Integer.parseInt(actualContentKeyType));
    }

    @Test
    public void ProtectionKeyReturnsPayloadWithTheRightProtectionKeyId() {
        String expectedProtectionKeyId = "expectedProtectionKey";
        String actualProtectionKeyId = ProtectionKey
                .getProtectionKey(expectedProtectionKeyId).getQueryParameters()
                .getFirst("ProtectionKeyId");
        expectedProtectionKeyId = String
                .format("'%s'", expectedProtectionKeyId);

        assertEquals(expectedProtectionKeyId, actualProtectionKeyId);
    }
}
