// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv.incubating;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv.AttributeKeyTemplate;
import java.util.List;

// this is a copy of io.opentelemetry.semconv.incubating.DbIncubatingAttributes (1.37.0)

// DO NOT EDIT, this is an Auto-generated file from
// buildscripts/templates/registry/incubating_java/IncubatingSemanticAttributes.java.j2
@SuppressWarnings("unused")
public final class DbIncubatingAttributes {
    /**
     * Deprecated, use {@code cassandra.consistency.level} instead.
     *
     * @deprecated Replaced by {@code cassandra.consistency.level}.
     */
    @Deprecated
    public static final AttributeKey<String> DB_CASSANDRA_CONSISTENCY_LEVEL
        = stringKey("db.cassandra.consistency_level");

    /**
     * Deprecated, use {@code cassandra.coordinator.dc} instead.
     *
     * @deprecated Replaced by {@code cassandra.coordinator.dc}.
     */
    @Deprecated
    public static final AttributeKey<String> DB_CASSANDRA_COORDINATOR_DC = stringKey("db.cassandra.coordinator.dc");

    /**
     * Deprecated, use {@code cassandra.coordinator.id} instead.
     *
     * @deprecated Replaced by {@code cassandra.coordinator.id}.
     */
    @Deprecated
    public static final AttributeKey<String> DB_CASSANDRA_COORDINATOR_ID = stringKey("db.cassandra.coordinator.id");

    /**
     * Deprecated, use {@code cassandra.query.idempotent} instead.
     *
     * @deprecated Replaced by {@code cassandra.query.idempotent}.
     */
    @Deprecated
    public static final AttributeKey<Boolean> DB_CASSANDRA_IDEMPOTENCE = booleanKey("db.cassandra.idempotence");

    /**
     * Deprecated, use {@code cassandra.page.size} instead.
     *
     * @deprecated Replaced by {@code cassandra.page.size}.
     */
    @Deprecated
    public static final AttributeKey<Long> DB_CASSANDRA_PAGE_SIZE = longKey("db.cassandra.page_size");

    /**
     * Deprecated, use {@code cassandra.speculative_execution.count} instead.
     *
     * @deprecated Replaced by {@code cassandra.speculative_execution.count}.
     */
    @Deprecated
    public static final AttributeKey<Long> DB_CASSANDRA_SPECULATIVE_EXECUTION_COUNT
        = longKey("db.cassandra.speculative_execution_count");

    /**
     * Deprecated, use {@code db.collection.name} instead.
     *
     * @deprecated Replaced by {@code db.collection.name}.
     */
    @Deprecated
    public static final AttributeKey<String> DB_CASSANDRA_TABLE = stringKey("db.cassandra.table");

    /**
     * The name of the connection pool; unique within the instrumented application. In case the
     * connection pool implementation doesn't provide a name, instrumentation SHOULD use a combination
     * of parameters that would make the name unique, for example, combining attributes {@code
     * server.address}, {@code server.port}, and {@code db.namespace}, formatted as {@code
     * server.address:server.port/db.namespace}. Instrumentations that generate connection pool name
     * following different patterns SHOULD document it.
     */
    public static final AttributeKey<String> DB_CLIENT_CONNECTION_POOL_NAME
        = stringKey("db.client.connection.pool.name");

    /** The state of a connection in the pool */
    public static final AttributeKey<String> DB_CLIENT_CONNECTION_STATE = stringKey("db.client.connection.state");

    /**
     * Deprecated, use {@code db.client.connection.pool.name} instead.
     *
     * @deprecated Replaced by {@code db.client.connection.pool.name}.
     */
    @Deprecated
    public static final AttributeKey<String> DB_CLIENT_CONNECTIONS_POOL_NAME
        = stringKey("db.client.connections.pool.name");

    /**
     * Deprecated, use {@code db.client.connection.state} instead.
     *
     * @deprecated Replaced by {@code db.client.connection.state}.
     */
    @Deprecated
    public static final AttributeKey<String> DB_CLIENT_CONNECTIONS_STATE = stringKey("db.client.connections.state");

