package com.azure.mixedreality.remoterendering.models;

import java.util.List;

public class RemoteRenderingServiceError {
    private String code;
    private String message;
    private String target;
    private RemoteRenderingServiceError innerError;
    private List<RemoteRenderingServiceError> rootErrors;

    public RemoteRenderingServiceError setCode(String code) {
        this.code = code;
        return this;
    }

    public RemoteRenderingServiceError setMessage(String message) {
        this.message = message;
        return this;
    }

    public RemoteRenderingServiceError setTarget(String target) {
        this.target = target;
        return this;
    }

    public RemoteRenderingServiceError setInnerError(RemoteRenderingServiceError innerError) {
        this.innerError = innerError;
        return this;
    }

    public RemoteRenderingServiceError setRootErrors(List<RemoteRenderingServiceError> rootErrors) {
        this.rootErrors = rootErrors;
        return this;
    }


    /**
     * Get the code property: Error code.
     *
     * @return the code value.
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Get the message property: A human-readable representation of the error.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Get the target property: The target of the particular error (e.g., the name of the property in error).
     *
     * @return the target value.
     */
    public String getTarget() {
        return this.target;
    }

    /**
     * Get the innerError property: An object containing more specific information than the current object about the
     * error.
     *
     * @return the innerError value.
     */
    public RemoteRenderingServiceError getInnerError() {
        return this.innerError;
    }

    /**
     * List of errors that led to this reported error.
     *
     * @return the list of errors.
     */
    public List<RemoteRenderingServiceError> listRootErrors() {
        return this.rootErrors;
    }
}
