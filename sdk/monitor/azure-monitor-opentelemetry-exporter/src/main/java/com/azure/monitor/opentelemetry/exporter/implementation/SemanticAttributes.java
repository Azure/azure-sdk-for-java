// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

// Includes work from:
/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.azure.monitor.opentelemetry.exporter.implementation;

import io.opentelemetry.api.common.AttributeKey;

import java.util.List;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

// this is a copy of io.opentelemetry.semconv.trace.attributes.SemanticAttributes
// because the module that contains that class is not stable,
// so don't want to take a dependency on it
public final class SemanticAttributes {
    /**
     * The URL of the OpenTelemetry schema for these keys and values.
     */
    public static final String SCHEMA_URL = "https://opentelemetry.io/schemas/1.13.0";

    /**
     * The full invoked ARN as provided on the {@code Context} passed to the function ({@code
     * Lambda-Runtime-Invoked-Function-Arn} header on the {@code /runtime/invocation/next}
     * applicable).
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>This may be different from {@code faas.id} if an alias is involved.
     * </ul>
     */
    public static final AttributeKey<String> AWS_LAMBDA_INVOKED_ARN =
        stringKey("aws.lambda.invoked_arn");

    /**
     * The <a
     * href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#id">event_id</a>
     * uniquely identifies the event.
     */
    public static final AttributeKey<String> CLOUDEVENTS_EVENT_ID = stringKey("cloudevents.event_id");

    /**
     * The <a
     * href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#source-1">source</a>
     * identifies the context in which an event happened.
     */
    public static final AttributeKey<String> CLOUDEVENTS_EVENT_SOURCE =
        stringKey("cloudevents.event_source");

    /**
     * The <a
     * href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#specversion">version
     * of the CloudEvents specification</a> which the event uses.
     */
    public static final AttributeKey<String> CLOUDEVENTS_EVENT_SPEC_VERSION =
        stringKey("cloudevents.event_spec_version");

    /**
     * The <a
     * href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#type">event_type</a>
     * contains a value describing the type of event related to the originating occurrence.
     */
    public static final AttributeKey<String> CLOUDEVENTS_EVENT_TYPE =
        stringKey("cloudevents.event_type");

    /**
     * The <a
     * href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#subject">subject</a>
     * of the event in the context of the event producer (identified by source).
     */
    public static final AttributeKey<String> CLOUDEVENTS_EVENT_SUBJECT =
        stringKey("cloudevents.event_subject");

    /**
     * Parent-child Reference type
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>The causal relationship between a child Span and a parent Span.
     * </ul>
     */
    public static final AttributeKey<String> OPENTRACING_REF_TYPE = stringKey("opentracing.ref_type");

    /**
     * An identifier for the database management system (DBMS) product being used. See below for a
     * list of well-known identifiers.
     */
    public static final AttributeKey<String> DB_SYSTEM = stringKey("db.system");

    /**
     * The connection string used to connect to the database. It is recommended to remove embedded
     * credentials.
     */
    public static final AttributeKey<String> DB_CONNECTION_STRING = stringKey("db.connection_string");

    /**
     * Username for accessing the database.
     */
    public static final AttributeKey<String> DB_USER = stringKey("db.user");

    /**
     * The fully-qualified class name of the <a
     * href="https://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/">Java Database Connectivity
     * (JDBC)</a> driver used to connect.
     */
    public static final AttributeKey<String> DB_JDBC_DRIVER_CLASSNAME =
        stringKey("db.jdbc.driver_classname");

    /**
     * This attribute is used to report the name of the database being accessed. For commands that
     * switch the database, this should be set to the target database (even if the command fails).
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>In some SQL databases, the database name to be used is called &quot;schema name&quot;. In
     *       case there are multiple layers that could be considered for database name (e.g. Oracle
     *       instance name and schema name), the database name to be used is the more specific layer
     *       (e.g. Oracle schema name).
     * </ul>
     */
    public static final AttributeKey<String> DB_NAME = stringKey("db.name");

    /**
     * The database statement being executed.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>The value may be sanitized to exclude sensitive information.
     * </ul>
     */
    public static final AttributeKey<String> DB_STATEMENT = stringKey("db.statement");

    /**
     * The name of the operation being executed, e.g. the <a
     * href="https://docs.mongodb.com/manual/reference/command/#database-operations">MongoDB command
     * name</a> such as {@code findAndModify}, or the SQL keyword.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>When setting this to an SQL keyword, it is not recommended to attempt any client-side
     *       parsing of {@code db.statement} just to get this property, but it should be set if the
     *       operation name is provided by the library being instrumented. If the SQL statement has an
     *       ambiguous operation, or performs more than one operation, this value may be omitted.
     * </ul>
     */
    public static final AttributeKey<String> DB_OPERATION = stringKey("db.operation");

    /**
     * The Microsoft SQL Server <a
     * href="https://docs.microsoft.com/en-us/sql/connect/jdbc/building-the-connection-url?view=sql-server-ver15">instance
     * name</a> connecting to. This name is used to determine the port of a named instance.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>If setting a {@code db.mssql.instance_name}, {@code net.peer.port} is no longer required
     *       (but still recommended if non-standard).
     * </ul>
     */
    public static final AttributeKey<String> DB_MSSQL_INSTANCE_NAME =
        stringKey("db.mssql.instance_name");

    /**
     * The fetch size used for paging, i.e. how many rows will be returned at once.
     */
    public static final AttributeKey<Long> DB_CASSANDRA_PAGE_SIZE = longKey("db.cassandra.page_size");

    /**
     * The consistency level of the query. Based on consistency values from <a
     * href="https://docs.datastax.com/en/cassandra-oss/3.0/cassandra/dml/dmlConfigConsistency.html">CQL</a>.
     */
    public static final AttributeKey<String> DB_CASSANDRA_CONSISTENCY_LEVEL =
        stringKey("db.cassandra.consistency_level");

    /**
     * The name of the primary table that the operation is acting upon, including the keyspace name
     * (if applicable).
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>This mirrors the db.sql.table attribute but references cassandra rather than sql. It is
     *       not recommended to attempt any client-side parsing of {@code db.statement} just to get
     *       this property, but it should be set if it is provided by the library being instrumented.
     *       If the operation is acting upon an anonymous table, or more than one table, this value
     *       MUST NOT be set.
     * </ul>
     */
    public static final AttributeKey<String> DB_CASSANDRA_TABLE = stringKey("db.cassandra.table");

    /**
     * Whether or not the query is idempotent.
     */
    public static final AttributeKey<Boolean> DB_CASSANDRA_IDEMPOTENCE =
        booleanKey("db.cassandra.idempotence");

    /**
     * The number of times a query was speculatively executed. Not set or {@code 0} if the query was
     * not executed speculatively.
     */
    public static final AttributeKey<Long> DB_CASSANDRA_SPECULATIVE_EXECUTION_COUNT =
        longKey("db.cassandra.speculative_execution_count");

    /**
     * The ID of the coordinating node for a query.
     */
    public static final AttributeKey<String> DB_CASSANDRA_COORDINATOR_ID =
        stringKey("db.cassandra.coordinator.id");

    /**
     * The data center of the coordinating node for a query.
     */
    public static final AttributeKey<String> DB_CASSANDRA_COORDINATOR_DC =
        stringKey("db.cassandra.coordinator.dc");

