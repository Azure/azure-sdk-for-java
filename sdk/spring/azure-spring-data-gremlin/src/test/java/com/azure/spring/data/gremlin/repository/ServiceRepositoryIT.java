// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.repository;

import com.azure.spring.data.gremlin.common.TestRepositoryConfiguration;
import com.azure.spring.data.gremlin.common.domain.Service;
import com.azure.spring.data.gremlin.common.domain.ServiceType;
import com.azure.spring.data.gremlin.common.domain.SimpleDependency;
import com.azure.spring.data.gremlin.common.repository.ServiceRepository;
import com.azure.spring.data.gremlin.common.repository.SimpleDependencyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestRepositoryConfiguration.class)
public class ServiceRepositoryIT {

    private static Service serviceA;
    private static Service serviceB;
    private static Service serviceC;

    private static Date createDateA;
    private static Date createDateB;
    private static Date createDateC;

    private static final Map<String, Object> PROPERTIES_A = new HashMap<>();
    private static final Map<String, Object> PROPERTIES_B = new HashMap<>();
    private static final Map<String, Object> PROPERTIES_C = new HashMap<>();

    private static final String ID_A = "1234";
    private static final String ID_B = "8731";
    private static final String ID_C = "5781";

    private static final int COUNT_A = 2;
    private static final int COUNT_B = 8;
    private static final int COUNT_C = 2;

    private static final String NAME_A = "name-A";
    private static final String NAME_B = "name-B";
    private static final String NAME_C = "name-A";

    @Autowired
    private ServiceRepository repository;

    @Autowired
    private SimpleDependencyRepository dependencyRepo;

    @BeforeAll
    public static void initialize() throws ParseException {
        PROPERTIES_B.put("serviceB-port", 8761);
        PROPERTIES_B.put("priority", "high");
        PROPERTIES_B.put("enabled-hystrix", false);

        PROPERTIES_A.put("serviceA-port", 8888);
        PROPERTIES_A.put("serviceB-port", 8761);
        PROPERTIES_A.put("priority", "highest");

        PROPERTIES_C.put("serviceC-port", 8090);
        PROPERTIES_C.put("serviceB-port", 8761);
        PROPERTIES_C.put("priority", "medium");

        createDateA = new SimpleDateFormat("yyyyMMdd").parse("20180601");
        createDateB = new SimpleDateFormat("yyyyMMdd").parse("20180603");
        createDateC = new SimpleDateFormat("yyyyMMdd").parse("20180503");

        serviceA = new Service(ID_A, COUNT_A, true, NAME_A, ServiceType.FRONT_END, createDateA, PROPERTIES_A);
        serviceB = new Service(ID_B, COUNT_B, false, NAME_B, ServiceType.BACK_END, createDateB, PROPERTIES_B);
        serviceC = new Service(ID_C, COUNT_C, false, NAME_C, ServiceType.BACK_END, createDateC, PROPERTIES_C);
    }

    @BeforeEach
    public void setup() {
        this.repository.deleteAll();
    }

    @AfterEach
    public void cleanup() {
        this.repository.deleteAll();
    }

    @Test
    public void testQueries() {
        Assertions.assertFalse(this.repository.findById(serviceA.getId()).isPresent());
        Assertions.assertFalse(this.repository.findById(serviceB.getId()).isPresent());

        this.repository.save(serviceA);
        this.repository.save(serviceB);

        Optional<Service> foundOptional = this.repository.findById(serviceA.getId());
        Assertions.assertTrue(foundOptional.isPresent());
        Assertions.assertEquals(foundOptional.get(), serviceA);

        foundOptional = this.repository.findById(serviceB.getId());
        Assertions.assertTrue(foundOptional.isPresent());
        Assertions.assertEquals(foundOptional.get(), serviceB);

        this.repository.deleteById(serviceA.getId());
        this.repository.deleteById(serviceB.getId());

        Assertions.assertFalse(this.repository.findById(serviceA.getId()).isPresent());
        Assertions.assertFalse(this.repository.findById(serviceB.getId()).isPresent());
    }

    @Test
    public void testEdgeFromToStringId() {
        final SimpleDependency depend = new SimpleDependency("fakeId", "faked", serviceA.getId(), serviceB.getId());

        this.repository.save(serviceA);
        this.repository.save(serviceB);
        this.dependencyRepo.save(depend);

        final Optional<SimpleDependency> foundOptional = this.dependencyRepo.findById(depend.getId());
        Assertions.assertTrue(foundOptional.isPresent());
        Assertions.assertEquals(foundOptional.get(), depend);

        this.dependencyRepo.delete(foundOptional.get());

        Assertions.assertTrue(this.repository.findById(serviceA.getId()).isPresent());
        Assertions.assertTrue(this.repository.findById(serviceB.getId()).isPresent());
    }

