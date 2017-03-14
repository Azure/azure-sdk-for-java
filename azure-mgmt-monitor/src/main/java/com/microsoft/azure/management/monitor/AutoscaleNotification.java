/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

import java.util.List;
import java.util.Map;

/**
 */
@Fluent
public interface AutoscaleNotification {

    /**
     * the operation associated with the notification and its value must be "scale"
     */
    String operation();

    /**
     * a value indicating whether to send email to subscription administrator.
     */
    boolean sendToSubscriptionAdministrator();

    /**
     * a value indicating whether to send email to subscription co-administrators.
     */
    boolean sendToSubscriptionCoAdministrators();

    /**
     * the custom e-mails list. This value can be null or empty, in which case this attribute will be ignored.
     */
    List<String> customEmails();

    /**
     * the collection of webhook notifications.
     */
    Map<String, WebhookNotification> webhooks();

    interface Definition extends
            DefinitionStages.WithAttach,
            DefinitionStages.Blank {
    }

    interface DefinitionStages {
        interface WithAttach extends
                Attachable.InDefinition<AutoscaleSetting.DefinitionStages.WithCreate> {
        }

        interface Blank {
            Definition withSendToSubscriptionAdministrator();
            Definition withoutSendToSubscriptionAdministrator();
            Definition withSendToSubscriptionCoAdministrators();
            Definition withoutSendToSubscriptionCoAdministrators();
            Definition withEmailNotificationCustomEmails(List<String> customEmails);
            Definition withEmailNotificationCustomEmail(String customEmail);
            Definition withWebhookNotification(String serviceUri, Map<String, String> properties);
        }
    }

    interface UpdateDefinition extends
            UpdateDefinitionStages.WithAttach,
            UpdateDefinitionStages.Blank {
    }

    interface UpdateDefinitionStages {
        interface WithAttach extends
                Attachable.InUpdate<AutoscaleSetting.Update> {
        }

        interface Blank {
            UpdateDefinition withSendToSubscriptionAdministrator();
            UpdateDefinition withoutSendToSubscriptionAdministrator();
            UpdateDefinition withSendToSubscriptionCoAdministrators();
            UpdateDefinition withoutSendToSubscriptionCoAdministrators();
            UpdateDefinition withEmailNotificationCustomEmails(List<String> customEmails);
            UpdateDefinition withEmailNotificationCustomEmail(String customEmail);
            UpdateDefinition withWebhookNotification(String serviceUri, Map<String, String> properties);
        }
    }

    interface Update extends
            Settable<AutoscaleSetting.Update>,
            UpdateStages.Blank {
    }

    interface UpdateStages {

        interface Blank {
            Update withSendToSubscriptionAdministrator();
            Update withoutSendToSubscriptionAdministrator();

            Update withSendToSubscriptionCoAdministrators();
            Update withoutSendToSubscriptionCoAdministrators();

            Update withEmailNotificationCustomEmail(String customEmails);
            Update withoutEmailNotificationCustomEmail(String customEmails);
            Update withEmailNotificationCustomEmails(List<String> customEmails);
            Update withoutEmailNotificationCustomEmails();

            Update withWebhookNotification(String serviceUri, Map<String, String> properties);
            Update withoutWebhookNotification();
        }
    }
}
