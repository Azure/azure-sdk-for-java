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

package com.microsoft.windowsazure.services.media.entities;

import static org.junit.Assert.*;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.content.AssetType;

/**
 * Tests for the methods and factories of the Asset entity.
 */
public class AssetEntityTest {

    @Test
    public void assetCreateReturnsDefaultCreatePayload() {
        AssetType payload = (AssetType) Asset.create().getRequestContents();

        assertNotNull(payload);
        assertNull(payload.getId());
        assertEquals(0, payload.getState());
        assertNull(payload.getCreated());
        assertNull(payload.getLastModified());
        assertNull(payload.getAlternateId());
        assertNull(payload.getName());
        assertEquals(0, payload.getOptions());
    }
}
