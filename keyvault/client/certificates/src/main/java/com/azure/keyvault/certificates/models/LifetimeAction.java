package com.azure.keyvault.certificates.models;

import com.azure.keyvault.certificates.ActionType;

public class LifetimeAction {

    /**
     * The type of the action. Possible values include: 'EmailContacts',
     * 'AutoRenew'.
     */
    private ActionType actionType;

    /**
     * Percentage of lifetime at which to trigger. Value should be between 1
     * and 99.
     */
    private Integer lifetimePercentage;

    /**
     * Days before expiry to attempt renewal. Value should be between 1 and
     * validity_in_months multiplied by 27. If validity_in_months is 36, then
     * value should be between 1 and 972 (36 * 27).
     */
    private Integer daysBeforeExpiry;


    public LifetimeAction(ActionType actionType) {
        this.actionType = actionType;
    }

    /**
     * Get the lifetimePercentage value.
     *
     * @return the lifetimePercentage value
     */
    public Integer lifetimePercentage() {
        return this.lifetimePercentage;
    }

    /**
     * Set the lifetimePercentage value.
     *
     * @param lifetimePercentage The lifetimePercentage value to set
     * @return the Trigger object itself.
     */
    public LifetimeAction lifetimePercentage(Integer lifetimePercentage) {
        this.lifetimePercentage = lifetimePercentage;
        return this;
    }

    /**
     * Get the daysBeforeExpiry value.
     *
     * @return the daysBeforeExpiry value
     */
    public Integer daysBeforeExpiry() {
        return this.daysBeforeExpiry;
    }

    /**
     * Set the daysBeforeExpiry value.
     *
     * @param daysBeforeExpiry The daysBeforeExpiry value to set
     * @return the Trigger object itself.
     */
    public LifetimeAction daysBeforeExpiry(Integer daysBeforeExpiry) {
        this.daysBeforeExpiry = daysBeforeExpiry;
        return this;
    }

    /**
     * Get the actionType value.
     *
     * @return the actionType value
     */
    public ActionType actionType() {
        return this.actionType;
    }

    /**
     * Set the actionType value.
     *
     * @param actionType The actionType value to set
     * @return the Action object itself.
     */
    public LifetimeAction actionType(ActionType actionType) {
        this.actionType = actionType;
        return this;
    }
}
