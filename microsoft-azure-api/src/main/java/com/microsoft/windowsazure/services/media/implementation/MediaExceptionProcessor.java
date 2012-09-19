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

package com.microsoft.windowsazure.services.media.implementation;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.utils.ServiceExceptionFactory;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.models.Asset;
import com.microsoft.windowsazure.services.media.models.ListAssetsOptions;

/**
 * Wrapper implementation of <code>MediaServicesContract</code> that
 * translates exceptions into ServiceExceptions.
 * 
 */
public class MediaExceptionProcessor implements MediaContract {

    private final MediaContract service;
    static Log log = LogFactory.getLog(MediaContract.class);

    public MediaExceptionProcessor(MediaContract service) {
        this.service = service;
    }

    @Inject
    public MediaExceptionProcessor(MediaRestProxy service) {
        this.service = service;
    }

    @Override
    public MediaContract withFilter(ServiceFilter filter) {
        return new MediaExceptionProcessor(service.withFilter(filter));
    }

    private ServiceException processCatch(ServiceException e) {
        log.warn(e.getMessage(), e.getCause());
        return ServiceExceptionFactory.process("MediaServices", e);
    }

    @Override
    public Asset createAsset(Asset asset) {
        return service.createAsset(asset);
    }

    @Override
    public Asset getAsset(Asset asset) {
        return service.getAsset(asset);
    }

    @Override
    public List<Asset> listAssets(ListAssetsOptions listAssetsOptions) {
        return service.listAssets(listAssetsOptions);
    }

    @Override
    public Asset updateAsset(Asset updatedAsset) {
        return service.updateAsset(updatedAsset);
    }

    @Override
    public void deleteAsset(String assetId) {
        service.deleteAsset(assetId);
    }

}
