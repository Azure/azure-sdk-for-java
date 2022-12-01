// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.UniqueKeyPolicy;
import com.azure.spring.data.cosmos.Constants;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.common.CosmosUtils;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.config.DatabaseThroughputConfig;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.generator.CountQueryGenerator;
import com.azure.spring.data.cosmos.core.generator.FindQuerySpecGenerator;
import com.azure.spring.data.cosmos.core.generator.NativeQueryGenerator;
import com.azure.spring.data.cosmos.core.mapping.event.AfterLoadEvent;
import com.azure.spring.data.cosmos.core.mapping.event.CosmosMappingEvent;
import com.azure.spring.data.cosmos.core.query.CosmosPageImpl;
import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.core.query.CosmosSliceImpl;
import com.azure.spring.data.cosmos.core.query.Criteria;
import com.azure.spring.data.cosmos.core.query.CriteriaType;
import com.azure.spring.data.cosmos.exception.CosmosExceptionUtils;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Template class for cosmos db
 */
public class MultiTenantDBCosmosTemplate extends CosmosTemplate {

    public MultiTenantDBCosmosTemplate(CosmosFactory cosmosFactory, CosmosConfig cosmosConfig, MappingCosmosConverter mappingCosmosConverter, IsNewAwareAuditingHandler cosmosAuditingHandler) {
        super(cosmosFactory, cosmosConfig, mappingCosmosConverter, cosmosAuditingHandler);

        super.databaseName = cosmosFactory.getDatabaseName();
    }

    public void setDatabaseName(String dbName, CosmosEntityInformation<?, ?> information) {
        super.databaseName = dbName;

        createContainerIfNotExists(information);
    }

    public String getDatabaseName() {
        return super.databaseName;
    }
}