    /**
     * The index of the database being accessed as used in the <a
     * href="https://redis.io/commands/select">{@code SELECT} command</a>, provided as an integer. To
     * be used instead of the generic {@code db.name} attribute.
     */
    public static final AttributeKey<Long> DB_REDIS_DATABASE_INDEX =
        longKey("db.redis.database_index");

    /**
     * The collection being accessed within the database stated in {@code db.name}.
     */
    public static final AttributeKey<String> DB_MONGODB_COLLECTION =
        stringKey("db.mongodb.collection");

    /**
     * The name of the primary table that the operation is acting upon, including the database name
     * (if applicable).
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>It is not recommended to attempt any client-side parsing of {@code db.statement} just to
     *       get this property, but it should be set if it is provided by the library being
     *       instrumented. If the operation is acting upon an anonymous table, or more than one table,
     *       this value MUST NOT be set.
     * </ul>
     */
    public static final AttributeKey<String> DB_SQL_TABLE = stringKey("db.sql.table");

    /**
     * The type of the exception (its fully-qualified class name, if applicable). The dynamic type of
     * the exception should be preferred over the static type in languages that support it.
     */
    public static final AttributeKey<String> EXCEPTION_TYPE = stringKey("exception.type");

    /**
     * The exception message.
     */
    public static final AttributeKey<String> EXCEPTION_MESSAGE = stringKey("exception.message");

    /**
     * A stacktrace as a string in the natural representation for the language runtime. The
     * representation is to be determined and documented by each language SIG.
     */
    public static final AttributeKey<String> EXCEPTION_STACKTRACE = stringKey("exception.stacktrace");

    /**
     * SHOULD be set to true if the exception event is recorded at a point where it is known that the
     * exception is escaping the scope of the span.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>An exception is considered to have escaped (or left) the scope of a span, if that span is
     *       ended while the exception is still logically &quot;in flight&quot;. This may be actually
     *       &quot;in flight&quot; in some languages (e.g. if the exception is passed to a Context
     *       manager's {@code __exit__} method in Python) but will usually be caught at the point of
     *       recording the exception in most languages.
     *   <li>It is usually not possible to determine at the point where an exception is thrown whether
     *       it will escape the scope of a span. However, it is trivial to know that an exception will
     *       escape, if one checks for an active exception just before ending the span, as done in the
     *       <a href="#recording-an-exception">example above</a>.
     *   <li>It follows that an exception may still escape the scope of the span even if the {@code
     *       exception.escaped} attribute was not set or set to false, since the event might have been
     *       recorded at a time where it was not clear whether the exception will escape.
     * </ul>
     */
    public static final AttributeKey<Boolean> EXCEPTION_ESCAPED = booleanKey("exception.escaped");

    /**
     * Type of the trigger which caused this function execution.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>For the server/consumer span on the incoming side, {@code faas.trigger} MUST be set.
     *   <li>Clients invoking FaaS instances usually cannot set {@code faas.trigger}, since they would
     *       typically need to look in the payload to determine the event type. If clients set it, it
     *       should be the same as the trigger that corresponding incoming would have (i.e., this has
     *       nothing to do with the underlying transport used to make the API call to invoke the
     *       lambda, which is often HTTP).
     * </ul>
     */
    public static final AttributeKey<String> FAAS_TRIGGER = stringKey("faas.trigger");

    /**
     * The execution ID of the current function execution.
     */
    public static final AttributeKey<String> FAAS_EXECUTION = stringKey("faas.execution");

    /**
     * The name of the source on which the triggering operation was performed. For example, in Cloud
     * Storage or S3 corresponds to the bucket name, and in Cosmos DB to the database name.
     */
    public static final AttributeKey<String> FAAS_DOCUMENT_COLLECTION =
        stringKey("faas.document.collection");

    /**
     * Describes the type of the operation that was performed on the data.
     */
    public static final AttributeKey<String> FAAS_DOCUMENT_OPERATION =
        stringKey("faas.document.operation");

    /**
     * A string containing the time when the data was accessed in the <a
     * href="https://www.iso.org/iso-8601-date-and-time-format.html">ISO 8601</a> format expressed in
     * <a href="https://www.w3.org/TR/NOTE-datetime">UTC</a>.
     */
    public static final AttributeKey<String> FAAS_DOCUMENT_TIME = stringKey("faas.document.time");

    /**
     * The document name/table subjected to the operation. For example, in Cloud Storage or S3 is the
     * name of the file, and in Cosmos DB the table name.
     */
    public static final AttributeKey<String> FAAS_DOCUMENT_NAME = stringKey("faas.document.name");

    /**
     * A string containing the function invocation time in the <a
     * href="https://www.iso.org/iso-8601-date-and-time-format.html">ISO 8601</a> format expressed in
     * <a href="https://www.w3.org/TR/NOTE-datetime">UTC</a>.
     */
    public static final AttributeKey<String> FAAS_TIME = stringKey("faas.time");

    /**
     * A string containing the schedule period as <a
     * href="https://docs.oracle.com/cd/E12058_01/doc/doc.1014/e12030/cron_expressions.htm">Cron
     * Expression</a>.
     */
    public static final AttributeKey<String> FAAS_CRON = stringKey("faas.cron");

    /**
     * A boolean that is true if the serverless function is executed for the first time (aka
     * cold-start).
     */
    public static final AttributeKey<Boolean> FAAS_COLDSTART = booleanKey("faas.coldstart");

    /**
     * The name of the invoked function.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>SHOULD be equal to the {@code faas.name} resource attribute of the invoked function.
     * </ul>
     */
    public static final AttributeKey<String> FAAS_INVOKED_NAME = stringKey("faas.invoked_name");

    /**
     * The cloud provider of the invoked function.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>SHOULD be equal to the {@code cloud.provider} resource attribute of the invoked function.
     * </ul>
     */
    public static final AttributeKey<String> FAAS_INVOKED_PROVIDER =
        stringKey("faas.invoked_provider");

    /**
     * The cloud region of the invoked function.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>SHOULD be equal to the {@code cloud.region} resource attribute of the invoked function.
     * </ul>
     */
    public static final AttributeKey<String> FAAS_INVOKED_REGION = stringKey("faas.invoked_region");

    /**
     * Transport protocol used. See note below.
     */
    public static final AttributeKey<String> NET_TRANSPORT = stringKey("net.transport");

    /**
     * Application layer protocol used. The value SHOULD be normalized to lowercase.
     */
    public static final AttributeKey<String> NET_APP_PROTOCOL_NAME =
        stringKey("net.app.protocol.name");

    /**
     * Version of the application layer protocol used. See note below.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>{@code net.app.protocol.version} refers to the version of the protocol used and might be
     *       different from the protocol client's version. If the HTTP client used has a version of
     *       {@code 0.27.2}, but sends HTTP version {@code 1.1}, this attribute should be set to
     *       {@code 1.1}.
     * </ul>
     */
    public static final AttributeKey<String> NET_APP_PROTOCOL_VERSION =
        stringKey("net.app.protocol.version");

    /**
     * Remote socket peer name.
     */
    public static final AttributeKey<String> NET_SOCK_PEER_NAME = stringKey("net.sock.peer.name");

