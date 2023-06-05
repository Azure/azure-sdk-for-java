// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.WebApplicationFirewallPolicyInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

/** Entry point for Web Application Firewall Policy. */
public interface WebApplicationFirewallPolicy
    extends GroupableResource<NetworkManager, WebApplicationFirewallPolicyInner>,
    Updatable<WebApplicationFirewallPolicy.UpdateStages.Update> {

    /** @return mode of the Web Application Firewall Policy */
    WebApplicationFirewallMode mode();

    /** @return whether request body inspection is enabled */
    boolean isRequestBodyInspectionEnabled();

    /**
     * Max request body size limit in KB.
     * Null if {@link WebApplicationFirewallPolicy#isRequestBodyInspectionEnabled()} is false
     *
     * @return request body size limit in KB */
    Integer requestBodySizeLimitInKb();

    /**
     * Max file upload size limit, in MB.
     * Null if {@link WebApplicationFirewallPolicy#isRequestBodyInspectionEnabled()} is false
     *
     * @return file upload limit in MB
     */
    Integer fileUploadSizeLimitInMb();

    /** @return Web Application Firewall Policy settings */
    PolicySettings getPolicySettings();

    /** @return Web Application Firewall managed rules */
    ManagedRulesDefinition getManagedRules();

    /** @return whether this policy is enabled */
    boolean isEnabled();

    interface Definitions
        extends DefinitionStages.WithCreate {
    }

    /** Grouping of Web Application Gateway stages. */
    interface DefinitionStages {
        /** The first stage of an application gateway definition. */
        interface Blank extends DefinitionWithRegion<WithGroup> {
        }

        /** The stage of a Web Application Firewall Policy definition allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreate>{
        }

        /**
         * The stage of a Web Application Firewall Policy definition allowing wo specify the mode of the policy.
         */
        interface WithMode {
            /**
             * Specify that the mode of the Web Application Firewall be Detection.
             *
             * @return the next stage of the definition
             */
            WithCreate withDetectionMode();

            /**
             * Specify that the mode of the Web Application Firewall be Protection.
             *
             * @return the next stage of the definition
             */
            WithCreate withPreventionMode();
        }

        /**
         * The stage of a Web Application Firewall Policy definition allowing to specify the state of the policy.
         */
        interface WithState {
            /**
             * Enable the Web Application Firewall Policy.
             *
             * @return the next stage of the definition
             */
            WithCreate enablePolicy();

            /**
             * Disable the Web Application Firewall Policy.
             *
             * @return the next stage of the definition
             */
            WithCreate disablePolicy();
        }

        /**
         * The stage of a Web Application Firewall Policy definition allowing to specify Bot protection rule.
         */
        interface WithBotProtection {
            /**
             * Specifies that the Web Application Firewall Policy to use Bot protection against malicious bots.
             *
             * @return the next stage of the definition
             */
            WithCreate withBotProtection();

            /**
             * Specifies that the Web Application Firewall Policy to use Bot protection against malicious bots.
             *
             * @param version the version of the rule, e.g. 0.1, 1.0
             * @return the next stage of the update
             * @see <a href="https://learn.microsoft.com/en-us/azure/web-application-firewall/ag/bot-protection">
             *     Bot Protection
             *     </a>
             */
            WithCreate withBotProtection(String version);
        }

        /**
         * The stage of a Web Application Firewall Policy definition allowing to enable request body inspection.
         */
        interface WithInspectRequestBody {
            /**
             * Enables request body inspection.
             *
             * @return the next stage of the definition
             */
            WithRequestBodyOrCreate withInspectRequestBody();
        }

        /**
         * The stage of a Web Application Firewall Policy definition allowing to specify request body configuration.
         */
        interface WithRequestBodyOrCreate
            extends WithRequestBody, WithCreate {
        }

        /**
         * The stage of a Web Application Firewall Policy allowing to specify request body configuration.
         */
        interface WithRequestBody {
            /**
             * Specifies the max request body size for the Web Application Firewall policy.
             *
             * @param limitInKb max request body size in KB
             * @return the next stage of the definition
             */
            WithRequestBodyOrCreate withRequestBodySizeLimitInKb(int limitInKb);

            /**
             * Specifies the max file upload size for the Web Application Firewall policy.
             *
             * @param limitInMb max file upload size in MB
             * @return the next stage of the definition
             */
            WithRequestBodyOrCreate withFileUploadSizeLimitInMb(int limitInMb);
        }

        /**
         * The stage of a Web Application Firewall Gateway definition containing all the required inputs for the resource to be
         * created, but also allowing for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<WebApplicationFirewallPolicy>,
            Resource.DefinitionWithTags<WithCreate>,
            WithMode,
            WithState,
            WithBotProtection,
            WithInspectRequestBody,
            WithRequestBody {
        }
    }

    /** Grouping of Web Application Firewall Policy update stages. */
    interface UpdateStages {
        /**
         * The stage of a Web Application Firewall Policy update allowing wo specify the mode of the policy.
         */
        interface WithMode {
            /**
             * Specify that the mode of the Web Application Firewall be Detection.
             *
             * @return the next stage of the update
             */
            Update withDetectionMode();

            /**
             * Specify that the mode of the Web Application Firewall be Protection.
             *
             * @return the next stage of the update
             */
            Update withPreventionMode();
        }

        /**
         * The stage of a Web Application Firewall Policy update allowing to specify the state of the policy.
         */
        interface WithState {
            /**
             * Enables the Web Application Firewall Policy.
             *
             * @return the next stage of the update
             */
            Update enablePolicy();

            /**
             * Disables the Web Application Firewall Policy.
             *
             * @return the next stage of the update
             */
            Update disablePolicy();
        }

        /**
         * The stage of a Web Application Firewall Policy update allowing to specify Bot protection rule.
         */
        interface WithBotProtection {
            /**
             * Specifies that the Web Application Firewall Policy to use Bot protection against malicious bots.
             *
             * @return the next stage of the update
             */
            Update withoutBotProtection();

            /**
             * Specifies that the Web Application Firewall Policy to use Bot protection against malicious bots.
             *
             * @param version the version of the rule, e.g. 0.1, 1.0
             * @return the next stage of the update
             * @see <a href="https://learn.microsoft.com/en-us/azure/web-application-firewall/ag/bot-protection">
             *     Bot Protection
             *     </a>
             */
            Update withBotProtection(String version);
        }

        /**
         * The stage of a Web Application Firewall Policy update allowing to enable request body inspection.
         */
        interface WithInspectRequestBody {
            /**
             * Enables request body inspection.
             *
             * @return the next stage of the update
             */
            WithRequestBodyOrUpdate withInspectRequestBody();

            /**
             * Disables request body inspection.
             *
             * @return the next stage of the update
             */
            Update withoutInspectRequestBody();
        }

        /**
         * The stage of a Web Application Firewall Policy update allowing to specify request body configuration.
         */
        interface WithRequestBodyOrUpdate extends WithRequestBody, Update {
        }

        /**
         * The stage of a Web Application Firewall Policy update allowing to specify request body configuration.
         */
        interface WithRequestBody {
            /**
             * Specifies the max request body size for the Web Application Firewall policy.
             *
             * @param limitInKb max request body size in KB
             * @return the next stage of the update
             */
            WithRequestBodyOrUpdate withRequestBodySizeLimitInKb(int limitInKb);

            /**
             * Specifies the max file upload size for the Web Application Firewall policy.
             *
             * @param limitInMb max file upload size in MB
             * @return the next stage of the update
             */
            WithRequestBodyOrUpdate withFileUploadSizeLimitInMb(int limitInMb);
        }

        /** The template for a Web Application Firewall Policy update operation,
         * containing all the settings that can be modified. */
        interface Update
            extends Appliable<WebApplicationFirewallPolicy>,
            UpdateWithTags<WebApplicationFirewallPolicy>,
            WithMode,
            WithState,
            WithBotProtection,
            WithInspectRequestBody,
            WithRequestBody {
        }
    }
}