    @Test
    public void testFindByName() {
        this.repository.save(serviceA);
        this.repository.save(serviceB);

        final List<Service> services = this.repository.findByName(serviceA.getName());

        Assertions.assertEquals(services.size(), 1);
        Assertions.assertEquals(services.get(0), serviceA);

        this.repository.deleteAll();

        Assertions.assertTrue(this.repository.findByName(serviceA.getName()).isEmpty());
    }

    @Test
    public void testFindByInstanceCount() {
        this.repository.save(serviceA);
        this.repository.save(serviceB);

        final List<Service> services = this.repository.findByInstanceCount(serviceB.getInstanceCount());

        Assertions.assertEquals(services.size(), 1);
        Assertions.assertEquals(services.get(0), serviceB);

        this.repository.deleteAll();

        Assertions.assertTrue(this.repository.findByInstanceCount(serviceB.getInstanceCount()).isEmpty());
    }

    @Test
    public void testFindByIsActive() {
        this.repository.save(serviceA);
        this.repository.save(serviceB);

        final List<Service> services = this.repository.findByActive(serviceB.isActive());

        Assertions.assertEquals(services.size(), 1);
        Assertions.assertEquals(services.get(0), serviceB);

        this.repository.deleteAll();

        Assertions.assertTrue(this.repository.findByActive(serviceB.isActive()).isEmpty());
    }

    @Test
    public void testFindByCreateAt() {
        this.repository.save(serviceA);
        this.repository.save(serviceB);

        final List<Service> services = this.repository.findByCreateAt(serviceA.getCreateAt());

        Assertions.assertEquals(services.size(), 1);
        Assertions.assertEquals(services.get(0), serviceA);

        this.repository.deleteAll();

        Assertions.assertTrue(this.repository.findByCreateAt(serviceB.getCreateAt()).isEmpty());
    }

    @Test
    public void testFindByProperties() {
        this.repository.save(serviceA);
        this.repository.save(serviceB);

        final List<Service> services = this.repository.findByProperties(serviceB.getProperties());

        Assertions.assertEquals(services.size(), 1);
        Assertions.assertEquals(services.get(0), serviceB);

        this.repository.deleteAll();

        Assertions.assertTrue(this.repository.findByProperties(serviceB.getProperties()).isEmpty());
    }

    @Test
    public void testFindById() {
        this.repository.save(serviceA);
        this.repository.save(serviceB);

        final Optional<Service> foundConfig = this.repository.findById(serviceA.getId());
        final Optional<Service> foundEureka = this.repository.findById(serviceB.getId());

        Assertions.assertTrue(foundConfig.isPresent());
        Assertions.assertTrue(foundEureka.isPresent());

        Assertions.assertEquals(foundConfig.get(), serviceA);
        Assertions.assertEquals(foundEureka.get(), serviceB);
    }

    @Test
    public void testFindByNameAndInstanceCount() {
        this.repository.save(serviceA);
        this.repository.save(serviceB);

        final List<Service> services = repository.findByNameAndInstanceCount(NAME_B, COUNT_B);

        Assertions.assertEquals(services.size(), 1);
        Assertions.assertEquals(services.get(0), serviceB);
        Assertions.assertTrue(repository.findByNameAndInstanceCount(NAME_B, COUNT_A).isEmpty());
    }

    @Test
    public void testFindByNameAndInstanceCountAndType() {
        this.repository.save(serviceA);
        this.repository.save(serviceB);

        final List<Service> services = repository.findByNameAndInstanceCountAndType(NAME_B, COUNT_B, ServiceType.BACK_END);

        Assertions.assertEquals(services.size(), 1);
        Assertions.assertEquals(services.get(0), serviceB);
        Assertions.assertTrue(repository.findByNameAndInstanceCountAndType(NAME_B, COUNT_A, ServiceType.BOTH).isEmpty());
    }

    @Test
    public void testFindByNameOrInstanceCount() {
        final List<Service> services = Arrays.asList(serviceA, serviceB);
        this.repository.saveAll(services);

        List<Service> foundServices = repository.findByNameOrInstanceCount(NAME_A, COUNT_B);

        services.sort(Comparator.comparing(Service::getId));
        foundServices.sort(Comparator.comparing(Service::getId));

        Assertions.assertEquals(foundServices.size(), 2);
        Assertions.assertEquals(foundServices, services);

        foundServices = repository.findByNameOrInstanceCount("fake-name", COUNT_A);

        Assertions.assertEquals(foundServices.size(), 1);
        Assertions.assertEquals(foundServices.get(0), serviceA);
    }

