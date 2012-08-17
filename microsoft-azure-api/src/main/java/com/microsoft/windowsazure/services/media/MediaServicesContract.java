/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.services.media;

import com.microsoft.windowsazure.services.core.FilterableService;
import com.microsoft.windowsazure.services.core.ServiceException;

/**
 * 
 * Defines the Media  contract.
 * 
 */
public interface MediaServicesContract extends FilterableService<MediaServicesContract> {

	/**
	 * Creates a new media service asset
	 *
	 * @param name
	 *			A <code>String</code> specifying the name of the asset to create.
	 *
	 * @return A <code>CreateAssetResult</code> object that represents the result.
	 *
	 * @exception ServiceException
	 *			If a service exception is encountered.
	 *
	 */
	CreateAssetResult createAsset(String name) throws ServiceException;

	/**
	 * Deletes a media service asset
	 *
	 * @param asset
	 *			A <code>AssetInfo</code> object specifying the asset to delete.
	 *
	 * @exception ServiceException
	 *			If a service exception is encountered.
	 *
	 */	
	void deleteAsset(AssetInfo asset) throws ServiceException;

	/**
	* // The set of operations to support. Will uncomment as they get added
	*
	* CreateAccessPolicyResult createAccessPolicy(String name, 
	*         int durationInMinutes,
	*         AccessPolicyPermissions permissions)
	*         throws ServiceException;
	*
	* CreateLocatorResult createLocator(AssetId asset,
	*                                   AccessPolicyId accessPolicy,
	*                                   DateTime startTime,
	*                                   LocatorType type)
	*                                   throws ServiceException;
	*
	* // Add overload that defaults locatorType to SAS, and defaults startTime to now - 5 minutes.
	*
	* // We need to upload blobs from here - do we go through blobservice, or do we just do it
	* // directly? I'm leaning towards direct - blob container is encoded in locator.
	*
	* UploadResult uploadFile(Locator locator, string content, UploadOptions options);
	* UploadResult uploadFile(Locator locator, InputStream sream, UploadOptions options);
	* 
	* // UploadOptions - do we need this? Look at blob client for what's in this. Can we reuse?
	* // Probably shouldn't, in case they diverge.
	*
	* DeleteLocatorResult deleteLocator(Locator locator)
	*         throws ServiceException; 
	*
	* CreateFileInfosResponse createFileInfos(Asset asset) 
	*         throws ServiceException;
	*
	* 
	*/
}
