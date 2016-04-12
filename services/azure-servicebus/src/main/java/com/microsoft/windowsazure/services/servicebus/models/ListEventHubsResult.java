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
 * Represents the result of a <code>listEventHubs</code> operation.
 */
public class ListEventHubsResult {

    private List<EventHubInfo> items;

    /**
     * Returns the items in the result list.
     *
     * @return A <code>List</code> of {@link EventHubInfo} objects that represent
     *         the items in the result list.
     */
    public List<EventHubInfo> getItems() {
        return items;
    }

    /**
     * Specifies the items in the result list.
     *
     * @param items
     *            A <code>List</code> object that contains the {@link EventHubInfo}
     *            objects assigned as the value of the result.
     */
    public void setItems(List<EventHubInfo> items) {
        this.items = items;
    }
}
