/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry.client.rest.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generic JSON error message.
 */
public class ErrorMessage {

  private int errorCode;
  private String message;

  public ErrorMessage(@JsonProperty("error_code") int errorCode,
                      @JsonProperty("message") String message) {
    this.errorCode = errorCode;
    this.message = message;
  }

  @JsonProperty("error_code")
  public int getErrorCode() {
    return errorCode;
  }

  @JsonProperty("error_code")
  public void setErrorCode(int error_code) {
    this.errorCode = error_code;
  }

  @JsonProperty
  public String getMessage() {
    return message;
  }

  @JsonProperty
  public void setMessage(String message) {
    this.message = message;
  }
}
