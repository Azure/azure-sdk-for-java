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

import java.util.ArrayList;
import java.util.List;

import com.microsoft.windowsazure.services.media.implementation.ODataEntity;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.ContentKeyAuthorizationPolicyOptionType;
import com.microsoft.windowsazure.services.media.implementation.content.ContentKeyAuthorizationPolicyRestrictionType;

/**
 * Type containing data about content key authorization policy options.
 * 
 */
public class ContentKeyAuthorizationPolicyOptionInfo extends ODataEntity<ContentKeyAuthorizationPolicyOptionType> {

    /**
     * Creates a new {@link ContentKeyAuthorizationPolicyOptionInfo} wrapping
     * the given ATOM entry and content objects.
     * 
     * @param entry
     *            the entry
     * @param content
     *            the content
     */
    public ContentKeyAuthorizationPolicyOptionInfo(EntryType entry, ContentKeyAuthorizationPolicyOptionType content) {
        super(entry, content);
    }

    /**
     * Get the access policy id.
     * 
     * @return the id.
     */
    public String getId() {
        return getContent().getId();
    }

    /**
     * Get the name.
     * 
     * @return the name.
     */
    public String getName() {
        return getContent().getName();
    }

    /**
     * Get the content key authorization policy options key delivery type.
     * 
     * @return the key delivery type.
     */
    public int getKeyDeliveryType() {
        return getContent().getKeyDeliveryType();
    }

    /**
     * Get the content key authorization policy options key delivery
     * configuration.
     * 
     * @return the key delivery configuration.
     */
    public String getKeyDeliveryConfiguration() {
        return getContent().getKeyDeliveryConfiguration();
    }

    /**
     * Get the content key authorization policy options restrictions.
     * 
     * @return the restrictions.
     */
    public List<ContentKeyAuthorizationPolicyRestriction> getRestrictions() {
        List<ContentKeyAuthorizationPolicyRestriction> result = new ArrayList<ContentKeyAuthorizationPolicyRestriction>();
        List<ContentKeyAuthorizationPolicyRestrictionType> restrictionsTypes = getContent().getRestrictions();

        if (restrictionsTypes != null) {
            for (ContentKeyAuthorizationPolicyRestrictionType restrictionType : restrictionsTypes) {
                ContentKeyAuthorizationPolicyRestriction contentKeyAuthPolicyRestriction = new ContentKeyAuthorizationPolicyRestriction(
                        restrictionType.getName(), restrictionType.getKeyRestrictionType(),
                        restrictionType.getRequirements());
                result.add(contentKeyAuthPolicyRestriction);
            }
        }

        return result;
    }

}
