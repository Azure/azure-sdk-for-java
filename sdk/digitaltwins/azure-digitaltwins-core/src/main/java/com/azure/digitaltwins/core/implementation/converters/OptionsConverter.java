// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.implementation.converters;

import com.azure.digitaltwins.core.implementation.models.*;
import com.azure.digitaltwins.core.models.*;

/**
 * Helper class used internally to convert convenience layer options objects into protocol layer options objects.
 * This class must be manually updated each time a new option is added to a service API.
 */
public final class OptionsConverter {
    public static DigitalTwinsAddOptions toProtocolLayerOptions(CreateOrReplaceDigitalTwinOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsAddOptions()
            .setIfNoneMatch(options.getIfNoneMatch());
    }

    public static DigitalTwinsAddRelationshipOptions toProtocolLayerOptions(CreateOrReplaceRelationshipOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsAddRelationshipOptions()
            .setIfNoneMatch(options.getIfNoneMatch());
    }

    public static DigitalTwinModelsListOptions toProtocolLayerOptions(ListModelsOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinModelsListOptions()
            .setMaxItemsPerPage(options.getMaxItemsPerPage());
    }

    public static EventRoutesListOptions toProtocolLayerOptions(ListDigitalTwinsEventRoutesOptions options) {
        if (options == null) {
            return null;
        }

        return new EventRoutesListOptions()
            .setMaxItemsPerPage(options.getMaxItemsPerPage());
    }

    public static DigitalTwinsDeleteOptions toProtocolLayerOptions(DeleteDigitalTwinOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsDeleteOptions()
            .setIfMatch(options.getIfMatch());
    }

    public static DigitalTwinsUpdateOptions toProtocolLayerOptions(UpdateDigitalTwinOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsUpdateOptions()
            .setIfMatch(options.getIfMatch());
    }

    public static DigitalTwinsUpdateRelationshipOptions toProtocolLayerOptions(UpdateRelationshipOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsUpdateRelationshipOptions()
            .setIfMatch(options.getIfMatch());
    }

    public static DigitalTwinsDeleteRelationshipOptions toProtocolLayerOptions(DeleteRelationshipOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsDeleteRelationshipOptions()
            .setIfMatch(options.getIfMatch());
    }

    public static DigitalTwinsUpdateComponentOptions toProtocolLayerOptions(UpdateComponentOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsUpdateComponentOptions()
            .setIfMatch(options.getIfMatch());
    }

    public static QueryTwinsOptions toProtocolLayerOptions(QueryOptions options) {
        if (options == null) {
            return null;
        }

        return new QueryTwinsOptions()
            .setMaxItemsPerPage(options.getMaxItemsPerPage());
    }
}
