package com.azure.learn.appconfig;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.learn.appconfig.implementation.AzureAppConfigurationImpl;
import com.azure.learn.appconfig.implementation.models.GetKeyValueResponse;
import com.azure.learn.appconfig.models.ConfigurationSetting;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Attempts to get the ConfigurationSetting with a matching {@link ConfigurationSetting#getKey() key}, and optional
 * {@link ConfigurationSetting#getLabel() label} and optional ETag combination.
 *
 * @param setting The configuration setting to retrieve.
 * @param ifChanged Flag indicating if the {@code setting} {@link ConfigurationSetting#getETag ETag} is used as a
 * If-None-Match header.
 * @param context Additional context that is passed through the Http pipeline during the service call.
 * @return A REST response contains the {@link ConfigurationSetting} stored in the service, or {@code null}, if the
 * configuration value does not exist or the key is an invalid value (which will also throw ServiceRequestException
 * described below).
 * @throws NullPointerException If {@code setting} is {@code null}.
 * @throws ResourceNotFoundException If a ConfigurationSetting with the same key and label does not exist.
 * @throws HttpResponseException If the call to the service failed for any other reason.
 */
@ServiceClient(builder = ConfigurationClientBuilder.class, isAsync = true)
public final class ConfigurationAsyncClient {
    private static final String APP_CONFIG_TRACING_NAMESPACE_VALUE = "Microsoft.AppConfiguration";

    private final AzureAppConfigurationImpl internalClient;
    private final ClientLogger logger = new ClientLogger(ConfigurationAsyncClient.class);

    ConfigurationAsyncClient(AzureAppConfigurationImpl internalClient) {
        this.internalClient = internalClient;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> getConfigurationSetting(String key) {
        return getConfigurationSetting(key, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> getConfigurationSetting(String key, String label) {
        try {
            Objects.requireNonNull(key, "'key' cannot be null");
            return withContext(context -> internalClient.getKeyValueWithResponseAsync(key, label, null, null, null, null,
                context))
                .map(response -> response.getValue());
        } catch (RuntimeException exception) {
            return monoError(logger, exception);
        }
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConfigurationSetting>> getConfigurationSettingWithResponse(ConfigurationSetting setting,
                                                                                    boolean ifChanged) {
        return withContext(context -> getConfigurationSettingWithResponse(setting, ifChanged, context));
    }

    Mono<Response<ConfigurationSetting>> getConfigurationSettingWithResponse(ConfigurationSetting setting,
                                                                             boolean ifChanged, Context context)  {
        context.addData(Tracer.AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE);
        try {
            Objects.requireNonNull(setting, "'setting' cannot be null");

            String ifNoneMatchETag = ifChanged ? getETagValue(setting.getEtag()) : null;
            return internalClient.getKeyValueWithResponseAsync(setting.getKey(), setting.getLabel(),
                null, null, ifNoneMatchETag, null, context)
                .onErrorResume(HttpResponseException.class, throwable -> {
                    HttpResponseException e = (HttpResponseException) throwable;
                    HttpResponse httpResponse = e.getResponse();
                    if (httpResponse.getStatusCode() == 304) {
                        GetKeyValueResponse nullConfigSettingResponse = new GetKeyValueResponse(httpResponse.getRequest(),
                            httpResponse.getStatusCode(), httpResponse.getHeaders(), null, null);
                        return Mono.just(nullConfigSettingResponse);
                    } else if (httpResponse.getStatusCode() == 404) {
                        return Mono.error(new ResourceNotFoundException("Setting not found.", httpResponse, throwable));
                    }
                    return monoError(logger, throwable);
                }).map(getKeyValueResponse -> {
                    return new ResponseBase<>(getKeyValueResponse.getRequest(), getKeyValueResponse.getStatusCode(),
                        getKeyValueResponse.getHeaders(), getKeyValueResponse.getValue(), null);
                });
        } catch (RuntimeException exception) {
            return monoError(logger, exception);
        }
    }

    private static String getETagValue(String etag) {
        return (etag == null || etag.equals("*")) ? etag : "\"" + etag + "\"";
    }
}
