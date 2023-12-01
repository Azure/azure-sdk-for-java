package com.azure.spring.cloud.appconfiguration.config.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;

import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;

public class AppConfigurationDataLocationResolverTest {

    @Mock
    private ConfigDataLocationResolverContext contextMock;

    @Mock
    private Binder binderMock;

    @Mock
    private ConfigurableBootstrapContext bootContextMock;

    @Mock
    private BindHandler bindHandlerMock;

    @Mock
    private BindResult<String> bindResultEndpointMock;

    @Mock
    private BindResult<String> bindResultConnectionStringMock;

    @Mock
    private BindResult<String> bindResultVersionMock;

    @Mock
    private BindResult<Object> bindResultPropertiesMock;

    @Mock
    private BindResult<Object> bindResultProviderPropertiesMock;

    @Mock
    private ConfigDataLocation locationMock;

    @Mock
    private Profiles profilesMock;
    
    @Mock
    private InstanceSupplier<Object> customizerSupplierMock;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(contextMock.getBinder()).thenReturn(binderMock);
        when(contextMock.getBootstrapContext()).thenReturn(bootContextMock);
        when(bootContextMock.getOrElse(Mockito.eq(BindHandler.class), Mockito.any())).thenReturn(bindHandlerMock);
    }

    @Test
    public void isResolvableTest() {
        when(locationMock.hasPrefix(Mockito.matches(AppConfigurationDataLocationResolver.PREFIX))).thenReturn(false);
        AppConfigurationDataLocationResolver resolver = new AppConfigurationDataLocationResolver();
        assertFalse(resolver.isResolvable(contextMock, locationMock));
        
        when(locationMock.hasPrefix(Mockito.matches(AppConfigurationDataLocationResolver.PREFIX))).thenReturn(true);
        when(binderMock.bind(Mockito.startsWith(AppConfigurationProperties.CONFIG_PREFIX + ".stores[0].endpoint"), Mockito.eq(String.class))).thenReturn(bindResultEndpointMock);
        when(binderMock.bind(Mockito.startsWith(AppConfigurationProperties.CONFIG_PREFIX + ".stores[0].connection-string"), Mockito.eq(String.class))).thenReturn(bindResultConnectionStringMock);
        when(binderMock.bind(Mockito.startsWith("spring.cloud.appconfiguration.version"), Mockito.eq(String.class))).thenReturn(bindResultVersionMock);
        when(bindResultEndpointMock.orElse(Mockito.anyString())).thenReturn("");
        when(bindResultConnectionStringMock.orElse(Mockito.anyString())).thenReturn("");
        when(bindResultVersionMock.orElse(Mockito.anyString())).thenReturn("");
        assertFalse(resolver.isResolvable(contextMock, locationMock));
        
        when(bindResultEndpointMock.orElse(Mockito.anyString())).thenReturn("endpoint");
        assertTrue(resolver.isResolvable(contextMock, locationMock));
        
        when(bindResultEndpointMock.orElse(Mockito.anyString())).thenReturn("");
        when(bindResultConnectionStringMock.orElse(Mockito.anyString())).thenReturn("connectionString");
        assertTrue(resolver.isResolvable(contextMock, locationMock));
    }

    @Test
    public void noResolveTest() {
        AppConfigurationDataLocationResolver resolver = new AppConfigurationDataLocationResolver();
        assertEquals(0, resolver.resolve(contextMock, locationMock).size());
    }

    @Test
    public void resolveProfileSpecificTest() {
        AppConfigurationDataLocationResolver resolver = new AppConfigurationDataLocationResolver();
        AppConfigurationProperties properties = new AppConfigurationProperties();
        AppConfigurationProviderProperties providerProperties = new AppConfigurationProviderProperties();
        providerProperties.setMaxRetryTime(1);

        ConfigStore configStore = new ConfigStore();
        configStore.setEndpoint("test.azconfig.test");
        properties.setStores(List.of(configStore));

        when(
            binderMock.bind(Mockito.startsWith(AppConfigurationProperties.CONFIG_PREFIX), Mockito.any(), Mockito.any()))
                .thenReturn(bindResultPropertiesMock);
        when(bindResultPropertiesMock.get()).thenReturn(properties);

        when(binderMock.bind(Mockito.startsWith(AppConfigurationProviderProperties.CONFIG_PREFIX), Mockito.any(),
            Mockito.any())).thenReturn(bindResultProviderPropertiesMock);
        when(bindResultProviderPropertiesMock.orElseGet(Mockito.any())).thenReturn(providerProperties);
        
        when(bootContextMock.getRegisteredInstanceSupplier(Mockito.any())).thenReturn(customizerSupplierMock);
        when(customizerSupplierMock.get(Mockito.any())).thenReturn(null);

        List<AppConfigDataResource> resources = resolver.resolveProfileSpecific(contextMock, locationMock,
            profilesMock);
        
        assertEquals(1, resources.size());
        assertTrue(resources.get(0).isConfigStoreEnabled());
        assertEquals("test.azconfig.test", resources.get(0).getEndpoint());
    }

}