    /**
     * The name of a collection (table, container) within the database.
     *
     * <p>Notes:
     *
     * <p>It is RECOMMENDED to capture the value as provided by the application without attempting to
     * do any case normalization.
     *
     * <p>The collection name SHOULD NOT be extracted from {@code db.query.text}, when the database
     * system supports query text with multiple collections in non-batch operations.
     *
     * <p>For batch operations, if the individual operations are known to have the same collection
     * name then that collection name SHOULD be used.
     *
     * @deprecated deprecated in favor of stable {@link
     *     io.opentelemetry.semconv.DbAttributes#DB_COLLECTION_NAME} attribute.
     */
    @Deprecated
    public static final AttributeKey<String> DB_COLLECTION_NAME = stringKey("db.collection.name");

    /**
     * Deprecated, use {@code server.address}, {@code server.port} attributes instead.
     *
     * @deprecated Replaced by {@code server.address} and {@code server.port}.
     */
    @Deprecated
    public static final AttributeKey<String> DB_CONNECTION_STRING = stringKey("db.connection_string");

    /**
     * Deprecated, use {@code azure.client.id} instead.
     *
     * @deprecated Replaced by {@code azure.client.id}.
     */
    @Deprecated
    public static final AttributeKey<String> DB_COSMOSDB_CLIENT_ID = stringKey("db.cosmosdb.client_id");

    /**
     * Deprecated, use {@code azure.cosmosdb.connection.mode} instead.
     *
     * @deprecated Replaced by {@code azure.cosmosdb.connection.mode}.
     */
    @Deprecated
    public static final AttributeKey<String> DB_COSMOSDB_CONNECTION_MODE = stringKey("db.cosmosdb.connection_mode");

    /**
     * Deprecated, use {@code cosmosdb.consistency.level} instead.
     *
     * @deprecated Replaced by {@code azure.cosmosdb.consistency.level}.
     */
    @Deprecated
    public static final AttributeKey<String> DB_COSMOSDB_CONSISTENCY_LEVEL = stringKey("db.cosmosdb.consistency_level");

    /**
     * Deprecated, use {@code db.collection.name} instead.
     *
     * @deprecated Replaced by {@code db.collection.name}.
     */
    @Deprecated
    public static final AttributeKey<String> DB_COSMOSDB_CONTAINER = stringKey("db.cosmosdb.container");

    /**
     * Deprecated, no replacement at this time.
     *
     * @deprecated Removed, no replacement at this time.
     */
    @Deprecated
    public static final AttributeKey<String> DB_COSMOSDB_OPERATION_TYPE = stringKey("db.cosmosdb.operation_type");

    /**
     * Deprecated, use {@code azure.cosmosdb.operation.contacted_regions} instead.
     *
     * @deprecated Replaced by {@code azure.cosmosdb.operation.contacted_regions}.
     */
    @Deprecated
    public static final AttributeKey<List<String>> DB_COSMOSDB_REGIONS_CONTACTED
        = stringArrayKey("db.cosmosdb.regions_contacted");

    /**
     * Deprecated, use {@code azure.cosmosdb.operation.request_charge} instead.
     *
     * @deprecated Replaced by {@code azure.cosmosdb.operation.request_charge}.
     */
    @Deprecated
    public static final AttributeKey<Double> DB_COSMOSDB_REQUEST_CHARGE = doubleKey("db.cosmosdb.request_charge");

    /**
     * Deprecated, use {@code azure.cosmosdb.request.body.size} instead.
     *
     * @deprecated Replaced by {@code azure.cosmosdb.request.body.size}.
     */
    @Deprecated
    public static final AttributeKey<Long> DB_COSMOSDB_REQUEST_CONTENT_LENGTH
        = longKey("db.cosmosdb.request_content_length");

    /**
     * Deprecated, use {@code db.response.status_code} instead.
     *
     * @deprecated Replaced by {@code db.response.status_code}.
     */
    @Deprecated
    public static final AttributeKey<Long> DB_COSMOSDB_STATUS_CODE = longKey("db.cosmosdb.status_code");

    /**
     * Deprecated, use {@code azure.cosmosdb.response.sub_status_code} instead.
     *
     * @deprecated Replaced by {@code azure.cosmosdb.response.sub_status_code}.
     */
    @Deprecated
    public static final AttributeKey<Long> DB_COSMOSDB_SUB_STATUS_CODE = longKey("db.cosmosdb.sub_status_code");

