// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;

import com.azure.core.util.CoreUtils;

/**
 * Communication identifier for Microsoft Teams User
 */
public class MicrosoftTeamsUserIdentifier extends CommunicationIdentifier {
    
    private final String id;
    private final boolean isAnonymous;

    /**
     * Creates a MicrosoftTeamsUserIdentifier object
     * 
     * @param userId the string identifier representing the identity
     * @param isAnonymous set this to true if the user is anonymous, 
     *                    for example when joining a meeting with a share link
     * @throws IllegalArgumentException thrown if id parameter fail the validation.
    */
    public MicrosoftTeamsUserIdentifier(String userId, boolean isAnonymous) {
        if (CoreUtils.isNullOrEmpty(userId)) {
            throw new IllegalArgumentException("The initialization parameter [userId] cannot be null or empty.");
        }
        this.id = userId;
        this.isAnonymous = isAnonymous;
    }

    /**
     * Creates a MicrosoftTeamsUserIdentifier object
     * 
     * @param userId the string identifier representing the identity
     * @throws IllegalArgumentException thrown if id parameter fail the validation.
    */
    public MicrosoftTeamsUserIdentifier(String userId) {
        this(userId, false);
    }

    /**
     * @return the string identifier representing the MicrosoftTeamsUserIdentifier object
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return True if the user is anonymous, for example when joining a meeting with a share link.
     */
    public boolean isAnonymous() {
        return this.isAnonymous;
    }
    
}
