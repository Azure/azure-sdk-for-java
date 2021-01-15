// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.common.repository;

import com.azure.spring.data.gremlin.common.domain.Service;
import com.azure.spring.data.gremlin.common.domain.ServiceType;
import com.azure.spring.data.gremlin.repository.GremlinRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface ServiceRepository extends GremlinRepository<Service, String> {

    List<Service> findByName(String name);

    List<Service> findByInstanceCount(int instanceCount);

    List<Service> findByActive(boolean isActive);

    List<Service> findByCreateAt(Date createAt);

    List<Service> findByProperties(Map<String, Object> properties);

    List<Service> findByNameAndInstanceCount(String name, int instanceCount);

    List<Service> findByNameOrInstanceCount(String name, int instanceCount);

    List<Service> findByNameAndInstanceCountAndType(String name, int instanceCount, ServiceType type);

    List<Service> findByNameAndActiveOrProperties(String name, boolean isActive, Map<String, Object> properties);

    List<Service> findByNameOrInstanceCountAndType(String name, int instanceCount, ServiceType type);

    List<Service> findByNameAndInstanceCountOrType(String name, int instanceCount, ServiceType type);

    List<Service> findByActiveExists();

    List<Service> findByCreateAtAfter(Date expiryDate);

    List<Service> findByNameOrTypeAndInstanceCountAndCreateAtAfter(String name, ServiceType type, int instanceCount,
                                                                   Date expiryDate);

    List<Service> findByCreateAtBefore(Date expiryDate);

    List<Service> findByCreateAtAfterAndCreateAtBefore(Date startDate, Date endDate);

    List<Service> findByCreateAtBetween(Date start, Date end);
}
