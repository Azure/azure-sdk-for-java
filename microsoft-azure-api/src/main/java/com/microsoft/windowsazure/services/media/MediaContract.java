/**
 * Copyright 2011 Microsoft Corporation
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

import java.util.List;

import com.microsoft.windowsazure.services.core.FilterableService;
import com.microsoft.windowsazure.services.media.models.Asset;
import com.microsoft.windowsazure.services.media.models.ListAssetsOptions;

/**
 * 
 * Defines the methods available for Windows Azure Media Services
 * 
 */
public interface MediaContract extends FilterableService<MediaContract> {

    public Asset createAsset(Asset asset);

    public Asset getAsset(Asset asset);

    public List<Asset> listAssets(ListAssetsOptions listAssetsOptions);

    public Asset updateAsset(Asset updatedAsset);

    public void deleteAsset(String assetId);

}
