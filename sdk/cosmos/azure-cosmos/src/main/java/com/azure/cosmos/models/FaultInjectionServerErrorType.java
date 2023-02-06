package com.azure.cosmos.models;

public enum FaultInjectionServerErrorType {
    // limit the error types which are related to the current improvements
    SERVER_410,
    SERVER_449,
    INTERNAL_SERVER_ERROR,
    TOO_MANY_REQUEST,
    NOT_FOUND_1002,
    SERVER_TIMEOUT,
    SERVER_DELAY
}
