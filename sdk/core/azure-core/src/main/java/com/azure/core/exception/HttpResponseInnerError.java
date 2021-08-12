package com.azure.core.exception;

/**
 * The inner error of a {@link HttpResponseError}.
 */
public class HttpResponseInnerError {

    private String code;
    private HttpResponseInnerError innerError;

    /**
     * Returns the error code of the inner error.
     *
     * @return the error code of this inner error.
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the error code of the inner error.
     *
     * @param code the error code of this inner error.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Returns the nested inner error for this error.
     *
     * @return the nested inner error for this error.
     */
    public HttpResponseInnerError getInnerError() {
        return innerError;
    }

    /**
     * Sets the nested inner error for this error.
     *
     * @param innerError the nested inner error for this error.
     */
    public void setInnerError(HttpResponseInnerError innerError) {
        this.innerError = innerError;
    }
}
