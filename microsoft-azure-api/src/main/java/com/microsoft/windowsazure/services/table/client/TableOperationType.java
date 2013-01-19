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

/**
 * Reserved for internal use. An enumeration type which represents the type of operation a {@link TableOperation}
 * represents.
 */
enum TableOperationType {
    INSERT, DELETE, REPLACE, RETRIEVE, MERGE, INSERT_OR_REPLACE, INSERT_OR_MERGE;

    /**
     * Gets the {@link TableUpdateType} associated the operation type, if applicable. Applies to
     * {@link #INSERT_OR_MERGE} and {@link #INSERT_OR_REPLACE} values.
     * 
     * @return
     *         The applicable {@link TableUpdateType}, or <code>null</code>.
     */
    public TableUpdateType getUpdateType() {
        if (this == INSERT_OR_MERGE) {
            return TableUpdateType.MERGE;
        }
        else if (this == INSERT_OR_REPLACE) {
            return TableUpdateType.REPLACE;
        }
        else {
            return null;
        }
    }
}
