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
import com.microsoft.windowsazure.services.management.models.ListResult;
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
    public ListResult<AffinityGroupInfo> listAffinityGroups(String subscriptionId) throws ServiceException {
        try {
            return next.listAffinityGroups(subscriptionId);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void listVirtualMachines(String subscriptionId) {
        // TODO Auto-generated method stub

    }

}
