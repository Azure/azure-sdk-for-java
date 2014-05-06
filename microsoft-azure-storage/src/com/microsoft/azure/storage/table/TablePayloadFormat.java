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
 * Describes the payload formats supported for Tables.
 */
public enum TablePayloadFormat {

    /**
     * Use AtomPub.
     * 
     * @Deprecated Deprecated as of 0.7.0 in favor of Json format.
     */
    @Deprecated
    AtomPub,

    /**
     * Use JSON with full metadata.
     */
    JsonFullMetadata,

    /**
     * Use JSON with minimal metadata.
     */
    Json,

    /**
     * Use JSON with no metadata.
     */
    JsonNoMetadata
}