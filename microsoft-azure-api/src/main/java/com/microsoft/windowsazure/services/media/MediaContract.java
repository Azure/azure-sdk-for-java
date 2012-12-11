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

package com.microsoft.windowsazure.services.media;

import java.net.URI;

import com.microsoft.windowsazure.services.core.FilterableService;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityContract;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;

/**
 * Contract for interacting with the back end of Media Services
 * 
 */
public interface MediaContract extends FilterableService<MediaContract>, EntityContract {

    URI getRestServiceUri();

    /**
     * Creates an instance of the <code>WritableBlobContainerContract</code> API that will
     * write to the blob container given by the provided locator.
     * 
     * @param locator
     *            locator specifying where to upload to
     * @return the implementation of <code>WritableBlobContainerContract</code>
     */
    WritableBlobContainerContract createBlobWriter(LocatorInfo locator);
}
