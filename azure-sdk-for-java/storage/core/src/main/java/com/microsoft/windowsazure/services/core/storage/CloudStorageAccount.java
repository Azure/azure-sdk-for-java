/**
 * Copyright 2011 Microsoft Corporation
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
package com.microsoft.windowsazure.services.core.storage;


/**
 * Represents a Windows Azure storage account.
 */
public class CloudStorageAccount  {
    /**
     * Represents the setting name for the account key.
     */
    public static final String ACCOUNT_KEY_NAME = "AccountKey";

    /**
     * Represents the setting name for the account name.
     */
    public static final String ACCOUNT_NAME_NAME = "AccountName";

    /**
     * The setting name for using the default storage endpoints with the specified protocol.
     */
    public static final String DEFAULT_ENDPOINTS_PROTOCOL_NAME = "DefaultEndpointsProtocol";

    /**
     * Represents the setting name for a shared access key.
     */
    public static final String SHARED_ACCESS_SIGNATURE_NAME = "SharedAccessSignature";
}
