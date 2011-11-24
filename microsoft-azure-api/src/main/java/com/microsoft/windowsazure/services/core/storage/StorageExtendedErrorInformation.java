package com.microsoft.windowsazure.services.core.storage;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Represents extended error information returned by the Windows Azure storage service.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public final class StorageExtendedErrorInformation implements Serializable {
    /**
     * The serialization version number.
     */
    private static final long serialVersionUID = 1527013626991334677L;

    /**
     * Represents additional error details, as a <code>java.util.HashMap</code> object.
     */
    private HashMap<String, String[]> additionalDetails;

    /**
     * Represents the storage service error code.
     */
    private String errorCode;

    /**
     * Represents the storage service error message.
     */
    private String errorMessage;

    /**
     * Creates an instance of the <code>StorageExtendedErrorInformation</code> class.
     */
    public StorageExtendedErrorInformation() {
        this.setAdditionalDetails(new HashMap<String, String[]>());
    }

    /**
     * @return the additionalDetails
     */
    public HashMap<String, String[]> getAdditionalDetails() {
        return this.additionalDetails;
    }

    /**
     * @return the errorCode
     */
    public String getErrorCode() {
        return this.errorCode;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * @param additionalDetails
     *            the additionalDetails to set
     */
    protected void setAdditionalDetails(final HashMap<String, String[]> additionalDetails) {
        this.additionalDetails = additionalDetails;
    }

    /**
     * @param errorCode
     *            the errorCode to set
     */
    public void setErrorCode(final String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * @param errorMessage
     *            the errorMessage to set
     */
    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
