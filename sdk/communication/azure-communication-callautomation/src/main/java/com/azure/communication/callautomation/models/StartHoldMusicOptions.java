package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;

/**
 * Options for the Start Hold Music operation.
 */
public class StartHoldMusicOptions {

    private CommunicationIdentifier targetParticipant;
    private PlaySource playSourceInfo;
    private boolean loop;
    private String operationContext;

    public StartHoldMusicOptions(CommunicationIdentifier targetParticipant, PlaySource playSourceInfo) {
        this.targetParticipant = targetParticipant;
        this.playSourceInfo = playSourceInfo;
        loop = true;
    }

    public CommunicationIdentifier getTargetParticipant() {
        return targetParticipant;
    }

    public PlaySource getPlaySourceInfo() {
        return playSourceInfo;
    }

    public boolean isLoop() {
        return loop;
    }

    public StartHoldMusicOptions setLoop(boolean loop) {
        this.loop = loop;
        return this;
    }

    public String getOperationContext() {
        return operationContext;
    }

    public StartHoldMusicOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }
}
