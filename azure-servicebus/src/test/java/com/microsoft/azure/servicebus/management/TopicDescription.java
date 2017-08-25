package com.microsoft.azure.servicebus.management;

import java.time.Duration;

public class TopicDescription extends ResourceDescripton{
    private static final String ATOM_XML_FORMAT = "<entry xmlns=\"http://www.w3.org/2005/Atom\">"
            + "<content type=\"application/xml\">"
                 + "<TopicDescription xmlns=\"http://schemas.microsoft.com/netservices/2010/10/servicebus/connect\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">"
                      + "<MaxSizeInMegabytes>%d</MaxSizeInMegabytes>"
                      + "<EnablePartitioning>%b</EnablePartitioning>"
                 + "</TopicDescription>"
            + "</content>"
          + "</entry>";
    
    private String path;
    private int maxSizeInMegaBytes;
    private boolean enablePartitioning;
    private boolean enableSubscriptionPartitioning;
    private boolean requiresDuplicateDetection;
    private boolean enableExpress;
    private boolean supportsOrdering;
    private boolean enableFilteringMessagesBeforePublishing;
    private Duration autoDeleteOnIdle;
    private Duration duplicateDetectionHistoryTimeWindow;
    
    public TopicDescription(String path)
    {
        this.path = path;
        this.maxSizeInMegaBytes = 1024;
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

    public boolean isEnablePartitioning() {
        return enablePartitioning;
    }

    public void setEnablePartitioning(boolean enablePartitioning) {
        this.enablePartitioning = enablePartitioning;
    }

    @Override
    String getAtomXml() {
        return String.format(ATOM_XML_FORMAT, this.maxSizeInMegaBytes, this.enablePartitioning);
    }
}
