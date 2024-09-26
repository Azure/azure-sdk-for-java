// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.messages.implementation.accesshelpers;

import com.azure.communication.messages.models.MessageTemplateItem;

/**
 * Access helper for {@link MessageTemplateItem}.
 */
public final class MessageTemplateItemAccessHelper {
    private static MessageTemplateItemAccessor accessor;

    /**
     * The accessor for {@link MessageTemplateItem}.
     */
    public interface MessageTemplateItemAccessor {
        /**
         * Sets the name of the {@link MessageTemplateItem}.
         *
         * @param messageTemplateItem The {@link MessageTemplateItem} to set the name.
         * @param name The name to set.
         */
        void setName(MessageTemplateItem messageTemplateItem, String name);
    }

    /**
     * Sets the accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(MessageTemplateItemAccessor accessor) {
        MessageTemplateItemAccessHelper.accessor = accessor;
    }

    /**
     * Sets the name of the {@link MessageTemplateItem}.
     *
     * @param messageTemplateItem The {@link MessageTemplateItem} to set the name.
     * @param name The name to set.
     */
    public static void setName(MessageTemplateItem messageTemplateItem, String name) {
        accessor.setName(messageTemplateItem, name);
    }

    private MessageTemplateItemAccessHelper() {
    }
}
