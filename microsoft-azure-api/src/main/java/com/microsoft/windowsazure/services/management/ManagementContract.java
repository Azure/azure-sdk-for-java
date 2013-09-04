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
package com.microsoft.windowsazure.services.management;

import com.microsoft.windowsazure.services.core.FilterableService;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.management.models.AffinityGroupInfo;
import com.microsoft.windowsazure.services.management.models.CreateAffinityGroupOptions;
import com.microsoft.windowsazure.services.management.models.CreateAffinityGroupResult;
import com.microsoft.windowsazure.services.management.models.DeleteAffinityGroupResult;
import com.microsoft.windowsazure.services.management.models.GetAffinityGroupResult;
import com.microsoft.windowsazure.services.management.models.ListResult;
import com.microsoft.windowsazure.services.management.models.UpdateAffinityGroupOptions;
import com.microsoft.windowsazure.services.management.models.UpdateAffinityGroupResult;

/**
 * 
 * Defines the service management contract.
 * 
 */
public interface ManagementContract extends FilterableService<ManagementContract> {

    /**
     * Gets the information of an affinity group.
     * 
     * @param name
     *            The name of the affinity group.
     * @return A <code>GetAffinityGroupResult</code> instance which contains the information of the affinity group.
     * @throws ServiceException
     *             the service exception
     */
    GetAffinityGroupResult getAffinityGroup(String name) throws ServiceException;

    /**
     * Creates an affinity group.
     * 
     * @param createAffinityGroupOptions
     *            The options to create an affinity group.
     * @return A <code>CreateAffinityGroupResult</code> instance which contains the information of the affinity group.
     * @throws ServiceException
     *             the service exception
     */
    CreateAffinityGroupResult createAffinityGroup(CreateAffinityGroupOptions createAffinityGroupOptions)
            throws ServiceException;

    /**
     * Delete affinity group.
     * 
     * @param name
     *            The name of the affinity group.
     * @return A <code>DeleteAffinityGroupResult</code> instance containing the result of delete affinity group
     *         operation.
     * 
     * @throws ServiceException
     *             the service exception
     */
    DeleteAffinityGroupResult deleteAffinityGroup(String name) throws ServiceException;

    /**
     * List affinity groups.
     * 
     * @return the result of the list affinity group operation.
     * @throws ServiceException
     *             the service exception
     */
    ListResult<AffinityGroupInfo> listAffinityGroups() throws ServiceException;

    /**
     * Update affinity group.
     * 
     * @param updateAffinityGroupOptions
     *            The options to update the affinity group.
     * @return A <code>UpdateAffinityGroupResult</code> class instance which contains the result of update affinity
     *         group operation.
     * 
     * @throws ServiceException
     *             the service exception
     */
    UpdateAffinityGroupResult updateAffinityGroup(UpdateAffinityGroupOptions updateAffinityGroupOptions)
            throws ServiceException;

}
