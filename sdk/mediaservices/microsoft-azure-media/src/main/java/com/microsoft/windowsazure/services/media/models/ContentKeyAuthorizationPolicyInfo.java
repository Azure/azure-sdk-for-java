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

package com.microsoft.windowsazure.services.media.models;

import com.microsoft.windowsazure.services.media.implementation.ODataEntity;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.ContentKeyAuthorizationPolicyType;

/**
 * Type containing data about content key authorization policy options.
 * 
 */
public class ContentKeyAuthorizationPolicyInfo extends ODataEntity<ContentKeyAuthorizationPolicyType> {

    /**
     * Creates a new {@link ContentKeyAuthorizationPolicyInfo} wrapping the
     * given ATOM entry and content objects.
     * 
     * @param entry
     *            the entry
     * @param content
     *            the content
     */
    public ContentKeyAuthorizationPolicyInfo(EntryType entry, ContentKeyAuthorizationPolicyType content) {
        super(entry, content);
    }

    /**
     * Get the content key authorization policy id.
     * 
     * @return the id.
     */
    public String getId() {
        return getContent().getId();
    }

    /**
     * Get the content key authorization policy name.
     * 
     * @return the name.
     */
    public String getName() {
        return getContent().getName();
    }

    /**
     * Get a link to the content key authorization policy's options
     * 
     * @return the link to the content key authorization policy's options
     */
    public LinkInfo<ContentKeyAuthorizationPolicyOptionInfo> getOptions() {
        return this.<ContentKeyAuthorizationPolicyOptionInfo> getRelationLink("Options");
    }
}
