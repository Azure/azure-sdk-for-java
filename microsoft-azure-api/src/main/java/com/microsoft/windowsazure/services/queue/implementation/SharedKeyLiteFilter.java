package com.microsoft.windowsazure.services.queue.implementation;

import javax.inject.Named;

import com.microsoft.windowsazure.services.queue.QueueConfiguration;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

/*
 * TODO: Should the "full" shared key signing?
 */
public class SharedKeyLiteFilter extends ClientFilter {
    private final com.microsoft.windowsazure.services.blob.implementation.SharedKeyLiteFilter blobSharedKeyFilter;

    public SharedKeyLiteFilter(@Named(QueueConfiguration.ACCOUNT_NAME) String accountName, @Named(QueueConfiguration.ACCOUNT_KEY) String accountKey) {
        blobSharedKeyFilter = new com.microsoft.windowsazure.services.blob.implementation.SharedKeyLiteFilter(accountName, accountKey);
    }

    @Override
    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {

        // Only sign if no other filter has done it yet
        if (cr.getHeaders().getFirst("Authorization") == null) {
            blobSharedKeyFilter.sign(cr);
        }

        return this.getNext().handle(cr);
    }
}
