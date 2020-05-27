package com.microsoft.azure.table;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.http.rest.RestProxy;
import com.microsoft.azure.tables.models.ServicesGetPropertiesResponse;
import com.microsoft.azure.tables.models.ServicesGetStatisticsResponse;
import com.microsoft.azure.tables.models.ServicesSetPropertiesResponse;
import com.microsoft.azure.tables.models.TableServiceErrorException;
import com.microsoft.azure.tables.models.TableServiceProperties;
import com.microsoft.azure.tables.models.TableServiceStats;
import reactor.core.publisher.Mono;

/** An instance of this class provides access to all the operations defined in Services. */
public final class Services {
    /** The proxy service used to perform REST calls. */
    private final ServicesService service;

    /** The service client containing this operation class. */
    private final AzureTable client;

    /**
     * Initializes an instance of Services.
     *
     * @param client the instance of the service client containing this operation class.
     */
    Services(AzureTable client) {
        this.service = RestProxy.create(ServicesService.class, client.getHttpPipeline());
        this.client = client;
    }

    /**
     * The interface defining all the services for AzureTableServices to be used by the proxy service to perform REST
     * calls.
     */
    @Host("{url}")
    @ServiceInterface(name = "AzureTableServices")
    private interface ServicesService {
        @Put("/")
        @ExpectedResponses({202})
        @UnexpectedResponseExceptionType(TableServiceErrorException.class)
        Mono<ServicesSetPropertiesResponse> setProperties(
                @HostParam("url") String url,
                @QueryParam("restype") String restype,
                @QueryParam("comp") String comp,
                @QueryParam("timeout") Integer timeout,
                @HeaderParam("x-ms-version") String version,
                @HeaderParam("x-ms-client-request-id") String requestId,
                @BodyParam("application/xml") TableServiceProperties tableServiceProperties);

        @Get("/")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(TableServiceErrorException.class)
        Mono<ServicesGetPropertiesResponse> getProperties(
                @HostParam("url") String url,
                @QueryParam("restype") String restype,
                @QueryParam("comp") String comp,
                @QueryParam("timeout") Integer timeout,
                @HeaderParam("x-ms-version") String version,
                @HeaderParam("x-ms-client-request-id") String requestId);

        @Get("/")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(TableServiceErrorException.class)
        Mono<ServicesGetStatisticsResponse> getStatistics(
                @HostParam("url") String url,
                @QueryParam("restype") String restype,
                @QueryParam("comp") String comp,
                @QueryParam("timeout") Integer timeout,
                @HeaderParam("x-ms-version") String version,
                @HeaderParam("x-ms-client-request-id") String requestId);
    }

    /**
     * Sets properties for an account's Table service endpoint, including properties for Analytics and CORS
     * (Cross-Origin Resource Sharing) rules.
     *
     * @param tableServiceProperties Table Service Properties.
     * @param timeout The timeout parameter is expressed in seconds.
     * @param requestId Provides a client-generated, opaque value with a 1 KB character limit that is recorded in the
     *     analytics logs when analytics logging is enabled.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws TableServiceErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ServicesSetPropertiesResponse> setPropertiesWithResponseAsync(
            TableServiceProperties tableServiceProperties, Integer timeout, String requestId) {
        final String restype = "service";
        final String comp = "properties";
        return service.setProperties(
                this.client.getUrl(),
                restype,
                comp,
                timeout,
                this.client.getVersion(),
                requestId,
                tableServiceProperties);
    }

    /**
     * Sets properties for an account's Table service endpoint, including properties for Analytics and CORS
     * (Cross-Origin Resource Sharing) rules.
     *
     * @param tableServiceProperties Table Service Properties.
     * @param timeout The timeout parameter is expressed in seconds.
     * @param requestId Provides a client-generated, opaque value with a 1 KB character limit that is recorded in the
     *     analytics logs when analytics logging is enabled.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws TableServiceErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> setPropertiesAsync(
            TableServiceProperties tableServiceProperties, Integer timeout, String requestId) {
        return setPropertiesWithResponseAsync(tableServiceProperties, timeout, requestId)
                .flatMap((ServicesSetPropertiesResponse res) -> Mono.empty());
    }

    /**
     * Gets the properties of an account's Table service, including properties for Analytics and CORS (Cross-Origin
     * Resource Sharing) rules.
     *
     * @param timeout The timeout parameter is expressed in seconds.
     * @param requestId Provides a client-generated, opaque value with a 1 KB character limit that is recorded in the
     *     analytics logs when analytics logging is enabled.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws TableServiceErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the properties of an account's Table service, including properties for Analytics and CORS (Cross-Origin
     *     Resource Sharing) rules.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ServicesGetPropertiesResponse> getPropertiesWithResponseAsync(Integer timeout, String requestId) {
        final String restype = "service";
        final String comp = "properties";
        return service.getProperties(this.client.getUrl(), restype, comp, timeout, this.client.getVersion(), requestId);
    }

    /**
     * Gets the properties of an account's Table service, including properties for Analytics and CORS (Cross-Origin
     * Resource Sharing) rules.
     *
     * @param timeout The timeout parameter is expressed in seconds.
     * @param requestId Provides a client-generated, opaque value with a 1 KB character limit that is recorded in the
     *     analytics logs when analytics logging is enabled.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws TableServiceErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the properties of an account's Table service, including properties for Analytics and CORS (Cross-Origin
     *     Resource Sharing) rules.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TableServiceProperties> getPropertiesAsync(Integer timeout, String requestId) {
        return getPropertiesWithResponseAsync(timeout, requestId)
                .flatMap(
                        (ServicesGetPropertiesResponse res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }

    /**
     * Retrieves statistics related to replication for the Table service. It is only available on the secondary location
     * endpoint when read-access geo-redundant replication is enabled for the account.
     *
     * @param timeout The timeout parameter is expressed in seconds.
     * @param requestId Provides a client-generated, opaque value with a 1 KB character limit that is recorded in the
     *     analytics logs when analytics logging is enabled.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws TableServiceErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return stats for the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ServicesGetStatisticsResponse> getStatisticsWithResponseAsync(Integer timeout, String requestId) {
        final String restype = "service";
        final String comp = "stats";
        return service.getStatistics(this.client.getUrl(), restype, comp, timeout, this.client.getVersion(), requestId);
    }

    /**
     * Retrieves statistics related to replication for the Table service. It is only available on the secondary location
     * endpoint when read-access geo-redundant replication is enabled for the account.
     *
     * @param timeout The timeout parameter is expressed in seconds.
     * @param requestId Provides a client-generated, opaque value with a 1 KB character limit that is recorded in the
     *     analytics logs when analytics logging is enabled.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws TableServiceErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return stats for the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TableServiceStats> getStatisticsAsync(Integer timeout, String requestId) {
        return getStatisticsWithResponseAsync(timeout, requestId)
                .flatMap(
                        (ServicesGetStatisticsResponse res) -> {
                            if (res.getValue() != null) {
                                return Mono.just(res.getValue());
                            } else {
                                return Mono.empty();
                            }
                        });
    }
}
