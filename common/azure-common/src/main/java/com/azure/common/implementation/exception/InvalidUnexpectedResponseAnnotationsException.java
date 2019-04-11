package com.azure.common.implementation.exception;

import java.lang.reflect.Method;

/**
 * An exception thrown when a Swagger interface method is parsed and has invalid
 * {@link com.azure.common.annotations.UnexpectedResponseExceptionType UnexpectedResponseExceptionType}
 * annotations.
 * <br/><br/>
 * Invalid cases are when multiple annotations list the same HTTP status code or multiple don't list any codes.
 */
public class InvalidUnexpectedResponseAnnotationsException extends RuntimeException {
    /**
     * Create a new InvalidUnexpectedResponseAnnotationsException with the provided swaggerInterface method.
     * @param swaggerInterfaceMethodName The fully qualified swagger interface method name that has multiple default exception annotations.
     */
    public InvalidUnexpectedResponseAnnotationsException(String swaggerInterfaceMethodName) {
        super(swaggerInterfaceMethodName + " lists multiple UnexpectedResponseExceptionType annotations that have an empty code value.");
    }

    /**
     * Create a new InvalidUnexpectedResponseAnnotationsException with the provided swaggerInterface method and status code.
     * @param swaggerInterfaceMethodName The fully qualified swagger interface method name that has multiple exceptions for a HTTP status code.
     * @param statusCode The HTTP status code.
     */
    public InvalidUnexpectedResponseAnnotationsException(String swaggerInterfaceMethodName, int statusCode) {
        super(swaggerInterfaceMethodName + " lists multiple UnexpectedResponseExceptionType annotations that have share the HTTP status code " + statusCode + ".");
    }
}