    /**
     * Remote socket peer address: IPv4 or IPv6 for internet protocols, path for local communication,
     * <a href="https://man7.org/linux/man-pages/man7/address_families.7.html">etc</a>.
     */
    public static final AttributeKey<String> NET_SOCK_PEER_ADDR = stringKey("net.sock.peer.addr");

    /**
     * Remote socket peer port.
     */
    public static final AttributeKey<Long> NET_SOCK_PEER_PORT = longKey("net.sock.peer.port");

    /**
     * Protocol <a href="https://man7.org/linux/man-pages/man7/address_families.7.html">address
     * family</a> which is used for communication.
     */
    public static final AttributeKey<String> NET_SOCK_FAMILY = stringKey("net.sock.family");

    /**
     * Logical remote hostname, see note below.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>{@code net.peer.name} SHOULD NOT be set if capturing it would require an extra DNS
     *       lookup.
     * </ul>
     */
    public static final AttributeKey<String> NET_PEER_NAME = stringKey("net.peer.name");

    /**
     * Logical remote port number
     */
    public static final AttributeKey<Long> NET_PEER_PORT = longKey("net.peer.port");

    /**
     * Logical local hostname or similar, see note below.
     */
    public static final AttributeKey<String> NET_HOST_NAME = stringKey("net.host.name");

    /**
     * Logical local port number, preferably the one that the peer used to connect
     */
    public static final AttributeKey<Long> NET_HOST_PORT = longKey("net.host.port");

    /**
     * Local socket address. Useful in case of a multi-IP host.
     */
    public static final AttributeKey<String> NET_SOCK_HOST_ADDR = stringKey("net.sock.host.addr");

    /**
     * Local socket port number.
     */
    public static final AttributeKey<Long> NET_SOCK_HOST_PORT = longKey("net.sock.host.port");

    /**
     * The internet connection type currently being used by the host.
     */
    public static final AttributeKey<String> NET_HOST_CONNECTION_TYPE =
        stringKey("net.host.connection.type");

    /**
     * This describes more details regarding the connection.type. It may be the type of cell
     * technology connection, but it could be used for describing details about a wifi connection.
     */
    public static final AttributeKey<String> NET_HOST_CONNECTION_SUBTYPE =
        stringKey("net.host.connection.subtype");

    /**
     * The name of the mobile carrier.
     */
    public static final AttributeKey<String> NET_HOST_CARRIER_NAME =
        stringKey("net.host.carrier.name");

    /**
     * The mobile carrier country code.
     */
    public static final AttributeKey<String> NET_HOST_CARRIER_MCC = stringKey("net.host.carrier.mcc");

    /**
     * The mobile carrier network code.
     */
    public static final AttributeKey<String> NET_HOST_CARRIER_MNC = stringKey("net.host.carrier.mnc");

    /**
     * The ISO 3166-1 alpha-2 2-character country code associated with the mobile carrier network.
     */
    public static final AttributeKey<String> NET_HOST_CARRIER_ICC = stringKey("net.host.carrier.icc");

    /**
     * The <a href="../../resource/semantic_conventions/README.md#service">{@code service.name}</a> of
     * the remote service. SHOULD be equal to the actual {@code service.name} resource attribute of
     * the remote service if any.
     */
    public static final AttributeKey<String> PEER_SERVICE = stringKey("peer.service");

    /**
     * Username or client_id extracted from the access token or <a
     * href="https://tools.ietf.org/html/rfc7235#section-4.2">Authorization</a> header in the inbound
     * request from outside the system.
     */
    public static final AttributeKey<String> ENDUSER_ID = stringKey("enduser.id");

    /**
     * Actual/assumed role the client is making the request under extracted from token or application
     * security context.
     */
    public static final AttributeKey<String> ENDUSER_ROLE = stringKey("enduser.role");

    /**
     * Scopes or granted authorities the client currently possesses extracted from token or
     * application security context. The value would come from the scope associated with an <a
     * href="https://tools.ietf.org/html/rfc6749#section-3.3">OAuth 2.0 Access Token</a> or an
     * attribute value in a <a
     * href="http://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html">SAML
     * 2.0 Assertion</a>.
     */
    public static final AttributeKey<String> ENDUSER_SCOPE = stringKey("enduser.scope");

    /**
     * Current &quot;managed&quot; thread ID (as opposed to OS thread ID).
     */
    public static final AttributeKey<Long> THREAD_ID = longKey("thread.id");

    /**
     * Current thread name.
     */
    public static final AttributeKey<String> THREAD_NAME = stringKey("thread.name");

    /**
     * The method or function name, or equivalent (usually rightmost part of the code unit's name).
     */
    public static final AttributeKey<String> CODE_FUNCTION = stringKey("code.function");

    /**
     * The &quot;namespace&quot; within which {@code code.function} is defined. Usually the qualified
     * class or module name, such that {@code code.namespace} + some separator + {@code code.function}
     * form a unique identifier for the code unit.
     */
    public static final AttributeKey<String> CODE_NAMESPACE = stringKey("code.namespace");

    /**
     * The source code file name that identifies the code unit as uniquely as possible (preferably an
     * absolute file path).
     */
    public static final AttributeKey<String> CODE_FILEPATH = stringKey("code.filepath");

    /**
     * The line number in {@code code.filepath} best representing the operation. It SHOULD point
     * within the code unit named in {@code code.function}.
     */
    public static final AttributeKey<Long> CODE_LINENO = longKey("code.lineno");

    /**
     * HTTP request method.
     */
    public static final AttributeKey<String> HTTP_METHOD = stringKey("http.method");

    /**
     * <a href="https://tools.ietf.org/html/rfc7231#section-6">HTTP response status code</a>.
     */
    public static final AttributeKey<Long> HTTP_STATUS_CODE = longKey("http.status_code");

    /**
     * Kind of HTTP protocol used.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>If {@code net.transport} is not specified, it can be assumed to be {@code IP.TCP} except
     *       if {@code http.flavor} is {@code QUIC}, in which case {@code IP.UDP} is assumed.
     * </ul>
     */
    public static final AttributeKey<String> HTTP_FLAVOR = stringKey("http.flavor");

    /**
     * Value of the <a href="https://www.rfc-editor.org/rfc/rfc9110.html#field.user-agent">HTTP
     * User-Agent</a> header sent by the client.
     */
    public static final AttributeKey<String> HTTP_USER_AGENT = stringKey("http.user_agent");

    /**
     * The size of the request payload body in bytes. This is the number of bytes transferred
     * excluding headers and is often, but not always, present as the <a
     * href="https://www.rfc-editor.org/rfc/rfc9110.html#field.content-length">Content-Length</a>
     * header. For requests using transport encoding, this should be the compressed size.
     */
    public static final AttributeKey<Long> HTTP_REQUEST_CONTENT_LENGTH =
        longKey("http.request_content_length");

    /**
     * The size of the response payload body in bytes. This is the number of bytes transferred
     * excluding headers and is often, but not always, present as the <a
     * href="https://www.rfc-editor.org/rfc/rfc9110.html#field.content-length">Content-Length</a>
     * header. For requests using transport encoding, this should be the compressed size.
     */
    public static final AttributeKey<Long> HTTP_RESPONSE_CONTENT_LENGTH =
        longKey("http.response_content_length");

