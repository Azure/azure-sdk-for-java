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

package com.microsoft.windowsazure.services.table.client;

import com.microsoft.windowsazure.services.core.storage.RequestOptions;

/**
 * Represents a set of timeout and retry policy options that may be specified for a table operation request.
 */
public class TableRequestOptions extends RequestOptions {
    /**
     * Reserved for internal use. Initializes the timeout and retry policy for this <code>TableRequestOptions</code>
     * instance, if they are currently <code>null</code>, using the values specified in the {@link CloudTableClient}
     * parameter.
     * 
     * @param client
     *            The {@link CloudTableClient} client object to copy the timeout and retry policy from.
     */
    protected void applyDefaults(final CloudTableClient client) {
        super.applyBaseDefaults(client);
    }
}
