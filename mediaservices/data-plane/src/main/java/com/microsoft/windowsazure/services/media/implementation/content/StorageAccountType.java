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
 * This type maps the XML returned in the odata ATOM serialization for StorageAccountType
 * entities.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class StorageAccountType implements MediaServiceDTO {

    @XmlElement(name = "Name", namespace = Constants.ODATA_DATA_NS)
    private String name;

    @XmlElement(name = "IsDefault", namespace = Constants.ODATA_DATA_NS)
    private boolean isdefault;

    @XmlElement(name = "BytesUsed", namespace = Constants.ODATA_DATA_NS)
    private long bytesUsed;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the isdefault
     */
    public boolean isDefault() {
        return isdefault;
    }

    /**
     * @param isdefault the isdefault to set
     */
    public void setAsDefault(boolean isdefault) {
        this.isdefault = isdefault;
    }

    /**
     * @return the bytesUsed
     */
    public long getBytesUsed() {
        return bytesUsed;
    }

    /**
     * @param bytesUsed the bytesUsed to set
     */
    public void setBytesUsed(long bytesUsed) {
        this.bytesUsed = bytesUsed;
    }

}