    /**
     * Full HTTP request URL in the form {@code scheme://host[:port]/path?query[#fragment]}. Usually
     * the fragment is not transmitted over HTTP, but if it is known, it should be included
     * nevertheless.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>{@code http.url} MUST NOT contain credentials passed via URL in form of {@code
     *       https://username:password@www.example.com/}. In such case the attribute's value should be
     *       {@code https://www.example.com/}.
     * </ul>
     */
    public static final AttributeKey<String> HTTP_URL = stringKey("http.url");

    /**
     * The ordinal number of request re-sending attempt.
     */
    public static final AttributeKey<Long> HTTP_RETRY_COUNT = longKey("http.retry_count");

    /**
     * The URI scheme identifying the used protocol.
     */
    public static final AttributeKey<String> HTTP_SCHEME = stringKey("http.scheme");

    /**
     * The full request target as passed in a HTTP request line or equivalent.
     */
    public static final AttributeKey<String> HTTP_TARGET = stringKey("http.target");

    /**
     * The matched route (path template in the format used by the respective server framework). See
     * note below
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>'http.route' MUST NOT be populated when this is not supported by the HTTP server
     *       framework as the route attribute should have low-cardinality and the URI path can NOT
     *       substitute it.
     * </ul>
     */
    public static final AttributeKey<String> HTTP_ROUTE = stringKey("http.route");

    /**
     * The IP address of the original client behind all proxies, if known (e.g. from <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Forwarded-For">X-Forwarded-For</a>).
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>This is not necessarily the same as {@code net.sock.peer.addr}, which would identify the
     *       network-level peer, which may be a proxy.
     *   <li>This attribute should be set when a source of information different from the one used for
     *       {@code net.sock.peer.addr}, is available even if that other source just confirms the same
     *       value as {@code net.sock.peer.addr}. Rationale: For {@code net.sock.peer.addr}, one
     *       typically does not know if it comes from a proxy, reverse proxy, or the actual client.
     *       Setting {@code http.client_ip} when it's the same as {@code net.sock.peer.addr} means
     *       that one is at least somewhat confident that the address is not that of the closest
     *       proxy.
     * </ul>
     */
    public static final AttributeKey<String> HTTP_CLIENT_IP = stringKey("http.client_ip");

    /**
     * The keys in the {@code RequestItems} object field.
     */
    public static final AttributeKey<List<String>> AWS_DYNAMODB_TABLE_NAMES =
        stringArrayKey("aws.dynamodb.table_names");

    /**
     * The JSON-serialized value of each item in the {@code ConsumedCapacity} response field.
     */
    public static final AttributeKey<List<String>> AWS_DYNAMODB_CONSUMED_CAPACITY =
        stringArrayKey("aws.dynamodb.consumed_capacity");

    /**
     * The JSON-serialized value of the {@code ItemCollectionMetrics} response field.
     */
    public static final AttributeKey<String> AWS_DYNAMODB_ITEM_COLLECTION_METRICS =
        stringKey("aws.dynamodb.item_collection_metrics");

    /**
     * The value of the {@code ProvisionedThroughput.ReadCapacityUnits} request parameter.
     */
    public static final AttributeKey<Double> AWS_DYNAMODB_PROVISIONED_READ_CAPACITY =
        doubleKey("aws.dynamodb.provisioned_read_capacity");

    /**
     * The value of the {@code ProvisionedThroughput.WriteCapacityUnits} request parameter.
     */
    public static final AttributeKey<Double> AWS_DYNAMODB_PROVISIONED_WRITE_CAPACITY =
        doubleKey("aws.dynamodb.provisioned_write_capacity");

    /**
     * The value of the {@code ConsistentRead} request parameter.
     */
    public static final AttributeKey<Boolean> AWS_DYNAMODB_CONSISTENT_READ =
        booleanKey("aws.dynamodb.consistent_read");

    /**
     * The value of the {@code ProjectionExpression} request parameter.
     */
    public static final AttributeKey<String> AWS_DYNAMODB_PROJECTION =
        stringKey("aws.dynamodb.projection");

    /**
     * The value of the {@code Limit} request parameter.
     */
    public static final AttributeKey<Long> AWS_DYNAMODB_LIMIT = longKey("aws.dynamodb.limit");

    /**
     * The value of the {@code AttributesToGet} request parameter.
     */
    public static final AttributeKey<List<String>> AWS_DYNAMODB_ATTRIBUTES_TO_GET =
        stringArrayKey("aws.dynamodb.attributes_to_get");

    /**
     * The value of the {@code IndexName} request parameter.
     */
    public static final AttributeKey<String> AWS_DYNAMODB_INDEX_NAME =
        stringKey("aws.dynamodb.index_name");

    /**
     * The value of the {@code Select} request parameter.
     */
    public static final AttributeKey<String> AWS_DYNAMODB_SELECT = stringKey("aws.dynamodb.select");

    /**
     * The JSON-serialized value of each item of the {@code GlobalSecondaryIndexes} request field
     */
    public static final AttributeKey<List<String>> AWS_DYNAMODB_GLOBAL_SECONDARY_INDEXES =
        stringArrayKey("aws.dynamodb.global_secondary_indexes");

    /**
     * The JSON-serialized value of each item of the {@code LocalSecondaryIndexes} request field.
     */
    public static final AttributeKey<List<String>> AWS_DYNAMODB_LOCAL_SECONDARY_INDEXES =
        stringArrayKey("aws.dynamodb.local_secondary_indexes");

    /**
     * The value of the {@code ExclusiveStartTableName} request parameter.
     */
    public static final AttributeKey<String> AWS_DYNAMODB_EXCLUSIVE_START_TABLE =
        stringKey("aws.dynamodb.exclusive_start_table");

    /**
     * The the number of items in the {@code TableNames} response parameter.
     */
    public static final AttributeKey<Long> AWS_DYNAMODB_TABLE_COUNT =
        longKey("aws.dynamodb.table_count");

    /**
     * The value of the {@code ScanIndexForward} request parameter.
     */
    public static final AttributeKey<Boolean> AWS_DYNAMODB_SCAN_FORWARD =
        booleanKey("aws.dynamodb.scan_forward");

    /**
     * The value of the {@code Segment} request parameter.
     */
    public static final AttributeKey<Long> AWS_DYNAMODB_SEGMENT = longKey("aws.dynamodb.segment");

    /**
     * The value of the {@code TotalSegments} request parameter.
     */
    public static final AttributeKey<Long> AWS_DYNAMODB_TOTAL_SEGMENTS =
        longKey("aws.dynamodb.total_segments");

    /**
     * The value of the {@code Count} response parameter.
     */
    public static final AttributeKey<Long> AWS_DYNAMODB_COUNT = longKey("aws.dynamodb.count");

    /**
     * The value of the {@code ScannedCount} response parameter.
     */
    public static final AttributeKey<Long> AWS_DYNAMODB_SCANNED_COUNT =
        longKey("aws.dynamodb.scanned_count");

    /**
     * The JSON-serialized value of each item in the {@code AttributeDefinitions} request field.
     */
    public static final AttributeKey<List<String>> AWS_DYNAMODB_ATTRIBUTE_DEFINITIONS =
        stringArrayKey("aws.dynamodb.attribute_definitions");

