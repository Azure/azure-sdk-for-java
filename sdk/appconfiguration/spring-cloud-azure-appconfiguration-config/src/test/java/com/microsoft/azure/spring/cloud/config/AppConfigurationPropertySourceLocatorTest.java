// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config;

import static com.microsoft.azure.spring.cloud.config.Constants.FEATURE_FLAG_CONTENT_TYPE;
import static com.microsoft.azure.spring.cloud.config.TestConstants.FEATURE_LABEL;
import static com.microsoft.azure.spring.cloud.config.TestConstants.FEATURE_VALUE;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING_2;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_CONTEXT;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_KEY_1;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_LABEL_1;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME_1;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME_2;
import static com.microsoft.azure.spring.cloud.config.TestConstants.TEST_VALUE_1;
import static com.microsoft.azure.spring.cloud.config.TestUtils.createItem;
import static org.assertj.core.api.Assertions.assertThat;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.microsoft.azure.spring.cloud.config.stores.ClientStore;
import com.microsoft.azure.spring.cloud.config.stores.ConfigStore;

import reactor.core.publisher.Flux;

public class AppConfigurationPropertySourceLocatorTest {
    private static final String APPLICATION_NAME = "foo";

    private static final String PROFILE_NAME_1 = "dev";

    private static final String PROFILE_NAME_2 = "prod";

    private static final String PREFIX = "/config";

    private static final ConfigurationSetting featureItem = createItem(".appconfig.featureflag/", "Alpha",
            FEATURE_VALUE, FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE);

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Mock
    private ConfigurableEnvironment environment;

    @Mock
    private ClientStore clientStoreMock;

    @Mock
    private ConfigStore configStore;

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

    private static final String EMPTY_CONTENT_TYPE = "";

    private static final ConfigurationSetting item1 = createItem(TEST_CONTEXT, TEST_KEY_1, TEST_VALUE_1, TEST_LABEL_1,
            EMPTY_CONTENT_TYPE);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(environment.getActiveProfiles()).thenReturn(new String[] { PROFILE_NAME_1, PROFILE_NAME_2 });

        when(properties.getName()).thenReturn(APPLICATION_NAME);
        when(properties.getProfileSeparator()).thenReturn("_");
        when(properties.getStores()).thenReturn(configStoresMock);
        when(properties.isEnabled()).thenReturn(true);
        when(configStoresMock.iterator()).thenReturn(configStoreIterator);
        when(configStoreIterator.hasNext()).thenReturn(true).thenReturn(false);
        when(configStoreIterator.next()).thenReturn(configStoreMock);

        when(configStoreMock.getConnectionString()).thenReturn(TEST_CONN_STRING);
        when(configStoreMock.getEndpoint()).thenReturn(TEST_STORE_NAME);
        when(configStoreMock.getPrefix()).thenReturn(null);

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
        when(configStoreMock.getLabels()).thenReturn(labels);
        when(properties.getDefaultContext()).thenReturn("application");

