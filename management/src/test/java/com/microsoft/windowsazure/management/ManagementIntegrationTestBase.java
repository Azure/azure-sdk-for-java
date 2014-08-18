/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.management;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.junit.Rule;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.io.ByteStreams;
import com.microsoft.windowsazure.core.pipeline.apache.ApacheConfigurationProperties;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseFilter;
import com.microsoft.windowsazure.core.utils.KeyStoreType;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.core.Builder;
import com.microsoft.windowsazure.core.Builder.Alteration;
import com.microsoft.windowsazure.core.Builder.Registry;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.LoggingFilter;

public abstract class ManagementIntegrationTestBase {

    protected static ManagementClient managementClient;
    protected static String recordFolder = "__files/";

    protected static String locationListRequestId = "4297edd3ce9dca4fb1ccab3760162d2e";
    protected static String subscriptionId = System.getenv(ManagementConfiguration.SUBSCRIPTION_ID);
    protected static Boolean mocked = new Boolean(System.getenv(ManagementConfiguration.MOCKED));
    protected static Boolean recording = new Boolean(System.getenv(ManagementConfiguration.RECORDING));

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8043));

    protected static void createService() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();
        config.setProperty(ApacheConfigurationProperties.PROPERTY_RETRY_HANDLER, new DefaultHttpRequestRetryHandler());

        // add LoggingFilter to any pipeline that is created
        Registry builder = (Registry) config.getBuilder();
        builder.alter(ManagementClient.class, Client.class, new Alteration<Client>() {
            @Override
            public Client alter(String profile, Client client, Builder builder, Map<String, Object> properties) {
                client.addFilter(new LoggingFilter());
                return client;
            }
        });

        managementClient = ManagementService.create(config);
        
        if (recording) {
            ServiceResponseFilter filter = new ServiceResponseFilter() {
                @Override
                public void filter(ServiceRequestContext request, ServiceResponseContext response) {
                    try {
                        URL url = this.getClass().getClassLoader().getResource(recordFolder);
                        File tape = new File(url.getPath() + getClass().getName() + ".xml");
                        Field f = response.getClass().getDeclaredField("clientResponse");
                        f.setAccessible(true);
                        HttpResponse httpResponse = (HttpResponse) f.get(response);
                        JAXBContext jaxbContext = JAXBContext.newInstance(httpResponse.getAllHeaders().getClass());
                        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                        jaxbMarshaller.marshal(httpResponse.getAllHeaders(), tape);
                        jaxbMarshaller.marshal(httpResponse.getAllHeaders(), System.out);
                        /*InputStream is = new BufferedInputStream(response.getEntityInputStream());
                        is.mark(Integer.MAX_VALUE);
                        URL url = this.getClass().getClassLoader().getResource(recordFolder);
                        File tape = new File(url.getPath() + getClass().getName() + ".xml");
                        tape.createNewFile();
                        ByteStreams.copy(is, new FileOutputStream(tape));
                        is.reset();
                        response.setEntityInputStream(is);*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            managementClient.withResponseFilterLast(filter);
        }
    }

    protected static Configuration createConfiguration() throws Exception {
        Boolean mocked = new Boolean(System.getenv(ManagementConfiguration.MOCKED));
        if (!mocked) {
            String baseUri = System.getenv(ManagementConfiguration.URI);
            return ManagementConfiguration.configure(
                baseUri != null ? new URI(baseUri) : null,
                System.getenv(ManagementConfiguration.SUBSCRIPTION_ID),
                System.getenv(ManagementConfiguration.KEYSTORE_PATH),
                System.getenv(ManagementConfiguration.KEYSTORE_PASSWORD),
                KeyStoreType.fromString(System.getenv(ManagementConfiguration.KEYSTORE_TYPE))
            );
        } else {
            String baseUri = "http://localhost:8043/";
            return ManagementConfiguration.configure(
                baseUri != null ? new URI(baseUri) : null,
                System.getenv(ManagementConfiguration.SUBSCRIPTION_ID),
                System.getenv(ManagementConfiguration.KEYSTORE_PATH),
                System.getenv(ManagementConfiguration.KEYSTORE_PASSWORD),
                KeyStoreType.fromString(System.getenv(ManagementConfiguration.KEYSTORE_TYPE))
            );
        }
    }
}