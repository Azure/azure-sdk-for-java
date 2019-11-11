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
import javax.xml.bind.annotation.XmlElement;

/**
 * This type maps the XML returned in the odata ATOM serialization for Asset
 * entities.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class MediaProcessorType implements MediaServiceDTO {

    @XmlElement(name = "Id", namespace = Constants.ODATA_DATA_NS)
    private String id;

    @XmlElement(name = "Name", namespace = Constants.ODATA_DATA_NS)
    private String name;

    @XmlElement(name = "Description", namespace = Constants.ODATA_DATA_NS)
    private String description;

    @XmlElement(name = "Sku", namespace = Constants.ODATA_DATA_NS)
    private String sku;

    @XmlElement(name = "Vendor", namespace = Constants.ODATA_DATA_NS)
    private String vendor;

    @XmlElement(name = "Version", namespace = Constants.ODATA_DATA_NS)
    private String version;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public MediaProcessorType setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public MediaProcessorType setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return this.description;
    }

    public MediaProcessorType setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getSku() {
        return this.sku;
    }

    public MediaProcessorType setSku(String sku) {
        this.sku = sku;
        return this;
    }

    public String getVendor() {
        return vendor;
    }

    public MediaProcessorType setVendor(String vendor) {
        this.vendor = vendor;
        return this;
    }

    public String getVersion() {
        return this.version;
    }

    public MediaProcessorType setVersion(String version) {
        this.version = version;
        return this;
    }
}
