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

    interface Definition<ParentT> extends
            DefinitionStages.WithAttach<ParentT>,
            DefinitionStages.Blank<ParentT> {
    }

    interface DefinitionStages {

        interface WithAttach<ParentT> extends
                Attachable.InDefinition<ParentT> {
        }

        interface Blank<ParentT> {
            Definition<ParentT> withSendToSubscriptionAdministrator();
            Definition<ParentT> withoutSendToSubscriptionAdministrator();
            Definition<ParentT> withSendToSubscriptionCoAdministrators();
            Definition<ParentT> withoutSendToSubscriptionCoAdministrators();
            Definition<ParentT> withEmailNotificationCustomEmails(List<String> customEmails);
            Definition<ParentT> withEmailNotificationCustomEmail(String customEmail);
            Definition<ParentT> withWebhookNotification(String serviceUri, Map<String, String> properties);
        }
    }

    interface Update<ParentT> extends
            UpdateStages.WithAttach<ParentT>,
            UpdateStages.Blank<ParentT>{
    }

    interface UpdateStages {

        interface WithAttach<ParentT> extends
                Attachable.InDefinition<ParentT> {
        }

        interface Blank<ParentT> {
            Update<ParentT> withSendToSubscriptionAdministrator();
            Update<ParentT> withoutSendToSubscriptionAdministrator();

            Update<ParentT> withSendToSubscriptionCoAdministrators();
            Update<ParentT> withoutSendToSubscriptionCoAdministrators();

            Update<ParentT> withEmailNotificationCustomEmail(String customEmails);
            Update<ParentT> withoutEmailNotificationCustomEmail(String customEmails);
            Update<ParentT> withEmailNotificationCustomEmails(List<String> customEmails);
            Update<ParentT> withoutEmailNotificationCustomEmails();

            Update<ParentT> withWebhookNotification(String serviceUri, Map<String, String> properties);
            Update<ParentT> withoutWebhookNotification();
        }
    }

}
