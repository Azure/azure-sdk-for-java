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

package com.microsoft.windowsazure.services.media.implementation;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.utils.ServiceExceptionFactory;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.CreateAccessPolicyOptions;
import com.microsoft.windowsazure.services.media.models.CreateAssetOptions;
import com.microsoft.windowsazure.services.media.models.CreateLocatorOptions;
import com.microsoft.windowsazure.services.media.models.ListAccessPolicyOptions;
import com.microsoft.windowsazure.services.media.models.ListAssetsOptions;
import com.microsoft.windowsazure.services.media.models.ListLocatorsOptions;
import com.microsoft.windowsazure.services.media.models.ListLocatorsResult;
import com.microsoft.windowsazure.services.media.models.ListMediaProcessorsOptions;
import com.microsoft.windowsazure.services.media.models.ListMediaProcessorsResult;
import com.microsoft.windowsazure.services.media.models.LocatorInfo;
import com.microsoft.windowsazure.services.media.models.LocatorType;
import com.microsoft.windowsazure.services.media.models.UpdateAssetOptions;
import com.microsoft.windowsazure.services.media.models.UpdateLocatorOptions;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * Wrapper implementation of <code>MediaServicesContract</code> that
 * translates exceptions into ServiceExceptions.
 * 
 */
public class MediaExceptionProcessor implements MediaContract {

    /** The service. */
    private final MediaContract service;

    /** The log. */
    static Log log = LogFactory.getLog(MediaContract.class);

    /**
     * Instantiates a new media exception processor.
     * 
     * @param service
     *            the service
     */
    public MediaExceptionProcessor(MediaContract service) {
        this.service = service;
    }

    /**
     * Instantiates a new media exception processor.
     * 
     * @param service
     *            the service
     */
    @Inject
    public MediaExceptionProcessor(MediaRestProxy service) {
        this.service = service;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.core.FilterableService#withFilter(com.microsoft.windowsazure.services.core.ServiceFilter)
     */
    @Override
    public MediaContract withFilter(ServiceFilter filter) {
        return new MediaExceptionProcessor(service.withFilter(filter));
    }

    /**
     * Process a catch.
     * 
     * @param e
     *            the e
     * @return the service exception
     */
    private ServiceException processCatch(ServiceException e) {
        log.warn(e.getMessage(), e.getCause());
        return ServiceExceptionFactory.process("MediaServices", e);
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#createAsset()
     */
    @Override
    public AssetInfo createAsset() throws ServiceException {
        try {
            return service.createAsset();
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#createAsset(java.lang.String, com.microsoft.windowsazure.services.media.models.CreateAssetOptions)
     */
    @Override
    public AssetInfo createAsset(CreateAssetOptions createAssetOptions) throws ServiceException {
        try {
            return service.createAsset(createAssetOptions);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#deleteAsset(java.lang.String)
     */
    @Override
    public void deleteAsset(String assetId) throws ServiceException {
        try {
            service.deleteAsset(assetId);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#getAsset(java.lang.String)
     */
    @Override
    public AssetInfo getAsset(String assetId) throws ServiceException {
        try {
            return service.getAsset(assetId);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#listAssets()
     */
    @Override
    public List<AssetInfo> listAssets() throws ServiceException {
        try {
            return service.listAssets();
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#listAssets(com.microsoft.windowsazure.services.media.models.ListAssetsOptions)
     */
    @Override
    public List<AssetInfo> listAssets(ListAssetsOptions listAssetsOptions) throws ServiceException {
        try {
            return service.listAssets(listAssetsOptions);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#updateAsset(com.microsoft.windowsazure.services.media.models.AssetInfo)
     */
    @Override
    public void updateAsset(String assetId, UpdateAssetOptions updateAssetOptions) throws ServiceException {
        try {
            service.updateAsset(assetId, updateAssetOptions);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#createAccessPolicy(double)
     */
    @Override
    public AccessPolicyInfo createAccessPolicy(String name, double durationInMinutes) throws ServiceException {
        try {
            return service.createAccessPolicy(name, durationInMinutes);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#createAccessPolicy(double, com.microsoft.windowsazure.services.media.models.CreateAccessPolicyOptions)
     */
    @Override
    public AccessPolicyInfo createAccessPolicy(String name, double durationInMinutes, CreateAccessPolicyOptions options)
            throws ServiceException {
        try {
            return service.createAccessPolicy(name, durationInMinutes, options);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#getAccessPolicies()
     */
    @Override
    public List<AccessPolicyInfo> listAccessPolicies() throws ServiceException {
        try {
            return service.listAccessPolicies();
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#deleteAccessPolicy(java.lang.String)
     */
    @Override
    public void deleteAccessPolicy(String id) throws ServiceException {
        try {
            service.deleteAccessPolicy(id);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#getAccessPolicy(java.lang.String)
     */
    @Override
    public AccessPolicyInfo getAccessPolicy(String id) throws ServiceException {
        try {
            return service.getAccessPolicy(id);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#listAccessPolicies(com.microsoft.windowsazure.services.media.models.ListAccessPolicyOptions)
     */@Override
    public List<AccessPolicyInfo> listAccessPolicies(ListAccessPolicyOptions options) throws ServiceException {
        try {
            return service.listAccessPolicies();
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#createLocator(java.lang.String, java.lang.String, com.microsoft.windowsazure.services.media.models.LocatorType, com.microsoft.windowsazure.services.media.models.CreateLocatorOptions)
     */
    @Override
    public LocatorInfo createLocator(String accessPolicyId, String assetId, LocatorType locatorType,
            CreateLocatorOptions createLocatorOptions) throws ServiceException {
        try {
            return service.createLocator(accessPolicyId, assetId, locatorType, createLocatorOptions);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#getLocator(java.lang.String)
     */
    @Override
    public LocatorInfo getLocator(String locatorId) throws ServiceException {
        try {
            return service.getLocator(locatorId);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#deleteLocator(java.lang.String)
     */
    @Override
    public void deleteLocator(String locatorId) throws ServiceException {
        try {
            service.deleteLocator(locatorId);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#updateLocator(java.lang.String, com.microsoft.windowsazure.services.media.models.UpdateLocatorOptions)
     */
    @Override
    public void updateLocator(String locatorId, UpdateLocatorOptions updateLocatorOptions) throws ServiceException {
        try {
            service.updateLocator(locatorId, updateLocatorOptions);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }

    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#listLocators()
     */
    @Override
    public ListLocatorsResult listLocators() throws ServiceException {
        try {
            return service.listLocators();
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#listLocators(com.microsoft.windowsazure.services.media.models.ListLocatorsOptions)
     */
    @Override
    public ListLocatorsResult listLocators(ListLocatorsOptions listLocatorsOptions) throws ServiceException {
        try {
            return service.listLocators(listLocatorsOptions);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#createLocator(java.lang.String, java.lang.String, com.microsoft.windowsazure.services.media.models.LocatorType)
     */
    @Override
    public LocatorInfo createLocator(String accessPolicyId, String assetId, LocatorType locatorType)
            throws ServiceException {
        try {
            return service.createLocator(accessPolicyId, assetId, locatorType);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ListMediaProcessorsResult listMediaProcessors() throws ServiceException {
        try {
            return service.listMediaProcessors();
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public ListMediaProcessorsResult listMediaProcessors(ListMediaProcessorsOptions listMediaProcessorsOptions)
            throws ServiceException {
        try {
            return service.listMediaProcessors(listMediaProcessorsOptions);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }

    }
}
