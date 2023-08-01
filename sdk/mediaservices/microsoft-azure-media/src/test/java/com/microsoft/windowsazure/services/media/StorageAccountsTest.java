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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.models.ListResult;
import com.microsoft.windowsazure.services.media.models.StorageAccountInfo;
import com.microsoft.windowsazure.services.media.models.StorageAccounts;

public class StorageAccountsTest extends IntegrationTestBase {
    
    @Test
    public void listStorageAccountsSuccess() throws Exception {
        // Arrange
        
        // Act
        ListResult<StorageAccountInfo> storageAccounts = service.list(StorageAccounts.list());
        
        // Assert
        assertNotNull(storageAccounts);
        assertTrue(storageAccounts.size() > 0);
        
    } 
}
