// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.domain.Address;
import com.azure.spring.data.cosmos.repository.TestRepositoryConfig;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Duration;
import java.time.OffsetDateTime;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class ContainerLockIT {

    private static final Duration SHORT_LEASE_DURATION = Duration.ofSeconds(3);

    @Autowired
    private CosmosTemplate template;
    @Autowired
    private CosmosTemplate reactiveTemplate;
    private static CosmosTemplate staticTemplate;

    private ContainerLock lock;
    private ContainerLock otherLock;

    @Before
    public void setup() {
        staticTemplate = template;
        CosmosEntityInformation entityInfo = new CosmosEntityInformation(Address.class);
        template.createContainerIfNotExists(entityInfo);
        AbstractIntegrationTestCollectionManager.registerContainerForCleanup(template, entityInfo.getContainerName());
        lock = new ContainerLock(template, entityInfo.getContainerName(), SHORT_LEASE_DURATION);
        otherLock = new ContainerLock(reactiveTemplate, entityInfo.getContainerName(), SHORT_LEASE_DURATION);
    }

    @After
    public void cleanup() {
        releaseLockIgnoreException(lock);
        releaseLockIgnoreException(otherLock);
    }

    private void releaseLockIgnoreException(ContainerLock lock) {
        try {
            lock.release();
        } catch (Exception ex) {
            // ignore
        }
    }

    @Test
    public void shouldAcquireAndReleaseLock() {
        lock.acquire(Duration.ofSeconds(1));
        lock.release();
    }

    @Test
    public void acquireShouldAcquireLockIfLeaseExpires() {
        OffsetDateTime start = OffsetDateTime.now();
        lock.acquire(Duration.ofSeconds(1));

        otherLock.acquire(SHORT_LEASE_DURATION.plusSeconds(1));
        assertTrue(OffsetDateTime.now().isAfter(start.plus(SHORT_LEASE_DURATION)));
    }

    @Test
    public void acquireShouldThrowExceptionIfWaitForIsLessThanLeaseDuration() {
        lock.acquire(Duration.ofSeconds(1));

        try {
            otherLock.acquire(SHORT_LEASE_DURATION.minusSeconds(1));
            Assert.fail();
        } catch (ContainerLock.LockAcquisitionFailedException ex) {
        }
    }

    @Test
    public void renewShouldRenewTheLeaseDuration() {
        lock.acquire(Duration.ofSeconds(1));
        OffsetDateTime originalExpiration = lock.getLeaseExpiration();

        lock.renew();
        assertTrue(lock.getLeaseExpiration().isAfter(originalExpiration));
    }

}
