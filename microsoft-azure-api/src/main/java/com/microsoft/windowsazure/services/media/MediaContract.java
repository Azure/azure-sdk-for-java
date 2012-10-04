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
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.CreateAccessPolicyOptions;
import com.microsoft.windowsazure.services.media.models.CreateAssetOptions;
import com.microsoft.windowsazure.services.media.models.ListAccessPolicyOptions;
import com.microsoft.windowsazure.services.media.models.ListAssetsOptions;
import com.microsoft.windowsazure.services.media.models.UpdateAssetOptions;

/**
 * Defines the methods available for Windows Azure Media Services.
 */
public interface MediaContract extends FilterableService<MediaContract> {

    /**
     * Creates the asset.
     * 
     * @param assetName
     *            the asset name
     * @return the asset info
     * @throws ServiceException
     */
    public AssetInfo createAsset(String assetName) throws ServiceException;

    /**
     * Creates the asset.
     * 
     * @param assetName
     *            the asset name
     * @param createAssetOptions
     *            the create asset options
     * @return the asset info
     * @throws ServiceException
     */
    public AssetInfo createAsset(String assetName, CreateAssetOptions createAssetOptions) throws ServiceException;

    /**
     * Delete asset.
     * 
     * @param assetId
     *            the asset id
     * @throws ServiceException
     */
    public void deleteAsset(String assetId) throws ServiceException;

    /**
     * Gets the asset.
     * 
     * @param assetId
     *            the asset id
     * @return the asset
     * @throws ServiceException
     */
    public AssetInfo getAsset(String assetId) throws ServiceException;

    /**
     * List assets.
     * 
     * @return the list
     * @throws ServiceException
     */
    public List<AssetInfo> listAssets() throws ServiceException;

    /**
     * List assets.
     * 
     * @param listAssetsOptions
     *            the list assets options
     * @return the list
     * @throws ServiceException
     */
    public List<AssetInfo> listAssets(ListAssetsOptions listAssetsOptions) throws ServiceException;

    /**
     * Update asset.
     * 
     * @param assetId
     *            the asset id
     * @param updateAssetOptions
     *            the update asset options
     * @throws ServiceException
     *             the service exception
     */
    public void updateAsset(String assetId, UpdateAssetOptions updateAssetOptions) throws ServiceException;

    /**
     * Create the access policy
     * 
     * @param name
     *            name of access policy
     * @param durationInMinutes
     *            Duration in minutes that blob access will be granted when using this access policy
     * @return Created access policy
     * @throws ServiceException
     */
    AccessPolicyInfo createAccessPolicy(String name, double durationInMinutes) throws ServiceException;

    /**
     * Create the access policy with the given options
     * 
     * @param name
     *            name of access policy
     * @param durationInMinutes
     *            Duration in minutes that blob access will be granted when using this access policy
     * @param options
     *            options for creation
     * @return the created access policy
     * @throws ServiceException
     */
    AccessPolicyInfo createAccessPolicy(String name, double durationInMinutes, CreateAccessPolicyOptions options)
            throws ServiceException;

    /**
     * Delete the access policy with the given id
     * 
     * @param id
     *            of access policy to delete
     * @throws ServiceException
     */
    void deleteAccessPolicy(String id) throws ServiceException;

    /**
     * Get a single access policy
     * 
     * @param id
     *            the id of the asset to retrieve
     * @return the asset
     * @throws ServiceException
     */
    AccessPolicyInfo getAccessPolicy(String id) throws ServiceException;

    /**
     * List access policies
     * 
     * @return the list
     * @throws ServiceException
     */
    List<AccessPolicyInfo> listAccessPolicies() throws ServiceException;

    /**
     * List access policies
     * 
     * @param options
     *            the list access policy options
     * @return the list
     * @throws ServiceException
     */
    List<AccessPolicyInfo> listAccessPolicies(ListAccessPolicyOptions options) throws ServiceException;
}
