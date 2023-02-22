// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;

import com.azure.core.util.CoreUtils;

/**
 * Communication identifier for Microsoft Teams User
 */
public final class MicrosoftBotIdentifier extends CommunicationIdentifier {
    private final String botId;
    private final boolean isGlobal;
    private boolean rawIdSet = false;

    private CommunicationCloudEnvironment cloudEnvironment = CommunicationCloudEnvironment.PUBLIC;

    /**
     * Creates a MicrosoftBotIdentifier object
     *
     * @param botId Id of the Microsoft bot.
     * @param isGlobal set this to true if the bot is global.
     * @throws IllegalArgumentException thrown if botId parameter fail the validation.
     */
    public MicrosoftBotIdentifier(String botId, boolean isGlobal) {
        if (CoreUtils.isNullOrEmpty(botId)) {
            throw new IllegalArgumentException("The initialization parameter [botId] cannot be null or empty.");
        }
        this.botId = botId;
        this.isGlobal = isGlobal;
        generateRawId();
    }

    /**
     * Creates a MicrosoftBotIdentifier object
     *
     * @param botId Id of the Microsoft bot.
     * @throws IllegalArgumentException thrown if botId parameter fail the validation.
     */
    public MicrosoftBotIdentifier(String botId) {
        this(botId, false);
    }

    /**
     * Get bot Id
     * @return bot Id of the Microsoft bot.
     */
    public String getBotId() {
        return this.botId;
    }

    /**
     * @return True if the bot is global.
     */
    public boolean isGlobal() {
        return this.isGlobal;
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
     * Set cloud environment of the bot identifier
     *
     * @param cloudEnvironment the cloud environment in which this identifier is created
     * @return this object
     */
    public MicrosoftBotIdentifier setCloudEnvironment(CommunicationCloudEnvironment cloudEnvironment) {
        this.cloudEnvironment = cloudEnvironment;
        generateRawId();
        return this;
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

        MicrosoftBotIdentifier thatId = (MicrosoftBotIdentifier) that;

        if (cloudEnvironment != null && !cloudEnvironment.equals(thatId.cloudEnvironment)) {
            return false;
        }

        if (thatId.cloudEnvironment != null && !thatId.cloudEnvironment.equals(this.cloudEnvironment)) {
            return false;
        }

        if (thatId.isGlobal() != this.isGlobal()) {
            return false;
        }

        if (!thatId.getBotId().equals(this.getBotId())) {
            return false;
        }

        return getRawId() == null
            || thatId.getRawId() == null
            || thatId.getRawId().equals(this.getRawId());
    }

    @Override
    public int hashCode() {
        return getRawId().hashCode();
    }

    private void generateRawId() {
        if (!rawIdSet) {
            if (this.isGlobal) {
                if (cloudEnvironment.equals(CommunicationCloudEnvironment.DOD)) {
                    super.setRawId(BOT_DOD_CLOUD_GLOBAL + this.botId);
                } else if (cloudEnvironment.equals(CommunicationCloudEnvironment.GCCH)) {
                    super.setRawId(BOT_GCCH_CLOUD_GLOBAL + this.botId);
                } else {
                    super.setRawId(BOT + this.botId);
                }
            } else if (cloudEnvironment.equals(CommunicationCloudEnvironment.DOD)) {
                super.setRawId(BOT_DOD_CLOUD + this.botId);
            } else if (cloudEnvironment.equals(CommunicationCloudEnvironment.GCCH)) {
                super.setRawId(BOT_GCCH_CLOUD + this.botId);
            } else {
                super.setRawId(BOT_PUBLIC_CLOUD + this.botId);
            }
        }
    }
}
