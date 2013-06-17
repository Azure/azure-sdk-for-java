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
import com.microsoft.windowsazure.services.management.models.ListResult;

/**
 * 
 * Defines the service bus contract.
 * 
 */
public interface ManagementContract extends FilterableService<ManagementContract> {

    /**
     * Renew subscription lock.
     * 
     * @param topicName
     *            A <code>String</code> object that represents the name of the topic.
     * @param queueName
     *            A <code>String</code> object that represents the name of the queue.
     * @param messageId
     *            A <code>String</code> object that represents the ID of the message.
     * @param lockToken
     *            A <code>String</code> object that represents the token of the lock.
     * @throws ServiceException
     *             If a service exception is encountered.
     */
    // void createAffinityGroup(AffinityGroup affinityGroup) throws ServiceException;

    ListResult<AffinityGroupInfo> listAffinityGroups(String subscriptionId) throws ServiceException;

    void listVirtualMachines(String subscriptionId);

}
