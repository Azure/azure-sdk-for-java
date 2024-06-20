// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

/** The ResultInformation model. */
@Immutable
public final class ResultInformation {
    /*
     * The code property.
     */
    @JsonProperty(value = "code")
    private Integer code;

    /*
     * The subCode property.
     */
    @JsonProperty(value = "subCode")
    private Integer subCode;

    /*
     * The message property.
     */
    @JsonProperty(value = "message")
    private String message;

    private ResultInformation() {
        code = null;
        subCode = null;
        message = null;
    }

    /**
     * Get the code property: The code property.
     *
     * @return the code value.
     */
    public Integer getCode() {
        return this.code;
    }

    /**
     * Get the subCode property: The subCode property.
     *
     * @return the subCode value.
     */
    public Integer getSubCode() {
        return this.subCode;
    }

    /**
     * Get the message property: The message property.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }

    static ResultInformation fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final ResultInformation information = new ResultInformation();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("code".equals(fieldName)) {
                    information.code = reader.getInt();
                } else if ("subCode".equals(fieldName)) {
                    information.subCode = reader.getInt();
                } else if ("message".equals(fieldName)) {
                    information.message = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return information;
        });
    }
}
