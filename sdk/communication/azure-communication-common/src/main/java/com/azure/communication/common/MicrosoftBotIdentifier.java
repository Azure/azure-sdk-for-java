// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;

import com.azure.core.util.CoreUtils;

/**
 * Communication identifier for Microsoft Bot
 */
public final class MicrosoftBotIdentifier extends CommunicationIdentifier {
    private final String botId;
    private final boolean isResourceAccountConfigured;
    private boolean rawIdSet = false;

    private final CommunicationCloudEnvironment cloudEnvironment;

    /**
     * Creates a MicrosoftBotIdentifier object
     *
     * @param botId botId The unique Microsoft app ID for the bot as registered with the Bot Framework.
     * @param cloudEnvironment the cloud environment in which this identifier is created.
     * @param isResourceAccountConfigured set this to true if the bot is tenantized. It is false if the bot is global and no resource account is configured.
     * @throws IllegalArgumentException thrown if botId parameter fail the validation.
     */
    public MicrosoftBotIdentifier(String botId, CommunicationCloudEnvironment cloudEnvironment, boolean isResourceAccountConfigured) {
        if (CoreUtils.isNullOrEmpty(botId)) {
            throw new IllegalArgumentException("The initialization parameter [botId] cannot be null or empty.");
        }
        this.botId = botId;
        this.cloudEnvironment = cloudEnvironment;
        this.isResourceAccountConfigured = isResourceAccountConfigured;
        generateRawId();
    }

    /**
     * Creates a MicrosoftBotIdentifier object
     *
     * @param botId Id of the Microsoft bot.
     * @throws IllegalArgumentException thrown if botId parameter fail the validation.
     */
    public MicrosoftBotIdentifier(String botId) {
        this(botId, CommunicationCloudEnvironment.PUBLIC, true);
    }

    /**
     * Creates a MicrosoftBotIdentifier object
     *
     * @param botId Id of the Microsoft bot.
     * @param isResourceAccountConfigured set this to true if the bot is tenantized. It is false if the bot is global and no resource account is configured.
     * @throws IllegalArgumentException thrown if botId parameter fail the validation.
     */
    public MicrosoftBotIdentifier(String botId, boolean isResourceAccountConfigured)  {
        this(botId, CommunicationCloudEnvironment.PUBLIC, isResourceAccountConfigured);
    }

    /**
     * Get bot Id
     * @return bot Id of the Microsoft bot.
     */
    public String getBotId() {
        return this.botId;
    }

    /**
     * @return True if the bot is tenantized. It is False if the bot is global and no resource account is configured.
     */
    public boolean isResourceAccountConfigured() {
        return this.isResourceAccountConfigured;
    }

    /**
     * Get cloud environment of the bot identifier
     *
     * @return cloud environment in which this identifier is created
     */
    public CommunicationCloudEnvironment getCloudEnvironment() {
        return cloudEnvironment;
    }

    /**
     * Set full id of the identifier
     * RawId is the encoded format for identifiers to store in databases or as stable keys in general.
     *
     * @param rawId full id of the identifier
     * @return MicrosoftBotIdentifier object itself
     */
    @Override
    public MicrosoftBotIdentifier setRawId(String rawId) {
        super.setRawId(rawId);
        rawIdSet = true;
        return this;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof MicrosoftBotIdentifier)) {
            return false;
        }

        return ((MicrosoftBotIdentifier) that).getRawId().equals(getRawId());
    }

    @Override
    public int hashCode() {
        return getRawId().hashCode();
    }

    private void generateRawId() {
        if (!rawIdSet) {
            if (!this.isResourceAccountConfigured) {
                if (cloudEnvironment.equals(CommunicationCloudEnvironment.DOD)) {
                    super.setRawId(BOT_DOD_CLOUD_GLOBAL_PREFIX + this.botId);
                } else if (cloudEnvironment.equals(CommunicationCloudEnvironment.GCCH)) {
                    super.setRawId(BOT_GCCH_CLOUD_GLOBAL_PREFIX + this.botId);
                } else {
                    super.setRawId(BOT_PREFIX + this.botId);
                }
            } else if (cloudEnvironment.equals(CommunicationCloudEnvironment.DOD)) {
                super.setRawId(BOT_DOD_CLOUD_PREFIX + this.botId);
            } else if (cloudEnvironment.equals(CommunicationCloudEnvironment.GCCH)) {
                super.setRawId(BOT_GCCH_CLOUD_PREFIX + this.botId);
            } else {
                super.setRawId(BOT_PUBLIC_CLOUD_PREFIX + this.botId);
            }
        }
    }
}