    /**
     * Deprecated, use {@code db.namespace} instead.
     *
     * @deprecated Replaced by {@code db.namespace}.
     */
    @Deprecated
    public static final AttributeKey<String> DB_ELASTICSEARCH_CLUSTER_NAME = stringKey("db.elasticsearch.cluster.name");

    /**
     * Deprecated, use {@code elasticsearch.node.name} instead.
     *
     * @deprecated Replaced by {@code elasticsearch.node.name}.
     */
    @Deprecated
    public static final AttributeKey<String> DB_ELASTICSEARCH_NODE_NAME = stringKey("db.elasticsearch.node.name");

    /**
     * Deprecated, use {@code db.operation.parameter} instead.
     *
     * @deprecated Replaced by {@code db.operation.parameter}.
     */
    @Deprecated
    public static final AttributeKeyTemplate<String> DB_ELASTICSEARCH_PATH_PARTS
        = AttributeKeyTemplate.stringKeyTemplate("db.elasticsearch.path_parts");

    /**
     * Deprecated, no general replacement at this time. For Elasticsearch, use {@code
     * db.elasticsearch.node.name} instead.
     *
     * @deprecated Removed, no general replacement at this time. For Elasticsearch, use {@code
     *     db.elasticsearch.node.name} instead.
     */
    @Deprecated
    public static final AttributeKey<String> DB_INSTANCE_ID = stringKey("db.instance.id");

    /**
     * Removed, no replacement at this time.
     *
     * @deprecated Removed, no replacement at this time.
     */
    @Deprecated
    public static final AttributeKey<String> DB_JDBC_DRIVER_CLASSNAME = stringKey("db.jdbc.driver_classname");

    /**
     * Deprecated, use {@code db.collection.name} instead.
     *
     * @deprecated Replaced by {@code db.collection.name}.
     */
    @Deprecated
    public static final AttributeKey<String> DB_MONGODB_COLLECTION = stringKey("db.mongodb.collection");

    /**
     * Deprecated, SQL Server instance is now populated as a part of {@code db.namespace} attribute.
     *
     * @deprecated Removed, no replacement at this time.
     */
    @Deprecated
    public static final AttributeKey<String> DB_MSSQL_INSTANCE_NAME = stringKey("db.mssql.instance_name");

    /**
     * Deprecated, use {@code db.namespace} instead.
     *
     * @deprecated Replaced by {@code db.namespace}.
     */
    @Deprecated
    public static final AttributeKey<String> DB_NAME = stringKey("db.name");

    /**
     * The name of the database, fully qualified within the server address and port.
     *
     * <p>Notes:
     *
     * <p>If a database system has multiple namespace components, they SHOULD be concatenated from the
     * most general to the most specific namespace component, using {@code |} as a separator between
     * the components. Any missing components (and their associated separators) SHOULD be omitted.
     * Semantic conventions for individual database systems SHOULD document what {@code db.namespace}
     * means in the context of that system. It is RECOMMENDED to capture the value as provided by the
     * application without attempting to do any case normalization.
     *
     * @deprecated deprecated in favor of stable {@link
     *     io.opentelemetry.semconv.DbAttributes#DB_NAMESPACE} attribute.
     */
    @Deprecated
    public static final AttributeKey<String> DB_NAMESPACE = stringKey("db.namespace");

    /**
     * Deprecated, use {@code db.operation.name} instead.
     *
     * @deprecated Replaced by {@code db.operation.name}.
     */
    @Deprecated
    public static final AttributeKey<String> DB_OPERATION = stringKey("db.operation");

    /**
     * The number of queries included in a batch operation.
     *
     * <p>Notes:
     *
     * <p>Operations are only considered batches when they contain two or more operations, and so
     * {@code db.operation.batch.size} SHOULD never be {@code 1}.
     *
     * @deprecated deprecated in favor of stable {@link
     *     io.opentelemetry.semconv.DbAttributes#DB_OPERATION_BATCH_SIZE} attribute.
     */
    @Deprecated
    public static final AttributeKey<Long> DB_OPERATION_BATCH_SIZE = longKey("db.operation.batch.size");