    /**
     * The JSON-serialized value of each item in the the {@code GlobalSecondaryIndexUpdates} request
     * field.
     */
    public static final AttributeKey<List<String>> AWS_DYNAMODB_GLOBAL_SECONDARY_INDEX_UPDATES =
        stringArrayKey("aws.dynamodb.global_secondary_index_updates");

    /**
     * The name of the operation being executed.
     */
    public static final AttributeKey<String> GRAPHQL_OPERATION_NAME =
        stringKey("graphql.operation.name");

    /**
     * The type of the operation being executed.
     */
    public static final AttributeKey<String> GRAPHQL_OPERATION_TYPE =
        stringKey("graphql.operation.type");

    /**
     * The GraphQL document being executed.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>The value may be sanitized to exclude sensitive information.
     * </ul>
     */
    public static final AttributeKey<String> GRAPHQL_DOCUMENT = stringKey("graphql.document");

    /**
     * A string identifying the messaging system.
     */
    public static final AttributeKey<String> MESSAGING_SYSTEM = stringKey("messaging.system");

    /**
     * The message destination name. This might be equal to the span name but is required
     * nevertheless.
     */
    public static final AttributeKey<String> MESSAGING_DESTINATION =
        stringKey("messaging.destination");

    /**
     * The kind of message destination
     */
    public static final AttributeKey<String> MESSAGING_DESTINATION_KIND =
        stringKey("messaging.destination_kind");

    /**
     * A boolean that is true if the message destination is temporary.
     */
    public static final AttributeKey<Boolean> MESSAGING_TEMP_DESTINATION =
        booleanKey("messaging.temp_destination");

    /**
     * The name of the transport protocol.
     */
    public static final AttributeKey<String> MESSAGING_PROTOCOL = stringKey("messaging.protocol");

    /**
     * The version of the transport protocol.
     */
    public static final AttributeKey<String> MESSAGING_PROTOCOL_VERSION =
        stringKey("messaging.protocol_version");

    /**
     * Connection string.
     */
    public static final AttributeKey<String> MESSAGING_URL = stringKey("messaging.url");

    /**
     * A value used by the messaging system as an identifier for the message, represented as a string.
     */
    public static final AttributeKey<String> MESSAGING_MESSAGE_ID = stringKey("messaging.message_id");

    /**
     * The <a href="#conversations">conversation ID</a> identifying the conversation to which the
     * message belongs, represented as a string. Sometimes called &quot;Correlation ID&quot;.
     */
    public static final AttributeKey<String> MESSAGING_CONVERSATION_ID =
        stringKey("messaging.conversation_id");

    /**
     * The (uncompressed) size of the message payload in bytes. Also use this attribute if it is
     * unknown whether the compressed or uncompressed payload size is reported.
     */
    public static final AttributeKey<Long> MESSAGING_MESSAGE_PAYLOAD_SIZE_BYTES =
        longKey("messaging.message_payload_size_bytes");

    /**
     * The compressed size of the message payload in bytes.
     */
    public static final AttributeKey<Long> MESSAGING_MESSAGE_PAYLOAD_COMPRESSED_SIZE_BYTES =
        longKey("messaging.message_payload_compressed_size_bytes");

    /**
     * A string identifying the kind of message consumption as defined in the <a
     * href="#operation-names">Operation names</a> section above. If the operation is
     * &quot;send&quot;, this attribute MUST NOT be set, since the operation can be inferred from the
     * span kind in that case.
     */
    public static final AttributeKey<String> MESSAGING_OPERATION = stringKey("messaging.operation");

    /**
     * The identifier for the consumer receiving a message. For Kafka, set it to {@code
     * {messaging.kafka.consumer_group} - {messaging.kafka.client_id}}, if both are present, or only
     * {@code messaging.kafka.consumer_group}. For brokers, such as RabbitMQ and Artemis, set it to
     * the {@code client_id} of the client consuming the message.
     */
    public static final AttributeKey<String> MESSAGING_CONSUMER_ID =
        stringKey("messaging.consumer_id");

    /**
     * RabbitMQ message routing key.
     */
    public static final AttributeKey<String> MESSAGING_RABBITMQ_ROUTING_KEY =
        stringKey("messaging.rabbitmq.routing_key");

    /**
     * Message keys in Kafka are used for grouping alike messages to ensure they're processed on the
     * same partition. They differ from {@code messaging.message_id} in that they're not unique. If
     * the key is {@code null}, the attribute MUST NOT be set.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>If the key type is not string, it's string representation has to be supplied for the
     *       attribute. If the key has no unambiguous, canonical string form, don't include its value.
     * </ul>
     */
    public static final AttributeKey<String> MESSAGING_KAFKA_MESSAGE_KEY =
        stringKey("messaging.kafka.message_key");

    /**
     * Name of the Kafka Consumer Group that is handling the message. Only applies to consumers, not
     * producers.
     */
    public static final AttributeKey<String> MESSAGING_KAFKA_CONSUMER_GROUP =
        stringKey("messaging.kafka.consumer_group");

    /**
     * Client Id for the Consumer or Producer that is handling the message.
     */
    public static final AttributeKey<String> MESSAGING_KAFKA_CLIENT_ID =
        stringKey("messaging.kafka.client_id");

    /**
     * Partition the message is sent to.
     */
    public static final AttributeKey<Long> MESSAGING_KAFKA_PARTITION =
        longKey("messaging.kafka.partition");

    /**
     * A boolean that is true if the message is a tombstone.
     */
    public static final AttributeKey<Boolean> MESSAGING_KAFKA_TOMBSTONE =
        booleanKey("messaging.kafka.tombstone");

    /**
     * Namespace of RocketMQ resources, resources in different namespaces are individual.
     */
    public static final AttributeKey<String> MESSAGING_ROCKETMQ_NAMESPACE =
        stringKey("messaging.rocketmq.namespace");

    /**
     * Name of the RocketMQ producer/consumer group that is handling the message. The client type is
     * identified by the SpanKind.
     */
    public static final AttributeKey<String> MESSAGING_ROCKETMQ_CLIENT_GROUP =
        stringKey("messaging.rocketmq.client_group");

    /**
     * The unique identifier for each client.
     */
    public static final AttributeKey<String> MESSAGING_ROCKETMQ_CLIENT_ID =
        stringKey("messaging.rocketmq.client_id");

    /**
     * Type of message.
     */
    public static final AttributeKey<String> MESSAGING_ROCKETMQ_MESSAGE_TYPE =
        stringKey("messaging.rocketmq.message_type");

    /**
     * The secondary classifier of message besides topic.
     */
    public static final AttributeKey<String> MESSAGING_ROCKETMQ_MESSAGE_TAG =
        stringKey("messaging.rocketmq.message_tag");

    /**
     * Key(s) of message, another way to mark message besides message id.
     */
    public static final AttributeKey<List<String>> MESSAGING_ROCKETMQ_MESSAGE_KEYS =
        stringArrayKey("messaging.rocketmq.message_keys");

    /**
     * Model of message consumption. This only applies to consumer spans.
     */
    public static final AttributeKey<String> MESSAGING_ROCKETMQ_CONSUMPTION_MODEL =
        stringKey("messaging.rocketmq.consumption_model");

    /**
     * A string identifying the remoting system. See below for a list of well-known identifiers.
     */
    public static final AttributeKey<String> RPC_SYSTEM = stringKey("rpc.system");

