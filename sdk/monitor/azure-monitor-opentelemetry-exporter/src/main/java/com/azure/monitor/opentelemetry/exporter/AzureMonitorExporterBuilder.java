// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.client.traits.ConnectionStringTrait;
import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.LogDataMapper;
import com.azure.monitor.opentelemetry.exporter.implementation.MetricDataMapper;
import com.azure.monitor.opentelemetry.exporter.implementation.NoopTracer;
import com.azure.monitor.opentelemetry.exporter.implementation.SpanDataMapper;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.AbstractTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.StatsbeatConnectionString;
import com.azure.monitor.opentelemetry.exporter.implementation.heartbeat.HeartbeatExporter;
import com.azure.monitor.opentelemetry.exporter.implementation.localstorage.LocalStorageStats;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemExporter;
import com.azure.monitor.opentelemetry.exporter.implementation.statsbeat.Feature;
import com.azure.monitor.opentelemetry.exporter.implementation.statsbeat.StatsbeatModule;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorHelper;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.PropertyHelper;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.TempDirs;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.VersionGenerator;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.ResourceParser;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Low level API to create OpenTelemetry span, log record and metric exporters for Azure. With OpenTelemetry autoconfiguration ({@link AutoConfiguredOpenTelemetrySdkBuilder}), we recommend using {@link AzureMonitorCustomizer}.
 */
