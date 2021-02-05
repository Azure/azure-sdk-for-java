package com.azure.mixedreality.remoterendering.models.internal;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.mixedreality.remoterendering.implementation.models.ConversionSettings;
import com.azure.mixedreality.remoterendering.implementation.models.ConversionInputSettings;
import com.azure.mixedreality.remoterendering.implementation.models.ConversionOutputSettings;
import com.azure.mixedreality.remoterendering.implementation.models.SessionProperties;
import com.azure.mixedreality.remoterendering.implementation.models.UpdateSessionSettings;
import com.azure.mixedreality.remoterendering.implementation.models.CreateSessionSettings;
import com.azure.mixedreality.remoterendering.implementation.models.Error;
import com.azure.mixedreality.remoterendering.models.*;

import java.time.Duration;
import java.util.stream.Collectors;

public final class ModelTranslator {

    public static <T, Y> Response<T> fromGenerated(Response<Y> response) {
        return new Response<T>() {

            private T value = fromGeneratedGeneric(response.getValue());

            @Override
            public int getStatusCode() {
                return response.getStatusCode();
            }

            @Override
            public HttpHeaders getHeaders() {
                return response.getHeaders();
            }

            @Override
            public HttpRequest getRequest() {
                return response.getRequest();
            }

            @Override
            public T getValue() {
                return this.value;
            }
        };
    }

    public static <T> T fromGenerated(AsyncPollResponse<Response<T>, Response<T>> r) {
        return null;//new AsyncPollResponse<T, T>();
    }

    private static <T, Y> T fromGeneratedGeneric(Y value) {
        if (value == null) {
            return null;
        }
        else if (value instanceof com.azure.mixedreality.remoterendering.implementation.models.Conversion) {
            return (T)fromGenerated((com.azure.mixedreality.remoterendering.implementation.models.Conversion)value);
        }
        else if (value instanceof SessionProperties) {
            return (T)fromGenerated((SessionProperties)value);
        }
        else if (value instanceof Error) {
            return (T)fromGenerated((Error)value);
        }
        else if (value instanceof com.azure.mixedreality.remoterendering.implementation.models.ConversionSettings) {
            return (T)fromGenerated((com.azure.mixedreality.remoterendering.implementation.models.ConversionSettings)value);
        }
        else {
            // throw?
            return null;
        }
    }

    public static Conversion fromGenerated(com.azure.mixedreality.remoterendering.implementation.models.Conversion conversion) {
        if (conversion == null) {
            return null;
        }
        return new Conversion()
            .setId(conversion.getId())
            .setOptions(fromGenerated(conversion.getSettings()))
            .setOutputAssetUrl(conversion.getOutput() != null ? conversion.getOutput().getOutputAssetUri() : null)
            .setError(fromGenerated(conversion.getError()))
            .setConversionStatus(ConversionStatus.fromString(conversion.getStatus().toString()))
            .setCreationTime(conversion.getCreationTime());
    }

    public static RenderingSession fromGenerated(SessionProperties sessionProperties) {
        if (sessionProperties == null) {
            return null;
        }
        return new RenderingSession()
            .setId(sessionProperties.getId())
            .setHandshakePort(sessionProperties.getHandshakePort())
            .setElapsedTime(Duration.ofMinutes(sessionProperties.getElapsedTimeMinutes()))
            .setHostname(sessionProperties.getHostname())
            .setMaxLeaseTime(Duration.ofMinutes(sessionProperties.getMaxLeaseTimeMinutes()))
            .setSessionSize(SessionSize.fromString(sessionProperties.getSize().toString()))
            .setSessionStatus(SessionStatus.fromString(sessionProperties.getStatus().toString()))
            .setTeraflops(sessionProperties.getTeraflops())
            .setError(fromGenerated(sessionProperties.getError()))
            .setCreationTime(sessionProperties.getCreationTime());
    }

    public static RemoteRenderingServiceError fromGenerated(Error error) {
        if (error == null) {
            return null;
        }
        var e = new RemoteRenderingServiceError()
            .setCode(error.getCode())
            .setMessage(error.getMessage())
            .setTarget(error.getTarget());
        if (error.getInnerError() != null) {
            e.setInnerError(fromGenerated(error.getInnerError()));
        }
        if (error.getDetails() != null) {
            e.setRootErrors(error.getDetails().stream().map(ModelTranslator::fromGenerated).collect(Collectors.toList()));
        }
        return e;
    }

    public static ConversionOptions fromGenerated(com.azure.mixedreality.remoterendering.implementation.models.ConversionSettings settings) {
        if (settings == null) {
            return null;
        }
        return new ConversionOptions()
            .inputBlobPrefix(settings.getInputLocation().getBlobPrefix())
            .inputRelativeAssetPath(settings.getInputLocation().getRelativeInputAssetPath())
            .inputStorageContainerReadListSas(settings.getInputLocation().getStorageContainerReadListSas())
            .inputStorageContainerUrl(settings.getInputLocation().getStorageContainerUri())

            .outputAssetFilename(settings.getOutputLocation().getOutputAssetFilename())
            .outputBlobPrefix(settings.getOutputLocation().getBlobPrefix())
            .outputStorageContainerUrl(settings.getOutputLocation().getStorageContainerUri())
            .outputStorageContainerWriteSas(settings.getOutputLocation().getStorageContainerWriteSas());
    }

    public static ConversionSettings toGenerated(ConversionOptions conversionOptions) {
        if (conversionOptions == null) {
            return null;
        }
        return new ConversionSettings(
            new ConversionInputSettings(
            conversionOptions.getInputStorageContainerUrl(),
            conversionOptions.getInputRelativeAssetPath())
            .setStorageContainerReadListSas(conversionOptions.getInputStorageContainerReadListSas())
            .setBlobPrefix(conversionOptions.getInputBlobPrefix()),
            new ConversionOutputSettings(conversionOptions.getOutputStorageContainerUrl())
                .setStorageContainerWriteSas(conversionOptions.getOutputStorageContainerWriteSas())
                .setBlobPrefix(conversionOptions.getOutputBlobPrefix())
                .setOutputAssetFilename(conversionOptions.getOutputAssetFilename())
        );
    }

    public static UpdateSessionSettings toGenerated(UpdateSessionOptions options) {
        if (options == null) {
            return null;
        }
        return new UpdateSessionSettings((int)options.getMaxLeaseTime().toMinutes());
    }

    public static CreateSessionSettings toGenerated(CreateSessionOptions options) {
        if (options == null) {
            return null;
        }
        return new CreateSessionSettings((int)options.getMaxLeaseTime().toMinutes(), com.azure.mixedreality.remoterendering.implementation.models.SessionSize.fromString(options.getSize().toString()));
    }
}
