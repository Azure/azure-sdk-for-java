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
import com.microsoft.windowsazure.services.media.implementation.content.OperationType;

/**
 * Data about a Media Services operation entity.
 * 
 */
public class OperationInfo extends ODataEntity<OperationType> {

    /**
     * Instantiates a new operation info.
     * 
     * @param entry
     *            the entry
     * @param content
     *            the content
     */
    public OperationInfo(EntryType entry, OperationType content) {
        super(entry, content);
    }

    /**
     * Get the operation id.
     * 
     * @return the id
     */
    public String getId() {
        return getContent().getId();
    }

    /**
     * Get the target entity id.
     * 
     * @return the target entity id.
     */
    public String getTargetEntityId() {
        return this.getContent().getTargetEntityId();
    }

    /**
     * Get the operation state.
     * 
     * @return the state
     */
    public OperationState getState() {
        return OperationState.fromCode(getContent().getState());
    }

    /**
     * Gets the error code.
     * 
     * @return the error code
     */
    public String getErrorCode() {
        return this.getContent().getErrorCode();
    }

    /**
     * Gets the error message.
     * 
     * @return the error message
     */
    public String getErrorMessage() {
        return this.getErrorMessage();
    }
}