public final class AzureMonitorExporterBuilder implements ConnectionStringTrait<AzureMonitorExporterBuilder>,
    TokenCredentialTrait<AzureMonitorExporterBuilder>, HttpTrait<AzureMonitorExporterBuilder> {

    private static final ClientLogger LOGGER = new ClientLogger(AzureMonitorExporterBuilder.class);

    private static final String APPLICATIONINSIGHTS_CONNECTION_STRING = "APPLICATIONINSIGHTS_CONNECTION_STRING";
    private static final String APPLICATIONINSIGHTS_AUTHENTICATION_SCOPE = "https://monitor.azure.com//.default";

    private static final String STATSBEAT_LONG_INTERVAL_SECONDS_PROPERTY_NAME
        = "STATSBEAT_LONG_INTERVAL_SECONDS_PROPERTY_NAME";
    private static final String STATSBEAT_SHORT_INTERVAL_SECONDS_PROPERTY_NAME
        = "STATSBEAT_SHORT_INTERVAL_SECONDS_PROPERTY_NAME";

    private static final Map<String, String> PROPERTIES
        = CoreUtils.getProperties("azure-monitor-opentelemetry-exporter.properties");

    private final ConfigProperties configProperties;

    private ConnectionString connectionString;
    private TokenCredential credential;

    private HttpPipeline httpPipeline;
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private final List<HttpPipelinePolicy> httpPipelinePolicies = new ArrayList<>();
    private ClientOptions clientOptions;
    private RetryOptions retryOptions;

    private boolean frozen;

    // this is only populated after the builder is frozen
    private TelemetryItemExporter builtTelemetryItemExporter;

    // this is only populated after the builder is frozen
    private StatsbeatModule statsbeatModule;

    /**
     * Creates an instance of {@link AzureMonitorExporterBuilder}.
     */
    public AzureMonitorExporterBuilder() {
        this.configProperties = DefaultConfigProperties.create(Collections.emptyMap());
    }

    AzureMonitorExporterBuilder(Map<String, String> testConfiguration) {
        this.configProperties = DefaultConfigProperties.create(testConfiguration);
    }

    /**
     * Sets the {@link HttpPipeline} to use for the service client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    @Override
    public AzureMonitorExporterBuilder pipeline(HttpPipeline pipeline) {
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "httpPipeline cannot be changed after any of the build methods have been called"));
        }
        this.httpPipeline = pipeline;
        return this;
    }

    /**
     * Sets the {@link HttpClient} to use for sending and receiving requests to and from the service.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param httpClient The {@link HttpClient} to use for requests.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    @Override
    public AzureMonitorExporterBuilder httpClient(HttpClient httpClient) {
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "httpClient cannot be changed after any of the build methods have been called"));
        }
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions logging configuration} to use when sending and receiving requests to and from
     * the service. If a {@code logLevel} is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param logOptions The {@link HttpLogOptions logging configuration} to use when sending and receiving requests to
     * and from the service.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    @Override
    public AzureMonitorExporterBuilder httpLogOptions(HttpLogOptions logOptions) {
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "httpLogOptions cannot be changed after any of the build methods have been called"));
        }
        this.httpLogOptions = logOptions;
        return this;
    }

    /**
     * Adds a {@link HttpPipelinePolicy pipeline policy} to apply on each request sent.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param pipelinePolicy A {@link HttpPipelinePolicy pipeline policy}.
     * @throws NullPointerException If {@code pipelinePolicy} is {@code null}.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    @Override
    public AzureMonitorExporterBuilder addPolicy(HttpPipelinePolicy pipelinePolicy) {
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "httpPipelinePolicy cannot be added after any of the build methods have been called"));
        }
        httpPipelinePolicies.add(Objects.requireNonNull(pipelinePolicy, "'policy' cannot be null."));
        return this;
    }

    /**
     * Sets the {@link RetryOptions} for all the requests made through the client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if an {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, an HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param retryOptions The {@link RetryOptions} to use for all the requests made through the client.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    @Override
    public AzureMonitorExporterBuilder retryOptions(RetryOptions retryOptions) {
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "retryOptions cannot be changed after any of the build methods have been called"));
        }
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Allows for setting common properties such as application ID, headers, proxy configuration, etc. Note that it is
     * recommended that this method be called with an instance of the {@link HttpClientOptions}
     * class (a subclass of the {@link ClientOptions} base class). The HttpClientOptions subclass provides more
     * configuration options suitable for HTTP clients, which is applicable for any class that implements this HttpTrait
     * interface.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param clientOptions A configured instance of {@link HttpClientOptions}.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     * @see HttpClientOptions
     */
    @Override
    public AzureMonitorExporterBuilder clientOptions(ClientOptions clientOptions) {
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "clientOptions cannot be changed after any of the build methods have been called"));
        }
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Sets the connection string to use for exporting telemetry events to Azure Monitor.
     *
     * @param connectionString The connection string for the Azure Monitor resource.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     * @throws NullPointerException If the connection string is {@code null}.
     * @throws IllegalArgumentException If the connection string is invalid.
     */
    @Override
    public AzureMonitorExporterBuilder connectionString(String connectionString) {
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "connectionString cannot be changed after any of the build methods have been called"));
        }
        this.connectionString = ConnectionString.parse(connectionString);
        return this;
    }

    /**
     * Sets the token credential required for authentication with the ingestion endpoint service.
     *
     * @param credential The Azure Identity TokenCredential.
     * @return The updated {@link AzureMonitorExporterBuilder} object.
     */
    @Override
    public AzureMonitorExporterBuilder credential(TokenCredential credential) {
        if (frozen) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "credential cannot be changed after any of the build methods have been called"));
        }
        this.credential = credential;
        return this;
    }

    /**
     * Creates an Azure Monitor span exporter based on the options set in the builder. This
     * exporter is an implementation of OpenTelemetry {@link SpanExporter}.
     *
     * @return An instance of {@link SpanExporter}.
     * @throws NullPointerException if the connection string is not set on this builder or if the
     * environment variable "APPLICATIONINSIGHTS_CONNECTION_STRING" is not set.
     */
    public SpanExporter buildSpanExporter() {
        internalBuildAndFreeze();
        return new AzureMonitorTraceExporter(createSpanDataMapper(configProperties), builtTelemetryItemExporter,
            statsbeatModule);
    }

    /**
     * Creates an Azure Monitor log record exporter based on the options set in the builder. This
     * exporter is an implementation of OpenTelemetry {@link LogRecordExporter}.
     *
     * @return An instance of {@link LogRecordExporter}.
     * @throws NullPointerException if the connection string is not set on this builder or if the
     * environment variable "APPLICATIONINSIGHTS_CONNECTION_STRING" is not set.
     */
    public LogRecordExporter buildLogRecordExporter() {
        internalBuildAndFreeze();
        return new AzureMonitorLogRecordExporter(
            new LogDataMapper(true, false, createDefaultsPopulator(configProperties)), builtTelemetryItemExporter);
    }

    void internalBuildAndFreeze() {
        if (!frozen) {
            HttpPipeline httpPipeline = createHttpPipeline();
            statsbeatModule = initStatsbeatModule(configProperties);
            File tempDir = TempDirs.getApplicationInsightsTempDir(LOGGER,
                "Telemetry will not be stored to disk and retried on sporadic network failures");
            // TODO (heya) change LocalStorageStats.noop() to statsbeatModule.getNonessentialStatsbeat() when we decide to collect non-essential Statsbeat by default.
            builtTelemetryItemExporter = AzureMonitorHelper.createTelemetryItemExporter(httpPipeline, statsbeatModule,
                tempDir, LocalStorageStats.noop());
            startStatsbeatModule(statsbeatModule, configProperties, tempDir); // wait till TelemetryItemExporter has been initialized before starting StatsbeatModule
            frozen = true;
        }
    }

    DefaultConfigProperties getConfigProperties() {
        return DefaultConfigProperties.create(Collections.emptyMap());
    }

    /**
     * Creates an Azure monitor metric exporter based on the options set in the builder. This
     * exporter is an implementation of OpenTelemetry {@link MetricExporter}.
     *
     * <p>When a new {@link MetricExporter} is created, it will automatically start {@link
     * HeartbeatExporter}.
     *
     * @return An instance of {@link MetricExporter}.
     * @throws NullPointerException if the connection string is not set on this builder or if the
     * environment variable "APPLICATIONINSIGHTS_CONNECTION_STRING" is not set.
     */
    public MetricExporter buildMetricExporter() {
        internalBuildAndFreeze();
        DefaultConfigProperties configProperties1 = getConfigProperties();
        HeartbeatExporter.start(MINUTES.toSeconds(15), createDefaultsPopulator(configProperties1),
            builtTelemetryItemExporter::send);
        return new AzureMonitorMetricExporter(new MetricDataMapper(createDefaultsPopulator(configProperties1), true),
            builtTelemetryItemExporter);
    }

    private Set<Feature> initStatsbeatFeatures() {
        if (System.getProperty("org.graalvm.nativeimage.imagecode") != null) {
            return Collections.singleton(Feature.GRAAL_VM_NATIVE);
        }
        return Collections.emptySet();
    }

    private StatsbeatConnectionString getStatsbeatConnectionString() {
        return StatsbeatConnectionString.create(connectionString, null, null);
    }

    private SpanDataMapper createSpanDataMapper(ConfigProperties configProperties) {
        return new SpanDataMapper(true, createDefaultsPopulator(configProperties),
            (event, instrumentationName) -> false, (span, event) -> false);
    }

    private BiConsumer<AbstractTelemetryBuilder, Resource> createDefaultsPopulator(ConfigProperties configProperties) {
        ConnectionString connectionString = getConnectionString(configProperties);
        ResourceParser resourceParser = new ResourceParser();
        return (builder, resource) -> {
            builder.setConnectionString(connectionString);
            builder.setResource(resource);
            builder.addTag(ContextTagKeys.AI_INTERNAL_SDK_VERSION.toString(), VersionGenerator.getSdkVersion());
            // TODO (trask) unify these
            resourceParser.updateRoleNameAndInstance(builder, resource);
        };
    }

    private ConnectionString getConnectionString(ConfigProperties configProperties) {
        if (connectionString != null) {
            return connectionString;
        }
        ConnectionString connectionString
            = ConnectionString.parse(configProperties.getString(APPLICATIONINSIGHTS_CONNECTION_STRING));
        return Objects.requireNonNull(connectionString, "'connectionString' cannot be null");
    }

    private HttpPipeline createHttpPipeline() {

        if (httpPipeline != null) {
            if (credential != null) {
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("'credential' is not supported when custom 'httpPipeline' is specified"));
            }
            if (httpClient != null) {
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("'httpClient' is not supported when custom 'httpPipeline' is specified"));
            }
            if (httpLogOptions != null) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "'httpLogOptions' is not supported when custom 'httpPipeline' is specified"));
            }
            if (!httpPipelinePolicies.isEmpty()) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "'httpPipelinePolicies' is not supported when custom 'httpPipeline' is specified"));
            }
            if (clientOptions != null) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "'clientOptions' is not supported when custom 'httpPipeline' is specified"));
            }
            if (retryOptions != null) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "'retryOptions' is not supported when custom 'httpPipeline' is specified"));
            }
            return httpPipeline;
        }

        List<HttpPipelinePolicy> policies = new ArrayList<>();
        String clientName = PROPERTIES.getOrDefault("name", "UnknownName");
        String clientVersion = PROPERTIES.getOrDefault("version", "UnknownVersion");

        String applicationId = CoreUtils.getApplicationId(clientOptions, httpLogOptions);

        policies
            .add(new UserAgentPolicy(applicationId, clientName, clientVersion, Configuration.getGlobalConfiguration()));
        policies.add(new CookiePolicy());
        if (credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(credential, APPLICATIONINSIGHTS_AUTHENTICATION_SCOPE));
        }

        if (retryOptions != null) {
            policies.add(new RetryPolicy(retryOptions));
        }

        policies.addAll(httpPipelinePolicies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));
        return new com.azure.core.http.HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .tracer(new NoopTracer())
            .build();
    }

    private StatsbeatModule initStatsbeatModule(ConfigProperties configProperties) {
        return new StatsbeatModule(PropertyHelper::lazyUpdateVmRpIntegration);
    }

    private void startStatsbeatModule(StatsbeatModule statsbeatModule, ConfigProperties configProperties,
        File tempDir) {
        HttpPipeline statsbeatHttpPipeline = createStatsbeatHttpPipeline();
        TelemetryItemExporter statsbeatTelemetryItemExporter
            = AzureMonitorHelper.createStatsbeatTelemetryItemExporter(statsbeatHttpPipeline, statsbeatModule, tempDir);

        statsbeatModule.start(statsbeatTelemetryItemExporter, this::getStatsbeatConnectionString,
            getConnectionString(configProperties)::getInstrumentationKey, false,
            configProperties.getLong(STATSBEAT_SHORT_INTERVAL_SECONDS_PROPERTY_NAME, MINUTES.toSeconds(15)), // Statsbeat short interval
            configProperties.getLong(STATSBEAT_LONG_INTERVAL_SECONDS_PROPERTY_NAME, DAYS.toSeconds(1)), // Statsbeat long interval
            false, initStatsbeatFeatures());
    }

    private HttpPipeline createStatsbeatHttpPipeline() {
        if (httpPipeline != null) {
            return httpPipeline;
        }

        List<HttpPipelinePolicy> policies = new ArrayList<>();
        String clientName = PROPERTIES.getOrDefault("name", "UnknownName");
        String clientVersion = PROPERTIES.getOrDefault("version", "UnknownVersion");

        String applicationId = CoreUtils.getApplicationId(clientOptions, httpLogOptions);

        policies
            .add(new UserAgentPolicy(applicationId, clientName, clientVersion, Configuration.getGlobalConfiguration()));
        policies.add(new CookiePolicy());
        policies.addAll(httpPipelinePolicies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));
        return new com.azure.core.http.HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .tracer(new NoopTracer())
            .build();
    }
}
