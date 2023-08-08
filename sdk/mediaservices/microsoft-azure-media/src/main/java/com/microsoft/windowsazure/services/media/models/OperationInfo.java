package com.microsoft.windowsazure.services.media.models;

import com.microsoft.windowsazure.services.media.implementation.ODataEntity;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.OperationType;

public class OperationInfo extends ODataEntity<OperationType> {

    public OperationInfo(EntryType entry, OperationType content) {
        super(entry, content);
    }

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public String getId() {
        return getContent().getId();
    }

    /**
     * Gets the target entity id.
     * 
     * @return the target entity id
     */
    public String getTargetEntityId() {
        return getContent().getTargetEntityId();
    }

    /**
     * Gets the state.
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
        return getContent().getErrorCode();
    }

    /**
     * Gets the error message.
     * 
     * @return the error message
     */
    public String getErrorMessage() {
        return getContent().getErrorMessage();
    }
}
