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

package com.microsoft.windowsazure.services.media.implementation.content;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * XML Serialization class for odata m:action elements
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ODataActionType {

    @XmlAttribute(required = true)
    private String metadata;

    @XmlAttribute(required = true)
    private String target;

    @XmlAttribute(required = true)
    private String title;

    /**
     * Get metadata
     * 
     * @return the metadata
     */
    public String getMetadata() {
        return metadata;
    }

    /**
     * Set metadata
     * 
     * @param metadata
     */
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    /**
     * Get target
     * 
     * @return the target
     */
    public String getTarget() {
        return target;
    }

    /**
     * set target
     * 
     * @param target
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * Get title
     * 
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * set title
     * 
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
