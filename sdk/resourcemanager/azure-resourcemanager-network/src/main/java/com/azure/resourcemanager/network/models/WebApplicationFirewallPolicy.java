// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.WebApplicationFirewallPolicyInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import reactor.core.publisher.Flux;

import java.util.List;

/** Entry point for Web Application Firewall Policy. */
public interface WebApplicationFirewallPolicy
    extends GroupableResource<NetworkManager, WebApplicationFirewallPolicyInner>,
    Updatable<WebApplicationFirewallPolicy.Update>, Refreshable<WebApplicationFirewallPolicy> {

    /**
     * Gets mode of the Web Application Firewall Policy.
     *
     * @return mode of the Web Application Firewall Policy
     */
    WebApplicationFirewallMode mode();

    /**
     * Checks whether request body inspection is enabled.
     *
     * @return whether request body inspection is enabled
     */
    boolean isRequestBodyInspectionEnabled();

    /**
     * Max request body size limit in KB.
     *
     * @return request body size limit in KB */
    Integer requestBodySizeLimitInKb();

    /**
     * Max file upload size limit, in MB.
     *
     * @return file upload limit in MB
     */
    Integer fileUploadSizeLimitInMb();

    /**
     * Gets Web Application Firewall Policy settings.
     *
     * @return Web Application Firewall Policy settings
     */
    PolicySettings getPolicySettings();

    /**
     * Gets Web Application Firewall managed rules.
     *
     * @return Web Application Firewall managed rules
     */
    ManagedRulesDefinition getManagedRules();

    /**
     * Checks whether this policy is enabled.
     *
     * @return whether this policy is enabled
     */
    boolean isEnabled();

    /**
     * Gets an immutable list of application gateway resource ids associated with this Web Application Firewall Policy.
     *
     * @return an immutable list of application gateway resource ids associated with this Web Application Firewall Policy
     */
    List<String> getAssociatedApplicationGatewayIds();

    /**
     * Get an immutable list of application gateways associated with this Web Application Firewall Policy by calling REST API.
     *
     * @return an immutable list of application gateways associated with this Web Application Firewall Policy
     */
    List<ApplicationGateway> getAssociatedApplicationGateways();

    /**
     * Get a list of application gateways associated with this Web Application Firewall Policy by calling REST API
     * in async manner.
     *
     * @return {@link Flux} of application gateways associated with this Web Application Firewall Policy
     */
    Flux<ApplicationGateway> getAssociatedApplicationGatewaysAsync();

    /**
     * The entirety of the Web Application Firewall Policy definition.
     */
    interface Definition extends DefinitionStages.Blank, DefinitionStages.WithGroup, DefinitionStages.WithCreate,
        DefinitionStages.WithRequestBodyOrCreate, DefinitionStages.WithManagedRulesOrCreate {
    }

    /** Grouping of Web Application Gateway stages. */
    interface DefinitionStages {
        /** The first stage of an application gateway definition. */
        interface Blank extends DefinitionWithRegion<WithGroup> {
        }

        /** The stage of a Web Application Firewall Policy definition allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithManagedRules> {
        }

        /** The stage of a Web Application Firewall Policy definition allowing to specify the managed rules. */
        interface WithManagedRules {

            /**
             * Specifies a known managed rule set to be added to the Web Application Firewall, with optional rule group
             * override configurations.
             *
             * @param managedRuleSet known managed rule set
             * @param managedRuleGroupOverrides rule group override configuration
             * @return the next stage of the definition
             */
            WithManagedRulesOrCreate withManagedRuleSet(KnownWebApplicationGatewayManagedRuleSet managedRuleSet,
                ManagedRuleGroupOverride... managedRuleGroupOverrides);

            /**
             * Specifies a managed rule set to be added to the Web Application Firewall, with optional rule group override
             * configurations.
             *
             * @param type managed rule set type
             * @param version managed rule set version
             * @param managedRuleGroupOverrides rule group override configuration
             * @return the next stage of the definition
             */
            WithManagedRulesOrCreate withManagedRuleSet(String type, String version,
                ManagedRuleGroupOverride... managedRuleGroupOverrides);

            /**
             * Specifies a managed rule set to be added to the Web Application Firewall, with full configuration.
             *
             * @param managedRuleSet managed rule set with full configuration
             * @return the next stage of the definition
             */
            WithManagedRulesOrCreate withManagedRuleSet(ManagedRuleSet managedRuleSet);
        }

        /** The stage of a Web Application Firewall Policy definition allowing to specify the managed rules along with
         * any other optional settings to be specified during creation.
         */
        interface WithManagedRulesOrCreate extends WithManagedRules, WithCreate {
        }

        /**
         * The stage of a Web Application Firewall Policy definition allowing to specify the mode of the policy.
         */
        interface WithMode {
            /**
             * Specify that the mode of the Web Application Firewall Policy be Detection.
             *
             * @return the next stage of the definition
             */
            WithCreate withDetectionMode();

            /**
             * Specify that the mode of the Web Application Firewall Policy be Protection.
             *
             * @return the next stage of the definition
             */
            WithCreate withPreventionMode();

            /**
             * Specify the mode of the Web Application Firewall.
             *
             * @param mode the mode of the policy
             * @return the next stage of the definition
             */
            WithCreate withMode(WebApplicationFirewallMode mode);
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
             * @see <a href="https://learn.microsoft.com/azure/web-application-firewall/ag/bot-protection">
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
             * When your WAF receives a request that's over the size limit, the behavior depends on
             * the mode of your WAF and the version of the managed ruleset you use.
             * <ul>
             *     <li>When your WAF policy is in prevention mode, WAF logs and blocks requests that are over the size limit.</li>
             *     <li>When your WAF policy is in detection mode, WAF inspects the body up to the limit specified and ignores the rest.
             *              If the Content-Length header is present and is greater than the file upload limit,
             *              WAF ignores the entire body and logs the request.</li>
             * </ul>
             *
             * @return the next stage of the definition
             */
            WithRequestBodyOrCreate enableRequestBodyInspection();
        }

        /**
         * The stage of a Web Application Firewall Policy definition allowing to specify request body configuration.
         */
        interface WithRequestBodyOrCreate extends WithRequestBody, WithCreate {
        }

        /**
         * The stage of a Web Application Firewall Policy allowing to specify request body configuration.
         */
        interface WithRequestBody {
            /**
             * Specifies the max request body size for the Web Application Firewall policy.
             *
             * The maximum request body size field is specified in kilobytes and controls overall request size limit
             * excluding any file uploads. This field has a minimum value of 8 KB and a maximum value of 128 KB.
             * The default value for request body size is 128 KB.
             *
             * @param limitInKb max request body size in KB
             * @return the next stage of the definition
             */
            WithRequestBodyOrCreate withRequestBodySizeLimitInKb(int limitInKb);

            /**
             * Specifies the max file upload size for the Web Application Firewall policy.
             *
             * The file upload limit field is specified in MB and it governs the maximum allowed file upload size.
             * Only requests with Content-Type of multipart/form-data are considered for file uploads. For content to be
             * considered as a file upload, it has to be a part of a multipart form with a filename header.For all other
             * content types, the request body size limit applies.
             *
             * This field can have a minimum value of 1 MB and the following maximums:
             * <ul>
             *     <li>100 MB for v1 Medium WAF gateways</li>
             *     <li>500 MB for v1 Large WAF gateways</li>
             *     <li>750 MB for v2 WAF gateways</li>
             * </ul>
             * The default value for file upload limit is 100 MB.
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
        interface WithCreate extends Creatable<WebApplicationFirewallPolicy>, Resource.DefinitionWithTags<WithCreate>,
            WithManagedRules, WithMode, WithState, WithBotProtection, WithInspectRequestBody {
        }
    }

    /** Grouping of Web Application Firewall Policy update stages. */
    interface UpdateStages {
        /**
         * The stage of a Web Application Firewall Policy update allowing to specify the managed rules.
         */
        interface WithManagedRuleSet {
            /**
             * Specifies a known managed rule set to be added to the Web Application Firewall, with optional rule group
             * override configurations.
             *
             * @param managedRuleSet known managed rule set
             * @param managedRuleGroupOverrides rule group override configuration
             * @return the next stage of the update
             */
            Update withManagedRuleSet(KnownWebApplicationGatewayManagedRuleSet managedRuleSet,
                ManagedRuleGroupOverride... managedRuleGroupOverrides);

            /**
             * Specifies a managed rule set to be added to the Web Application Firewall, with optional rule group override
             * configurations.
             *
             * @param type managed rule set type
             * @param version managed rule set version
             * @param managedRuleGroupOverrides rule group override configuration
             * @return the next stage of the update
             */
            Update withManagedRuleSet(String type, String version,
                ManagedRuleGroupOverride... managedRuleGroupOverrides);

            /**
             * Specifies a managed rule set to be added to the Web Application Firewall, with full configuration.
             *
             * @param managedRuleSet managed rule set with full configuration
             * @return the next stage of the update
             */
            Update withManagedRuleSet(ManagedRuleSet managedRuleSet);

            /**
             * Removes the specified managed rule set from the Web Application Firewall.
             *
             * @param managedRuleSet managed rule set to be removed
             * @return the next stage of the update
             */
            Update withoutManagedRuleSet(KnownWebApplicationGatewayManagedRuleSet managedRuleSet);

            /**
             * Removes the specified managed rule set from the Web Application Firewall.
             *
             * @param type type of the managed rule set to be removed
             * @param version version of the managed rule set to be removed
             * @return the next stage of the update
             */
            Update withoutManagedRuleSet(String type, String version);
        }

        /**
         * The stage of a Web Application Firewall Policy update allowing to specify the mode of the policy.
         */
        interface WithMode {
            /**
             * Specify that the mode of the Web Application Firewall Policy be Detection.
             *
             * @return the next stage of the update
             */
            Update withDetectionMode();

            /**
             * Specify that the mode of the Web Application Firewall Policy be Protection.
             *
             * @return the next stage of the update
             */
            Update withPreventionMode();

            /**
             * Specify the mode of the Web Application Firewall Policy.
             *
             * @param mode the mode of the policy
             * @return the next stage of the update
             */
            Update withMode(WebApplicationFirewallMode mode);
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
             * @see <a href="https://learn.microsoft.com/azure/web-application-firewall/ag/bot-protection">
             *     Bot Protection
             *     </a>
             */
            Update withBotProtection(String version);

            /**
             * Specifies that the Web Application Firewall Policy to use Bot protection against malicious bots.
             *
             * @return the next stage of the update
             */
            Update withBotProtection();
        }

        /**
         * The stage of a Web Application Firewall Policy update allowing to enable request body inspection.
         */
        interface WithInspectRequestBody {
            /**
             * Enables request body inspection.
             *
             * When your WAF receives a request that's over the size limit, the behavior depends on
             * the mode of your WAF and the version of the managed ruleset you use.
             * <ul>
             *     <li>When your WAF policy is in prevention mode, WAF logs and blocks requests that are over the size limit.</li>
             *     <li>When your WAF policy is in detection mode, WAF inspects the body up to the limit specified and ignores the rest.
             *              If the Content-Length header is present and is greater than the file upload limit,
             *              WAF ignores the entire body and logs the request.</li>
             * </ul>
             *
             * @return the next stage of the update
             */
            WithRequestBodyOrUpdate enableRequestBodyInspection();

            /**
             * Disables request body inspection.
             * Turning off the request body inspection allows for messages larger than 128 KB to be sent to WAF,
             * but the message body isn't inspected for vulnerabilities.
             *
             * @return the next stage of the update
             */
            Update disableRequestBodyInspection();
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
             * The maximum request body size field is specified in kilobytes and controls overall request size limit
             * excluding any file uploads. This field has a minimum value of 8 KB and a maximum value of 128 KB.
             * The default value for request body size is 128 KB.
             *
             * @param limitInKb max request body size in KB
             * @return the next stage of the update
             */
            WithRequestBodyOrUpdate withRequestBodySizeLimitInKb(int limitInKb);

            /**
             * Specifies the max file upload size for the Web Application Firewall policy.
             *
             * The file upload limit field is specified in MB and it governs the maximum allowed file upload size.
             * Only requests with Content-Type of multipart/form-data are considered for file uploads. For content to be
             * considered as a file upload, it has to be a part of a multipart form with a filename header.For all other
             * content types, the request body size limit applies.
             *
             * This field can have a minimum value of 1 MB and the following maximums:
             * <ul>
             *     <li>100 MB for v1 Medium WAF gateways</li>
             *     <li>500 MB for v1 Large WAF gateways</li>
             *     <li>750 MB for v2 WAF gateways</li>
             * </ul>
             * The default value for file upload limit is 100 MB.
             *
             * @param limitInMb max file upload size in MB
             * @return the next stage of the update
             */
            WithRequestBodyOrUpdate withFileUploadSizeLimitInMb(int limitInMb);
        }
    }

    /** The template for a Web Application Firewall Policy update operation,
     *  containing all the settings that can be modified. */
    interface Update
        extends Appliable<WebApplicationFirewallPolicy>, Resource.UpdateWithTags<WebApplicationFirewallPolicy>,
        UpdateStages.WithMode, UpdateStages.WithState, UpdateStages.WithBotProtection,
        UpdateStages.WithInspectRequestBody, UpdateStages.WithRequestBody, UpdateStages.WithManagedRuleSet {
    }
}
