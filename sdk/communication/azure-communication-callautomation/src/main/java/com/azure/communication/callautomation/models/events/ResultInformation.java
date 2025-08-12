// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The ResultInformation model. */
@Immutable
public final class ResultInformation implements JsonSerializable<ResultInformation> {
    /*
     * The code property.
     */
    private final Integer code;

    /*
     * The subCode property.
     */
    private final Integer subCode;

    /*
     * The message property.
     */
    private final String message;

    /*
     * Sip response from SBC. This can be helpful to troubleshoot PSTN call if this result was unexpected.
     * This is only applicable for PSTN calls and will be null if SBC/Carrier does not provide this information.
     * Do not solely rely on this information for troubleshooting, as it may not always be available.
     */
    private SipDiagnosticInfo sipDetails;

    /*
     * Q850 cause from SBC. This can be helpful to troubleshoot call issues if this result was unexpected.
     * This is only applicable for PSTN calls and will be null if SBC/Carrier does not provide this information.
     * Do not solely rely on this information for troubleshooting, as it may not always be available.
     */
    private SipDiagnosticInfo q850Details;

    private ResultInformation(Integer code, Integer subCode, String message, SipDiagnosticInfo sipDetails,
        SipDiagnosticInfo q850Details) {
        this.code = code;
        this.subCode = subCode;
        this.message = message;
        this.sipDetails = sipDetails;
        this.q850Details = q850Details;
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

    /**
     * Get the sipDetails property: Sip response from SBC. This can be helpful to troubleshoot PSTN call if this result
     * was unexpected.
     * This is only applicable for PSTN calls and will be null if SBC/Carrier does not provide this information.
     * Do not solely rely on this information for troubleshooting, as it may not always be available.
     * 
     * @return the sipDetails value.
     */
    public SipDiagnosticInfo getSipDetails() {
        return this.sipDetails;
    }

    /**
     * Get the q850Details property: Q850 cause from SBC. This can be helpful to troubleshoot call issues if this result
     * was unexpected.
     * This is only applicable for PSTN calls and will be null if SBC/Carrier does not provide this information.
     * Do not solely rely on this information for troubleshooting, as it may not always be available.
     * 
     * @return the q850Details value.
     */
    public SipDiagnosticInfo getQ850Details() {
        return this.q850Details;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeNumberField("code", this.code);
        jsonWriter.writeNumberField("subCode", this.subCode);
        jsonWriter.writeStringField("message", this.message);
        jsonWriter.writeJsonField("sipDetails", this.sipDetails);
        jsonWriter.writeJsonField("q850Details", this.q850Details);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResultInformation from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResultInformation if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ResultInformation.
     */
    public static ResultInformation fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Integer code = null;
            Integer subCode = null;
            String message = null;
            SipDiagnosticInfo sipDetails = null;
            SipDiagnosticInfo q850Details = null;
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("code".equals(fieldName)) {
                    code = reader.getNullable(JsonReader::getInt);
                } else if ("subCode".equals(fieldName)) {
                    subCode = reader.getNullable(JsonReader::getInt);
                } else if ("message".equals(fieldName)) {
                    message = reader.getString();
                } else if ("sipDetails".equals(fieldName)) {
                    sipDetails = SipDiagnosticInfo.fromJson(reader);
                } else if ("q850Details".equals(fieldName)) {
                    q850Details = SipDiagnosticInfo.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }
            return new ResultInformation(code, subCode, message, sipDetails, q850Details);
        });
    }
}
