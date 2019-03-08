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

import java.util.HashMap;
import java.util.Map;

/**
 * Contains metadata key/value pairs to be associated with a storage resource. The user may store any additional
 * information about the resource that they like using this map. It is passed to create and setMetadata methods on any
 * URL type. Null may be passed to set no metadata.
 */
public final class Metadata extends HashMap<String, String> {
    public static final Metadata NONE = new Metadata();

    public Metadata() {
        super();
    }

    public Metadata(Map<? extends String, ? extends String> m) {
        super(m);
    }
}