    @Test
    public void testFindByNameAndIsActiveOrProperties() {
        final List<Service> services = Arrays.asList(serviceA, serviceB);
        this.repository.saveAll(services);

        List<Service> foundServices = repository.findByNameAndActiveOrProperties(NAME_A, true, PROPERTIES_B);

        services.sort(Comparator.comparing(Service::getId));
        foundServices.sort(Comparator.comparing(Service::getId));

        Assertions.assertEquals(foundServices.size(), 2);
        Assertions.assertEquals(foundServices, services);

        foundServices = repository.findByNameAndActiveOrProperties(NAME_B, false, new HashMap<>());
        Assertions.assertEquals(foundServices.size(), 1);
        Assertions.assertEquals(foundServices.get(0), serviceB);
    }

    @Test
    public void testFindByNameOrInstanceCountAndType() {
        final List<Service> services = Arrays.asList(serviceA, serviceB);
        this.repository.saveAll(services);

        List<Service> foundServices = repository.findByNameOrInstanceCountAndType(NAME_A, COUNT_B, ServiceType.BACK_END);

        services.sort(Comparator.comparing(Service::getId));
        foundServices.sort(Comparator.comparing(Service::getId));

        Assertions.assertEquals(foundServices.size(), 2);
        Assertions.assertEquals(foundServices, services);

        foundServices = repository.findByNameOrInstanceCountAndType(NAME_B, COUNT_A, ServiceType.BACK_END);
        Assertions.assertEquals(foundServices.size(), 1);
        Assertions.assertEquals(foundServices.get(0), serviceB);
    }

    @Test
    public void testFindByNameAndInstanceCountOrType() {
        final List<Service> services = Arrays.asList(serviceA, serviceB);
        this.repository.saveAll(services);

        List<Service> foundServices = repository.findByNameAndInstanceCountOrType(NAME_A, COUNT_A, ServiceType.BACK_END);

        services.sort(Comparator.comparing(Service::getId));
        foundServices.sort(Comparator.comparing(Service::getId));

        Assertions.assertEquals(foundServices.size(), 2);
        Assertions.assertEquals(foundServices, services);

        foundServices = repository.findByNameAndInstanceCountOrType(NAME_A, COUNT_B, ServiceType.BACK_END);
        Assertions.assertEquals(foundServices.size(), 1);
        Assertions.assertEquals(foundServices.get(0), serviceB);
    }

    @Test
    public void testExistsByName() {
        final List<Service> services = Arrays.asList(serviceA, serviceB, serviceC);
        this.repository.saveAll(services);

        final List<Service> foundServices = repository.findByActiveExists();

        Assertions.assertEquals(foundServices.size(), 1);
        Assertions.assertEquals(foundServices.get(0), serviceA);

        this.repository.deleteAll();

        Assertions.assertTrue(repository.findByActiveExists().isEmpty());
    }

    @Test
    public void testFindByCreateAtAfter() throws ParseException {
        final List<Service> services = Arrays.asList(serviceA, serviceB);
        this.repository.saveAll(services);

        Date testDate = new SimpleDateFormat("yyyyMMdd").parse("20180602");
        List<Service> foundServices = repository.findByCreateAtAfter(testDate);
        Assertions.assertEquals(foundServices.size(), 1);
        Assertions.assertEquals(foundServices.get(0), serviceB);

        testDate = new SimpleDateFormat("yyyyMMdd").parse("20180502");
        foundServices = repository.findByCreateAtAfter(testDate);
        services.sort(Comparator.comparing(Service::getId));
        foundServices.sort(Comparator.comparing(Service::getId));
        Assertions.assertEquals(foundServices.size(), 2);
        Assertions.assertEquals(foundServices, services);

        testDate = new SimpleDateFormat("yyyyMMdd").parse("20180606");
        foundServices = repository.findByCreateAtAfter(testDate);
        Assertions.assertTrue(foundServices.isEmpty());
    }

