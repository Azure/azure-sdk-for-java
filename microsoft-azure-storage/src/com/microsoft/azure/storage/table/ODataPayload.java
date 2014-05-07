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

import java.util.ArrayList;

/**
 * Reserved for internal use. A class that represents an OData payload and resulting entities.
 */
final class ODataPayload<T> {
    /**
     * A collection of table entities.
     */
    ArrayList<T> results;

    /**
     * A collection of {@link TableResults} which include additional information about the entities returned by an
     * operation.
     */
    ArrayList<TableResult> tableResults;

    /**
     * Constructs an {@link ODataPayload} instance with new empty entity and {@link TableResult} collections.
     */
    ODataPayload() {
        this.results = new ArrayList<T>();
        this.tableResults = new ArrayList<TableResult>();
    }
}
