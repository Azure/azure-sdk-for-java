/*
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
package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.blob.models.ModifiedAccessConditions;

/**
 * Constants for common values of HTTP Etags to be used in conjunction with {@link ModifiedAccessConditions}
 */
public final class ETag {

    /**
     * Used for matching with no ETag.
     */
    //TODO: Validate that this should not be null. Whatever it is should not set the header.
    public static final String NONE = "";

    /**
     * Used for matching with any ETag.
     */
    public static final String ANY = "*";

}
