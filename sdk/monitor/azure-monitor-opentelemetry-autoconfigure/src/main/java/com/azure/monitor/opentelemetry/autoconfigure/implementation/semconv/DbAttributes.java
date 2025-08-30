// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;

// this is a copy of io.opentelemetry.semconv.DbAttributes (1.37.0)

// DO NOT EDIT, this is an Auto-generated file from
// buildscripts/templates/registry/java/SemanticAttributes.java.j2
@SuppressWarnings("unused")
public final class DbAttributes {
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
     */
    public static final AttributeKey<String> DB_COLLECTION_NAME = stringKey("db.collection.name");

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
     */
    public static final AttributeKey<String> DB_NAMESPACE = stringKey("db.namespace");

    /**
     * The number of queries included in a batch operation.
     *
     * <p>Notes:
     *
     * <p>Operations are only considered batches when they contain two or more operations, and so
     * {@code db.operation.batch.size} SHOULD never be {@code 1}.
     */
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
     */
    public static final AttributeKey<String> DB_OPERATION_NAME = stringKey("db.operation.name");

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
     */
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
     */
    public static final AttributeKey<String> DB_QUERY_TEXT = stringKey("db.query.text");

    /**
     * Database response status code.
     *
     * <p>Notes:
     *
     * <p>The status code returned by the database. Usually it represents an error code, but may also
     * represent partial success, warning, or differentiate between various types of successful
     * outcomes. Semantic conventions for individual database systems SHOULD document what {@code
     * db.response.status_code} means in the context of that system.
     */
    public static final AttributeKey<String> DB_RESPONSE_STATUS_CODE = stringKey("db.response.status_code");

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
     */
    public static final AttributeKey<String> DB_STORED_PROCEDURE_NAME = stringKey("db.stored_procedure.name");

    /**
     * The database management system (DBMS) product as identified by the client instrumentation.
     *
     * <p>Notes:
     *
     * <p>The actual DBMS may differ from the one identified by the client. For example, when using
     * PostgreSQL client libraries to connect to a CockroachDB, the {@code db.system.name} is set to
     * {@code postgresql} based on the instrumentation's best knowledge.
     */
    public static final AttributeKey<String> DB_SYSTEM_NAME = stringKey("db.system.name");

    // Enum definition
    /** Values for {@link #DB_SYSTEM_NAME}. */
    public static final class DbSystemNameValues {
        /** <a href="https://mariadb.org/">MariaDB</a> */
        public static final String MARIADB = "mariadb";

        /** <a href="https://www.microsoft.com/sql-server">Microsoft SQL Server</a> */
        public static final String MICROSOFT_SQL_SERVER = "microsoft.sql_server";

        /** <a href="https://www.mysql.com/">MySQL</a> */
        public static final String MYSQL = "mysql";

        /** <a href="https://www.postgresql.org/">PostgreSQL</a> */
        public static final String POSTGRESQL = "postgresql";

        private DbSystemNameValues() {
        }
    }

    private DbAttributes() {
    }
}
