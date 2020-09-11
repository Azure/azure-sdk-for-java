// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.monitor.fluent.inner.ActionGroupResourceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.util.List;

/** An immutable client-side representation of an Azure Action Group. */
@Fluent
public interface ActionGroup
    extends GroupableResource<MonitorManager, ActionGroupResourceInner>,
        Refreshable<ActionGroup>,
        Updatable<ActionGroup.Update> {
    /**
     * Get the groupShortName value.
     *
     * @return the groupShortName value
     */
    String shortName();

    /**
     * Get the emailReceivers value.
     *
     * @return the emailReceivers value
     */
    List<EmailReceiver> emailReceivers();

    /**
     * Get the smsReceivers value.
     *
     * @return the smsReceivers value
     */
    List<SmsReceiver> smsReceivers();

    /**
     * Get the webhookReceivers value.
     *
     * @return the webhookReceivers value
     */
    List<WebhookReceiver> webhookReceivers();

    /**
     * Get the itsmReceivers value.
     *
     * @return the itsmReceivers value
     */
    List<ItsmReceiver> itsmReceivers();

    /**
     * Get the pushNotificationReceivers value.
     *
     * @return the pushNotificationReceivers value
     */
    List<AzureAppPushReceiver> pushNotificationReceivers();

    /**
     * Get the automationRunbookReceivers value.
     *
     * @return the automationRunbookReceivers value
     */
    List<AutomationRunbookReceiver> automationRunbookReceivers();

    /**
     * Get the voiceReceivers value.
     *
     * @return the voiceReceivers value
     */
    List<VoiceReceiver> voiceReceivers();

    /**
     * Get the logicAppReceivers value.
     *
     * @return the logicAppReceivers value
     */
    List<LogicAppReceiver> logicAppReceivers();

    /**
     * Get the azureFunctionReceivers value.
     *
     * @return the azureFunctionReceivers value
     */
    List<AzureFunctionReceiver> azureFunctionReceivers();

    /**
     * Receivers action definition allowing to set each receiver's configuration.
     *
     * @param <ParentT> the next stage of the definition.
     */
    interface ActionDefinition<ParentT> {
        /**
         * Sets the email receiver.
         *
         * @param emailAddress the email Address value to set
         * @return the next stage of the definition
         */
        ActionDefinition<ParentT> withEmail(String emailAddress);

        /**
         * Sets the SMS receiver.
         *
         * @param countryCode the countryCode value to set
         * @param phoneNumber the phoneNumber value to set
         * @return the next stage of the definition
         */
        ActionDefinition<ParentT> withSms(String countryCode, String phoneNumber);

        /**
         * Sets the Webhook receiver.
         *
         * @param serviceUri the serviceUri value to set
         * @return the next stage of the definition
         */
        ActionDefinition<ParentT> withWebhook(String serviceUri);

        /**
         * Sets the ITSM receiver.
         *
         * @param workspaceId the workspaceId value to set
         * @param connectionId the connectionId value to set
         * @param ticketConfiguration the ticketConfiguration value to set
         * @param region the region value to set
         * @return the next stage of the definition
         */
        ActionDefinition<ParentT> withItsm(
            String workspaceId, String connectionId, String ticketConfiguration, String region);

        /**
         * Sets the Azure Mobile App Push Notification receiver.
         *
         * @param emailAddress the emailAddress value to set
         * @return the next stage of the definition
         */
        ActionDefinition<ParentT> withPushNotification(String emailAddress);

        /**
         * Sets the Azure Automation Runbook notification receiver.
         *
         * @param automationAccountId the automationAccountId value to set
         * @param runbookName the runbookName value to set
         * @param webhookResourceId the webhookResourceId value to set
         * @param isGlobalRunbook the isGlobalRunbook value to set
         * @return the next stage of the definition
         */
        ActionDefinition<ParentT> withAutomationRunbook(
            String automationAccountId, String runbookName, String webhookResourceId, boolean isGlobalRunbook);

        /**
         * Sets the Voice notification receiver.
         *
         * @param countryCode the countryCode value to set
         * @param phoneNumber the phoneNumber value to set
         * @return the next stage of the definition
         */
        ActionDefinition<ParentT> withVoice(String countryCode, String phoneNumber);

        /**
         * Sets the Logic App receiver.
         *
         * @param logicAppResourceId the logicAppResourceId value to set
         * @param callbackUrl the callbackUrl value to set
         * @return the next stage of the definition
         */
        ActionDefinition<ParentT> withLogicApp(String logicAppResourceId, String callbackUrl);

        /**
         * Sets the Azure Functions receiver.
         *
         * @param functionAppResourceId the functionAppResourceId value to set
         * @param functionName the functionName value to set
         * @param httpTriggerUrl the httpTriggerUrl value to set
         * @return the next stage of the definition
         */
        ActionDefinition<ParentT> withAzureFunction(
            String functionAppResourceId, String functionName, String httpTriggerUrl);

        /**
         * Attaches the defined receivers to the Action Group configuration.
         *
         * @return the next stage of the definition
         */
        ParentT attach();
    }

    /** The entirety of a Action Group definition. */
    interface Definition<T> extends DefinitionStages.Blank,
        ActionDefinition<T>,
        DefinitionStages.WithCreate {
    }

    /** Grouping of Action Group definition stages. */
    interface DefinitionStages {
        /** The first stage of a Action Group definition allowing the resource group to be specified. */
        interface Blank extends GroupableResource.DefinitionStages.WithGroupAndRegion<WithCreate> {
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created but
         * also allows for any other optional settings to be specified.
         */
        interface WithCreate extends Creatable<ActionGroup>, DefinitionWithTags<WithCreate> {

            /**
             * Begins the definition of Action Group receivers with the specified name prefix.
             *
             * @param actionNamePrefix prefix for each receiver name.
             * @return the next stage of the definition
             */
            ActionDefinition<? extends ActionGroup.DefinitionStages.WithCreate> defineReceiver(String actionNamePrefix);

            /**
             * Sets the short name of the action group. This will be used in SMS messages. Maximum length cannot exceed
             * 12 symbols.
             *
             * @param shortName short name of the action group. Cannot exceed 12 symbols.
             * @return the next stage of the definition
             */
            WithCreate withShortName(String shortName);
        }
    }

    /** Grouping of Action Group update stages. */
    interface UpdateStages {

        /** The stage of update which contains all the top level fields and transition stages to receiver updates. */
        interface WithActionDefinition {
            /**
             * Removes all the receivers that contain specified actionNamePrefix string in the name.
             *
             * @param actionNamePrefix the actionNamePrefix value to use during receiver filtering.
             * @return the next stage of the update
             */
            Update withoutReceiver(String actionNamePrefix);

            /**
             * Begins a definition for a new receiver group in the current Action group object.
             *
             * @param actionNamePrefix the actionNamePrefix value to use during receiver name creation.
             * @return the next stage of the update
             */
            ActionDefinition<? extends Update> defineReceiver(String actionNamePrefix);

            /**
             * Begins an update flow for an existing receiver group.
             *
             * @param actionNamePrefix the actionNamePrefix value to use during receiver filtering.
             * @return the next stage of the update
             */
            WithActionUpdateDefinition updateReceiver(String actionNamePrefix);

            /**
             * Sets the short name of the action group. This will be used in SMS messages. Maximum length cannot exceed
             * 12 symbols.
             *
             * @param shortName short name of the action group. Cannot exceed 12 symbols
             * @return the next stage of the update
             */
            Update withShortName(String shortName);
        }

        /** Receivers action update stage allowing to set each receiver's configuration. */
        interface WithActionUpdateDefinition {
            /**
             * Removes email receiver from current receiver's group.
             *
             * @return the next stage of the receiver group update
             */
            WithActionUpdateDefinition withoutEmail();

            /**
             * Removes SMS receiver from current receiver's group.
             *
             * @return the next stage of the receiver group update
             */
            WithActionUpdateDefinition withoutSms();

            /**
             * Removes Webhook receiver from current receiver's group.
             *
             * @return the next stage of the receiver group update
             */
            WithActionUpdateDefinition withoutWebhook();

            /**
             * Removes ITSM receiver from current receiver's group.
             *
             * @return the next stage of the receiver group update
             */
            WithActionUpdateDefinition withoutItsm();

            /**
             * Removes Azure mobile App Push notification receiver from current receiver's group.
             *
             * @return the next stage of the receiver group update
             */
            WithActionUpdateDefinition withoutPushNotification();

            /**
             * Removes Azure Automation Runbook receiver from current receiver's group.
             *
             * @return the next stage of the receiver group update
             */
            WithActionUpdateDefinition withoutAutomationRunbook();

            /**
             * Removes Voice receiver from current receiver's group.
             *
             * @return the next stage of the receiver group update
             */
            WithActionUpdateDefinition withoutVoice();

            /**
             * Removes Azure Logic App receiver from current receiver's group.
             *
             * @return the next stage of the receiver group update
             */
            WithActionUpdateDefinition withoutLogicApp();

            /**
             * Removes Azure Function receiver from current receiver's group.
             *
             * @return the next stage of the receiver group update
             */
            WithActionUpdateDefinition withoutAzureFunction();

            /**
             * Sets the email receiver.
             *
             * @param emailAddress the email Address value to set
             * @return the next stage of the update
             */
            WithActionUpdateDefinition withEmail(String emailAddress);

            /**
             * Sets the SMS receiver.
             *
             * @param countryCode the countryCode value to set
             * @param phoneNumber the phoneNumber value to set
             * @return the next stage of the update
             */
            WithActionUpdateDefinition withSms(String countryCode, String phoneNumber);

            /**
             * Sets the Webhook receiver.
             *
             * @param serviceUri the serviceUri value to set
             * @return the next stage of the update
             */
            WithActionUpdateDefinition withWebhook(String serviceUri);

            /**
             * Sets the ITSM receiver.
             *
             * @param workspaceId the workspaceId value to set
             * @param connectionId the connectionId value to set
             * @param ticketConfiguration the ticketConfiguration value to set
             * @param region the region value to set
             * @return the next stage of the update
             */
            WithActionUpdateDefinition withItsm(
                String workspaceId, String connectionId, String ticketConfiguration, String region);

            /**
             * Sets the Azure Mobile App Push Notification receiver.
             *
             * @param emailAddress the emailAddress value to set
             * @return the next stage of the update
             */
            WithActionUpdateDefinition withPushNotification(String emailAddress);

            /**
             * Sets the Azure Automation Runbook notification receiver.
             *
             * @param automationAccountId the automationAccountId value to set
             * @param runbookName the runbookName value to set
             * @param webhookResourceId the webhookResourceId value to set
             * @param isGlobalRunbook the isGlobalRunbook value to set
             * @return the next stage of the update
             */
            WithActionUpdateDefinition withAutomationRunbook(
                String automationAccountId, String runbookName, String webhookResourceId, boolean isGlobalRunbook);

            /**
             * Sets the Voice notification receiver.
             *
             * @param countryCode the countryCode value to set
             * @param phoneNumber the phoneNumber value to set
             * @return the next stage of the update
             */
            WithActionUpdateDefinition withVoice(String countryCode, String phoneNumber);

            /**
             * Sets the Logic App receiver.
             *
             * @param logicAppResourceId the logicAppResourceId value to set
             * @param callbackUrl the callbackUrl value to set
             * @return the next stage of the update
             */
            WithActionUpdateDefinition withLogicApp(String logicAppResourceId, String callbackUrl);

            /**
             * Sets the Azure Functions receiver.
             *
             * @param functionAppResourceId the functionAppResourceId value to set
             * @param functionName the functionName value to set
             * @param httpTriggerUrl the httpTriggerUrl value to set
             * @return the next stage of the update
             */
            WithActionUpdateDefinition withAzureFunction(
                String functionAppResourceId, String functionName, String httpTriggerUrl);

            /**
             * Returns to the Action Group update flow.
             *
             * @return the next stage of the update
             */
            Update parent();
        }
    }

    /** The template for an update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<ActionGroup>, UpdateStages.WithActionDefinition, Resource.UpdateWithTags<Update> {
    }
}
