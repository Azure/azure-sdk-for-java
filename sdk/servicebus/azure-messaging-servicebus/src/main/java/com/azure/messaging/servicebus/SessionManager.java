package com.azure.messaging.servicebus;

import java.nio.ByteBuffer;
import java.util.Map;

public class SessionManager {
    public ByteBuffer getSessionState(String sessionId) { return null;}
    public void setSessionState(String sessionId, ByteBuffer sessionState) {}
    public void abandon(MessageLockToken lockToken) {}
    public void abandon(MessageLockToken lockToken, Map<String, Object> propertiesToModify) {}
    public void complete(MessageLockToken lockToken) { }
    public void defer(MessageLockToken lockToken) { }
    public void defer(MessageLockToken lockToken, Map<String, Object> propertiesToModify) { }
    public void deadLetter(MessageLockToken lockToken) { }
    public void deadLetter(MessageLockToken lockToken, DeadLetterOptions deadLetterOptions) { }
}
