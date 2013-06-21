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
package com.microsoft.windowsazure.services.management.implementation;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.utils.ServiceExceptionFactory;
import com.microsoft.windowsazure.services.management.ManagementContract;
import com.microsoft.windowsazure.services.management.models.AffinityGroupInfo;
import com.microsoft.windowsazure.services.management.models.CreateAffinityGroupOptions;
import com.microsoft.windowsazure.services.management.models.CreateAffinityGroupResult;
import com.microsoft.windowsazure.services.management.models.DeleteAffinityGroupResult;
import com.microsoft.windowsazure.services.management.models.GetAffinityGroupResult;
import com.microsoft.windowsazure.services.management.models.ListResult;
import com.microsoft.windowsazure.services.management.models.UpdateAffinityGroupOptions;
import com.microsoft.windowsazure.services.management.models.UpdateAffinityGroupResult;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

public class ManagementExceptionProcessor implements ManagementContract {

    private final ManagementContract next;
    static Log log = LogFactory.getLog(ManagementContract.class);

    public ManagementExceptionProcessor(ManagementContract next) {
        this.next = next;
    }

    @Inject
    public ManagementExceptionProcessor(ManagementRestProxy next) {
        this.next = next;
    }

    @Override
    public ManagementContract withFilter(ServiceFilter filter) {
        return new ManagementExceptionProcessor(next.withFilter(filter));
    }

    private ServiceException processCatch(ServiceException e) {
        log.warn(e.getMessage(), e.getCause());
        return ServiceExceptionFactory.process("serviceBus", e);
    }

    @Override
    public ListResult<AffinityGroupInfo> listAffinityGroups() throws ServiceException {
        try {
            return next.listAffinityGroups();
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public CreateAffinityGroupResult createAffinityGroup(String name, String label, String location)
            throws ServiceException {
        try {
            return next.createAffinityGroup(name, label, location);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public GetAffinityGroupResult getAffinityGroup(String name) throws ServiceException {
        try {
            return next.getAffinityGroup(name);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public DeleteAffinityGroupResult deleteAffinityGroup(String name) throws ServiceException {
        try {
            return next.deleteAffinityGroup(name);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public CreateAffinityGroupResult createAffinityGroup(String name, String label, String location,
            CreateAffinityGroupOptions createAffinityGroupOptions) throws ServiceException {
        try {
            return next.createAffinityGroup(name, label, location, createAffinityGroupOptions);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public UpdateAffinityGroupResult updateAffinityGroup(String name, String label,
            UpdateAffinityGroupOptions updateAffinityGroupOptions) throws ServiceException {
        try {
            return next.updateAffinityGroup(name, label, updateAffinityGroupOptions);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

}
