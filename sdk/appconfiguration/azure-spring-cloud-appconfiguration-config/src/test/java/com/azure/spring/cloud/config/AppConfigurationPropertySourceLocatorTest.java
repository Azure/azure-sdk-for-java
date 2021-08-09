// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import static com.azure.spring.cloud.config.Constants.FEATURE_FLAG_CONTENT_TYPE;
import static com.azure.spring.cloud.config.TestConstants.FEATURE_LABEL;
import static com.azure.spring.cloud.config.TestConstants.FEATURE_VALUE;
import static com.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING_2;
import static com.azure.spring.cloud.config.TestConstants.TEST_CONTEXT;
import static com.azure.spring.cloud.config.TestConstants.TEST_KEY_1;
import static com.azure.spring.cloud.config.TestConstants.TEST_LABEL_1;
import static com.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME;
import static com.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME_1;
import static com.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME_2;
import static com.azure.spring.cloud.config.TestConstants.TEST_VALUE_1;
import static com.azure.spring.cloud.config.TestUtils.createItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreTrigger;
import com.azure.spring.cloud.config.properties.ConfigStore;
import com.azure.spring.cloud.config.properties.FeatureFlagStore;
import com.azure.spring.cloud.config.stores.ClientStore;

import reactor.core.publisher.Flux;

public class AppConfigurationPropertySourceLocatorTest {

    private static final String APPLICATION_NAME = "foo";

    private static final String PROFILE_NAME_1 = "dev";

    private static final String PROFILE_NAME_2 = "prod";

    private static final ConfigurationSetting FEATURE_ITEM = createItem(".appconfig.featureflag/", "Alpha",
        FEATURE_VALUE, FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE);
    private static final String EMPTY_CONTENT_TYPE = "";
    private static final ConfigurationSetting ITEM_1 = createItem(TEST_CONTEXT, TEST_KEY_1, TEST_VALUE_1, TEST_LABEL_1,
        EMPTY_CONTENT_TYPE);
    
    @Mock
    private ConfigurableEnvironment environment;
    @Mock
    private ClientStore clientStoreMock;
    @Mock
    private ConfigStore configStore;
    @Mock
    private FeatureFlagStore featureFlagStoreMock;
    @Mock
    private ConfigurationAsyncClient configClientMock;
    @Mock
    private PagedFlux<ConfigurationSetting> settingsMock;
    @Mock
    private Iterable<PagedResponse<ConfigurationSetting>> iterableMock;
    @Mock
    private Iterator<PagedResponse<ConfigurationSetting>> iteratorMock;
    @Mock
    private Flux<PagedResponse<ConfigurationSetting>> pageMock;
    @Mock
    private PagedResponse<ConfigurationSetting> pagedMock;
    @Mock
    private List<ConfigStore> configStoresMock;
    @Mock
    private ConfigStore configStoreMock;
    @Mock
    private Iterator<ConfigStore> configStoreIterator;
    @Mock
    private AppConfigurationProviderProperties appPropertiesMock;
    @Mock
    private AppConfigurationProperties properties;
    private AppConfigurationPropertySourceLocator locator;
    private AppConfigurationProviderProperties appProperties;
    private KeyVaultCredentialProvider tokenCredentialProvider = null;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(environment.getActiveProfiles()).thenReturn(new String[]{PROFILE_NAME_1, PROFILE_NAME_2});
        MutablePropertySources sources = new MutablePropertySources();
        
        sources.addFirst(new PropertySource<String>("refreshArgs") {

            @Override
            public Object getProperty(String name) {
                return null;
            }
        });
        
        when(environment.getPropertySources()).thenReturn(sources);

        when(properties.getName()).thenReturn(APPLICATION_NAME);
        when(properties.getStores()).thenReturn(configStoresMock);
        when(properties.isEnabled()).thenReturn(true);
        when(configStoresMock.iterator()).thenReturn(configStoreIterator);
        when(configStoreIterator.hasNext()).thenReturn(true).thenReturn(false);
        when(configStoreIterator.next()).thenReturn(configStoreMock);

