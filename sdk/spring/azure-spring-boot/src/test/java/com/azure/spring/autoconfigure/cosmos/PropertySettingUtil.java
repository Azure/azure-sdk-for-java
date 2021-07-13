// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.cosmos;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConsistencyLevel;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.springframework.test.context.support.TestPropertySourceUtils.addInlinedPropertiesToEnvironment;

public class PropertySettingUtil {
    public static final String URI = "https://test.documents.azure.com:443/";
    public static final String KEY = "FakeKey";
    public static final String DATABASE_NAME = "test";
    public static final boolean POPULATE_QUERY_METRICS = true;
    public static final ConsistencyLevel CONSISTENCY_LEVEL = ConsistencyLevel.STRONG;
    public static final ConnectionMode CONNECTION_MODE = ConnectionMode.DIRECT;
    public static final String CLIENT_ID = "for-test-purpose";
    public static final String CLOUD = "AzureChina";
    public static final String PROPERTY_URI = "spring.cloud.azure.cosmos.uri";
    public static final String PROPERTY_KEY = "spring.cloud.azure.cosmos.key";
    public static final String PROPERTY_DBNAME = "spring.cloud.azure.cosmos.database";
    public static final String PROPERTY_CONSISTENCY_LEVEL = "spring.cloud.azure.cosmos.consistency-level";
    public static final String PROPERTY_POPULATE_QUERY_METRICS = "spring.cloud.azure.cosmos.populateQueryMetrics";
    public static final String PROPERTY_CONNECTION_MODE = "spring.cloud.azure.cosmos.connection-mode";
    public static final String PROPERTY_CLIENT_ID = "spring.cloud.azure.cosmos.credential.client-id";
    public static final String PROPERTY_CLOUD = "spring.cloud.azure.cosmos.environment.cloud";
    public static final String PROPERTY_UNIFIED_CLIENT_ID = "spring.cloud.azure.credential.client-id";
    public static final String PROPERTY_UNIFIED_CLOUD = "spring.cloud.azure.environment.cloud";

    public static void configureCosmosProperties(AnnotationConfigApplicationContext context) {
        addInlinedPropertiesToEnvironment(
            context,
            PROPERTY_URI + "=" + URI,
            PROPERTY_KEY + "=" + KEY,
            PROPERTY_DBNAME + "=" + DATABASE_NAME,
            PROPERTY_CONSISTENCY_LEVEL + "=" + CONSISTENCY_LEVEL.name(),
            PROPERTY_POPULATE_QUERY_METRICS + "=" + POPULATE_QUERY_METRICS,
            PROPERTY_CONNECTION_MODE + "=" + CONNECTION_MODE.name(),
            PROPERTY_CLIENT_ID + "=" + CLIENT_ID,
            PROPERTY_CLOUD + "=" + CLOUD
        );
    }

    public static String[] getCosmosPropertyValues() {
        return new String[] { PROPERTY_URI + "=" + URI,
            PROPERTY_KEY + "=" + KEY,
            PROPERTY_DBNAME + "=" + DATABASE_NAME,
            PROPERTY_CONSISTENCY_LEVEL + "=" + CONSISTENCY_LEVEL.name(),
            PROPERTY_POPULATE_QUERY_METRICS + "=" + POPULATE_QUERY_METRICS,
            PROPERTY_CONNECTION_MODE + "=" + CONNECTION_MODE.name()
        };
    }

    public static String[] getUnifiedPropertyValues() {
        return new String[] { PROPERTY_UNIFIED_CLIENT_ID + "=" + CLIENT_ID,
            PROPERTY_UNIFIED_CLOUD + "=" + CLOUD
        };
    }
}
