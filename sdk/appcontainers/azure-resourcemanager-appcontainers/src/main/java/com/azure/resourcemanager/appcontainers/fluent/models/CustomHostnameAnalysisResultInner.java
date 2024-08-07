// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.appcontainers.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.appcontainers.models.CustomHostnameAnalysisResultCustomDomainVerificationFailureInfo;
import com.azure.resourcemanager.appcontainers.models.DnsVerificationTestResult;
import java.io.IOException;
import java.util.List;

/**
 * Custom domain analysis.
 */
@Fluent
public final class CustomHostnameAnalysisResultInner implements JsonSerializable<CustomHostnameAnalysisResultInner> {
    /*
     * Host name that was analyzed
     */
    private String hostname;

    /*
     * <code>true</code> if hostname is already verified; otherwise, <code>false</code>.
     */
    private Boolean isHostnameAlreadyVerified;

    /*
     * DNS verification test result.
     */
    private DnsVerificationTestResult customDomainVerificationTest;

    /*
     * Raw failure information if DNS verification fails.
     */
    private CustomHostnameAnalysisResultCustomDomainVerificationFailureInfo customDomainVerificationFailureInfo;

    /*
     * <code>true</code> if there is a conflict on the Container App's managed environment; otherwise,
     * <code>false</code>.
     */
    private Boolean hasConflictOnManagedEnvironment;

    /*
     * <code>true</code> if there is a conflict on the Container App's managed environment level custom domain;
     * otherwise, <code>false</code>.
     */
    private Boolean conflictWithEnvironmentCustomDomain;

    /*
     * Name of the conflicting Container App on the Managed Environment if it's within the same subscription.
     */
    private String conflictingContainerAppResourceId;

    /*
     * CName records visible for this hostname.
     */
    private List<String> cNameRecords;

    /*
     * TXT records visible for this hostname.
     */
    private List<String> txtRecords;

    /*
     * A records visible for this hostname.
     */
    private List<String> aRecords;

    /*
     * Alternate CName records visible for this hostname.
     */
    private List<String> alternateCNameRecords;

    /*
     * Alternate TXT records visible for this hostname.
     */
    private List<String> alternateTxtRecords;

    /**
     * Creates an instance of CustomHostnameAnalysisResultInner class.
     */
    public CustomHostnameAnalysisResultInner() {
    }

    /**
     * Get the hostname property: Host name that was analyzed.
     * 
     * @return the hostname value.
     */
    public String hostname() {
        return this.hostname;
    }

    /**
     * Get the isHostnameAlreadyVerified property: &lt;code&gt;true&lt;/code&gt; if hostname is already verified;
     * otherwise, &lt;code&gt;false&lt;/code&gt;.
     * 
     * @return the isHostnameAlreadyVerified value.
     */
    public Boolean isHostnameAlreadyVerified() {
        return this.isHostnameAlreadyVerified;
    }

    /**
     * Get the customDomainVerificationTest property: DNS verification test result.
     * 
     * @return the customDomainVerificationTest value.
     */
    public DnsVerificationTestResult customDomainVerificationTest() {
        return this.customDomainVerificationTest;
    }

    /**
     * Get the customDomainVerificationFailureInfo property: Raw failure information if DNS verification fails.
     * 
     * @return the customDomainVerificationFailureInfo value.
     */
    public CustomHostnameAnalysisResultCustomDomainVerificationFailureInfo customDomainVerificationFailureInfo() {
        return this.customDomainVerificationFailureInfo;
    }

    /**
     * Get the hasConflictOnManagedEnvironment property: &lt;code&gt;true&lt;/code&gt; if there is a conflict on the
     * Container App's managed environment; otherwise, &lt;code&gt;false&lt;/code&gt;.
     * 
     * @return the hasConflictOnManagedEnvironment value.
     */
    public Boolean hasConflictOnManagedEnvironment() {
        return this.hasConflictOnManagedEnvironment;
    }

