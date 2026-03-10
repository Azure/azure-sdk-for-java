// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.cdn.fluent.models.RuleSetInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

import java.util.Map;

/**
 * An immutable client-side representation of an Azure Front Door (AFD) rule set that lives under a
 * {@link CdnProfile}.
 */
@Fluent
public interface RuleSet extends ExternalChildResource<RuleSet, CdnProfile>, HasInnerModel<RuleSetInner> {

    /**
     * Gets the name of the profile which holds the rule set.
     *
     * @return the profile name
     */
    String profileName();

    /**
     * Gets the provisioning state reported by the service.
     *
     * @return the provisioning state
     */
    AfdProvisioningState provisioningState();

    /**
     * Gets the deployment status for the rule set.
     *
     * @return the deployment status
     */
    DeploymentStatus deploymentStatus();

    /**
     * Gets the rules in this rule set, indexed by name.
     *
     * @return rules in this rule set, indexed by name
     */
    Map<String, Rule> rules();

    /**
     * Grouping of rule set definition stages as part of a parent {@link CdnProfile} definition.
     */
    interface DefinitionStages {
        /**
         * The first stage of a rule set definition.
         *
         * @param <ParentT> the stage of the parent CDN profile definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithAttach<ParentT> {
        }

        /**
         * The stage of the definition containing all optional settings prior to attachment.
         *
         * @param <ParentT> the stage of the parent CDN profile definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends Attachable<ParentT> {
            /**
             * Starts the definition of a new rule to be attached to this rule set.
             *
             * @param name a new rule name
             * @return the first stage of a new rule definition
             */
            Rule.DefinitionStages.Blank<WithAttach<ParentT>> defineRule(String name);
        }

        /**
         * The final stage of a rule set definition.
         *
         * @param <ParentT> the stage of the parent CDN profile definition to return to after attaching this definition
         */
        interface Attachable<ParentT> {
            /**
             * Attaches the defined rule set to the parent CDN profile.
             *
             * @return the next stage of the parent definition
             */
            ParentT attach();
        }
    }

    /**
     * The entirety of a rule set definition.
     *
     * @param <ParentT> the stage of the parent CDN profile definition to return to after attaching this definition
     */
    interface Definition<ParentT> extends DefinitionStages.Blank<ParentT>, DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of rule set definition stages that run as part of a {@link CdnProfile#update()} flow.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a rule set definition inside a profile update.
         *
         * @param <ParentT> the stage of the parent CDN profile update to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithAttach<ParentT> {
        }

        /**
         * The stage of the definition containing all optional settings prior to attachment.
         *
         * @param <ParentT> the stage of the parent CDN profile update to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends Attachable<ParentT> {
        }

        /**
         * The final stage of a rule set definition inside a profile update.
         *
         * @param <ParentT> the stage of the parent CDN profile update to return to after attaching this definition
         */
        interface Attachable<ParentT> {
            /**
             * Attaches the defined rule set to the parent CDN profile update.
             *
             * @return the next stage of the parent update
             */
            ParentT attach();
        }
    }

    /**
     * The entirety of a rule set update inside a {@link CdnProfile#update()} flow.
     */
    interface Update extends Settable<CdnProfile.Update> {
        /**
         * Starts the definition of a new rule to be attached to this rule set.
         *
         * @param name a new rule name
         * @return the first stage of a new rule definition
         */
        Rule.UpdateDefinitionStages.Blank<Update> defineRule(String name);

        /**
         * Begins the update of an existing rule in this rule set.
         *
         * @param name the name of an existing rule
         * @return the first stage of the rule update
         */
        Rule.Update updateRule(String name);

        /**
         * Removes a rule from this rule set.
         *
         * @param name the name of an existing rule
         * @return the next stage of the rule set update
         */
        Update withoutRule(String name);
    }
}
