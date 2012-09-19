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

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.utils.ServiceExceptionFactory;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * Wrapper implementation of <code>MediaServicesContract</code> that
 * translates exceptions into ServiceExceptions.
 * 
 */
public class MediaExceptionProcessor implements MediaContract {

    private final MediaContract next;
    static Log log = LogFactory.getLog(MediaContract.class);

    public MediaExceptionProcessor(MediaContract next) {
        this.next = next;
    }

    @Inject
    public MediaExceptionProcessor(MediaRestProxy next) {
        this.next = next;
    }

    @Override
    public MediaContract withFilter(ServiceFilter filter) {
        return new MediaExceptionProcessor(next.withFilter(filter));
    }

    private ServiceException processCatch(ServiceException e) {
        log.warn(e.getMessage(), e.getCause());
        return ServiceExceptionFactory.process("MediaServices", e);
    }

    @Override
    public AssetInfo createAsset(String name) throws ServiceException {
        try {
            return next.createAsset(name);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }
}
