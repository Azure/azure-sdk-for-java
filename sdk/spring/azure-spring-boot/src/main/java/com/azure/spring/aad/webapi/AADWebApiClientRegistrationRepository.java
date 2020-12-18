// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapi;

import com.azure.spring.aad.AADClientRegistrationRepository;
import com.azure.spring.aad.webapp.AzureClientRegistration;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.Iterator;
import java.util.List;

/**
 * Manage all AAD oauth2 clients configured by property "azure.activedirectory.xxx" for webapi
 */
public class AADWebApiClientRegistrationRepository extends AADClientRegistrationRepository
    implements Iterable<ClientRegistration> {

    public AADWebApiClientRegistrationRepository(AzureClientRegistration azureClient,
                                                 List<ClientRegistration> otherClients,
                                                 AADAuthenticationProperties properties) {
        super(azureClient, otherClients, properties);
    }

    @NotNull
    @Override
    public Iterator<ClientRegistration> iterator() {
        return allClients.values().iterator();
    }

}
