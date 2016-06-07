/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * An object describing the difference in setting values between two web app
 * slots.
 */
@JsonFlatten
public class SlotDifference extends Resource {
    /**
     * Indicates the type of the difference: Information, Warning or Error.
     */
    @JsonProperty(value = "properties.type")
    private String slotDifferenceType;

    /**
     * The type of the settings: General, AppSetting or ConnectionString.
     */
    @JsonProperty(value = "properties.settingType")
    private String settingType;

    /**
     * Rule that describes how to process the difference in settings during
     * web app slot swap.
     */
    @JsonProperty(value = "properties.diffRule")
    private String diffRule;

    /**
     * Name of the setting.
     */
    @JsonProperty(value = "properties.settingName")
    private String settingName;

    /**
     * Value of the setting in the current web app slot.
     */
    @JsonProperty(value = "properties.valueInCurrentSlot")
    private String valueInCurrentSlot;

    /**
     * Value of the setting in the target web app slot.
     */
    @JsonProperty(value = "properties.valueInTargetSlot")
    private String valueInTargetSlot;

    /**
     * Description of the difference.
     */
    @JsonProperty(value = "properties.description")
    private String description;

    /**
     * Get the slotDifferenceType value.
     *
     * @return the slotDifferenceType value
     */
    public String slotDifferenceType() {
        return this.slotDifferenceType;
    }

    /**
     * Set the slotDifferenceType value.
     *
     * @param slotDifferenceType the slotDifferenceType value to set
     * @return the SlotDifference object itself.
     */
    public SlotDifference withSlotDifferenceType(String slotDifferenceType) {
        this.slotDifferenceType = slotDifferenceType;
        return this;
    }

    /**
     * Get the settingType value.
     *
     * @return the settingType value
     */
    public String settingType() {
        return this.settingType;
    }

    /**
     * Set the settingType value.
     *
     * @param settingType the settingType value to set
     * @return the SlotDifference object itself.
     */
    public SlotDifference withSettingType(String settingType) {
        this.settingType = settingType;
        return this;
    }

    /**
     * Get the diffRule value.
     *
     * @return the diffRule value
     */
    public String diffRule() {
        return this.diffRule;
    }

    /**
     * Set the diffRule value.
     *
     * @param diffRule the diffRule value to set
     * @return the SlotDifference object itself.
     */
    public SlotDifference withDiffRule(String diffRule) {
        this.diffRule = diffRule;
        return this;
    }

    /**
     * Get the settingName value.
     *
     * @return the settingName value
     */
    public String settingName() {
        return this.settingName;
    }

    /**
     * Set the settingName value.
     *
     * @param settingName the settingName value to set
     * @return the SlotDifference object itself.
     */
    public SlotDifference withSettingName(String settingName) {
        this.settingName = settingName;
        return this;
    }

    /**
     * Get the valueInCurrentSlot value.
     *
     * @return the valueInCurrentSlot value
     */
    public String valueInCurrentSlot() {
        return this.valueInCurrentSlot;
    }

    /**
     * Set the valueInCurrentSlot value.
     *
     * @param valueInCurrentSlot the valueInCurrentSlot value to set
     * @return the SlotDifference object itself.
     */
    public SlotDifference withValueInCurrentSlot(String valueInCurrentSlot) {
        this.valueInCurrentSlot = valueInCurrentSlot;
        return this;
    }

    /**
     * Get the valueInTargetSlot value.
     *
     * @return the valueInTargetSlot value
     */
    public String valueInTargetSlot() {
        return this.valueInTargetSlot;
    }

    /**
     * Set the valueInTargetSlot value.
     *
     * @param valueInTargetSlot the valueInTargetSlot value to set
     * @return the SlotDifference object itself.
     */
    public SlotDifference withValueInTargetSlot(String valueInTargetSlot) {
        this.valueInTargetSlot = valueInTargetSlot;
        return this;
    }

    /**
     * Get the description value.
     *
     * @return the description value
     */
    public String description() {
        return this.description;
    }

    /**
     * Set the description value.
     *
     * @param description the description value to set
     * @return the SlotDifference object itself.
     */
    public SlotDifference withDescription(String description) {
        this.description = description;
        return this;
    }

}