    /**
     * The name of the operation or command being executed.
     *
     * <p>Notes:
     *
     * <p>It is RECOMMENDED to capture the value as provided by the application without attempting to
     * do any case normalization.
     *
     * <p>The operation name SHOULD NOT be extracted from {@code db.query.text}, when the database
     * system supports query text with multiple operations in non-batch operations.
     *
     * <p>If spaces can occur in the operation name, multiple consecutive spaces SHOULD be normalized
     * to a single space.
     *
     * <p>For batch operations, if the individual operations are known to have the same operation name
     * then that operation name SHOULD be used prepended by {@code BATCH }, otherwise {@code
     * db.operation.name} SHOULD be {@code BATCH} or some other database system specific term if more
     * applicable.
     *
     * @deprecated deprecated in favor of stable {@link
     *     io.opentelemetry.semconv.DbAttributes#DB_OPERATION_NAME} attribute.
     */
    @Deprecated
    public static final AttributeKey<String> DB_OPERATION_NAME = stringKey("db.operation.name");

    /**
     * A database operation parameter, with {@code <key>} being the parameter name, and the attribute
     * value being a string representation of the parameter value.
     *
     * <p>Notes:
     *
     * <p>For example, a client-side maximum number of rows to read from the database MAY be recorded
     * as the {@code db.operation.parameter.max_rows} attribute.
     *
     * <p>{@code db.query.text} parameters SHOULD be captured using {@code db.query.parameter.<key>}
     * instead of {@code db.operation.parameter.<key>}.
     */
    public static final AttributeKeyTemplate<String> DB_OPERATION_PARAMETER
        = AttributeKeyTemplate.stringKeyTemplate("db.operation.parameter");

    /**
     * A database query parameter, with {@code <key>} being the parameter name, and the attribute
     * value being a string representation of the parameter value.
     *
     * <p>Notes:
     *
     * <p>If a query parameter has no name and instead is referenced only by index, then {@code <key>}
     * SHOULD be the 0-based index.
     *
     * <p>{@code db.query.parameter.<key>} SHOULD match up with the parameterized placeholders present
     * in {@code db.query.text}.
     *
     * <p>{@code db.query.parameter.<key>} SHOULD NOT be captured on batch operations.
     *
     * <p>Examples:
     *
     * <ul>
     *   <li>For a query {@code SELECT * FROM users where username = %s} with the parameter {@code
     *       "jdoe"}, the attribute {@code db.query.parameter.0} SHOULD be set to {@code "jdoe"}.
     *   <li>For a query {@code "SELECT * FROM users WHERE username = %(username)s;} with parameter
     *       {@code username = "jdoe"}, the attribute {@code db.query.parameter.username} SHOULD be
     *       set to {@code "jdoe"}.
     * </ul>
     */
    public static final AttributeKeyTemplate<String> DB_QUERY_PARAMETER
        = AttributeKeyTemplate.stringKeyTemplate("db.query.parameter");

    /**
     * Low cardinality summary of a database query.
     *
     * <p>Notes:
     *
     * <p>The query summary describes a class of database queries and is useful as a grouping key,
     * especially when analyzing telemetry for database calls involving complex queries.
     *
     * <p>Summary may be available to the instrumentation through instrumentation hooks or other
     * means. If it is not available, instrumentations that support query parsing SHOULD generate a
     * summary following <a
     * href="/docs/database/database-spans.md#generating-a-summary-of-the-query">Generating query
     * summary</a> section.
     *
     * @deprecated deprecated in favor of stable {@link
     *     io.opentelemetry.semconv.DbAttributes#DB_QUERY_SUMMARY} attribute.
     */
    @Deprecated
    public static final AttributeKey<String> DB_QUERY_SUMMARY = stringKey("db.query.summary");

    /**
     * The database query being executed.
     *
     * <p>Notes:
     *
     * <p>For sanitization see <a
     * href="/docs/database/database-spans.md#sanitization-of-dbquerytext">Sanitization of {@code
     * db.query.text}</a>. For batch operations, if the individual operations are known to have the
     * same query text then that query text SHOULD be used, otherwise all of the individual query
     * texts SHOULD be concatenated with separator {@code ; } or some other database system specific
     * separator if more applicable. Parameterized query text SHOULD NOT be sanitized. Even though
     * parameterized query text can potentially have sensitive data, by using a parameterized query
     * the user is giving a strong signal that any sensitive data will be passed as parameter values,
     * and the benefit to observability of capturing the static part of the query text by default
     * outweighs the risk.
     *
     * @deprecated deprecated in favor of stable {@link
     *     io.opentelemetry.semconv.DbAttributes#DB_QUERY_TEXT} attribute.
     */
    @Deprecated
    public static final AttributeKey<String> DB_QUERY_TEXT = stringKey("db.query.text");

