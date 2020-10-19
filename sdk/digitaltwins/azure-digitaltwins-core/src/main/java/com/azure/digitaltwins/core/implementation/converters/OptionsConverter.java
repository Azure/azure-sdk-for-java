// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.implementation.converters;

import com.azure.digitaltwins.core.implementation.models.*;
import com.azure.digitaltwins.core.models.*;

/**
 * Helper class used internally to convert convenience layer options objects into protocol layer options objects.
 * This class must be manually updated each time a new option is added to a service API.
 */
public class OptionsConverter {
    public static DigitalTwinsAddOptions toProtocolLayerOptions(CreateDigitalTwinOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsAddOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    public static DigitalTwinsAddRelationshipOptions toProtocolLayerOptions(CreateRelationshipOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsAddRelationshipOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    public static DigitalTwinModelsAddOptions toProtocolLayerOptions(CreateModelsOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinModelsAddOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    public static DigitalTwinModelsUpdateOptions toProtocolLayerOptions(UpdateModelOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinModelsUpdateOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    public static DigitalTwinModelsListOptions toProtocolLayerOptions(ListModelsOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinModelsListOptions()
            .setTraceparent(options.getTraceparent())
            .setTracestate(options.getTracestate())
            .setMaxItemsPerPage(options.getMaxItemsPerPage());
    }

    public static DigitalTwinModelsGetByIdOptions toProtocolLayerOptions(GetModelOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinModelsGetByIdOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    public static DigitalTwinModelsDeleteOptions toProtocolLayerOptions(DeleteModelOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinModelsDeleteOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    public static EventRoutesAddOptions toProtocolLayerOptions(CreateEventRouteOptions options) {
        if (options == null) {
            return null;
        }

        return new EventRoutesAddOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    public static EventRoutesListOptions toProtocolLayerOptions(ListEventRoutesOptions options) {
        if (options == null) {
            return null;
        }

        return new EventRoutesListOptions()
            .setTraceparent(options.getTraceparent())
            .setTracestate(options.getTracestate())
            .setMaxItemsPerPage(options.getMaxItemsPerPage());
    }

    public static EventRoutesGetByIdOptions toProtocolLayerOptions(GetEventRouteOptions options) {
        if (options == null) {
            return null;
        }

        return new EventRoutesGetByIdOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    public static EventRoutesDeleteOptions toProtocolLayerOptions(DeleteEventRouteOptions options) {
        if (options == null) {
            return null;
        }

        return new EventRoutesDeleteOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    public static DigitalTwinsGetByIdOptions toProtocolLayerOptions(GetDigitalTwinOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsGetByIdOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    public static DigitalTwinsDeleteOptions toProtocolLayerOptions(DeleteDigitalTwinOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsDeleteOptions()
            .setTraceparent(options.getTraceparent())
            .setTracestate(options.getTracestate())
            .setIfMatch(options.getIfMatch());
    }

    public static DigitalTwinsUpdateOptions toProtocolLayerOptions(UpdateDigitalTwinOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsUpdateOptions()
            .setTraceparent(options.getTraceparent())
            .setTracestate(options.getTracestate())
            .setIfMatch(options.getIfMatch());
    }

    public static DigitalTwinsGetRelationshipByIdOptions toProtocolLayerOptions(GetRelationshipOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsGetRelationshipByIdOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    public static DigitalTwinsUpdateRelationshipOptions toProtocolLayerOptions(UpdateRelationshipOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsUpdateRelationshipOptions()
            .setTraceparent(options.getTraceparent())
            .setTracestate(options.getTracestate())
            .setIfMatch(options.getIfMatch());
    }

    public static DigitalTwinsDeleteRelationshipOptions toProtocolLayerOptions(DeleteRelationshipOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsDeleteRelationshipOptions()
            .setTraceparent(options.getTraceparent())
            .setTracestate(options.getTracestate())
            .setIfMatch(options.getIfMatch());
    }

    public static DigitalTwinsListRelationshipsOptions toProtocolLayerOptions(ListRelationshipsOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsListRelationshipsOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    public static DigitalTwinsListIncomingRelationshipsOptions toProtocolLayerOptions(ListIncomingRelationshipsOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsListIncomingRelationshipsOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    public static DigitalTwinsGetComponentOptions toProtocolLayerOptions(GetComponentOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsGetComponentOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    public static DigitalTwinsUpdateComponentOptions toProtocolLayerOptions(UpdateComponentOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsUpdateComponentOptions()
            .setTraceparent(options.getTraceparent())
            .setTracestate(options.getTracestate())
            .setIfMatch(options.getIfMatch());
    }

    public static QueryTwinsOptions toProtocolLayerOptions(QueryOptions options) {
        if (options == null) {
            return null;
        }

        return new QueryTwinsOptions()
            .setTraceparent(options.getTraceparent())
            .setTracestate(options.getTracestate())
            .setMaxItemsPerPage(options.getMaxItemsPerPage());
    }
}
