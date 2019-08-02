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

import java.util.Date;

import com.microsoft.windowsazure.services.media.implementation.ODataEntity;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.NotificationEndPointType;

/**
 * Type containing data about notification end points.
 * 
 */
public class NotificationEndPointInfo extends
        ODataEntity<NotificationEndPointType> {

    /**
     * Creates a new {@link NotificationEndPointInfo} wrapping the given ATOM
     * entry and content objects.
     * 
     * @param entry
     *            Entry containing this AccessPolicy data
     * @param content
     *            Content with the AccessPolicy data
     */
    public NotificationEndPointInfo(EntryType entry,
            NotificationEndPointType content) {
        super(entry, content);
    }

    /**
     * Get the notification end point id.
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
     * Get the creation date.
     * 
     * @return the date.
     */
    public Date getCreated() {
        return getContent().getCreated();
    }

    /**
     * Get the type of the end point.
     * 
     * @return the end point type.
     */
    public EndPointType getEndPointType() {
        return EndPointType.fromCode(getContent().getEndPointType());
    }

    /**
     * Gets the end point address.
     * 
     * @return the end point address
     */
    public String getEndPointAddress() {
        return getContent().getEndPointAddress();
    }

}
