/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.microsoft.windowsazure.services.servicebus.models;

/**
 * Represents the result of a <code>getEventHub</code> operation.
 */
public class GetEventHubResult {

    private EventHubInfo value;

    /**
     * Creates an instance of the <code>GetEventHubResult</code> class.
     *
     * @param value
     *            A {@link EventHubInfo} object assigned as the value of the
     *            result.
     */
    public GetEventHubResult(EventHubInfo value) {
        this.setValue(value);
    }

    /**
     * Specfies the value of the result.
     *
     * @param value
     *            A {@link EventHubInfo} object assigned as the value of the
     *            result.
     */
    public void setValue(EventHubInfo value) {
        this.value = value;
    }

    /**
     * Returns the value of the result.
     *
     * @return A {@link EventHubInfo} object that represents the value of the
     *         result.
     */
    public EventHubInfo getValue() {
        return value;
    }

}
