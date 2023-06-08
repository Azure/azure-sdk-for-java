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
    Updatable<WebApplicationFirewallPolicy.Update>,
    Refreshable<WebApplicationFirewallPolicy> {

    /** @return mode of the Web Application Firewall Policy */
    WebApplicationFirewallMode mode();

    /** @return whether request body inspection is enabled */
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

    /** @return Web Application Firewall Policy settings */
    PolicySettings getPolicySettings();

    /** @return Web Application Firewall managed rules */
    ManagedRulesDefinition getManagedRules();

    /** @return whether this policy is enabled */
    boolean isEnabled();

    /** @return a list of application gateways associated with this Web Application Firewall Policy */
    List<ApplicationGateway> getAssociatedApplicationGateways();

    /** @return {@link Flux} of application gateways associated with this Web Application Firewall Policy */
    Flux<ApplicationGateway> getAssociatedApplicationGatewaysAsync();

    /**
     * The entirety of the Web Application Firewall Policy definition.
     */
    interface Definition
        extends DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithCreate,
        DefinitionStages.WithRequestBodyOrCreate {
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
        interface WithCreate
            extends Creatable<WebApplicationFirewallPolicy>,
            Resource.DefinitionWithTags<WithCreate>,
            WithMode,
            WithState,
            WithBotProtection,
            WithInspectRequestBody {
        }
    }

    /** Grouping of Web Application Firewall Policy update stages. */
    interface UpdateStages {
        /**
         * The stage of a Web Application Firewall Policy update allowing wo specify the mode of the policy.
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
             * @see <a href="https://learn.microsoft.com/en-us/azure/web-application-firewall/ag/bot-protection">
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
            WithRequestBodyOrUpdate withInspectRequestBody();

            /**
             * Disables request body inspection.
             * Turning off the request body inspection allows for messages larger than 128 KB to be sent to WAF,
             * but the message body isn't inspected for vulnerabilities.
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
        extends Appliable<WebApplicationFirewallPolicy>,
        Resource.UpdateWithTags<WebApplicationFirewallPolicy>,
        UpdateStages.WithMode,
        UpdateStages.WithState,
        UpdateStages.WithBotProtection,
        UpdateStages.WithInspectRequestBody,
        UpdateStages.WithRequestBody {
    }
}
