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
package com.microsoft.windowsazure.services.table;

/**
 * This class contains static strings used to identify parts of a service configuration instance associated with the
 * Windows Azure Table service.
 * <p>
 * These values must not be altered.
 */
public class TableConfiguration {
    /**
     * The Table configuration account name constant. This <code>String</code> value is used as a key in the
     * configuration file, to identify the value for the DNS prefix name for the storage account.
     */
    public final static String ACCOUNT_NAME = "table.accountName";

    /**
     * The Table configuration account key constant. This <code>String</code> value is used as a key in the
     * configuration file, to identify the value for the storage service account key.
     */
    public final static String ACCOUNT_KEY = "table.accountKey";

    /**
     * The Table configuration URI constant. This <code>String</code> value is used as a key in the
     * configuration file, to identify the URI value for the Table service REST API address for the
     * storage account.
     */
    public final static String URI = "table.uri";
}
