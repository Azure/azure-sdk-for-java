/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Routing rules for ramp up testing. This rule allows to redirect static
 * traffic % to a slot or to gradually change routing % based on performance.
 */
public class RampUpRule {
    /**
     * Hostname of a slot to which the traffic will be redirected if decided
     * to. E.g. mysite-stage.azurewebsites.net.
     */
    private String actionHostName;

    /**
     * Percentage of the traffic which will be redirected to
     * {Microsoft.Web.Hosting.Administration.RampUpRule.ActionHostName}.
     */
    private Double reroutePercentage;

    /**
     * [Optional] In auto ramp up scenario this is the step to to add/remove
     * from
     * {Microsoft.Web.Hosting.Administration.RampUpRule.ReroutePercentage}
     * until it reaches
     * {Microsoft.Web.Hosting.Administration.RampUpRule.MinReroutePercentage}
     * or
     * {Microsoft.Web.Hosting.Administration.RampUpRule.MaxReroutePercentage}.
     * Site metrics are checked every N minutes specificed in
     * {Microsoft.Web.Hosting.Administration.RampUpRule.ChangeIntervalInMinutes}.
     * Custom decision algorithm can be provided in TiPCallback
     * site extension which Url can be specified in
     * {Microsoft.Web.Hosting.Administration.RampUpRule.ChangeDecisionCallbackUrl}.
     */
    private Double changeStep;

    /**
     * [Optional] Specifies interval in mimuntes to reevaluate
     * ReroutePercentage.
     */
    private Integer changeIntervalInMinutes;

    /**
     * [Optional] Specifies lower boundary above which ReroutePercentage will
     * stay.
     */
    private Double minReroutePercentage;

    /**
     * [Optional] Specifies upper boundary below which ReroutePercentage will
     * stay.
     */
    private Double maxReroutePercentage;

    /**
     * Custom decision algorithm can be provided in TiPCallback site extension
     * which Url can be specified. See TiPCallback site extension for the
     * scaffold and contracts.
     * https://www.siteextensions.net/packages/TiPCallback/.
     */
    private String changeDecisionCallbackUrl;

    /**
     * Name of the routing rule. The recommended name would be to point to the
     * slot which will receive the traffic in the experiment.
     */
    private String name;

    /**
     * Get the actionHostName value.
     *
     * @return the actionHostName value
     */
    public String actionHostName() {
        return this.actionHostName;
    }

    /**
     * Set the actionHostName value.
     *
     * @param actionHostName the actionHostName value to set
     * @return the RampUpRule object itself.
     */
    public RampUpRule withActionHostName(String actionHostName) {
        this.actionHostName = actionHostName;
        return this;
    }

    /**
     * Get the reroutePercentage value.
     *
     * @return the reroutePercentage value
     */
    public Double reroutePercentage() {
        return this.reroutePercentage;
    }

    /**
     * Set the reroutePercentage value.
     *
     * @param reroutePercentage the reroutePercentage value to set
     * @return the RampUpRule object itself.
     */
    public RampUpRule withReroutePercentage(Double reroutePercentage) {
        this.reroutePercentage = reroutePercentage;
        return this;
    }

    /**
     * Get the changeStep value.
     *
     * @return the changeStep value
     */
    public Double changeStep() {
        return this.changeStep;
    }

    /**
     * Set the changeStep value.
     *
     * @param changeStep the changeStep value to set
     * @return the RampUpRule object itself.
     */
    public RampUpRule withChangeStep(Double changeStep) {
        this.changeStep = changeStep;
        return this;
    }

    /**
     * Get the changeIntervalInMinutes value.
     *
     * @return the changeIntervalInMinutes value
     */
    public Integer changeIntervalInMinutes() {
        return this.changeIntervalInMinutes;
    }

    /**
     * Set the changeIntervalInMinutes value.
     *
     * @param changeIntervalInMinutes the changeIntervalInMinutes value to set
     * @return the RampUpRule object itself.
     */
    public RampUpRule withChangeIntervalInMinutes(Integer changeIntervalInMinutes) {
        this.changeIntervalInMinutes = changeIntervalInMinutes;
        return this;
    }

    /**
     * Get the minReroutePercentage value.
     *
     * @return the minReroutePercentage value
     */
    public Double minReroutePercentage() {
        return this.minReroutePercentage;
    }

    /**
     * Set the minReroutePercentage value.
     *
     * @param minReroutePercentage the minReroutePercentage value to set
     * @return the RampUpRule object itself.
     */
    public RampUpRule withMinReroutePercentage(Double minReroutePercentage) {
        this.minReroutePercentage = minReroutePercentage;
        return this;
    }

    /**
     * Get the maxReroutePercentage value.
     *
     * @return the maxReroutePercentage value
     */
    public Double maxReroutePercentage() {
        return this.maxReroutePercentage;
    }

    /**
     * Set the maxReroutePercentage value.
     *
     * @param maxReroutePercentage the maxReroutePercentage value to set
     * @return the RampUpRule object itself.
     */
    public RampUpRule withMaxReroutePercentage(Double maxReroutePercentage) {
        this.maxReroutePercentage = maxReroutePercentage;
        return this;
    }

    /**
     * Get the changeDecisionCallbackUrl value.
     *
     * @return the changeDecisionCallbackUrl value
     */
    public String changeDecisionCallbackUrl() {
        return this.changeDecisionCallbackUrl;
    }

    /**
     * Set the changeDecisionCallbackUrl value.
     *
     * @param changeDecisionCallbackUrl the changeDecisionCallbackUrl value to set
     * @return the RampUpRule object itself.
     */
    public RampUpRule withChangeDecisionCallbackUrl(String changeDecisionCallbackUrl) {
        this.changeDecisionCallbackUrl = changeDecisionCallbackUrl;
        return this;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the RampUpRule object itself.
     */
    public RampUpRule withName(String name) {
        this.name = name;
        return this;
    }

}
