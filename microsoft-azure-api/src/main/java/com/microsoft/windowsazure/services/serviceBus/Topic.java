package com.microsoft.windowsazure.services.serviceBus;

import javax.ws.rs.core.MediaType;

import com.microsoft.windowsazure.services.serviceBus.implementation.Content;
import com.microsoft.windowsazure.services.serviceBus.implementation.Entry;
import com.microsoft.windowsazure.services.serviceBus.implementation.EntryModel;
import com.microsoft.windowsazure.services.serviceBus.implementation.TopicDescription;

public class Topic extends EntryModel<TopicDescription> {
    public Topic() {
        super(new Entry(), new TopicDescription());
        getEntry().setContent(new Content());
        getEntry().getContent().setType(MediaType.APPLICATION_XML);
        getEntry().getContent().setTopicDescription(getModel());
    }

    public Topic(Entry entry) {
        super(entry, entry.getContent().getTopicDescription());
    }

    public String getName() {
        return getEntry().getTitle();
    }

    public Topic setName(String value) {
        getEntry().setTitle(value);
        return this;
    }
}
