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
package com.microsoft.windowsazure.services.queue.models;


/**
 * A wrapper class for the service properties returned in response to Queue
 * Service REST API operations. This is returned by calls to implementations of
 * {@link com.microsoft.windowsazure.services.queue.QueueContract#getServiceProperties()} and
 * {@link com.microsoft.windowsazure.services.queue.QueueContract#getServiceProperties(QueueServiceOptions)}.
 * <p>
 * See the <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/hh452243.aspx">Get
 * Queue Service Properties</a> documentation on MSDN for details of the
 * underlying Queue Service REST API operation.
 */
public class GetServicePropertiesResult {
    private ServiceProperties value;

    /**
     * Gets a {@link ServiceProperties} instance containing the service property
     * values associated with the storage account.
     * <p>
     * Modifying the values in the {@link ServiceProperties} instance returned
     * does not affect the values associated with the storage account. To change
     * the values in the storage account, call the
     * {@link com.microsoft.windowsazure.services.queue.QueueContract#setServiceProperties} method and pass the modified
     * {@link ServiceProperties} instance as a parameter.
     * 
     * @return A {@link ServiceProperties} instance containing the property
     *         values associated with the storage account.
     */
    public ServiceProperties getValue() {
        return value;
    }

    /**
     * Reserved for internal use. Sets the value of the
     * {@link ServiceProperties} instance associated with a storage service call
     * result. This method is invoked by the API to store service properties
     * returned by a call to a REST operation and is not intended for public
     * use.
     * 
     * @param value
     *            A {@link ServiceProperties} instance containing the property
     *            values associated with the storage account.
     */
    public void setValue(ServiceProperties value) {
        this.value = value;
    }
}
