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
 * This type maps the XML returned in the odata ATOM serialization for
 * ErrorDetail entities.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorDetailType implements MediaServiceDTO {

    /** The code. */
    @XmlElement(name = "Code", namespace = Constants.ODATA_DATA_NS)
    private String code;

    /** The message. */
    @XmlElement(name = "Message", namespace = Constants.ODATA_DATA_NS)
    private String message;

    /**
     * Gets the code.
     * 
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the code.
     * 
     * @param code
     *            the id to set
     * @return the error detail type
     */
    public ErrorDetailType setCode(String code) {
        this.code = code;
        return this;
    }

    /**
     * Gets the message.
     * 
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message.
     * 
     * @param message
     *            the message to set
     * @return the error detail type
     */
    public ErrorDetailType setMessage(String message) {
        this.message = message;
        return this;
    }

}
