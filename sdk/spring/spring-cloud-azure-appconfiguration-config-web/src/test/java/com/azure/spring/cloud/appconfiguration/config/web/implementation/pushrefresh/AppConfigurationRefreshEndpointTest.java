// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.web.implementation.pushrefresh;

import static com.azure.spring.cloud.appconfiguration.config.web.implementation.TestConstants.TOPIC;
import static com.azure.spring.cloud.appconfiguration.config.web.implementation.TestConstants.TRIGGER_KEY;
import static com.azure.spring.cloud.appconfiguration.config.web.implementation.TestConstants.TRIGGER_LABEL;
import static com.azure.spring.cloud.appconfiguration.config.web.implementation.TestConstants.VALIDATION_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;

import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring.AccessToken;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring.PushNotification;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreTrigger;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;

public class AppConfigurationRefreshEndpointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private BufferedReader reader;

    @Mock
    private Stream<String> lines;

    @Mock
    private ContextRefresher contextRefresher;

    @Mock
    private ApplicationEventPublisher publisher;

    private ArrayList<ConfigStore> configStores;

    private ArrayList<AppConfigurationStoreTrigger> triggers;

    private AppConfigurationStoreMonitoring monitoring;
    
    private String endpoint = "https://fake.test.azconfig.io";

    private String tokenName = "token";

    private String tokenSecret = "secret";

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);

        monitoring = new AppConfigurationStoreMonitoring();
        AppConfigurationStoreTrigger trigger = new AppConfigurationStoreTrigger();
        trigger.setKey(TRIGGER_KEY);
        trigger.setLabel(TRIGGER_LABEL);
        triggers = new ArrayList<AppConfigurationStoreTrigger>();
        triggers.add(trigger);
        monitoring.setTriggers(triggers);
        PushNotification pushNotification = monitoring.getPushNotification();
        AccessToken primaryToken = pushNotification.getPrimaryToken();
        primaryToken.setName(tokenName);
        primaryToken.setSecret(tokenSecret);
        pushNotification.setPrimaryToken(primaryToken);
        monitoring.setPushNotification(pushNotification);
        monitoring.setEnabled(true);
        ConfigStore configStore = new ConfigStore();
        configStores = new ArrayList<>();
        configStore.setMonitoring(monitoring);
        configStore.setEndpoint("https://fake.test.azconfig.io");
        configStores.add(configStore);

        when(request.getReader()).thenReturn(reader);
        when(reader.lines()).thenReturn(lines);
    }
    
    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void webHookValidation() throws IOException {
        Map<String, String> allRequestParams = new HashMap<String, String>();
        AppConfigurationProperties properties = new AppConfigurationProperties();

        properties.setStores(configStores);
        allRequestParams.put(tokenName, tokenSecret);

        AppConfigurationRefreshEndpoint endpoint = new AppConfigurationRefreshEndpoint(contextRefresher, properties);

        when(lines.collect(Mockito.any())).thenReturn("[{\r\n"
            + "  \"id\": \"2d1781af-3a4c-4d7c-bd0c-e34b19da4e66\",\r\n"
            + "  \"topic\":" + TOPIC + ",\r\n"
            + "  \"subject\": \"https://fake.test.azconfig.io/kv/Foo?label=FizzBuzz\",\r\n"
            + "  \"data\": {\r\n"
            + "    \"validationCode\": \"512d38b6-c7b8-40c8-89fe-f46f9e9622b6\",\r\n"
            + "    \"validationUrl\":" + VALIDATION_URL + ",\r\n"
            + "    \"syncToken\": \"zAJw6V16=MzoyMCMyODA3Mzc3;sn=2807377\"\r\n"
            + "  },\r\n"
            + "  \"eventType\": \"Microsoft.EventGrid.SubscriptionValidationEvent\",\r\n"
            + "  \"eventTime\": \"2018-01-25T22:12:19.4556811Z\",\r\n"
            + "  \"metadataVersion\": \"1\",\r\n"
            + "  \"dataVersion\": \"1\"\r\n"
            + "}]");

        assertEquals("{ \"validationResponse\": \"512d38b6-c7b8-40c8-89fe-f46f9e9622b6\"}",
            endpoint.refresh(request, response, allRequestParams));
    }

    @Test
    public void webHookRefresh() throws IOException {
        Map<String, String> allRequestParams = new HashMap<String, String>();
        AppConfigurationProperties properties = new AppConfigurationProperties();

        properties.setStores(configStores);
        allRequestParams.put(tokenName, tokenSecret);

        AppConfigurationRefreshEndpoint endpoint = new AppConfigurationRefreshEndpoint(contextRefresher, properties);
        endpoint.setApplicationEventPublisher(publisher);

        when(lines.collect(Mockito.any())).thenReturn(getResetNotification());

        assertEquals(HttpStatus.OK.getReasonPhrase(), endpoint.refresh(request, response, allRequestParams));
        verify(publisher, times(1)).publishEvent(argThat(refreshEvent -> {
            boolean hasEndpoint = ((AppConfigurationRefreshEvent) refreshEvent).getEndpoint().equals("https://fake.test.azconfig.io");
            boolean hasSyncToken = ((AppConfigurationRefreshEvent) refreshEvent).getSyncToken().equals("zAJw6V16=MzoyMCMyODA3Mzc3;sn=2807377");
            return hasEndpoint && hasSyncToken;
        }));
    }

    @Test
    public void webHookRefreshNotFound() throws IOException {
        Map<String, String> allRequestParams = new HashMap<String, String>();
        AppConfigurationProperties properties = new AppConfigurationProperties();

        properties.setStores(configStores);
        allRequestParams.put(tokenName, tokenSecret);

        AppConfigurationRefreshEndpoint endpoint = new AppConfigurationRefreshEndpoint(null, properties);
        endpoint.setApplicationEventPublisher(publisher);

        when(lines.collect(Mockito.any())).thenReturn(getResetNotification());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            endpoint.refresh(request, response, allRequestParams));
    }

    @Test
    public void noTokenName() throws IOException {
        Map<String, String> allRequestParams = new HashMap<String, String>();
        AppConfigurationProperties properties = new AppConfigurationProperties();

        String tokenName = "token";
        String tokenSecret = "secret";
        PushNotification pushNotification = monitoring.getPushNotification();
        AccessToken primaryToken = pushNotification.getPrimaryToken();
        primaryToken.setSecret(tokenSecret);
        pushNotification.setPrimaryToken(primaryToken);
        monitoring.setPushNotification(pushNotification);
        ConfigStore configStore = new ConfigStore();
        ArrayList<ConfigStore> configStores = new ArrayList<>();
        configStore.setMonitoring(monitoring);
        properties.setStores(configStores);
        allRequestParams.put(tokenName, tokenSecret);

        AppConfigurationRefreshEndpoint endpoint = new AppConfigurationRefreshEndpoint(contextRefresher, properties);

        when(lines.collect(Mockito.any())).thenReturn(getResetNotification());

        assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), endpoint.refresh(request, response, allRequestParams));
    }

    @Test
    public void noTokenSecret() throws IOException {
        Map<String, String> allRequestParams = new HashMap<String, String>();
        AppConfigurationProperties properties = new AppConfigurationProperties();

        String tokenName = "token";
        String tokenSecret = "secret";
        PushNotification pushNotification = monitoring.getPushNotification();
        AccessToken primaryToken = pushNotification.getPrimaryToken();
        primaryToken.setName(tokenName);
        pushNotification.setPrimaryToken(primaryToken);
        monitoring.setPushNotification(pushNotification);
        ConfigStore configStore = new ConfigStore();
        ArrayList<ConfigStore> configStores = new ArrayList<>();
        configStore.setMonitoring(monitoring);
        properties.setStores(configStores);
        allRequestParams.put(tokenName, tokenSecret);

        AppConfigurationRefreshEndpoint endpoint = new AppConfigurationRefreshEndpoint(contextRefresher, properties);

        when(lines.collect(Mockito.any())).thenReturn(getResetNotification());

        assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), endpoint.refresh(request, response, allRequestParams));
    }

    @Test
    public void noPramToken() throws IOException {
        Map<String, String> allRequestParams = new HashMap<String, String>();
        AppConfigurationProperties properties = new AppConfigurationProperties();

        String tokenName = "token";
        String tokenSecret = "secret";
        PushNotification pushNotification = monitoring.getPushNotification();
        AccessToken primaryToken = pushNotification.getPrimaryToken();
        primaryToken.setName(tokenName);
        primaryToken.setSecret(tokenSecret);
        pushNotification.setPrimaryToken(primaryToken);
        monitoring.setPushNotification(pushNotification);
        ConfigStore configStore = new ConfigStore();
        ArrayList<ConfigStore> configStores = new ArrayList<>();
        configStore.setMonitoring(monitoring);
        properties.setStores(configStores);

        AppConfigurationRefreshEndpoint endpoint = new AppConfigurationRefreshEndpoint(contextRefresher, properties);

        when(lines.collect(Mockito.any())).thenReturn(getResetNotification());

        assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), endpoint.refresh(request, response, allRequestParams));
    }

    @Test
    public void invalidParamToken() throws IOException {
        Map<String, String> allRequestParams = new HashMap<String, String>();
        AppConfigurationProperties properties = new AppConfigurationProperties();

        String tokenName = "token";
        String tokenSecret = "secret";
        PushNotification pushNotification = monitoring.getPushNotification();
        AccessToken primaryToken = pushNotification.getPrimaryToken();
        primaryToken.setName(tokenName);
        primaryToken.setSecret(tokenSecret);
        pushNotification.setPrimaryToken(primaryToken);
        monitoring.setPushNotification(pushNotification);
        ConfigStore configStore = new ConfigStore();
        ArrayList<ConfigStore> configStores = new ArrayList<>();
        configStore.setMonitoring(monitoring);
        properties.setStores(configStores);
        allRequestParams.put(tokenName, "noSecret");

        AppConfigurationRefreshEndpoint endpoint = new AppConfigurationRefreshEndpoint(contextRefresher, properties);

        when(lines.collect(Mockito.any())).thenReturn(getResetNotification());

        assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), endpoint.refresh(request, response, allRequestParams));
    }

    private String getResetNotification() {
        return " [ {\r\n"
            + "  \"id\" : \"e2f7023c-b982-4050-80d9-8ed6bf24e183\",\r\n"
            + "  \"topic\":" + TOPIC + ",\r\n"
            + "  \"subject\" : \"https://fake.test.azconfig.io/kv/%2Fapplication%2Fconfig.message?api-version=1.0\",\r\n"
            + "  \"data\" : {\r\n"
            + "    \"key\" : \"trigger_key\",\r\n"
            + "    \"label\" : \"trigger_label\",\r\n"
            + "    \"syncToken\" : \"zAJw6V16=MzoyMCMyODA3Mzc3;sn=2807377\"\r\n"
            + "  },\r\n"
            + "  \"eventType\" : \"Microsoft.AppConfiguration.KeyValueModified\",\r\n"
            + "  \"dataVersion\" : \"1\",\r\n"
            + "  \"metadataVersion\" : \"1\",\r\n"
            + "  \"eventTime\" : \"2020-06-03T21:19:04.019421Z\"\r\n"
            + "} ]";
    }

}
