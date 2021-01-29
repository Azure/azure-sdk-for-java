package com.azure.mixedreality.remoterendering.models;

import com.azure.mixedreality.remoterendering.implementation.models.Error;

import java.util.List;
import java.util.stream.Collectors;

public class RemoteRenderingServiceError {
    private Error error;

    RemoteRenderingServiceError(Error error) {
        this.error = error;
    }

    /**
     * Get the code property: Error code.
     *
     * @return the code value.
     */
    public String getCode() {
        return error.getCode();
    }

    /**
     * Get the message property: A human-readable representation of the error.
     *
     * @return the message value.
     */
    public String getMessage() {
        return error.getMessage();
    }

    /**
     * Get the target property: The target of the particular error (e.g., the name of the property in error).
     *
     * @return the target value.
     */
    public String getTarget() {
        return error.getTarget();
    }

    /**
     * Get the innerError property: An object containing more specific information than the current object about the
     * error.
     *
     * @return the innerError value.
     */
    public RemoteRenderingServiceError getInnerError() {
        var innerError = error.getInnerError();
        return innerError != null ? new RemoteRenderingServiceError(innerError) : null;
    }

    /**
     * List of errors that led to this reported error.
     *
     * @return the list of errors.
     */
    public List<RemoteRenderingServiceError> listRootErrors() {
        return error.getDetails().stream().map(error -> new RemoteRenderingServiceError(error)).collect(Collectors.toList());
    }
}
