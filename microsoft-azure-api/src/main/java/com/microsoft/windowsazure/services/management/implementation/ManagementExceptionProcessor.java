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
import com.microsoft.windowsazure.services.management.models.GetAffinityGroupResult;
import com.microsoft.windowsazure.services.management.models.ListResult;
import com.microsoft.windowsazure.services.management.models.UpdateAffinityGroupOptions;
import com.microsoft.windowsazure.services.management.models.UpdateAffinityGroupResult;

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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CreateAffinityGroupResult createAffinityGroup(String expectedAffinityGroupName, String expectedLabel,
            String expectedLocation) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GetAffinityGroupResult getAffinityGroup(String affinityGroupName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteAffinityGroup(String affinityGroupName) {
        // TODO Auto-generated method stub

    }

    @Override
    public CreateAffinityGroupResult createAffinityGroup(String expectedAffinityGroupName, String expectedLabel,
            String expectedLocation, CreateAffinityGroupOptions createAffinityGroupOptions) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UpdateAffinityGroupResult updateAffinityGroup(String expectedAffinityGroupLabel,
            UpdateAffinityGroupOptions updateAffinityGroupOptions) {
        // TODO Auto-generated method stub
        return null;
    }

}
