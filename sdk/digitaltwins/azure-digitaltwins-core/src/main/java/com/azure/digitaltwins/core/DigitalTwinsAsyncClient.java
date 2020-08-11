package com.azure.digitaltwins.core;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.digitaltwins.core.models.DigitalTwinsGetByIdResponse;
import reactor.core.publisher.Mono;


/**
 * This class provides a client for interacting asynchronously with an Azure Digital Twins instance.
 *
 * <p>
 * This client is instantiated through {@link DigitalTwinsClientBuilder}.
 * </p>
 *
 * <p>
 * This client allows for management of digital twins, their components, and their relationships. It also allows for managing
 * the digital twin models and event routes tied to your Azure Digital Twins instance.
 * </p>
 */
@ServiceClient(builder = DigitalTwinsClientBuilder.class)
public class DigitalTwinsAsyncClient {
    protected final DigitalTwinsServiceVersion serviceVersion;

    protected final AzureDigitalTwinsAPI protocolLayer;

    protected DigitalTwinsAsyncClient(HttpPipeline pipeline, DigitalTwinsServiceVersion serviceVersion, String host) {
        this.protocolLayer = new AzureDigitalTwinsAPIBuilder().host(host).pipeline(pipeline).buildClient();
        this.serviceVersion = serviceVersion;
    }

    /**
     * Gets the Azure Digital Twins service API version that this client is configured to use for all service requests.
     * Unless configured while building this client through {@link DigitalTwinsClientBuilder#serviceVersion(DigitalTwinsServiceVersion)},
     * this value will be equal to the latest service API version supported by this client.
     *
     * @return The Azure Digital Twins service API version.
     */
    public DigitalTwinsServiceVersion getServiceVersion() {
        return this.serviceVersion;
    }

    /**
     * Gets the {@link HttpPipeline} that this client is configured to use for all service requests. This pipeline can
     * be customized while building this client through {@link DigitalTwinsClientBuilder#httpPipeline(HttpPipeline)}.
     *
     * @return The {@link HttpPipeline} that this client uses for all service requests.
     */
    public HttpPipeline getHttpPipeline() {
        return this.protocolLayer.getHttpPipeline();
    }

    // TODO This is just a temporary implementation for sample purposes. This should be spruced up/replaced once this API is actually designed
    public Mono<DigitalTwinsGetByIdResponse> getDigitalTwin(String digitalTwinId) {
        //TODO the java track 2 guidelines say that this type of API should return Response<Object>, but the generated code doesn't expose this. Need to talk to autorest
        // team if this is intentional or not. DigitalTwinsGetByIdResponse type is basically Response<Object> since it has all the http request/response details.
        return this.protocolLayer.getDigitalTwins().getByIdWithResponseAsync(digitalTwinId);
    }
}
