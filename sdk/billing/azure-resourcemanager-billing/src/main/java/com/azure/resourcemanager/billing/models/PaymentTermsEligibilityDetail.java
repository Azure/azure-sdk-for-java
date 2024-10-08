// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.billing.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Details of the payment terms eligibility.
 */
@Fluent
public final class PaymentTermsEligibilityDetail implements JsonSerializable<PaymentTermsEligibilityDetail> {
    /*
     * Indicates the reason for the ineligibility of the payment terms.
     */
    private PaymentTermsEligibilityCode code;

    /*
     * Indicates the message for the ineligibility of the payment terms.
     */
    private String message;

    /**
     * Creates an instance of PaymentTermsEligibilityDetail class.
     */
    public PaymentTermsEligibilityDetail() {
    }

    /**
     * Get the code property: Indicates the reason for the ineligibility of the payment terms.
     * 
     * @return the code value.
     */
    public PaymentTermsEligibilityCode code() {
        return this.code;
    }

    /**
     * Set the code property: Indicates the reason for the ineligibility of the payment terms.
     * 
     * @param code the code value to set.
     * @return the PaymentTermsEligibilityDetail object itself.
     */
    public PaymentTermsEligibilityDetail withCode(PaymentTermsEligibilityCode code) {
        this.code = code;
        return this;
    }

    /**
     * Get the message property: Indicates the message for the ineligibility of the payment terms.
     * 
     * @return the message value.
     */
    public String message() {
        return this.message;
    }

    /**
     * Set the message property: Indicates the message for the ineligibility of the payment terms.
     * 
     * @param message the message value to set.
     * @return the PaymentTermsEligibilityDetail object itself.
     */
    public PaymentTermsEligibilityDetail withMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("code", this.code == null ? null : this.code.toString());
        jsonWriter.writeStringField("message", this.message);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of PaymentTermsEligibilityDetail from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of PaymentTermsEligibilityDetail if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the PaymentTermsEligibilityDetail.
     */
    public static PaymentTermsEligibilityDetail fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            PaymentTermsEligibilityDetail deserializedPaymentTermsEligibilityDetail
                = new PaymentTermsEligibilityDetail();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("code".equals(fieldName)) {
                    deserializedPaymentTermsEligibilityDetail.code
                        = PaymentTermsEligibilityCode.fromString(reader.getString());
                } else if ("message".equals(fieldName)) {
                    deserializedPaymentTermsEligibilityDetail.message = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedPaymentTermsEligibilityDetail;
        });
    }
}
