package com.azure.mixedreality.remoterendering.models.internal;

import com.azure.mixedreality.remoterendering.implementation.models.*;
import com.azure.mixedreality.remoterendering.implementation.models.Error;
import com.azure.mixedreality.remoterendering.models.*;
import com.azure.mixedreality.remoterendering.models.Conversion;

public class ModelTranslator {

    public static Conversion fromGenerated(com.azure.mixedreality.remoterendering.implementation.models.Conversion conversion) {
        return new Conversion(conversion);
    }

    public static ConversionOptions fromGenerated(com.azure.mixedreality.remoterendering.implementation.models.ConversionSettings settings) {
        return new ConversionOptions(fromGenerated(settings.getInputLocation()), fromGenerated(settings.getOutputLocation()));
    }

    public static ConversionSettings toGenerated(ConversionOptions conversionOptions) {
        return new ConversionSettings(toGenerated(conversionOptions.getConversionInputOptions()), toGenerated(conversionOptions.getConversionOutputOptions()));
    }

    public static ConversionInputOptions fromGenerated(ConversionInputSettings converionInputSettings) {
        return new ConversionInputOptions(
            converionInputSettings.getStorageContainerUri(),
            converionInputSettings.getStorageContainerReadListSas(),
            converionInputSettings.getBlobPrefix(),
            converionInputSettings.getRelativeInputAssetPath());
    }

    public static ConversionInputSettings toGenerated(ConversionInputOptions conversionInputOptions) {
        return new ConversionInputSettings(conversionInputOptions.getStorageContainerUri(), conversionInputOptions.getRelativeAssetPath())
            .setStorageContainerReadListSas(conversionInputOptions.getStorageContainerReadListSas())
            .setBlobPrefix(conversionInputOptions.getBlobPrefix());
    }

    public static  ConversionOutputOptions fromGenerated(ConversionOutputSettings converionOutputSettings) {
        return new ConversionOutputOptions(
            converionOutputSettings.getStorageContainerUri(),
            converionOutputSettings.getStorageContainerWriteSas(),
            converionOutputSettings.getBlobPrefix(),
            converionOutputSettings.getOutputAssetFilename());
    }

    public static ConversionOutputSettings toGenerated(ConversionOutputOptions conversionOutputOptions) {
        return new ConversionOutputSettings(conversionOutputOptions.getStorageContainerUri())
            .setStorageContainerWriteSas(conversionOutputOptions.getStorageContainerWriteSas())
            .setBlobPrefix(conversionOutputOptions.getBlobPrefix())
            .setOutputAssetFilename(conversionOutputOptions.getAssetFilename());
    }

    public static Session fromGenerated(SessionProperties sessionProperties) {
        return new Session(sessionProperties);
    }

    public static UpdateSessionSettings toGenerated(SessionUpdateOptions options) {
        return new UpdateSessionSettings(options.getMaxLeaseTimeMinutes());
    }

    public static CreateSessionSettings toGenerated(SessionCreationOptions options) {
        return new CreateSessionSettings(options.getMaxLeaseTimeMinutes(), com.azure.mixedreality.remoterendering.implementation.models.SessionSize.fromString(options.getSize().toString()));
    }
}