    /**
     * The full (logical) name of the service being called, including its package name, if applicable.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>This is the logical name of the service from the RPC interface perspective, which can be
     *       different from the name of any implementing class. The {@code code.namespace} attribute
     *       may be used to store the latter (despite the attribute name, it may include a class name;
     *       e.g., class with method actually executing the call on the server side, RPC client stub
     *       class on the client side).
     * </ul>
     */
    public static final AttributeKey<String> RPC_SERVICE = stringKey("rpc.service");

    /**
     * The name of the (logical) method being called, must be equal to the $method part in the span
     * name.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>This is the logical name of the method from the RPC interface perspective, which can be
     *       different from the name of any implementing method/function. The {@code code.function}
     *       attribute may be used to store the latter (e.g., method actually executing the call on
     *       the server side, RPC client stub method on the client side).
     * </ul>
     */
    public static final AttributeKey<String> RPC_METHOD = stringKey("rpc.method");

    /**
     * The <a href="https://github.com/grpc/grpc/blob/v1.33.2/doc/statuscodes.md">numeric status
     * code</a> of the gRPC request.
     */
    public static final AttributeKey<Long> RPC_GRPC_STATUS_CODE = longKey("rpc.grpc.status_code");

    /**
     * Protocol version as in {@code jsonrpc} property of request/response. Since JSON-RPC 1.0 does
     * not specify this, the value can be omitted.
     */
    public static final AttributeKey<String> RPC_JSONRPC_VERSION = stringKey("rpc.jsonrpc.version");

    /**
     * {@code id} property of request or response. Since protocol allows id to be int, string, {@code
     * null} or missing (for notifications), value is expected to be cast to string for simplicity.
     * Use empty string in case of {@code null} value. Omit entirely if this is a notification.
     */
    public static final AttributeKey<String> RPC_JSONRPC_REQUEST_ID =
        stringKey("rpc.jsonrpc.request_id");

    /**
     * {@code error.code} property of response if it is an error response.
     */
    public static final AttributeKey<Long> RPC_JSONRPC_ERROR_CODE = longKey("rpc.jsonrpc.error_code");

    /**
     * {@code error.message} property of response if it is an error response.
     */
    public static final AttributeKey<String> RPC_JSONRPC_ERROR_MESSAGE =
        stringKey("rpc.jsonrpc.error_message");

    /**
     * Whether this is a received or sent message.
     */
    public static final AttributeKey<String> MESSAGE_TYPE = stringKey("message.type");

    /**
     * MUST be calculated as two different counters starting from {@code 1} one for sent messages and
     * one for received message.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>This way we guarantee that the values will be consistent between different
     *       implementations.
     * </ul>
     */
    public static final AttributeKey<Long> MESSAGE_ID = longKey("message.id");

    /**
     * Compressed size of the message in bytes.
     */
    public static final AttributeKey<Long> MESSAGE_COMPRESSED_SIZE =
        longKey("message.compressed_size");

    /**
     * Uncompressed size of the message in bytes.
     */
    public static final AttributeKey<Long> MESSAGE_UNCOMPRESSED_SIZE =
        longKey("message.uncompressed_size");

    // Enum definitions
    public static final class OpentracingRefTypeValues {
        /**
         * The parent Span depends on the child Span in some capacity.
         */
        public static final String CHILD_OF = "child_of";
        /**
         * The parent Span does not depend in any way on the result of the child Span.
         */
        public static final String FOLLOWS_FROM = "follows_from";

        private OpentracingRefTypeValues() {
        }
    }

    public static final class DbSystemValues {
        /**
         * Some other SQL database. Fallback only. See notes.
         */
        public static final String OTHER_SQL = "other_sql";
        /**
         * Microsoft SQL Server.
         */
        public static final String MSSQL = "mssql";
        /**
         * MySQL.
         */
        public static final String MYSQL = "mysql";
        /**
         * Oracle Database.
         */
        public static final String ORACLE = "oracle";
        /**
         * IBM Db2.
         */
        public static final String DB2 = "db2";
        /**
         * PostgreSQL.
         */
        public static final String POSTGRESQL = "postgresql";
        /**
         * Amazon Redshift.
         */
        public static final String REDSHIFT = "redshift";
        /**
         * Apache Hive.
         */
        public static final String HIVE = "hive";
        /**
         * Cloudscape.
         */
        public static final String CLOUDSCAPE = "cloudscape";
        /**
         * HyperSQL DataBase.
         */
        public static final String HSQLDB = "hsqldb";
        /**
         * Progress Database.
         */
        public static final String PROGRESS = "progress";
        /**
         * SAP MaxDB.
         */
        public static final String MAXDB = "maxdb";
        /**
         * SAP HANA.
         */
        public static final String HANADB = "hanadb";
        /**
         * Ingres.
         */
        public static final String INGRES = "ingres";
        /**
         * FirstSQL.
         */
        public static final String FIRSTSQL = "firstsql";
        /**
         * EnterpriseDB.
         */
        public static final String EDB = "edb";
        /**
         * InterSystems Caché.
         */
        public static final String CACHE = "cache";
        /**
         * Adabas (Adaptable Database System).
         */
        public static final String ADABAS = "adabas";
        /**
         * Firebird.
         */
        public static final String FIREBIRD = "firebird";
        /**
         * Apache Derby.
         */
        public static final String DERBY = "derby";
        /**
         * FileMaker.
         */
        public static final String FILEMAKER = "filemaker";
        /**
         * Informix.
         */
        public static final String INFORMIX = "informix";
        /**
         * InstantDB.
         */
        public static final String INSTANTDB = "instantdb";
        /**
         * InterBase.
         */
        public static final String INTERBASE = "interbase";
        /**
         * MariaDB.
         */
        public static final String MARIADB = "mariadb";
        /**
         * Netezza.
         */
        public static final String NETEZZA = "netezza";
        /**
         * Pervasive PSQL.
         */
        public static final String PERVASIVE = "pervasive";
        /**
         * PointBase.
         */
        public static final String POINTBASE = "pointbase";
        /**
         * SQLite.
         */
        public static final String SQLITE = "sqlite";
        /**
         * Sybase.
         */
        public static final String SYBASE = "sybase";
        /**
         * Teradata.
         */
        public static final String TERADATA = "teradata";
        /**
         * Vertica.
         */
        public static final String VERTICA = "vertica";
        /**
         * H2.
         */
        public static final String H2 = "h2";
        /**
         * ColdFusion IMQ.
         */
        public static final String COLDFUSION = "coldfusion";
        /**
         * Apache Cassandra.
         */
        public static final String CASSANDRA = "cassandra";
        /**
         * Apache HBase.
         */
        public static final String HBASE = "hbase";
        /**
         * MongoDB.
         */
        public static final String MONGODB = "mongodb";
        /**
         * Redis.
         */
        public static final String REDIS = "redis";
        /**
         * Couchbase.
         */
        public static final String COUCHBASE = "couchbase";
        /**
         * CouchDB.
         */
        public static final String COUCHDB = "couchdb";
        /**
         * Microsoft Azure Cosmos DB.
         */
        public static final String COSMOSDB = "cosmosdb";
        /**
         * Amazon DynamoDB.
         */
        public static final String DYNAMODB = "dynamodb";
        /**
         * Neo4j.
         */
        public static final String NEO4J = "neo4j";
        /**
         * Apache Geode.
         */
        public static final String GEODE = "geode";
        /**
         * Elasticsearch.
         */
        public static final String ELASTICSEARCH = "elasticsearch";
        /**
         * Memcached.
         */
        public static final String MEMCACHED = "memcached";
        /**
         * CockroachDB.
         */
        public static final String COCKROACHDB = "cockroachdb";
        /**
         * OpenSearch.
         */
        public static final String OPENSEARCH = "opensearch";

