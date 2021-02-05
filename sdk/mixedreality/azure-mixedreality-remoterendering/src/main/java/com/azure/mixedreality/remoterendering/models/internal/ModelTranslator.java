package com.azure.mixedreality.remoterendering.models.internal;

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

public class ModelTranslator {

    public static Conversion fromGenerated(com.azure.mixedreality.remoterendering.implementation.models.Conversion conversion) {
        if (conversion == null) {
            return null;
        }
        return new Conversion()
            .setId(conversion.getId())
            .setOptions(fromGenerated(conversion.getSettings()))
            .setOutputAssetUri(conversion.getOutput() != null ? conversion.getOutput().getOutputAssetUri() : null)
            .setError(fromGenerated(conversion.getError()))
            .setConversionStatus(ConversionStatus.fromString(conversion.getStatus().toString()))
            .setCreationTime(conversion.getCreationTime());
    }

    public static Session fromGenerated(SessionProperties sessionProperties) {
        if (sessionProperties == null) {
            return null;
        }
        return new Session()
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
        return new RemoteRenderingServiceError()
            .setCode(error.getCode())
            .setMessage(error.getMessage())
            .setTarget(error.getTarget())
            .setInnerError((error.getInnerError() != null) ? fromGenerated(error.getInnerError()) : null)
            .setRootErrors(error.getDetails().stream().map(ModelTranslator::fromGenerated).collect(Collectors.toList()));
    }

    public static ConversionOptions fromGenerated(com.azure.mixedreality.remoterendering.implementation.models.ConversionSettings settings) {
        if (settings == null) {
            return null;
        }
        return new ConversionOptions()
            .inputBlobPrefix(settings.getInputLocation().getBlobPrefix())
            .inputRelativeAssetPath(settings.getInputLocation().getRelativeInputAssetPath())
            .inputStorageContainerReadListSas(settings.getInputLocation().getStorageContainerReadListSas())
            .inputStorageContainerUri(settings.getInputLocation().getStorageContainerUri())

            .outputAssetFilename(settings.getOutputLocation().getOutputAssetFilename())
            .outputBlobPrefix(settings.getOutputLocation().getBlobPrefix())
            .outputStorageContainerUri(settings.getOutputLocation().getStorageContainerUri())
            .outputStorageContainerWriteSas(settings.getOutputLocation().getStorageContainerWriteSas());
    }

    public static ConversionSettings toGenerated(ConversionOptions conversionOptions) {
        if (conversionOptions == null) {
            return null;
        }
        return new ConversionSettings(
            new ConversionInputSettings(
            conversionOptions.getInputStorageContainerUri(),
            conversionOptions.getInputRelativeAssetPath())
            .setStorageContainerReadListSas(conversionOptions.getInputStorageContainerReadListSas())
            .setBlobPrefix(conversionOptions.getInputBlobPrefix()),
            new ConversionOutputSettings(conversionOptions.getOutputStorageContainerUri())
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