        locator = new AppConfigurationPropertySourceLocator(properties, appProperties, clientStoreMock,
                tokenCredentialProvider, null);
        PropertySource<?> source = locator.locate(environment);
        assertThat(source).isInstanceOf(CompositePropertySource.class);

        Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
        // Application name: foo and active profile: dev,prod, should construct below
        // composite Property Source:
        // [/foo_prod/, /foo_dev/, /foo/, /application_prod/, /application_dev/,
        // /application/]
        String[] expectedSourceNames = new String[] { "/foo_prod/store1/\0", "/foo_dev/store1/\0", "/foo/store1/\0",
                "/application_prod/store1/\0", "/application_dev/store1/\0", "/application/store1/\0" };
        assertThat(sources.size()).isEqualTo(6);
        assertThat(sources.stream().map(s -> s.getName()).toArray()).containsExactly((Object[]) expectedSourceNames);
    }

    @Test
    public void revisionsCheck() {
        String[] labels = new String[1];
        labels[0] = "\0";
        when(configStoreMock.getLabels()).thenReturn(labels);
        when(properties.getDefaultContext()).thenReturn("application");
        when(clientStoreMock.getRevison(Mockito.any(), Mockito.anyString())).thenReturn(item1)
        .thenReturn(featureItem);

        locator = new AppConfigurationPropertySourceLocator(properties, appProperties, clientStoreMock,
                tokenCredentialProvider, null);
        PropertySource<?> source = locator.locate(environment);
        assertThat(source).isInstanceOf(CompositePropertySource.class);

        Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
        // Application name: foo and active profile: dev,prod, should construct below
        // composite Property Source:
        // [/foo_prod/, /foo_dev/, /foo/, /application_prod/, /application_dev/,
        // /application/]
        String[] expectedSourceNames = new String[] { "/foo_prod/store1/\0", "/foo_dev/store1/\0", "/foo/store1/\0",
                "/application_prod/store1/\0", "/application_dev/store1/\0", "/application/store1/\0" };
        assertThat(sources.size()).isEqualTo(6);
        assertThat(sources.stream().map(s -> s.getName()).toArray()).containsExactly((Object[]) expectedSourceNames);
    }

    @Test
    public void compositeSourceIsCreatedForPrefixedConfig() {
        String[] labels = new String[1];
        labels[0] = "\0";
        when(configStoreMock.getLabels()).thenReturn(labels);
        when(configStoreMock.getPrefix()).thenReturn(PREFIX);
        when(properties.getDefaultContext()).thenReturn("application");
        locator = new AppConfigurationPropertySourceLocator(properties, appProperties, clientStoreMock,
                tokenCredentialProvider, null);

        PropertySource<?> source = locator.locate(environment);

        assertThat(source).isInstanceOf(CompositePropertySource.class);

        Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
        // Application name: foo, active profile: dev,prod and prefix: /config,
        // should construct below composite Property Source:
        // [/config/foo_prod/, /config/foo_dev/, /config/foo/, /config/application_prod/,
        // /config/application_dev/, /config/application/]
        String[] expectedSourceNames = new String[] { "/config/foo_prod/store1/\0", "/config/foo_dev/store1/\0",
                "/config/foo/store1/\0", "/config/application_prod/store1/\0", "/config/application_dev/store1/\0",
                "/config/application/store1/\0" };
        assertThat(sources.size()).isEqualTo(6);
        assertThat(sources.stream().map(s -> s.getName()).toArray()).containsExactly((Object[]) expectedSourceNames);
    }

    @Test
    public void nullApplicationNameCreateDefaultContextOnly() {
        String[] labels = new String[1];
        labels[0] = "\0";
        when(configStoreMock.getLabels()).thenReturn(labels);
        when(environment.getActiveProfiles()).thenReturn(new String[] {});
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
        String[] expectedSourceNames = new String[] { "/application/store1/\0" };
        assertThat(sources.size()).isEqualTo(1);
        assertThat(sources.stream().map(s -> s.getName()).toArray()).containsExactly((Object[]) expectedSourceNames);
    }

    @Test
    public void emptyApplicationNameCreateDefaultContextOnly() {
        String[] labels = new String[1];
        labels[0] = "\0";
        when(configStoreMock.getLabels()).thenReturn(labels);
        when(environment.getActiveProfiles()).thenReturn(new String[] {});
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
        String[] expectedSourceNames = new String[] { "/application/store1/\0" };
        assertThat(sources.size()).isEqualTo(1);
        assertThat(sources.stream().map(s -> s.getName()).toArray()).containsExactly((Object[]) expectedSourceNames);
    }

    @Test
    public void defaultFailFastThrowException() throws IOException {
        expected.expect(NullPointerException.class);

        when(configStoreMock.isFailFast()).thenReturn(true);
        when(properties.getDefaultContext()).thenReturn("application");

        locator = new AppConfigurationPropertySourceLocator(properties, appProperties,
                clientStoreMock, tokenCredentialProvider, null);

        when(clientStoreMock.listSettings(Mockito.any(), Mockito.anyString())).thenThrow(new RuntimeException());
        locator.locate(environment);
        verify(configStoreMock, times(1)).isFailFast();
    }

    @Test
    public void refreshThrowException() throws IOException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        Field field = AppConfigurationPropertySourceLocator.class.getDeclaredField("startup");
        field.setAccessible(true);
        field.set(null, new AtomicBoolean(false));
        StateHolder.setLoadState(TEST_STORE_NAME, true);

        expected.expect(NullPointerException.class);

        when(environment.getActiveProfiles()).thenReturn(new String[] {});
        when(environment.getProperty("spring.application.name")).thenReturn(null);

        locator = new AppConfigurationPropertySourceLocator(properties, appProperties,
                clientStoreMock, tokenCredentialProvider, null);

        locator.locate(environment);
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
        when(configStoreMock.getLabels()).thenReturn(labels);
        when(environment.getActiveProfiles()).thenReturn(new String[] {});
        when(configStoreMock.getEndpoint()).thenReturn(TEST_STORE_NAME);

        properties = new AppConfigurationProperties();
        TestUtils.addStore(properties, TEST_STORE_NAME_1, TEST_CONN_STRING);
        TestUtils.addStore(properties, TEST_STORE_NAME_2, TEST_CONN_STRING_2);

        locator = new AppConfigurationPropertySourceLocator(properties, appProperties,
                clientStoreMock, tokenCredentialProvider, null);

        PropertySource<?> source = locator.locate(environment);
        assertThat(source).isInstanceOf(CompositePropertySource.class);

        Collection<PropertySource<?>> sources = ((CompositePropertySource) source).getPropertySources();
        String[] expectedSourceNames = new String[] { "/application/" + TEST_STORE_NAME_2 + "/\0",
                "/application/" + TEST_STORE_NAME_1 + "/\0" };
        assertThat(sources.size()).isEqualTo(2);
        assertThat(sources.stream().map(s -> s.getName()).toArray()).containsExactly((Object[]) expectedSourceNames);
    }

    @Test
    public void awaitOnError() throws Exception {
        List<ConfigStore> configStores = new ArrayList<ConfigStore>();
        configStores.add(configStore);
        AppConfigurationProperties properties = new AppConfigurationProperties();
        properties.setProfileSeparator("_");
        properties.setName("TestStoreName");
        properties.setStores(configStores);

        appPropertiesMock.setPrekillTime(5);

        Environment env = Mockito.mock(ConfigurableEnvironment.class);
        String[] array = {};
        when(env.getActiveProfiles()).thenReturn(array);
        String[] labels = { "" };
        when(configStore.getLabels()).thenReturn(labels);
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
