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

package com.microsoft.windowsazure.services.media.implementation;

import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;

/**
 * Class wrapping deserialized OData entities. Allows easy
 * access to entry and content types.
 * 
 */
public abstract class ODataEntity<T> {

    private final EntryType entry;
    private final T content;

    public ODataEntity(EntryType entry, T content) {
        this.entry = entry;
        this.content = content;
    }

    /**
     * @return the entry
     */
    public EntryType getEntry() {
        return entry;
    }

    /**
     * @return the content
     */
    public T getContent() {
        return content;
    }
}
