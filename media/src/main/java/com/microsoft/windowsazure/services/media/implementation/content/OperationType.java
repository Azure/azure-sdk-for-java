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
 * This type maps the XML returned in the odata ATOM serialization for operation
 * entities.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class OperationType implements MediaServiceDTO {

    /** The id. */
    @XmlElement(name = "Id", namespace = Constants.ODATA_DATA_NS)
    private String id;

    /** The target entity id. */
    @XmlElement(name = "TargetEntityId", namespace = Constants.ODATA_DATA_NS)
    private String targetEntityId;

    /** The state. */
    @XmlElement(name = "State", namespace = Constants.ODATA_DATA_NS)
    private String state;

    /** The error code. */
    @XmlElement(name = "ErrorCode", namespace = Constants.ODATA_DATA_NS)
    private String errorCode;

    /** The error message. */
    @XmlElement(name = "ErrorMessage", namespace = Constants.ODATA_DATA_NS)
    private String errorMessage;

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     * 
     * @param id
     *            the id to set
     * @return the operation type
     */
    public OperationType setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets the target entity id.
     * 
     * @return the target entity id
     */
    public String getTargetEntityId() {
        return targetEntityId;
    }

    /**
     * Sets the target entity id.
     * 
     * @param targetEntityId
     *            the target entity id
     * @return the operation type
     */
    public OperationType setTargetEntityId(String targetEntityId) {
        this.targetEntityId = targetEntityId;
        return this;
    }

    /**
     * Gets the state.
     * 
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the state.
     * 
     * @param state
     *            the state to set
     * @return the operation type
     */
    public OperationType setState(String state) {
        this.state = state;
        return this;
    }

    /**
     * Gets the error code.
     * 
     * @return the error code
     */
    public String getErrorCode() {
        return this.errorCode;
    }

    /**
     * Sets the error code.
     * 
     * @param errorCode
     *            the error code
     * @return the operation type
     */
    public OperationType setErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    /**
     * Gets the error message.
     * 
     * @return the error message
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * Sets the error message.
     * 
     * @param errorMessage
     *            the error message
     * @return the operation type
     */
    public OperationType setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

}