    @Test
    public void testFindByNameOrTypeAndInstanceCountAndCreateAtAfter() throws ParseException {
        final List<Service> services = Arrays.asList(serviceA, serviceB);
        this.repository.saveAll(services);

        Date testDate = new SimpleDateFormat("yyyyMMdd").parse("20180601");
        List<Service> foundServices = repository.findByNameOrTypeAndInstanceCountAndCreateAtAfter(NAME_A,
                serviceB.getType(), COUNT_B, testDate);

        services.sort(Comparator.comparing(Service::getId));
        foundServices.sort(Comparator.comparing(Service::getId));
        Assertions.assertEquals(foundServices.size(), 2);
        Assertions.assertEquals(foundServices, services);

        testDate = new SimpleDateFormat("yyyyMMdd").parse("20180607");
        foundServices = repository.findByNameOrTypeAndInstanceCountAndCreateAtAfter(NAME_A, serviceB.getType(), COUNT_B,
                testDate);
        Assertions.assertEquals(foundServices.size(), 1);
        Assertions.assertEquals(foundServices.get(0), serviceA);
        Assertions.assertTrue(repository.findByNameOrTypeAndInstanceCountAndCreateAtAfter("fake-name", serviceB.getType(),
                COUNT_B, testDate).isEmpty());
    }

    @Test
    public void testFindByCreateAtBefore() throws ParseException {
        final List<Service> services = Arrays.asList(serviceA, serviceB);
        this.repository.saveAll(services);

        Date testDate = new SimpleDateFormat("yyyyMMdd").parse("20180602");
        List<Service> foundServices = repository.findByCreateAtBefore(testDate);
        Assertions.assertEquals(foundServices.size(), 1);
        Assertions.assertEquals(foundServices.get(0), serviceA);

        testDate = new SimpleDateFormat("yyyyMMdd").parse("20180606");
        foundServices = repository.findByCreateAtBefore(testDate);
        services.sort(Comparator.comparing(Service::getId));
        foundServices.sort(Comparator.comparing(Service::getId));
        Assertions.assertEquals(foundServices.size(), 2);
        Assertions.assertEquals(foundServices, services);

        testDate = new SimpleDateFormat("yyyyMMdd").parse("20180506");
        foundServices = repository.findByCreateAtBefore(testDate);
        Assertions.assertTrue(foundServices.isEmpty());
    }

    @Test
    public void testFindByCreateAtBeforeAndCreateAtAfter() throws ParseException {
        final List<Service> services = Arrays.asList(serviceA, serviceB);
        this.repository.saveAll(services);

        Date startDate = new SimpleDateFormat("yyyyMMdd").parse("20180602");
        Date endDate = new SimpleDateFormat("yyyyMMdd").parse("20180606");
        List<Service> foundServices = repository.findByCreateAtAfterAndCreateAtBefore(startDate, endDate);
        Assertions.assertEquals(foundServices.size(), 1);
        Assertions.assertEquals(foundServices.get(0), serviceB);

        startDate = new SimpleDateFormat("yyyyMMdd").parse("20180506");
        endDate = new SimpleDateFormat("yyyyMMdd").parse("20180606");
        foundServices = repository.findByCreateAtAfterAndCreateAtBefore(startDate, endDate);
        services.sort(Comparator.comparing(Service::getId));
        foundServices.sort(Comparator.comparing(Service::getId));
        Assertions.assertEquals(foundServices.size(), 2);
        Assertions.assertEquals(foundServices, services);

        startDate = new SimpleDateFormat("yyyyMMdd").parse("20180606");
        endDate = new SimpleDateFormat("yyyyMMdd").parse("20180607");
        foundServices = repository.findByCreateAtAfterAndCreateAtBefore(startDate, endDate);
        Assertions.assertTrue(foundServices.isEmpty());
    }

    @Test
    public void testFindByCreateAtBetween() throws ParseException {
        final List<Service> services = Arrays.asList(serviceA, serviceB);
        this.repository.saveAll(services);

        Date startDate = new SimpleDateFormat("yyyyMMdd").parse("20180602");
        Date endDate = new SimpleDateFormat("yyyyMMdd").parse("20180606");
        List<Service> foundServices = repository.findByCreateAtBetween(startDate, endDate);
        Assertions.assertEquals(foundServices.size(), 1);
        Assertions.assertEquals(foundServices.get(0), serviceB);

        startDate = new SimpleDateFormat("yyyyMMdd").parse("20180601");
        endDate = new SimpleDateFormat("yyyyMMdd").parse("20180604");
        foundServices = repository.findByCreateAtBetween(startDate, endDate);
        services.sort(Comparator.comparing(Service::getId));
        foundServices.sort(Comparator.comparing(Service::getId));
        Assertions.assertEquals(foundServices.size(), 2);
        Assertions.assertEquals(foundServices, services);

        startDate = new SimpleDateFormat("yyyyMMdd").parse("20180606");
        endDate = new SimpleDateFormat("yyyyMMdd").parse("20180607");
        foundServices = repository.findByCreateAtBetween(startDate, endDate);
        Assertions.assertTrue(foundServices.isEmpty());
    }
}

