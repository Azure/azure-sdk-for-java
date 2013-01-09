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
package com.microsoft.windowsazure.services.table.models;

import com.microsoft.windowsazure.services.table.TableContract;

/**
 * Represents the response to a request for a single table entry in the list of tables in the storage account returned
 * from a Table Service REST API Query Tables operation. This is returned by calls to implementations of
 * {@link TableContract#getTable(String)} and {@link TableContract#getTable(String, TableServiceOptions)}.
 * <p>
 * See the <a href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179405.aspx">Query Tables</a> documentation
 * on MSDN for details of the underlying Table Service REST API operation.
 */
public class GetTableResult {
    private TableEntry tableEntry;

    /**
     * Gets the table entry returned in the server response.
     * 
     * @return
     *         A {@link TableEntry} instance representing the table entry returned in the server response.
     */
    public TableEntry getTableEntry() {
        return tableEntry;
    }

    /**
     * Reserved for internal use. Sets the table entry value from the <strong>TableName</strong> entity in the
     * properties of the <strong>entry</strong> entity returned in the body of the server response.
     * <p>
     * This method is invoked by the API to set the value from the Table Service REST API operation response returned by
     * the server.
     * 
     * @param tableEntry
     *            A {@link TableEntry} instance representing the table entry returned in the server response.
     */
    public void setTableEntry(TableEntry tableEntry) {
        this.tableEntry = tableEntry;
    }
}