    /**
     * Get the conflictWithEnvironmentCustomDomain property: &lt;code&gt;true&lt;/code&gt; if there is a conflict on the
     * Container App's managed environment level custom domain; otherwise, &lt;code&gt;false&lt;/code&gt;.
     * 
     * @return the conflictWithEnvironmentCustomDomain value.
     */
    public Boolean conflictWithEnvironmentCustomDomain() {
        return this.conflictWithEnvironmentCustomDomain;
    }

    /**
     * Get the conflictingContainerAppResourceId property: Name of the conflicting Container App on the Managed
     * Environment if it's within the same subscription.
     * 
     * @return the conflictingContainerAppResourceId value.
     */
    public String conflictingContainerAppResourceId() {
        return this.conflictingContainerAppResourceId;
    }

    /**
     * Get the cNameRecords property: CName records visible for this hostname.
     * 
     * @return the cNameRecords value.
     */
    public List<String> cNameRecords() {
        return this.cNameRecords;
    }

    /**
     * Set the cNameRecords property: CName records visible for this hostname.
     * 
     * @param cNameRecords the cNameRecords value to set.
     * @return the CustomHostnameAnalysisResultInner object itself.
     */
    public CustomHostnameAnalysisResultInner withCNameRecords(List<String> cNameRecords) {
        this.cNameRecords = cNameRecords;
        return this;
    }

    /**
     * Get the txtRecords property: TXT records visible for this hostname.
     * 
     * @return the txtRecords value.
     */
    public List<String> txtRecords() {
        return this.txtRecords;
    }

    /**
     * Set the txtRecords property: TXT records visible for this hostname.
     * 
     * @param txtRecords the txtRecords value to set.
     * @return the CustomHostnameAnalysisResultInner object itself.
     */
    public CustomHostnameAnalysisResultInner withTxtRecords(List<String> txtRecords) {
        this.txtRecords = txtRecords;
        return this;
    }

    /**
     * Get the aRecords property: A records visible for this hostname.
     * 
     * @return the aRecords value.
     */
    public List<String> aRecords() {
        return this.aRecords;
    }

    /**
     * Set the aRecords property: A records visible for this hostname.
     * 
     * @param aRecords the aRecords value to set.
     * @return the CustomHostnameAnalysisResultInner object itself.
     */
    public CustomHostnameAnalysisResultInner withARecords(List<String> aRecords) {
        this.aRecords = aRecords;
        return this;
    }

    /**
     * Get the alternateCNameRecords property: Alternate CName records visible for this hostname.
     * 
     * @return the alternateCNameRecords value.
     */
    public List<String> alternateCNameRecords() {
        return this.alternateCNameRecords;
    }

    /**
     * Set the alternateCNameRecords property: Alternate CName records visible for this hostname.
     * 
     * @param alternateCNameRecords the alternateCNameRecords value to set.
     * @return the CustomHostnameAnalysisResultInner object itself.
     */
    public CustomHostnameAnalysisResultInner withAlternateCNameRecords(List<String> alternateCNameRecords) {
        this.alternateCNameRecords = alternateCNameRecords;
        return this;
    }

    /**
     * Get the alternateTxtRecords property: Alternate TXT records visible for this hostname.
     * 
     * @return the alternateTxtRecords value.
     */
    public List<String> alternateTxtRecords() {
        return this.alternateTxtRecords;
    }

