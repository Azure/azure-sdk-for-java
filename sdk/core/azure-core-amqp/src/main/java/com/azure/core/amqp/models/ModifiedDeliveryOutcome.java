// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.annotation.Fluent;

import java.util.Map;

/**
 * The modified outcome.
 * <p>
 * At the source the modified outcome means that the message is no longer acquired by the receiver, and has been made
 * available for (re-)delivery to the same or other targets receiving from the node. The message has been changed at the
 * node in the ways indicated by the fields of the outcome. As modified is a terminal outcome, transfer of payload data
 * will not be able to be resumed if the link becomes suspended. A delivery can become modified at the source even
 * before all transfer frames have been sent. This does not imply that the remaining transfers for the delivery will not
 * be sent. The source MAY spontaneously attain the modified outcome for a message (for example the source might
 * implement some sort of time-bound acquisition lock, after which the acquisition of a message at a node is revoked to
 * allow for delivery to an alternative consumer with the message modified in some way to denote the previous failed,
 * e.g., with delivery-failed set to true).
 * </p>
 * <p>
 * At the target, the modified outcome is used to indicate that a given transfer was not and will not be acted upon, and
 * that the message SHOULD be modified in the specified ways at the node.
 * </p>
 *
 * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#type-modified">Modified
 *     outcome</a>
 */
@Fluent
public final class ModifiedDeliveryOutcome extends DeliveryOutcome {
    private Map<String, Object> messageAnnotations;
    private Boolean isUndeliverableHere;
    private Boolean isDeliveryFailed;

    /**
     * Creates an instance with the delivery state modified set.
     */
    public ModifiedDeliveryOutcome() {
        super(DeliveryState.MODIFIED);
    }

    /**
     * Gets whether or not the message is undeliverable here.
     *
     * @return {@code true} to not redeliver message.
     */
    public Boolean isUndeliverableHere() {
        return this.isUndeliverableHere;
    }

    /**
     * Sets whether or not the message is undeliverable here.
     *
     * @param isUndeliverable If the message is undeliverable here.
     *
     * @return The updated {@link ModifiedDeliveryOutcome} outcome.
     */
    public ModifiedDeliveryOutcome setUndeliverableHere(boolean isUndeliverable) {
        this.isUndeliverableHere = isUndeliverable;
        return this;
    }

    /**
     * Gets whether or not to count the transfer as an unsuccessful delivery attempt.
     *
     * @return {@code true} to increment the delivery count.
     */
    public Boolean isDeliveryFailed() {
        return isDeliveryFailed;
    }

    /**
     * Sets whether or not to count the transfer as an unsuccessful delivery attempt.
     *
     * @param isDeliveryFailed {@code true} to count the transfer as an unsuccessful delivery attempt.
     *
     * @return The updated {@link ModifiedDeliveryOutcome} outcome.
     */
    public ModifiedDeliveryOutcome setDeliveryFailed(boolean isDeliveryFailed) {
        this.isDeliveryFailed = isDeliveryFailed;
        return this;
    }

    /**
     * Gets a map containing attributes to combine with the existing message-annotations held in the message's header
     * section. Where the existing message-annotations of the message contain an entry with the same key as an entry in
     * this field, the value in this field associated with that key replaces the one in the existing headers; where the
     * existing message-annotations has no such value, the value in this map is added.
     *
     * @return Map containing attributes to combine with existing message annotations on the message.
     */
    public Map<String, Object> getMessageAnnotations() {
        return messageAnnotations;
    }

    /**
     * Sets the message annotations to add to the message.
     *
     * @param messageAnnotations the message annotations to add to the message.
     *
     * @return The updated {@link ModifiedDeliveryOutcome} object.
     */
    public ModifiedDeliveryOutcome setMessageAnnotations(Map<String, Object> messageAnnotations) {
        this.messageAnnotations = messageAnnotations;
        return this;
    }
}
