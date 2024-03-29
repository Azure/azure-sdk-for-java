// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.logic.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The X12 agreement validation settings. */
@Fluent
public final class X12ValidationSettings {
    /*
     * The value indicating whether to validate character set in the message.
     */
    @JsonProperty(value = "validateCharacterSet", required = true)
    private boolean validateCharacterSet;

    /*
     * The value indicating whether to check for duplicate interchange control number.
     */
    @JsonProperty(value = "checkDuplicateInterchangeControlNumber", required = true)
    private boolean checkDuplicateInterchangeControlNumber;

    /*
     * The validity period of interchange control number.
     */
    @JsonProperty(value = "interchangeControlNumberValidityDays", required = true)
    private int interchangeControlNumberValidityDays;

    /*
     * The value indicating whether to check for duplicate group control number.
     */
    @JsonProperty(value = "checkDuplicateGroupControlNumber", required = true)
    private boolean checkDuplicateGroupControlNumber;

    /*
     * The value indicating whether to check for duplicate transaction set control number.
     */
    @JsonProperty(value = "checkDuplicateTransactionSetControlNumber", required = true)
    private boolean checkDuplicateTransactionSetControlNumber;

    /*
     * The value indicating whether to Whether to validate EDI types.
     */
    @JsonProperty(value = "validateEDITypes", required = true)
    private boolean validateEdiTypes;

    /*
     * The value indicating whether to Whether to validate XSD types.
     */
    @JsonProperty(value = "validateXSDTypes", required = true)
    private boolean validateXsdTypes;

    /*
     * The value indicating whether to allow leading and trailing spaces and zeroes.
     */
    @JsonProperty(value = "allowLeadingAndTrailingSpacesAndZeroes", required = true)
    private boolean allowLeadingAndTrailingSpacesAndZeroes;

    /*
     * The value indicating whether to trim leading and trailing spaces and zeroes.
     */
    @JsonProperty(value = "trimLeadingAndTrailingSpacesAndZeroes", required = true)
    private boolean trimLeadingAndTrailingSpacesAndZeroes;

    /*
     * The trailing separator policy.
     */
    @JsonProperty(value = "trailingSeparatorPolicy", required = true)
    private TrailingSeparatorPolicy trailingSeparatorPolicy;

    /** Creates an instance of X12ValidationSettings class. */
    public X12ValidationSettings() {
    }

    /**
     * Get the validateCharacterSet property: The value indicating whether to validate character set in the message.
     *
     * @return the validateCharacterSet value.
     */
    public boolean validateCharacterSet() {
        return this.validateCharacterSet;
    }

    /**
     * Set the validateCharacterSet property: The value indicating whether to validate character set in the message.
     *
     * @param validateCharacterSet the validateCharacterSet value to set.
     * @return the X12ValidationSettings object itself.
     */
    public X12ValidationSettings withValidateCharacterSet(boolean validateCharacterSet) {
        this.validateCharacterSet = validateCharacterSet;
        return this;
    }

    /**
     * Get the checkDuplicateInterchangeControlNumber property: The value indicating whether to check for duplicate
     * interchange control number.
     *
     * @return the checkDuplicateInterchangeControlNumber value.
     */
    public boolean checkDuplicateInterchangeControlNumber() {
        return this.checkDuplicateInterchangeControlNumber;
    }

    /**
     * Set the checkDuplicateInterchangeControlNumber property: The value indicating whether to check for duplicate
     * interchange control number.
     *
     * @param checkDuplicateInterchangeControlNumber the checkDuplicateInterchangeControlNumber value to set.
     * @return the X12ValidationSettings object itself.
     */
    public X12ValidationSettings withCheckDuplicateInterchangeControlNumber(
        boolean checkDuplicateInterchangeControlNumber) {
        this.checkDuplicateInterchangeControlNumber = checkDuplicateInterchangeControlNumber;
        return this;
    }

    /**
     * Get the interchangeControlNumberValidityDays property: The validity period of interchange control number.
     *
     * @return the interchangeControlNumberValidityDays value.
     */
    public int interchangeControlNumberValidityDays() {
        return this.interchangeControlNumberValidityDays;
    }

    /**
     * Set the interchangeControlNumberValidityDays property: The validity period of interchange control number.
     *
     * @param interchangeControlNumberValidityDays the interchangeControlNumberValidityDays value to set.
     * @return the X12ValidationSettings object itself.
     */
    public X12ValidationSettings withInterchangeControlNumberValidityDays(int interchangeControlNumberValidityDays) {
        this.interchangeControlNumberValidityDays = interchangeControlNumberValidityDays;
        return this;
    }

    /**
     * Get the checkDuplicateGroupControlNumber property: The value indicating whether to check for duplicate group
     * control number.
     *
     * @return the checkDuplicateGroupControlNumber value.
     */
    public boolean checkDuplicateGroupControlNumber() {
        return this.checkDuplicateGroupControlNumber;
    }

    /**
     * Set the checkDuplicateGroupControlNumber property: The value indicating whether to check for duplicate group
     * control number.
     *
     * @param checkDuplicateGroupControlNumber the checkDuplicateGroupControlNumber value to set.
     * @return the X12ValidationSettings object itself.
     */
    public X12ValidationSettings withCheckDuplicateGroupControlNumber(boolean checkDuplicateGroupControlNumber) {
        this.checkDuplicateGroupControlNumber = checkDuplicateGroupControlNumber;
        return this;
    }

