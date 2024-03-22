// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.autofailover;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;

public class ReplicaLookUpTest {

    private ReplicaLookUp replicaLookUp;

    private AppConfigurationProperties properties;

    @Mock
    private InitialDirContext contextMock;

    @Mock
    private Attributes srvOriginMock;

    @Mock
    private Attributes srvReplicaMock;

    @Mock
    private Attribute srvAttrOriginMock;

    @Mock
    private Attribute srvAttrReplicaMock;

    @Mock
    private NamingEnumeration<?> namingOriginMock;

    @Mock
    private NamingEnumeration<?> namingReplicaMock;

    private ConfigStore configStore;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);

        properties = new AppConfigurationProperties();
        configStore = new ConfigStore();
        configStore.setEndpoint("https://fake.endpoint.azconfig.test");
    }

    @Test
    public void updateAutoFailoverEndpointsUnconfiguredTest() throws NamingException {
        properties.setStores(List.of());
        replicaLookUp = new ReplicaLookUp(properties);
        assertEquals(0, replicaLookUp.getAutoFailoverEndpoints("test.endpoint.azconfig.test").size());
        replicaLookUp.context = contextMock;

        replicaLookUp.updateAutoFailoverEndpoints();
        assertEquals(0, replicaLookUp.getAutoFailoverEndpoints("test.endpoint.azconfig.test").size());
    }

    @Test
    public void updateAutoFailoverEndpointsNoReplicasTest() throws NamingException {
        when(contextMock.getAttributes(Mockito.anyString(), Mockito.any())).thenReturn(srvOriginMock)
            .thenThrow(new NameNotFoundException());
        when(srvOriginMock.get(Mockito.anyString())).thenReturn(srvAttrOriginMock);
        Mockito.doReturn(namingOriginMock).when(srvAttrOriginMock).getAll();
        when(namingOriginMock.hasMore()).thenReturn(true).thenReturn(false);
        Mockito.doReturn("1 1 1 fake.endpoint.azconfig.test.").when(namingOriginMock).next();

        ConfigStore configStore = new ConfigStore();
        configStore.setEndpoint("https://fake.endpoint.azconfig.test");
        properties.setStores(List.of(configStore));
        replicaLookUp = new ReplicaLookUp(properties);
        assertEquals(0, replicaLookUp.getAutoFailoverEndpoints("https://fake.endpoint.azconfig.test").size());
        replicaLookUp.context = contextMock;

        replicaLookUp.updateAutoFailoverEndpoints();
        assertEquals(0, replicaLookUp.getAutoFailoverEndpoints("https://fake.endpoint.azconfig.test").size());
    }

    @Test
    public void updateAutoFailoverEndpointsAReplicasTest() throws NamingException {
        setupMock();
        when(namingReplicaMock.hasMore()).thenReturn(true).thenReturn(false);
        Mockito.doReturn("1 1 1 fake.endpoint.replica.azconfig.test.").when(namingReplicaMock).next();

        assertEquals(0, replicaLookUp.getAutoFailoverEndpoints("https://fake.endpoint.azconfig.test").size());

        replicaLookUp.updateAutoFailoverEndpoints();
        assertEquals(1, replicaLookUp.getAutoFailoverEndpoints("https://fake.endpoint.azconfig.test").size());
    }

    @Test
    public void updateAutoFailoverEndpointsMuplipleReplicasTest() throws NamingException {
        setupMock();
        when(namingReplicaMock.hasMore()).thenReturn(true).thenReturn(true).thenReturn(false);
        Mockito.doReturn("1 1 1 fake.endpoint.replica.azconfig.test.")
            .doReturn("1 1 1 fake.endpoint.replica2.azconfig.test.").when(namingReplicaMock).next();

        assertEquals(0, replicaLookUp.getAutoFailoverEndpoints("https://fake.endpoint.azconfig.test").size());

        replicaLookUp.updateAutoFailoverEndpoints();
        assertEquals(2, replicaLookUp.getAutoFailoverEndpoints("https://fake.endpoint.azconfig.test").size());
    }

    @Test
    public void updateAutoFailoverEndpointsConnectionStringTest() throws NamingException {
        configStore.setConnectionStrings(List.of("Endpoint=https://fake.endpoint.azconfig.test;Id=0;Secret=0="));
        setupMock();
        when(namingReplicaMock.hasMore()).thenReturn(true).thenReturn(false);
        Mockito.doReturn("1 1 1 fake.endpoint.replica.azconfig.test.").when(namingReplicaMock).next();
        
        assertEquals(0, replicaLookUp.getAutoFailoverEndpoints("https://fake.endpoint.azconfig.test").size());

        replicaLookUp.updateAutoFailoverEndpoints();
        assertEquals(1, replicaLookUp.getAutoFailoverEndpoints("https://fake.endpoint.azconfig.test").size());
    }
    
    @Test
    public void updateAutoFailoverEndpointsInvalidReplicaTest() throws NamingException {
        setupMock();
        when(namingReplicaMock.hasMore()).thenReturn(true).thenReturn(false);
        Mockito.doReturn("1 1 1 fake.endpoint.replica.invalid.test.").when(namingReplicaMock).next();

        assertEquals(0, replicaLookUp.getAutoFailoverEndpoints("https://fake.endpoint.azconfig.test").size());

        replicaLookUp.updateAutoFailoverEndpoints();
        assertEquals(0, replicaLookUp.getAutoFailoverEndpoints("https://fake.endpoint.azconfig.test").size());
    }

    private void setupMock() throws NamingException {
        when(contextMock.getAttributes(Mockito.anyString(), Mockito.any())).thenReturn(srvOriginMock)
            .thenReturn(srvReplicaMock).thenThrow(new NameNotFoundException());
        when(srvOriginMock.get(Mockito.anyString())).thenReturn(srvAttrOriginMock);
        Mockito.doReturn(namingOriginMock).when(srvAttrOriginMock).getAll();
        when(namingOriginMock.hasMore()).thenReturn(true).thenReturn(false);
        Mockito.doReturn("1 1 1 fake.endpoint.").when(namingOriginMock).next();

        when(srvReplicaMock.get(Mockito.anyString())).thenReturn(srvAttrReplicaMock);
        Mockito.doReturn(namingReplicaMock).when(srvAttrReplicaMock).getAll();
        
        properties.setStores(List.of(configStore));
        replicaLookUp = new ReplicaLookUp(properties);
        
        replicaLookUp.context = contextMock;
    }
}
