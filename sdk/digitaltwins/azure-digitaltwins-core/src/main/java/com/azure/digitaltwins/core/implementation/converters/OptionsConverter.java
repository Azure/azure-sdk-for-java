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
    public static DigitalTwinsAddOptions toProtocolLayerOptions(CreateDigitalTwinOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsAddOptions().setTraceparent(options.getTraceParent()).setTracestate(options.getTraceState());
    }

    public static DigitalTwinsAddRelationshipOptions toProtocolLayerOptions(CreateRelationshipOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsAddRelationshipOptions().setTraceparent(options.getTraceParent()).setTracestate(options.getTraceState());
    }

    public static DigitalTwinModelsAddOptions toProtocolLayerOptions(CreateModelsOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinModelsAddOptions().setTraceparent(options.getTraceParent()).setTracestate(options.getTraceState());
    }

    public static DigitalTwinModelsUpdateOptions toProtocolLayerOptions(DecommissionModelOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinModelsUpdateOptions().setTraceparent(options.getTraceParent()).setTracestate(options.getTraceState());
    }

    public static DigitalTwinModelsListOptions toProtocolLayerOptions(ListModelsOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinModelsListOptions()
            .setTraceparent(options.getTraceParent())
            .setTracestate(options.getTraceState())
            .setMaxItemsPerPage(options.getMaxItemsPerPage());
    }

    public static DigitalTwinModelsGetByIdOptions toProtocolLayerOptions(GetModelOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinModelsGetByIdOptions().setTraceparent(options.getTraceParent()).setTracestate(options.getTraceState());
    }

    public static DigitalTwinModelsDeleteOptions toProtocolLayerOptions(DeleteModelOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinModelsDeleteOptions().setTraceparent(options.getTraceParent()).setTracestate(options.getTraceState());
    }

    public static EventRoutesAddOptions toProtocolLayerOptions(CreateEventRouteOptions options) {
        if (options == null) {
            return null;
        }

        return new EventRoutesAddOptions().setTraceparent(options.getTraceParent()).setTracestate(options.getTraceState());
    }

    public static EventRoutesListOptions toProtocolLayerOptions(ListEventRoutesOptions options) {
        if (options == null) {
            return null;
        }

        return new EventRoutesListOptions()
            .setTraceparent(options.getTraceParent())
            .setTracestate(options.getTraceState())
            .setMaxItemsPerPage(options.getMaxItemsPerPage());
    }

    public static EventRoutesGetByIdOptions toProtocolLayerOptions(GetEventRouteOptions options) {
        if (options == null) {
            return null;
        }

        return new EventRoutesGetByIdOptions().setTraceparent(options.getTraceParent()).setTracestate(options.getTraceState());
    }

    public static EventRoutesDeleteOptions toProtocolLayerOptions(DeleteEventRouteOptions options) {
        if (options == null) {
            return null;
        }

        return new EventRoutesDeleteOptions().setTraceparent(options.getTraceParent()).setTracestate(options.getTraceState());
    }

    public static DigitalTwinsGetByIdOptions toProtocolLayerOptions(GetDigitalTwinOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsGetByIdOptions().setTraceparent(options.getTraceParent()).setTracestate(options.getTraceState());
    }

    public static DigitalTwinsDeleteOptions toProtocolLayerOptions(DeleteDigitalTwinOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsDeleteOptions()
            .setTraceparent(options.getTraceParent())
            .setTracestate(options.getTraceState())
            .setIfMatch(options.getIfMatch());
    }

    public static DigitalTwinsUpdateOptions toProtocolLayerOptions(UpdateDigitalTwinOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsUpdateOptions()
            .setTraceparent(options.getTraceParent())
            .setTracestate(options.getTraceState())
            .setIfMatch(options.getIfMatch());
    }

    public static DigitalTwinsGetRelationshipByIdOptions toProtocolLayerOptions(GetRelationshipOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsGetRelationshipByIdOptions().setTraceparent(options.getTraceParent()).setTracestate(options.getTraceState());
    }

    public static DigitalTwinsUpdateRelationshipOptions toProtocolLayerOptions(UpdateRelationshipOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsUpdateRelationshipOptions()
            .setTraceparent(options.getTraceParent())
            .setTracestate(options.getTraceState())
            .setIfMatch(options.getIfMatch());
    }

    public static DigitalTwinsDeleteRelationshipOptions toProtocolLayerOptions(DeleteRelationshipOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsDeleteRelationshipOptions()
            .setTraceparent(options.getTraceParent())
            .setTracestate(options.getTraceState())
            .setIfMatch(options.getIfMatch());
    }

    public static DigitalTwinsListRelationshipsOptions toProtocolLayerOptions(ListRelationshipsOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsListRelationshipsOptions().setTraceparent(options.getTraceParent()).setTracestate(options.getTraceState());
    }

    public static DigitalTwinsListIncomingRelationshipsOptions toProtocolLayerOptions(ListIncomingRelationshipsOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsListIncomingRelationshipsOptions().setTraceparent(options.getTraceParent()).setTracestate(options.getTraceState());
    }

    public static DigitalTwinsGetComponentOptions toProtocolLayerOptions(GetComponentOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsGetComponentOptions().setTraceparent(options.getTraceParent()).setTracestate(options.getTraceState());
    }

    public static DigitalTwinsUpdateComponentOptions toProtocolLayerOptions(UpdateComponentOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsUpdateComponentOptions()
            .setTraceparent(options.getTraceParent())
            .setTracestate(options.getTraceState())
            .setIfMatch(options.getIfMatch());
    }

    public static QueryTwinsOptions toProtocolLayerOptions(QueryOptions options) {
        if (options == null) {
            return null;
        }

        return new QueryTwinsOptions()
            .setTraceparent(options.getTraceParent())
            .setTracestate(options.getTraceState())
            .setMaxItemsPerPage(options.getMaxItemsPerPage());
    }
}
