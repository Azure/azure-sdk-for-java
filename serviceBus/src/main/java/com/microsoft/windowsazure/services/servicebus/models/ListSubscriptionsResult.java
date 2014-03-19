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
package com.microsoft.windowsazure.services.servicebus.models;

import java.util.List;

/**
 * Represents the result of a <code>listSubscriptions</code> operation.
 */
public class ListSubscriptionsResult {

    private List<SubscriptionInfo> items;

    /**
     * Returns the items in the result list.
     * 
     * @return A <code>List</code> of {@link SubscriptionInfo} objects that
     *         represent the items in the result list.
     */
    public List<SubscriptionInfo> getItems() {
        return items;
    }

    /**
     * Specifies the items in the result list.
     * 
     * @param items
     *            A <code>List</code> object that contains the
     *            {@link SubscriptionInfo} objects assigned as the value of the
     *            result.
     */
    public void setItems(List<SubscriptionInfo> items) {
        this.items = items;
    }
}