    /**
     * Deprecated, use {@code db.namespace} instead.
     *
     * @deprecated Replaced by {@code db.namespace}.
     */
    @Deprecated
    public static final AttributeKey<Long> DB_REDIS_DATABASE_INDEX = longKey("db.redis.database_index");

    /** Number of rows returned by the operation. */
    public static final AttributeKey<Long> DB_RESPONSE_RETURNED_ROWS = longKey("db.response.returned_rows");

    /**
     * Database response status code.
     *
     * <p>Notes:
     *
     * <p>The status code returned by the database. Usually it represents an error code, but may also
     * represent partial success, warning, or differentiate between various types of successful
     * outcomes. Semantic conventions for individual database systems SHOULD document what {@code
     * db.response.status_code} means in the context of that system.
     *
     * @deprecated deprecated in favor of stable {@link
     *     io.opentelemetry.semconv.DbAttributes#DB_RESPONSE_STATUS_CODE} attribute.
     */
    @Deprecated
    public static final AttributeKey<String> DB_RESPONSE_STATUS_CODE = stringKey("db.response.status_code");

    /**
     * Deprecated, use {@code db.collection.name} instead.
     *
     * @deprecated Replaced by {@code db.collection.name}, but only if not extracting the value from
     *     {@code db.query.text}.
     */
    @Deprecated
    public static final AttributeKey<String> DB_SQL_TABLE = stringKey("db.sql.table");

    /**
     * The database statement being executed.
     *
     * @deprecated Replaced by {@code db.query.text}.
     */
    @Deprecated
    public static final AttributeKey<String> DB_STATEMENT = stringKey("db.statement");

    /**
     * The name of a stored procedure within the database.
     *
     * <p>Notes:
     *
     * <p>It is RECOMMENDED to capture the value as provided by the application without attempting to
     * do any case normalization.
     *
     * <p>For batch operations, if the individual operations are known to have the same stored
     * procedure name then that stored procedure name SHOULD be used.
     *
     * @deprecated deprecated in favor of stable {@link
     *     io.opentelemetry.semconv.DbAttributes#DB_STORED_PROCEDURE_NAME} attribute.
     */
    @Deprecated
    public static final AttributeKey<String> DB_STORED_PROCEDURE_NAME = stringKey("db.stored_procedure.name");

    /**
     * Deprecated, use {@code db.system.name} instead.
     *
     * @deprecated Replaced by {@code db.system.name}.
     */
    @Deprecated
    public static final AttributeKey<String> DB_SYSTEM = stringKey("db.system");

    /**
     * The database management system (DBMS) product as identified by the client instrumentation.
     *
     * <p>Notes:
     *
     * <p>The actual DBMS may differ from the one identified by the client. For example, when using
     * PostgreSQL client libraries to connect to a CockroachDB, the {@code db.system.name} is set to
     * {@code postgresql} based on the instrumentation's best knowledge.
     *
     * @deprecated deprecated in favor of stable {@link
     *     io.opentelemetry.semconv.DbAttributes#DB_SYSTEM_NAME} attribute.
     */
    @Deprecated
    public static final AttributeKey<String> DB_SYSTEM_NAME = stringKey("db.system.name");

    /**
     * Deprecated, no replacement at this time.
     *
     * @deprecated Removed, no replacement at this time.
     */
    @Deprecated
    public static final AttributeKey<String> DB_USER = stringKey("db.user");

    // Enum definitions

    /**
     * Values for {@link #DB_CASSANDRA_CONSISTENCY_LEVEL}
     *
     * @deprecated Replaced by {@code cassandra.consistency.level}.
     */
    @Deprecated
    public static final class DbCassandraConsistencyLevelIncubatingValues {
        /** all. */
        public static final String ALL = "all";

        /** each_quorum. */
        public static final String EACH_QUORUM = "each_quorum";

        /** quorum. */
        public static final String QUORUM = "quorum";

        /** local_quorum. */
        public static final String LOCAL_QUORUM = "local_quorum";

        /** one. */
        public static final String ONE = "one";

        /** two. */
        public static final String TWO = "two";

        /** three. */
        public static final String THREE = "three";

        /** local_one. */
        public static final String LOCAL_ONE = "local_one";

