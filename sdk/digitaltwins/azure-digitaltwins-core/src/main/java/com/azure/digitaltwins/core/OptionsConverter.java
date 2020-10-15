package com.azure.digitaltwins.core;

import com.azure.digitaltwins.core.implementation.models.*;
import com.azure.digitaltwins.core.models.*;

class OptionsConverter {
    static DigitalTwinsAddOptions toProtocolLayerOptions(CreateDigitalTwinOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsAddOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    static DigitalTwinsAddRelationshipOptions toProtocolLayerOptions(CreateRelationshipOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsAddRelationshipOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    static DigitalTwinModelsAddOptions toProtocolLayerOptions(CreateModelsOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinModelsAddOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    static DigitalTwinModelsUpdateOptions toProtocolLayerOptions(UpdateModelOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinModelsUpdateOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    static DigitalTwinModelsListOptions toProtocolLayerOptions(ListModelsOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinModelsListOptions()
            .setTraceparent(options.getTraceparent())
            .setTracestate(options.getTracestate())
            .setMaxItemsPerPage(options.getMaxItemsPerPage());
    }

    static DigitalTwinModelsGetByIdOptions toProtocolLayerOptions(GetModelOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinModelsGetByIdOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    static DigitalTwinModelsDeleteOptions toProtocolLayerOptions(DeleteModelOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinModelsDeleteOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    static EventRoutesAddOptions toProtocolLayerOptions(CreateEventRouteOptions options) {
        if (options == null) {
            return null;
        }

        return new EventRoutesAddOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    static EventRoutesListOptions toProtocolLayerOptions(ListEventRoutesOptions options) {
        if (options == null) {
            return null;
        }

        return new EventRoutesListOptions()
            .setTraceparent(options.getTraceparent())
            .setTracestate(options.getTracestate())
            .setMaxItemsPerPage(options.getMaxItemsPerPage());
    }

    static EventRoutesGetByIdOptions toProtocolLayerOptions(GetEventRouteOptions options) {
        if (options == null) {
            return null;
        }

        return new EventRoutesGetByIdOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    static EventRoutesDeleteOptions toProtocolLayerOptions(DeleteEventRouteOptions options) {
        if (options == null) {
            return null;
        }

        return new EventRoutesDeleteOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    static DigitalTwinsGetByIdOptions toProtocolLayerOptions(GetDigitalTwinOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsGetByIdOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    static DigitalTwinsDeleteOptions toProtocolLayerOptions(DeleteDigitalTwinOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsDeleteOptions()
            .setTraceparent(options.getTraceparent())
            .setTracestate(options.getTracestate())
            .setIfMatch(options.getIfMatch());
    }

    static DigitalTwinsUpdateOptions toProtocolLayerOptions(UpdateDigitalTwinOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsUpdateOptions()
            .setTraceparent(options.getTraceparent())
            .setTracestate(options.getTracestate())
            .setIfMatch(options.getIfMatch());
    }

    static DigitalTwinsGetRelationshipByIdOptions toProtocolLayerOptions(GetRelationshipOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsGetRelationshipByIdOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    static DigitalTwinsUpdateRelationshipOptions toProtocolLayerOptions(UpdateRelationshipOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsUpdateRelationshipOptions()
            .setTraceparent(options.getTraceparent())
            .setTracestate(options.getTracestate())
            .setIfMatch(options.getIfMatch());
    }

    static DigitalTwinsDeleteRelationshipOptions toProtocolLayerOptions(DeleteRelationshipOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsDeleteRelationshipOptions()
            .setTraceparent(options.getTraceparent())
            .setTracestate(options.getTracestate())
            .setIfMatch(options.getIfMatch());
    }

    static DigitalTwinsListRelationshipsOptions toProtocolLayerOptions(ListRelationshipsOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsListRelationshipsOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    static DigitalTwinsListIncomingRelationshipsOptions toProtocolLayerOptions(ListIncomingRelationshipsOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsListIncomingRelationshipsOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    static DigitalTwinsGetComponentOptions toProtocolLayerOptions(GetComponentOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsGetComponentOptions().setTraceparent(options.getTraceparent()).setTracestate(options.getTracestate());
    }

    static DigitalTwinsUpdateComponentOptions toProtocolLayerOptions(UpdateComponentOptions options) {
        if (options == null) {
            return null;
        }

        return new DigitalTwinsUpdateComponentOptions()
            .setTraceparent(options.getTraceparent())
            .setTracestate(options.getTracestate())
            .setIfMatch(options.getIfMatch());
    }

    static QueryTwinsOptions toProtocolLayerOptions(QueryOptions options) {
        if (options == null) {
            return null;
        }

        return new QueryTwinsOptions()
            .setTraceparent(options.getTraceparent())
            .setTracestate(options.getTracestate())
            .setMaxItemsPerPage(options.getMaxItemsPerPage());
    }
}