    /**
     * Set the alternateTxtRecords property: Alternate TXT records visible for this hostname.
     * 
     * @param alternateTxtRecords the alternateTxtRecords value to set.
     * @return the CustomHostnameAnalysisResultInner object itself.
     */
    public CustomHostnameAnalysisResultInner withAlternateTxtRecords(List<String> alternateTxtRecords) {
        this.alternateTxtRecords = alternateTxtRecords;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (customDomainVerificationFailureInfo() != null) {
            customDomainVerificationFailureInfo().validate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("cNameRecords", this.cNameRecords, (writer, element) -> writer.writeString(element));
        jsonWriter.writeArrayField("txtRecords", this.txtRecords, (writer, element) -> writer.writeString(element));
        jsonWriter.writeArrayField("aRecords", this.aRecords, (writer, element) -> writer.writeString(element));
        jsonWriter.writeArrayField("alternateCNameRecords", this.alternateCNameRecords,
            (writer, element) -> writer.writeString(element));
        jsonWriter.writeArrayField("alternateTxtRecords", this.alternateTxtRecords,
            (writer, element) -> writer.writeString(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of CustomHostnameAnalysisResultInner from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of CustomHostnameAnalysisResultInner if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the CustomHostnameAnalysisResultInner.
     */
    public static CustomHostnameAnalysisResultInner fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            CustomHostnameAnalysisResultInner deserializedCustomHostnameAnalysisResultInner
                = new CustomHostnameAnalysisResultInner();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("hostName".equals(fieldName)) {
                    deserializedCustomHostnameAnalysisResultInner.hostname = reader.getString();
                } else if ("isHostnameAlreadyVerified".equals(fieldName)) {
                    deserializedCustomHostnameAnalysisResultInner.isHostnameAlreadyVerified
                        = reader.getNullable(JsonReader::getBoolean);
                } else if ("customDomainVerificationTest".equals(fieldName)) {
                    deserializedCustomHostnameAnalysisResultInner.customDomainVerificationTest
                        = DnsVerificationTestResult.fromString(reader.getString());
                } else if ("customDomainVerificationFailureInfo".equals(fieldName)) {
                    deserializedCustomHostnameAnalysisResultInner.customDomainVerificationFailureInfo
                        = CustomHostnameAnalysisResultCustomDomainVerificationFailureInfo.fromJson(reader);
                } else if ("hasConflictOnManagedEnvironment".equals(fieldName)) {
                    deserializedCustomHostnameAnalysisResultInner.hasConflictOnManagedEnvironment
                        = reader.getNullable(JsonReader::getBoolean);
                } else if ("conflictWithEnvironmentCustomDomain".equals(fieldName)) {
                    deserializedCustomHostnameAnalysisResultInner.conflictWithEnvironmentCustomDomain
                        = reader.getNullable(JsonReader::getBoolean);
                } else if ("conflictingContainerAppResourceId".equals(fieldName)) {
                    deserializedCustomHostnameAnalysisResultInner.conflictingContainerAppResourceId
                        = reader.getString();
                } else if ("cNameRecords".equals(fieldName)) {
                    List<String> cNameRecords = reader.readArray(reader1 -> reader1.getString());
                    deserializedCustomHostnameAnalysisResultInner.cNameRecords = cNameRecords;
                } else if ("txtRecords".equals(fieldName)) {
                    List<String> txtRecords = reader.readArray(reader1 -> reader1.getString());
                    deserializedCustomHostnameAnalysisResultInner.txtRecords = txtRecords;
                } else if ("aRecords".equals(fieldName)) {
                    List<String> aRecords = reader.readArray(reader1 -> reader1.getString());
                    deserializedCustomHostnameAnalysisResultInner.aRecords = aRecords;
                } else if ("alternateCNameRecords".equals(fieldName)) {
                    List<String> alternateCNameRecords = reader.readArray(reader1 -> reader1.getString());
                    deserializedCustomHostnameAnalysisResultInner.alternateCNameRecords = alternateCNameRecords;
                } else if ("alternateTxtRecords".equals(fieldName)) {
                    List<String> alternateTxtRecords = reader.readArray(reader1 -> reader1.getString());
                    deserializedCustomHostnameAnalysisResultInner.alternateTxtRecords = alternateTxtRecords;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedCustomHostnameAnalysisResultInner;
        });
    }
}