        private DbSystemValues() {
        }
    }

    public static final class DbCassandraConsistencyLevelValues {
        /**
         * all.
         */
        public static final String ALL = "all";
        /**
         * each_quorum.
         */
        public static final String EACH_QUORUM = "each_quorum";
        /**
         * quorum.
         */
        public static final String QUORUM = "quorum";
        /**
         * local_quorum.
         */
        public static final String LOCAL_QUORUM = "local_quorum";
        /**
         * one.
         */
        public static final String ONE = "one";
        /**
         * two.
         */
        public static final String TWO = "two";
        /**
         * three.
         */
        public static final String THREE = "three";
        /**
         * local_one.
         */
        public static final String LOCAL_ONE = "local_one";
        /**
         * any.
         */
        public static final String ANY = "any";
        /**
         * serial.
         */
        public static final String SERIAL = "serial";
        /**
         * local_serial.
         */
        public static final String LOCAL_SERIAL = "local_serial";

        private DbCassandraConsistencyLevelValues() {
        }
    }

    public static final class FaasTriggerValues {
        /**
         * A response to some data source operation such as a database or filesystem read/write.
         */
        public static final String DATASOURCE = "datasource";
        /**
         * To provide an answer to an inbound HTTP request.
         */
        public static final String HTTP = "http";
        /**
         * A function is set to be executed when messages are sent to a messaging system.
         */
        public static final String PUBSUB = "pubsub";
        /**
         * A function is scheduled to be executed regularly.
         */
        public static final String TIMER = "timer";
        /**
         * If none of the others apply.
         */
        public static final String OTHER = "other";

        private FaasTriggerValues() {
        }
    }

    public static final class FaasDocumentOperationValues {
        /**
         * When a new object is created.
         */
        public static final String INSERT = "insert";
        /**
         * When an object is modified.
         */
        public static final String EDIT = "edit";
        /**
         * When an object is deleted.
         */
        public static final String DELETE = "delete";

        private FaasDocumentOperationValues() {
        }
    }

    public static final class FaasInvokedProviderValues {
        /**
         * Alibaba Cloud.
         */
        public static final String ALIBABA_CLOUD = "alibaba_cloud";
        /**
         * Amazon Web Services.
         */
        public static final String AWS = "aws";
        /**
         * Microsoft Azure.
         */
        public static final String AZURE = "azure";
        /**
         * Google Cloud Platform.
         */
        public static final String GCP = "gcp";
        /**
         * Tencent Cloud.
         */
        public static final String TENCENT_CLOUD = "tencent_cloud";

        private FaasInvokedProviderValues() {
        }
    }

    public static final class NetTransportValues {
        /**
         * ip_tcp.
         */
        public static final String IP_TCP = "ip_tcp";
        /**
         * ip_udp.
         */
        public static final String IP_UDP = "ip_udp";
        /**
         * Named or anonymous pipe. See note below.
         */
        public static final String PIPE = "pipe";
        /**
         * In-process communication.
         */
        public static final String INPROC = "inproc";
        /**
         * Something else (non IP-based).
         */
        public static final String OTHER = "other";
        /**
         * @deprecated This item has been removed as of 1.13.0 of the semantic conventions.
         */
        @Deprecated
        public static final String IP = "ip";
        /**
         * @deprecated This item has been removed as of 1.13.0 of the semantic conventions.
         */
        @Deprecated
        public static final String UNIX = "unix";

        private NetTransportValues() {
        }
    }

    public static final class NetSockFamilyValues {
        /**
         * IPv4 address.
         */
        public static final String INET = "inet";
        /**
         * IPv6 address.
         */
        public static final String INET6 = "inet6";
        /**
         * Unix domain socket path.
         */
        public static final String UNIX = "unix";

        private NetSockFamilyValues() {
        }
    }

    public static final class NetHostConnectionTypeValues {
        /**
         * wifi.
         */
        public static final String WIFI = "wifi";
        /**
         * wired.
         */
        public static final String WIRED = "wired";
        /**
         * cell.
         */
        public static final String CELL = "cell";
        /**
         * unavailable.
         */
        public static final String UNAVAILABLE = "unavailable";
        /**
         * unknown.
         */
        public static final String UNKNOWN = "unknown";

        private NetHostConnectionTypeValues() {
        }
    }

    public static final class NetHostConnectionSubtypeValues {
        /**
         * GPRS.
         */
        public static final String GPRS = "gprs";
        /**
         * EDGE.
         */
        public static final String EDGE = "edge";
        /**
         * UMTS.
         */
        public static final String UMTS = "umts";
        /**
         * CDMA.
         */
        public static final String CDMA = "cdma";
        /**
         * EVDO Rel. 0.
         */
        public static final String EVDO_0 = "evdo_0";
        /**
         * EVDO Rev. A.
         */
        public static final String EVDO_A = "evdo_a";
        /**
         * CDMA2000 1XRTT.
         */
        public static final String CDMA2000_1XRTT = "cdma2000_1xrtt";
        /**
         * HSDPA.
         */
        public static final String HSDPA = "hsdpa";
        /**
         * HSUPA.
         */
        public static final String HSUPA = "hsupa";
        /**
         * HSPA.
         */
        public static final String HSPA = "hspa";
        /**
         * IDEN.
         */
        public static final String IDEN = "iden";
        /**
         * EVDO Rev. B.
         */
        public static final String EVDO_B = "evdo_b";
        /**
         * LTE.
         */
        public static final String LTE = "lte";
        /**
         * EHRPD.
         */
        public static final String EHRPD = "ehrpd";
        /**
         * HSPAP.
         */
        public static final String HSPAP = "hspap";
        /**
         * GSM.
         */
        public static final String GSM = "gsm";
        /**
         * TD-SCDMA.
         */
        public static final String TD_SCDMA = "td_scdma";
        /**
         * IWLAN.
         */
        public static final String IWLAN = "iwlan";
        /**
         * 5G NR (New Radio).
         */
        public static final String NR = "nr";
        /**
         * 5G NRNSA (New Radio Non-Standalone).
         */
        public static final String NRNSA = "nrnsa";
        /**
         * LTE CA.
         */
        public static final String LTE_CA = "lte_ca";

        private NetHostConnectionSubtypeValues() {
        }
    }

    public static final class HttpFlavorValues {
        /**
         * HTTP/1.0.
         */
        public static final String HTTP_1_0 = "1.0";
        /**
         * HTTP/1.1.
         */
        public static final String HTTP_1_1 = "1.1";
        /**
         * HTTP/2.
         */
        public static final String HTTP_2_0 = "2.0";
        /**
         * HTTP/3.
         */
        public static final String HTTP_3_0 = "3.0";
        /**
         * SPDY protocol.
         */
        public static final String SPDY = "SPDY";
        /**
         * QUIC protocol.
         */
        public static final String QUIC = "QUIC";

