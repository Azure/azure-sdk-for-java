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

import java.util.List;

import com.microsoft.windowsazure.services.core.FilterableService;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.CreateAssetOptions;
import com.microsoft.windowsazure.services.media.models.ListAssetsOptions;

/**
 * Defines the methods available for Windows Azure Media Services.
 */
public interface MediaContract extends FilterableService<MediaContract> {

    /**
     * Creates the asset.
     * 
     * @param asset
     *            the asset
     * @return the asset info
     */
    public AssetInfo createAsset(AssetInfo asset);

    /**
     * List assets.
     * 
     * @param listAssetsOptions
     *            the list assets options
     * @return the list
     */
    public List<AssetInfo> listAssets(ListAssetsOptions listAssetsOptions);

    /**
     * Update asset.
     * 
     * @param updatedAsset
     *            the updated asset
     * @return the asset info
     */
    public AssetInfo updateAsset(AssetInfo updatedAsset);

    /**
     * Delete asset.
     * 
     * @param assetId
     *            the asset id
     */
    public void deleteAsset(String assetId);

    /**
     * Creates the asset.
     * 
     * @param assetName
     *            the asset name
     * @return the asset info
     */
    public AssetInfo createAsset(String assetName);

    /**
     * Creates the asset.
     * 
     * @param assetName
     *            the asset name
     * @param createAssetOptions
     *            the create asset options
     * @return the asset info
     */
    public AssetInfo createAsset(String assetName, CreateAssetOptions createAssetOptions);

    /**
     * Gets the asset.
     * 
     * @param assetId
     *            the asset id
     * @return the asset
     */
    public AssetInfo getAsset(String assetId);

    /**
     * List assets.
     * 
     * @return the list
     */
    public List<AssetInfo> listAssets();

    /**
     * Delete asset.
     * 
     * @param assetInfo
     *            the asset info
     */
    public void deleteAsset(AssetInfo assetInfo);

    List<AssetInfo> getAssets() throws ServiceException;
}
