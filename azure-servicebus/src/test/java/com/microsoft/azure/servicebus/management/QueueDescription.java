package com.microsoft.azure.servicebus.management;

import java.time.Duration;

public class QueueDescription extends ResourceDescripton{
    // Supports only a limited set of properties, just for unit tests.. There are some quirks too. Order of xml elements also matters
    private static final String ATOM_XML_FORMAT = "<entry xmlns=\"http://www.w3.org/2005/Atom\">"
       + "<content type=\"application/xml\">"
            + "<QueueDescription xmlns=\"http://schemas.microsoft.com/netservices/2010/10/servicebus/connect\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">"
                 + "<LockDuration>%s</LockDuration>"
                 + "<MaxSizeInMegabytes>%d</MaxSizeInMegabytes>"
                 + "<RequiresSession>%b</RequiresSession>"
                 + "<DefaultMessageTimeToLive>%s</DefaultMessageTimeToLive>"
                 + "<MaxDeliveryCount>%s</MaxDeliveryCount>"
                 + "<EnablePartitioning>%b</EnablePartitioning>"
            + "</QueueDescription>"
       + "</content>"
     + "</entry>";
    private String path;
    private String forwardTo;
    private String forwardDeadLetteredMessagesTo;
    private int maxSizeInMegaBytes;
    private int maxDeliveryCount;
    private boolean requiresDuplicateDetection;
    private boolean requiresSession;
    private boolean enableDeadLetteringOnMessageExpiration;
    private boolean enablePartitioning;
    private boolean enableExpress;
    private boolean supportsOrdering;
    private Duration lockDuration;
    private Duration defaultMessageTimeToLive;
    private Duration autoDeleteOnIdle;
    private Duration duplicateDetectionHistoryTimeWindow;
    
    public QueueDescription(String path)
    {
        this.path = path;
        this.defaultMessageTimeToLive = Duration.ofDays(7);
        this.maxSizeInMegaBytes = 1024;
        this.lockDuration = Duration.ofMinutes(1);
    }

    @Override
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getMaxSizeInMegaBytes() {
        return maxSizeInMegaBytes;
    }

    public void setMaxSizeInMegaBytes(int maxSizeInMegaBytes) {
        this.maxSizeInMegaBytes = maxSizeInMegaBytes;
    }

    public boolean isRequiresSession() {
        return requiresSession;
    }

    public void setRequiresSession(boolean requiresSession) {
        this.requiresSession = requiresSession;
    }

    public boolean isEnablePartitioning() {
        return enablePartitioning;
    }

    public void setEnablePartitioning(boolean enablePartitioning) {
        this.enablePartitioning = enablePartitioning;
    }
    
    public Duration getLockDuration() {
        return lockDuration;
    }

    public void setLockDuration(Duration lockDuration) {
        this.lockDuration = lockDuration;
    }

    public Duration getDefaultMessageTimeToLive() {
        return defaultMessageTimeToLive;
    }

    public void setDefaultMessageTimeToLive(Duration defaultMessageTimeToLive) {
        this.defaultMessageTimeToLive = defaultMessageTimeToLive;
    }    

    public int getMaxDeliveryCount() {
        return maxDeliveryCount;
    }

    public void setMaxDeliveryCount(int maxDeliveryCount) {
        this.maxDeliveryCount = maxDeliveryCount;
    }

    @Override
    String getAtomXml()
    {
        //return String.format(ATOM_XML_FORMAT, SerializerUtil.serializeDuration(this.lockDuration), this.maxSizeInMegaBytes, this.requiresSession, SerializerUtil.serializeDuration(this.defaultMessageTimeToLive), SerializerUtil.serializeEnablePartitioning(this.enablePartitioning));
        return String.format(ATOM_XML_FORMAT, SerializerUtil.serializeDuration(this.lockDuration), this.maxSizeInMegaBytes, this.requiresSession, SerializerUtil.serializeDuration(this.defaultMessageTimeToLive), this.maxDeliveryCount, this.enablePartitioning);
    }
}