        private HttpFlavorValues() {
        }
    }

    public static final class GraphqlOperationTypeValues {
        /**
         * GraphQL query.
         */
        public static final String QUERY = "query";
        /**
         * GraphQL mutation.
         */
        public static final String MUTATION = "mutation";
        /**
         * GraphQL subscription.
         */
        public static final String SUBSCRIPTION = "subscription";

        private GraphqlOperationTypeValues() {
        }
    }

    public static final class MessagingDestinationKindValues {
        /**
         * A message sent to a queue.
         */
        public static final String QUEUE = "queue";
        /**
         * A message sent to a topic.
         */
        public static final String TOPIC = "topic";

        private MessagingDestinationKindValues() {
        }
    }

    public static final class MessagingOperationValues {
        /**
         * receive.
         */
        public static final String RECEIVE = "receive";
        /**
         * process.
         */
        public static final String PROCESS = "process";

        private MessagingOperationValues() {
        }
    }

    public static final class MessagingRocketmqMessageTypeValues {
        /**
         * Normal message.
         */
        public static final String NORMAL = "normal";
        /**
         * FIFO message.
         */
        public static final String FIFO = "fifo";
        /**
         * Delay message.
         */
        public static final String DELAY = "delay";
        /**
         * Transaction message.
         */
        public static final String TRANSACTION = "transaction";

        private MessagingRocketmqMessageTypeValues() {
        }
    }

    public static final class MessagingRocketmqConsumptionModelValues {
        /**
         * Clustering consumption model.
         */
        public static final String CLUSTERING = "clustering";
        /**
         * Broadcasting consumption model.
         */
        public static final String BROADCASTING = "broadcasting";

        private MessagingRocketmqConsumptionModelValues() {
        }
    }

    public static final class RpcSystemValues {
        /**
         * gRPC.
         */
        public static final String GRPC = "grpc";
        /**
         * Java RMI.
         */
        public static final String JAVA_RMI = "java_rmi";
        /**
         * .NET WCF.
         */
        public static final String DOTNET_WCF = "dotnet_wcf";
        /**
         * Apache Dubbo.
         */
        public static final String APACHE_DUBBO = "apache_dubbo";

        private RpcSystemValues() {
        }
    }

    public static final class RpcGrpcStatusCodeValues {
        /**
         * OK.
         */
        public static final long OK = 0;
        /**
         * CANCELLED.
         */
        public static final long CANCELLED = 1;
        /**
         * UNKNOWN.
         */
        public static final long UNKNOWN = 2;
        /**
         * INVALID_ARGUMENT.
         */
        public static final long INVALID_ARGUMENT = 3;
        /**
         * DEADLINE_EXCEEDED.
         */
        public static final long DEADLINE_EXCEEDED = 4;
        /**
         * NOT_FOUND.
         */
        public static final long NOT_FOUND = 5;
        /**
         * ALREADY_EXISTS.
         */
        public static final long ALREADY_EXISTS = 6;
        /**
         * PERMISSION_DENIED.
         */
        public static final long PERMISSION_DENIED = 7;
        /**
         * RESOURCE_EXHAUSTED.
         */
        public static final long RESOURCE_EXHAUSTED = 8;
        /**
         * FAILED_PRECONDITION.
         */
        public static final long FAILED_PRECONDITION = 9;
        /**
         * ABORTED.
         */
        public static final long ABORTED = 10;
        /**
         * OUT_OF_RANGE.
         */
        public static final long OUT_OF_RANGE = 11;
        /**
         * UNIMPLEMENTED.
         */
        public static final long UNIMPLEMENTED = 12;
        /**
         * INTERNAL.
         */
        public static final long INTERNAL = 13;
        /**
         * UNAVAILABLE.
         */
        public static final long UNAVAILABLE = 14;
        /**
         * DATA_LOSS.
         */
        public static final long DATA_LOSS = 15;
        /**
         * UNAUTHENTICATED.
         */
        public static final long UNAUTHENTICATED = 16;

        private RpcGrpcStatusCodeValues() {
        }
    }

    public static final class MessageTypeValues {
        /**
         * sent.
         */
        public static final String SENT = "SENT";
        /**
         * received.
         */
        public static final String RECEIVED = "RECEIVED";

        private MessageTypeValues() {
        }
    }

    // Manually defined and not YET in the YAML
    /**
     * The name of an event describing an exception.
     *
     * <p>Typically an event with that name should not be manually created. Instead {@link
     * io.opentelemetry.api.trace.Span#recordException(Throwable)} should be used.
     */
    public static final String EXCEPTION_EVENT_NAME = "exception";

    /**
     * The name of the keyspace being accessed.
     *
     * @deprecated this item has been removed as of 1.8.0 of the semantic conventions. Please use
     * {@link SemanticAttributes#DB_NAME} instead.
     */
    @Deprecated
    public static final AttributeKey<String> DB_CASSANDRA_KEYSPACE =
        stringKey("db.cassandra.keyspace");

    /**
     * The <a href="https://hbase.apache.org/book.html#_namespace">HBase namespace</a> being accessed.
     *
     * @deprecated this item has been removed as of 1.8.0 of the semantic conventions. Please use
     * {@link SemanticAttributes#DB_NAME} instead.
     */
    @Deprecated
    public static final AttributeKey<String> DB_HBASE_NAMESPACE = stringKey("db.hbase.namespace");

    /**
     * The size of the uncompressed request payload body after transport decoding. Not set if
     * transport encoding not used.
     *
     * @deprecated this item has been removed as of 1.13.0 of the semantic conventions. Please use
     * {@link SemanticAttributes#HTTP_REQUEST_CONTENT_LENGTH} instead.
     */
    @Deprecated
    public static final AttributeKey<Long> HTTP_REQUEST_CONTENT_LENGTH_UNCOMPRESSED =
        longKey("http.request_content_length_uncompressed");

    /**
     * @deprecated This item has been removed as of 1.13.0 of the semantic conventions. Please use
     * {@link SemanticAttributes#HTTP_RESPONSE_CONTENT_LENGTH} instead.
     */
    @Deprecated
    public static final AttributeKey<Long> HTTP_RESPONSE_CONTENT_LENGTH_UNCOMPRESSED =
        longKey("http.response_content_length_uncompressed");

    /**
     * @deprecated This item has been removed as of 1.13.0 of the semantic conventions. Please use
     * {@link SemanticAttributes#NET_HOST_NAME} instead.
     */
    @Deprecated
    public static final AttributeKey<String> HTTP_SERVER_NAME = stringKey("http.server_name");

    /**
     * @deprecated This item has been removed as of 1.13.0 of the semantic conventions. Please use
     * {@link SemanticAttributes#NET_HOST_NAME} instead.
     */
    @Deprecated
    public static final AttributeKey<String> HTTP_HOST = stringKey("http.host");

    /**
     * @deprecated This item has been removed as of 1.13.0 of the semantic conventions.
     */
    @Deprecated
    public static final AttributeKey<String> NET_PEER_IP = stringKey("net.peer.ip");

    /**
     * @deprecated This item has been removed as of 1.13.0 of the semantic conventions.
     */
    @Deprecated
    public static final AttributeKey<String> NET_HOST_IP = stringKey("net.host.ip");

    private SemanticAttributes() {
    }
}
