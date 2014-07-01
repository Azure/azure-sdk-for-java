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
package com.microsoft.azure.storage;

/**
 * Specifies the type of a continuation token.
 */
public enum ResultContinuationType {
    /**
     * Specifies no continuation.
     */
    NONE,

    /**
     * Specifies the token is a blob listing continuation token.
     */
    BLOB,

    /**
     * Specifies the token is a container listing continuation token.
     */
    CONTAINER,

    /**
     * Specifies the token is a file listing continuation token.
     */
    FILE,

    /**
     * Specifies the token is a queue listing continuation token (reserved for future use).
     */
    QUEUE,

    /**
     * Specifies the token is a table query continuation token (reserved for future use).
     */
    TABLE,

    /**
     * Specifies the token is a share listing continuation token.
     */
    SHARE,
}