        when(configStoreMock.getConnectionString()).thenReturn(TEST_CONN_STRING);
        when(configStoreMock.getEndpoint()).thenReturn(TEST_STORE_NAME);
        when(configStoreMock.isEnabled()).thenReturn(true);
        when(configStoreMock.isEnabled()).thenReturn(true);

        AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();
        monitoring.setEnabled(false);
        AppConfigurationStoreTrigger trigger = new AppConfigurationStoreTrigger();
        trigger.setKey("sentinal");
        trigger.setKey("test");
        ArrayList<AppConfigurationStoreTrigger> triggers = new ArrayList<AppConfigurationStoreTrigger>();
        triggers.add(trigger);
        monitoring.setTriggers(triggers);
        when(configStoreMock.getMonitoring()).thenReturn(monitoring);

        when(configClientMock.listConfigurationSettings(Mockito.any())).thenReturn(settingsMock);
        when(settingsMock.byPage()).thenReturn(pageMock);
        when(iterableMock.iterator()).thenReturn(iteratorMock);
        when(iteratorMock.hasNext()).thenReturn(true).thenReturn(false);
        when(iteratorMock.next()).thenReturn(pagedMock);
        when(pagedMock.getItems()).thenReturn(new ArrayList<ConfigurationSetting>());

        appProperties = new AppConfigurationProviderProperties();
        appProperties.setVersion("1.0");
        appProperties.setMaxRetries(12);
        appProperties.setMaxRetryTime(0);
    }

    @After
    public void cleanup()
        throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = AppConfigurationPropertySourceLocator.class.getDeclaredField("startup");
        field.setAccessible(true);
        field.set(null, new AtomicBoolean(true));
        StateHolder.setLoadState(TEST_STORE_NAME, false);
    }

    @Test
    public void compositeSourceIsCreated() {
        String[] labels = new String[1];
        labels[0] = "\0";
        when(configStoreMock.getLabels(Mockito.any())).thenReturn(labels);
        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStoreMock);
        when(properties.getDefaultContext()).thenReturn("application");

        locator = new AppConfigurationPropertySourceLocator(properties, appProperties, clientStoreMock,
            tokenCredentialProvider, null);
        PropertySource<?> source = locator.locate(environment);
        assertThat(source).isInstanceOf(CompositePropertySource.class);

        Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
        
        String[] expectedSourceNames = new String[]{
            "/foo/store1/\0",
            "/application/store1/\0"
        };
        assertThat(sources.size()).isEqualTo(2);
        assertThat(sources.stream().map(s -> s.getName()).toArray()).containsExactly((Object[]) expectedSourceNames);
    }
    
    @Test
    public void storeCreatedWithFeatureFlags() {
        String[] labels = new String[1];
        labels[0] = "\0";
        when(configStoreMock.getLabels(Mockito.any())).thenReturn(labels);
        when(properties.getDefaultContext()).thenReturn("application");
        when(clientStoreMock.getWatchKey(Mockito.any(), Mockito.anyString())).thenReturn(ITEM_1)
        .thenReturn(FEATURE_ITEM);
        
        FeatureFlagStore featureFlagStore = new FeatureFlagStore();
        featureFlagStore.setEnabled(true);
        
        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStore);

        locator = new AppConfigurationPropertySourceLocator(properties, appProperties, clientStoreMock,
            tokenCredentialProvider, null);
        PropertySource<?> source = locator.locate(environment);
        assertThat(source).isInstanceOf(CompositePropertySource.class);

        Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
        // Application name: foo and active profile: dev,prod, should construct below
        // composite Property Source:
        // [/foo_prod/, /foo_dev/, /foo/, /application_prod/, /application_dev/,
        // /application/]
        String[] expectedSourceNames = new String[]{
            "/foo/store1/\0",
            "/application/store1/\0"
        };
        assertThat(sources.size()).isEqualTo(2);
        assertThat(sources.stream().map(s -> s.getName()).toArray()).containsExactly((Object[]) expectedSourceNames);
    }

    @Test
    public void watchedKeyCheck() {
        String[] labels = new String[1];
        labels[0] = "\0";
        when(configStoreMock.getLabels(Mockito.any())).thenReturn(labels);
        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStoreMock);
        when(properties.getDefaultContext()).thenReturn("application");
        when(clientStoreMock.getWatchKey(Mockito.any(), Mockito.anyString())).thenReturn(ITEM_1)
            .thenReturn(FEATURE_ITEM);

        locator = new AppConfigurationPropertySourceLocator(properties, appProperties, clientStoreMock,
            tokenCredentialProvider, null);
        PropertySource<?> source = locator.locate(environment);
        assertThat(source).isInstanceOf(CompositePropertySource.class);

        Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
        // Application name: foo and active profile: dev,prod, should construct below
        // composite Property Source:
        // [/foo_prod/, /foo_dev/, /foo/, /application_prod/, /application_dev/,
        // /application/]
        String[] expectedSourceNames = new String[]{
            "/foo/store1/\0",
            "/application/store1/\0"
        };
        assertThat(sources.size()).isEqualTo(2);
        assertThat(sources.stream().map(s -> s.getName()).toArray()).containsExactly((Object[]) expectedSourceNames);
    }

    @Test
    public void nullApplicationNameCreateDefaultContextOnly() {
        String[] labels = new String[1];
        labels[0] = "\0";
        when(configStoreMock.getLabels(Mockito.any())).thenReturn(labels);
        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStoreMock);
        when(environment.getActiveProfiles()).thenReturn(new String[]{});
        when(environment.getProperty("spring.application.name")).thenReturn(null);
        when(properties.getDefaultContext()).thenReturn("application");
        when(properties.getName()).thenReturn(null);

        locator = new AppConfigurationPropertySourceLocator(properties, appProperties,
            clientStoreMock, tokenCredentialProvider, null);

        PropertySource<?> source = locator.locate(environment);
        assertThat(source).isInstanceOf(CompositePropertySource.class);

        Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
        // Default context, null application name, empty active profile,
        // should construct composite Property Source: [/application/]
        String[] expectedSourceNames = new String[]{"/application/store1/\0"};
        assertThat(sources.size()).isEqualTo(1);
        assertThat(sources.stream().map(s -> s.getName()).toArray()).containsExactly((Object[]) expectedSourceNames);
    }

    @Test
    public void emptyApplicationNameCreateDefaultContextOnly() {
        String[] labels = new String[1];
        labels[0] = "\0";
        when(configStoreMock.getLabels(Mockito.any())).thenReturn(labels);
        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStoreMock);
        when(environment.getActiveProfiles()).thenReturn(new String[]{});
        when(environment.getProperty("spring.application.name")).thenReturn("");
        when(properties.getName()).thenReturn("");
        when(properties.getDefaultContext()).thenReturn("application");
        locator = new AppConfigurationPropertySourceLocator(properties, appProperties,
            clientStoreMock, tokenCredentialProvider, null);

        PropertySource<?> source = locator.locate(environment);
        assertThat(source).isInstanceOf(CompositePropertySource.class);

        Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
        // Default context, empty application name, empty active profile,
        // should construct composite Property Source: [/application/]
        String[] expectedSourceNames = new String[]{"/application/store1/\0"};
        assertThat(sources.size()).isEqualTo(1);
        assertThat(sources.stream().map(s -> s.getName()).toArray()).containsExactly((Object[]) expectedSourceNames);
    }

    @Test
    public void defaultFailFastThrowException() throws IOException {
        when(configStoreMock.isFailFast()).thenReturn(true);
        when(properties.getDefaultContext()).thenReturn("application");

        locator = new AppConfigurationPropertySourceLocator(properties, appProperties,
            clientStoreMock, tokenCredentialProvider, null);

        when(clientStoreMock.listSettings(Mockito.any(), Mockito.anyString())).thenThrow(new RuntimeException());
        NullPointerException e = assertThrows(NullPointerException.class, () -> locator.locate(environment));
        assertThat(e).hasMessage(null);
        verify(configStoreMock, times(1)).isFailFast();
    }

    @Test
    public void refreshThrowException() throws IOException, NoSuchFieldException, SecurityException,
        IllegalArgumentException, IllegalAccessException {
        Field field = AppConfigurationPropertySourceLocator.class.getDeclaredField("startup");
        field.setAccessible(true);
        field.set(null, new AtomicBoolean(false));
        StateHolder.setLoadState(TEST_STORE_NAME, true);

        when(environment.getActiveProfiles()).thenReturn(new String[]{});
        when(environment.getProperty("spring.application.name")).thenReturn(null);

        locator = new AppConfigurationPropertySourceLocator(properties, appProperties,
            clientStoreMock, tokenCredentialProvider, null);
        
        NullPointerException e = assertThrows(NullPointerException.class, () -> locator.locate(environment));
        assertThat(e).hasMessage(null);
    }

    @Test
    public void notFailFastShouldPass() throws IOException {
        when(configStoreMock.isFailFast()).thenReturn(false);
        locator = new AppConfigurationPropertySourceLocator(properties, appProperties,
            clientStoreMock, tokenCredentialProvider, null);

        when(configStoreMock.isFailFast()).thenReturn(false);
        when(clientStoreMock.listSettings(Mockito.any(), Mockito.anyString())).thenThrow(new RuntimeException());
        when(configStoreMock.getEndpoint()).thenReturn(TEST_STORE_NAME);

        PropertySource<?> source = locator.locate(environment);
        assertThat(source).isInstanceOf(CompositePropertySource.class);

        // Once a store fails it should stop attempting to load
        verify(configStoreMock, times(1)).isFailFast();
    }

    @Test
    public void multiplePropertySourcesExistForMultiStores() {
        String[] labels = new String[1];
        labels[0] = "\0";
        when(configStoreMock.getLabels(Mockito.any())).thenReturn(labels);
        when(environment.getActiveProfiles()).thenReturn(new String[]{});
        when(configStoreMock.getEndpoint()).thenReturn(TEST_STORE_NAME);

        properties = new AppConfigurationProperties();
        TestUtils.addStore(properties, TEST_STORE_NAME_1, TEST_CONN_STRING);
        TestUtils.addStore(properties, TEST_STORE_NAME_2, TEST_CONN_STRING_2);

        locator = new AppConfigurationPropertySourceLocator(properties, appProperties,
            clientStoreMock, tokenCredentialProvider, null);

        PropertySource<?> source = locator.locate(environment);
        assertThat(source).isInstanceOf(CompositePropertySource.class);

        Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
        String[] expectedSourceNames = new String[]{"/application/" + TEST_STORE_NAME_2 + "/\0",
            "/application/" + TEST_STORE_NAME_1 + "/\0"};
        assertThat(sources.size()).isEqualTo(2);
        assertThat(sources.stream().map(s -> s.getName()).toArray()).containsExactly((Object[]) expectedSourceNames);
    }

    @Test
    public void awaitOnError() throws Exception {
        List<ConfigStore> configStores = new ArrayList<ConfigStore>();
        configStores.add(configStore);
        AppConfigurationProperties properties = new AppConfigurationProperties();
        properties.setName("TestStoreName");
        properties.setStores(configStores);
        
        when(appPropertiesMock.getPrekillTime()).thenReturn(5);

        ConfigurableEnvironment env = Mockito.mock(ConfigurableEnvironment.class);
        MutablePropertySources sources = new MutablePropertySources();

        sources.addFirst(new PropertySource<String>("refreshArgs") {

            @Override
            public Object getProperty(String name) {
                return null;
            }
        });

        when(env.getPropertySources()).thenReturn(sources);
        
        String[] array = {};
        when(env.getActiveProfiles()).thenReturn(array);
        String[] labels = {""};
        when(configStore.getLabels(Mockito.any())).thenReturn(labels);
        when(configStore.isEnabled()).thenReturn(true);
        when(clientStoreMock.listSettings(Mockito.any(), Mockito.any())).thenThrow(new NullPointerException(""));
        when(appPropertiesMock.getPrekillTime()).thenReturn(-60);
        when(appPropertiesMock.getStartDate()).thenReturn(new Date());

        locator = new AppConfigurationPropertySourceLocator(properties, appPropertiesMock, clientStoreMock,
            tokenCredentialProvider, null);

        boolean threwException = false;
        try {
            locator.locate(env);
        } catch (Exception e) {
            threwException = true;
        }
        assertTrue(threwException);
        verify(appPropertiesMock, times(1)).getPrekillTime();
    }
}
