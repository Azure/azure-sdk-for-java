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

package com.microsoft.azure.storage.table;

/**
 * Reserved for internal use. An enumeration type which represents the type of operation a {@link TableOperation}
 * represents.
 */
enum TableOperationType {

    /**
     * The table operation creates an entity which does not yet exist.
     */
    INSERT,

    /**
     * The table operation delete an existing entity.
     */
    DELETE,

    /**
     * The table operation replaces an existing entity.
     */
    REPLACE,

    /**
     * The table operation retrieves an existing entity.
     */
    RETRIEVE,

    /**
     * The table operation updates an existing entity.
     */
    MERGE,

    /**
     * The table operation replaces an existing entity or inserts it.
     */
    INSERT_OR_REPLACE,

    /**
     * The table operation updates an existing entity or inserts it.
     */
    INSERT_OR_MERGE;

    /**
     * Gets the {@link TableUpdateType} associated the operation type, if applicable. Applies to
     * {@link #INSERT_OR_MERGE} and {@link #INSERT_OR_REPLACE} values.
     * 
     * @return
     *         The applicable {@link TableUpdateType}, or <code>null</code>.
     */
    protected TableUpdateType getUpdateType() {
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