        /** any. */
        public static final String ANY = "any";

        /** serial. */
        public static final String SERIAL = "serial";

        /** local_serial. */
        public static final String LOCAL_SERIAL = "local_serial";

        private DbCassandraConsistencyLevelIncubatingValues() {
        }
    }

    /** Values for {@link #DB_CLIENT_CONNECTION_STATE}. */
    public static final class DbClientConnectionStateIncubatingValues {
        /** idle. */
        public static final String IDLE = "idle";

        /** used. */
        public static final String USED = "used";

        private DbClientConnectionStateIncubatingValues() {
        }
    }

    /**
     * Values for {@link #DB_CLIENT_CONNECTIONS_STATE}
     *
     * @deprecated Replaced by {@code db.client.connection.state}.
     */
    @Deprecated
    public static final class DbClientConnectionsStateIncubatingValues {
        /** idle. */
        public static final String IDLE = "idle";

        /** used. */
        public static final String USED = "used";

        private DbClientConnectionsStateIncubatingValues() {
        }
    }

    /**
     * Values for {@link #DB_COSMOSDB_CONNECTION_MODE}
     *
     * @deprecated Replaced by {@code azure.cosmosdb.connection.mode}.
     */
    @Deprecated
    public static final class DbCosmosdbConnectionModeIncubatingValues {
        /** Gateway (HTTP) connection. */
        public static final String GATEWAY = "gateway";

        /** Direct connection. */
        public static final String DIRECT = "direct";

        private DbCosmosdbConnectionModeIncubatingValues() {
        }
    }

    /**
     * Values for {@link #DB_COSMOSDB_CONSISTENCY_LEVEL}
     *
     * @deprecated Replaced by {@code azure.cosmosdb.consistency.level}.
     */
    @Deprecated
    public static final class DbCosmosdbConsistencyLevelIncubatingValues {
        /** strong. */
        public static final String STRONG = "Strong";

        /** bounded_staleness. */
        public static final String BOUNDED_STALENESS = "BoundedStaleness";

        /** session. */
        public static final String SESSION = "Session";

        /** eventual. */
        public static final String EVENTUAL = "Eventual";

        /** consistent_prefix. */
        public static final String CONSISTENT_PREFIX = "ConsistentPrefix";

        private DbCosmosdbConsistencyLevelIncubatingValues() {
        }
    }

    /**
     * Values for {@link #DB_COSMOSDB_OPERATION_TYPE}
     *
     * @deprecated Removed, no replacement at this time.
     */
    @Deprecated
    public static final class DbCosmosdbOperationTypeIncubatingValues {
        /** batch. */
        public static final String BATCH = "batch";

        /** create. */
        public static final String CREATE = "create";

        /** delete. */
        public static final String DELETE = "delete";

        /** execute. */
        public static final String EXECUTE = "execute";

        /** execute_javascript. */
        public static final String EXECUTE_JAVASCRIPT = "execute_javascript";

        /** invalid. */
        public static final String INVALID = "invalid";

        /** head. */
        public static final String HEAD = "head";

        /** head_feed. */
        public static final String HEAD_FEED = "head_feed";

        /** patch. */
        public static final String PATCH = "patch";

        /** query. */
        public static final String QUERY = "query";

        /** query_plan. */
        public static final String QUERY_PLAN = "query_plan";

        /** read. */
        public static final String READ = "read";

        /** read_feed. */
        public static final String READ_FEED = "read_feed";

        /** replace. */
        public static final String REPLACE = "replace";

        /** upsert. */
        public static final String UPSERT = "upsert";

        private DbCosmosdbOperationTypeIncubatingValues() {
        }
    }

    /**
     * Values for {@link #DB_SYSTEM}
     *
     * @deprecated Replaced by {@code db.system.name}.
     */
    @Deprecated
    public static final class DbSystemValues {
        /** Some other SQL database. Fallback only. See notes. */
        public static final String OTHER_SQL = "other_sql";

        /** Adabas (Adaptable Database System) */
        public static final String ADABAS = "adabas";

        /**
         * Deprecated, use {@code intersystems_cache} instead.
         *
         * @deprecated Replaced by {@code intersystems_cache}.
         */
        @Deprecated
        public static final String CACHE = "cache";

        /** InterSystems Caché */
        public static final String INTERSYSTEMS_CACHE = "intersystems_cache";

