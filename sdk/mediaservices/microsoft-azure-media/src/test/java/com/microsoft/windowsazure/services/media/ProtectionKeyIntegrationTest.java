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

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.models.ContentKeyType;
import com.microsoft.windowsazure.services.media.models.ProtectionKey;

public class ProtectionKeyIntegrationTest extends IntegrationTestBase {

    @Test
    public void canGetProtectionKeyId() throws ServiceException {
        // Arrange

        // Act
        String protectionKeyId = service.action(ProtectionKey
                .getProtectionKeyId(ContentKeyType.CommonEncryption));

        // Assert
        assertNotNull(protectionKeyId);
    }

    @Test
    public void canGetProtectionKey() throws ServiceException {
        // Arrange
        String protectionKeyId = service.action(ProtectionKey
                .getProtectionKeyId(ContentKeyType.CommonEncryption));

        // Act
        String protectionKey = service.action(ProtectionKey
                .getProtectionKey(protectionKeyId));

        // Assert
        assertNotNull(protectionKey);
    }

}
