// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.implementation.util.HookHelper;
import com.azure.core.http.HttpHeaders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * Describes a hook that receives anomaly incident alerts.
 */
public abstract class NotificationHook {
    private String id;
    private List<String> admins;

    static {
        HookHelper.setAccessor(new HookHelper.HookAccessor() {
            @Override
            public void setId(NotificationHook hook, String id) {
                hook.setId(id);
            }

            @Override
            public List<String> getAdminsRaw(NotificationHook hook) {
                return hook.getAdminsRaw();
            }

            @Override
            public List<String> getEmailsToAlertRaw(EmailNotificationHook emailHook) {
                return emailHook.getEmailsToAlertRaw();
            }

            @Override
            public HttpHeaders getHttpHeadersRaw(WebNotificationHook webHook) {
                return webHook.getHttpHeadersRaw();
            }
        });
    }

    /**
     * Gets the id of the NotificationHook.
     *
     * @return The id of the NotificationHook.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the name for the Notification hook.
     *
     * @return The name.
     */
    public abstract String getName();

    /**
     * Gets the description for the Notification hook.
     *
     * @return The description.
     */
    public abstract String getDescription();

    /**
     * Gets the list of user e-mails and clientIds with administrative rights to manage the hook.
     * <p>
     * The administrators have total control over the hook, being allowed to update or delete the hook.
     * Each element in this list represents a user with administrator access, but the value of each string element
     * is either user email address or clientId uniquely identifying the user service principal.
     *
     * @return A list containing email or clientId of admins
     */
    public List<String> getAdmins() {
        if (this.admins != null) {
            return Collections.unmodifiableList(this.admins);
        } else {
            return Collections.emptyList();
        }
    }

    private void setId(String id) {
        this.id = id;
    }

    private List<String> getAdminsRaw() {
        // Getter that won't translate null admin-emails to empty-list.
        return this.admins;
    }

    void setAdministrators(List<String> admins) {
        this.admins = admins != null ? dedupe(admins) : null;
    }

    /**
     * Removes duplicates in a list.
     *
     * @param list The list to dedupe
     * @return A new deduped list.
     */
    List<String> dedupe(List<String> list) {
        TreeSet<String> seen = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        List<String> dedupedList = new ArrayList<>(list);
        dedupedList.removeIf(e -> !seen.add(e));
        return dedupedList;
    }
}
