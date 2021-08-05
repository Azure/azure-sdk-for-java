// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloudfoundry.environment;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ContextConfiguration(classes = {VcapProcessor.class})
public class AzureCloudFoundryServiceApplicationTest {

    private static final Logger LOG = LoggerFactory.getLogger(AzureCloudFoundryServiceApplicationTest.class);

    @Autowired
    private VcapProcessor parser;

    @Test
    public void testVcapSingleService() throws IOException {
        final Resource resource = new ClassPathResource("cloudfoundry/vcap1.json");
        final String content;
        try {
            content = new String(Files.readAllBytes(Paths.get(resource.getURI())));
            final VcapPojo[] pojos = parser.parseVcapService(content).toArray(new VcapPojo[0]);
            assertNotNull(pojos);
            assertEquals(1, pojos.length);
            final VcapPojo pojo = pojos[0];

            LOG.debug("pojo = " + pojo);
            final VcapServiceConfig config = pojo.getServiceConfig();
            assertEquals(3, config.getCredentials().size());
            assertEquals(2, config.getTags().length);
            assertEquals(0, config.getVolumeMounts().length);
            assertEquals("azure-storage", config.getLabel());
            assertEquals("provider", config.getProvider());
            assertEquals("azure-storage", pojo.getServiceBrokerName());
            assertEquals("azure-storage-service", config.getName());
            assertEquals("standard", config.getPlan());
            assertNull(config.getSyslogDrainUrl());
            assertEquals("Azure", config.getTags()[0]);
            assertEquals("Storage", config.getTags()[1]);

            assertEquals("pak", config.getCredentials().get("primary_access_key"));
            assertEquals("sak", config.getCredentials().get("secondary_access_key"));
            assertEquals("sam", config.getCredentials().get("storage_account_name"));
        } catch (IOException e) {
            LOG.error("Error reading json file", e);
            throw e;
        }
    }

    @Test
    public void testVcapSingleServiceWithNulls() throws IOException {
        final Resource resource = new ClassPathResource("cloudfoundry/vcap2.json");
        final String content;
        try {
            content = new String(
                Files.readAllBytes(Paths.get(resource.getURI())));
            final VcapPojo[] pojos = parser.parseVcapService(content).toArray(new VcapPojo[0]);
            assertNotNull(pojos);
            assertEquals(1, pojos.length);
            final VcapPojo pojo = pojos[0];

            LOG.debug("pojo = " + pojo);
            final VcapServiceConfig config = pojo.getServiceConfig();
            assertEquals(4, config.getCredentials().size());
            assertEquals(0, config.getTags().length);
            assertEquals(0, config.getVolumeMounts().length);
            assertEquals("azure-documentdb", config.getLabel());
            assertNull(config.getProvider());
            assertEquals("azure-documentdb", pojo.getServiceBrokerName());
            assertEquals("mydocumentdb", config.getName());
            assertEquals("standard", config.getPlan());
            assertNull(config.getSyslogDrainUrl());

            assertEquals("docdb123mj", config.getCredentials().get("documentdb_database_id"));
            assertEquals("dbs/ZFxCAA==/", config.getCredentials().get("documentdb_database_link"));
            assertEquals("https://hostname:443/", config.getCredentials().get("documentdb_host_endpoint"));
            assertEquals(
                "3becR7JFnWamMvGwWYWWTV4WpeNhN8tOzJ74yjAxPKDpx65q2lYz60jt8WXU6HrIKrAIwhs0Hglf0123456789==",
                config.getCredentials().get("documentdb_master_key"));
        } catch (IOException e) {
            LOG.error("Error reading json file", e);
            throw e;
        }
    }

    @Test
    public void testVcapUserProvidedService() throws IOException {
        final Resource resource = new ClassPathResource("cloudfoundry/vcap3.json");
        final String content;
        try {
            content = new String(
                Files.readAllBytes(Paths.get(resource.getURI())));
            final VcapPojo[] pojos = parser.parseVcapService(content).toArray(new VcapPojo[0]);
            assertNotNull(pojos);
            assertEquals(1, pojos.length);
            final VcapPojo pojo = pojos[0];

            LOG.debug("pojo = " + pojo);
            final VcapServiceConfig config = pojo.getServiceConfig();
            assertEquals(4, config.getCredentials().size());
            assertEquals(0, config.getTags().length);
            assertEquals(0, config.getVolumeMounts().length);
            assertEquals("user-provided", config.getLabel());
            assertNull(config.getProvider());
            assertEquals("azure-documentdb", pojo.getServiceBrokerName());
            assertEquals("mydocumentdb", config.getName());
            assertEquals("standard", config.getPlan());
            assertNull(config.getSyslogDrainUrl());

            assertEquals("docdb123mj", config.getCredentials().get("documentdb_database_id"));
            assertEquals("dbs/ZFxCAA==/", config.getCredentials().get("documentdb_database_link"));
            assertEquals("https://hostname:443/", config.getCredentials().get("documentdb_host_endpoint"));
            assertEquals(
                "3becR7JFnWamMvGwWYWWTV4WpeNhN8tOzJ74yjAxPKDpx65q2lYz60jt8WXU6HrIKrAIwhs0Hglf0123456789==",
                config.getCredentials().get("documentdb_master_key"));
        } catch (IOException e) {
            LOG.error("Error reading json file", e);
            throw e;
        }
    }

}