    /**
     * Get the checkDuplicateTransactionSetControlNumber property: The value indicating whether to check for duplicate
     * transaction set control number.
     *
     * @return the checkDuplicateTransactionSetControlNumber value.
     */
    public boolean checkDuplicateTransactionSetControlNumber() {
        return this.checkDuplicateTransactionSetControlNumber;
    }

    /**
     * Set the checkDuplicateTransactionSetControlNumber property: The value indicating whether to check for duplicate
     * transaction set control number.
     *
     * @param checkDuplicateTransactionSetControlNumber the checkDuplicateTransactionSetControlNumber value to set.
     * @return the X12ValidationSettings object itself.
     */
    public X12ValidationSettings withCheckDuplicateTransactionSetControlNumber(
        boolean checkDuplicateTransactionSetControlNumber) {
        this.checkDuplicateTransactionSetControlNumber = checkDuplicateTransactionSetControlNumber;
        return this;
    }

    /**
     * Get the validateEdiTypes property: The value indicating whether to Whether to validate EDI types.
     *
     * @return the validateEdiTypes value.
     */
    public boolean validateEdiTypes() {
        return this.validateEdiTypes;
    }

    /**
     * Set the validateEdiTypes property: The value indicating whether to Whether to validate EDI types.
     *
     * @param validateEdiTypes the validateEdiTypes value to set.
     * @return the X12ValidationSettings object itself.
     */
    public X12ValidationSettings withValidateEdiTypes(boolean validateEdiTypes) {
        this.validateEdiTypes = validateEdiTypes;
        return this;
    }

    /**
     * Get the validateXsdTypes property: The value indicating whether to Whether to validate XSD types.
     *
     * @return the validateXsdTypes value.
     */
    public boolean validateXsdTypes() {
        return this.validateXsdTypes;
    }

    /**
     * Set the validateXsdTypes property: The value indicating whether to Whether to validate XSD types.
     *
     * @param validateXsdTypes the validateXsdTypes value to set.
     * @return the X12ValidationSettings object itself.
     */
    public X12ValidationSettings withValidateXsdTypes(boolean validateXsdTypes) {
        this.validateXsdTypes = validateXsdTypes;
        return this;
    }

    /**
     * Get the allowLeadingAndTrailingSpacesAndZeroes property: The value indicating whether to allow leading and
     * trailing spaces and zeroes.
     *
     * @return the allowLeadingAndTrailingSpacesAndZeroes value.
     */
    public boolean allowLeadingAndTrailingSpacesAndZeroes() {
        return this.allowLeadingAndTrailingSpacesAndZeroes;
    }

    /**
     * Set the allowLeadingAndTrailingSpacesAndZeroes property: The value indicating whether to allow leading and
     * trailing spaces and zeroes.
     *
     * @param allowLeadingAndTrailingSpacesAndZeroes the allowLeadingAndTrailingSpacesAndZeroes value to set.
     * @return the X12ValidationSettings object itself.
     */
    public X12ValidationSettings withAllowLeadingAndTrailingSpacesAndZeroes(
        boolean allowLeadingAndTrailingSpacesAndZeroes) {
        this.allowLeadingAndTrailingSpacesAndZeroes = allowLeadingAndTrailingSpacesAndZeroes;
        return this;
    }

    /**
     * Get the trimLeadingAndTrailingSpacesAndZeroes property: The value indicating whether to trim leading and trailing
     * spaces and zeroes.
     *
     * @return the trimLeadingAndTrailingSpacesAndZeroes value.
     */
    public boolean trimLeadingAndTrailingSpacesAndZeroes() {
        return this.trimLeadingAndTrailingSpacesAndZeroes;
    }

    /**
     * Set the trimLeadingAndTrailingSpacesAndZeroes property: The value indicating whether to trim leading and trailing
     * spaces and zeroes.
     *
     * @param trimLeadingAndTrailingSpacesAndZeroes the trimLeadingAndTrailingSpacesAndZeroes value to set.
     * @return the X12ValidationSettings object itself.
     */
    public X12ValidationSettings withTrimLeadingAndTrailingSpacesAndZeroes(
        boolean trimLeadingAndTrailingSpacesAndZeroes) {
        this.trimLeadingAndTrailingSpacesAndZeroes = trimLeadingAndTrailingSpacesAndZeroes;
        return this;
    }

    /**
     * Get the trailingSeparatorPolicy property: The trailing separator policy.
     *
     * @return the trailingSeparatorPolicy value.
     */
    public TrailingSeparatorPolicy trailingSeparatorPolicy() {
        return this.trailingSeparatorPolicy;
    }

    /**
     * Set the trailingSeparatorPolicy property: The trailing separator policy.
     *
     * @param trailingSeparatorPolicy the trailingSeparatorPolicy value to set.
     * @return the X12ValidationSettings object itself.
     */
    public X12ValidationSettings withTrailingSeparatorPolicy(TrailingSeparatorPolicy trailingSeparatorPolicy) {
        this.trailingSeparatorPolicy = trailingSeparatorPolicy;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (trailingSeparatorPolicy() == null) {
            throw LOGGER
                .logExceptionAsError(
                    new IllegalArgumentException(
                        "Missing required property trailingSeparatorPolicy in model X12ValidationSettings"));
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(X12ValidationSettings.class);
}
