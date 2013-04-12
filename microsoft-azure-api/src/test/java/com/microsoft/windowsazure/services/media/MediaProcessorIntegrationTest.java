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

package com.microsoft.windowsazure.services.media;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.microsoft.windowsazure.services.media.models.MediaProcessor;
import com.microsoft.windowsazure.services.media.models.MediaProcessorInfo;

public class MediaProcessorIntegrationTest extends IntegrationTestBase {

    private void verifyMediaProcessorInfo(String message, MediaProcessorInfo mediaProcessorInfo) {
        assertEquals(message + " id length", 49, mediaProcessorInfo.getId().length());
        assertTrue(message + " name length > 0", mediaProcessorInfo.getName().length() > 0);
        assertNotNull(message + " description", mediaProcessorInfo.getDescription());
        assertNotNull(message + " sku", mediaProcessorInfo.getSku());
        assertTrue(message + " vendor length > 0", mediaProcessorInfo.getVendor().length() > 0);
        assertTrue(message + " version length > 0", mediaProcessorInfo.getVersion().length() > 0);
    }

    private void verifyMediaProcessorInfo(String message, String id, String name, String description, String sku,
            String vendor, String version, MediaProcessorInfo mediaProcessorInfo) {
        assertEquals(message + " id", id, mediaProcessorInfo.getId());
        assertEquals(message + " name", name, mediaProcessorInfo.getName());
        assertEquals(message + " description", description, mediaProcessorInfo.getDescription());
        assertEquals(message + " sku", sku, mediaProcessorInfo.getSku());
        assertEquals(message + " vendor", vendor, mediaProcessorInfo.getVendor());
        assertEquals(message + " version", version, mediaProcessorInfo.getVersion());
    }

    @Test
    public void listMediaProcessorsSuccess() throws ServiceException {
        // Arrange

        // Act
        ListResult<MediaProcessorInfo> listMediaProcessorsResult = service.list(MediaProcessor.list());

        // Assert
        assertNotNull("listMediaProcessorsResult", listMediaProcessorsResult);
        assertTrue("listMediaProcessorsResult size > 0", listMediaProcessorsResult.size() > 0);
        List<MediaProcessorInfo> ps = listMediaProcessorsResult;
        for (int i = 0; i < ps.size(); i++) {
            MediaProcessorInfo mediaProcessorInfo = ps.get(i);
            verifyMediaProcessorInfo("mediaProcessorInfo:" + i, mediaProcessorInfo);
        }
    }

    @Test
    public void listMediaProcessorWithOptionSuccess() throws ServiceException {
        ListResult<MediaProcessorInfo> listMediaProcessorsResult = service.list(MediaProcessor.list().setTop(2)
                .set("$filter", "Id eq 'nb:mpid:UUID:aec03716-7c5e-4f68-b592-f4850eba9f10'"));

        assertNotNull("listMediaProcessorsResult", listMediaProcessorsResult);
        assertEquals("listMediaProcessors size", 1, listMediaProcessorsResult.size());
        MediaProcessorInfo mediaProcessorInfo = listMediaProcessorsResult.get(0);
        verifyMediaProcessorInfo("mediaProcessorInfo", "nb:mpid:UUID:aec03716-7c5e-4f68-b592-f4850eba9f10",
                "Storage Decryption", "Storage Decryption", "", "Microsoft", "1.6", mediaProcessorInfo);
    }
}