        /** Apache Cassandra */
        public static final String CASSANDRA = "cassandra";

        /** ClickHouse */
        public static final String CLICKHOUSE = "clickhouse";

        /**
         * Deprecated, use {@code other_sql} instead.
         *
         * @deprecated Replaced by {@code other_sql}.
         */
        @Deprecated
        public static final String CLOUDSCAPE = "cloudscape";

        /** CockroachDB */
        public static final String COCKROACHDB = "cockroachdb";

        /**
         * Deprecated, no replacement at this time.
         *
         * @deprecated Obsoleted.
         */
        @Deprecated
        public static final String COLDFUSION = "coldfusion";

        /** Microsoft Azure Cosmos DB */
        public static final String COSMOSDB = "cosmosdb";

        /** Couchbase */
        public static final String COUCHBASE = "couchbase";

        /** CouchDB */
        public static final String COUCHDB = "couchdb";

        /** IBM Db2 */
        public static final String DB2 = "db2";

        /** Apache Derby */
        public static final String DERBY = "derby";

        /** Amazon DynamoDB */
        public static final String DYNAMODB = "dynamodb";

        /** EnterpriseDB */
        public static final String EDB = "edb";

        /** Elasticsearch */
        public static final String ELASTICSEARCH = "elasticsearch";

        /** FileMaker */
        public static final String FILEMAKER = "filemaker";

        /** Firebird */
        public static final String FIREBIRD = "firebird";

        /**
         * Deprecated, use {@code other_sql} instead.
         *
         * @deprecated Replaced by {@code other_sql}.
         */
        @Deprecated
        public static final String FIRSTSQL = "firstsql";

        /** Apache Geode */
        public static final String GEODE = "geode";

        /** H2 */
        public static final String H2 = "h2";

        /** SAP HANA */
        public static final String HANADB = "hanadb";

        /** Apache HBase */
        public static final String HBASE = "hbase";

        /** Apache Hive */
        public static final String HIVE = "hive";

        /** HyperSQL DataBase */
        public static final String HSQLDB = "hsqldb";

        /** InfluxDB */
        public static final String INFLUXDB = "influxdb";

        /** Informix */
        public static final String INFORMIX = "informix";

        /** Ingres */
        public static final String INGRES = "ingres";

        /** InstantDB */
        public static final String INSTANTDB = "instantdb";

        /** InterBase */
        public static final String INTERBASE = "interbase";

        /** MariaDB */
        public static final String MARIADB = "mariadb";

        /** SAP MaxDB */
        public static final String MAXDB = "maxdb";

        /** Memcached */
        public static final String MEMCACHED = "memcached";

        /** MongoDB */
        public static final String MONGODB = "mongodb";

        /** Microsoft SQL Server */
        public static final String MSSQL = "mssql";

        /**
         * Deprecated, Microsoft SQL Server Compact is discontinued.
         *
         * @deprecated Replaced by {@code other_sql}.
         */
        @Deprecated
        public static final String MSSQLCOMPACT = "mssqlcompact";

        /** MySQL */
        public static final String MYSQL = "mysql";

        /** Neo4j */
        public static final String NEO4J = "neo4j";

        /** Netezza */
        public static final String NETEZZA = "netezza";

        /** OpenSearch */
        public static final String OPENSEARCH = "opensearch";

        /** Oracle Database */
        public static final String ORACLE = "oracle";

        /** Pervasive PSQL */
        public static final String PERVASIVE = "pervasive";

        /** PointBase */
        public static final String POINTBASE = "pointbase";

        /** PostgreSQL */
        public static final String POSTGRESQL = "postgresql";

        /** Progress Database */
        public static final String PROGRESS = "progress";

        /** Redis */
        public static final String REDIS = "redis";

        /** Amazon Redshift */
        public static final String REDSHIFT = "redshift";

        /** Cloud Spanner */
        public static final String SPANNER = "spanner";

        /** SQLite */
        public static final String SQLITE = "sqlite";

        /** Sybase */
        public static final String SYBASE = "sybase";

        /** Teradata */
        public static final String TERADATA = "teradata";

        /** Trino */
        public static final String TRINO = "trino";

        /** Vertica */
        public static final String VERTICA = "vertica";

        private DbSystemValues() {
        }
    }

    private DbIncubatingAttributes() {
    }
}
