package com.azure.digitaltwins.core.snippets;

public abstract class CodeSnippetBase {

    //region DigitalTwinSnippets
    public abstract void createDigitalTwin();
    public abstract void createDigitalTwinWithResponse();
    public abstract void getDigitalTwin();
    public abstract void getDigitalTwinWithResponse();
    public abstract void updateDigitalTwin();
    public abstract void updateDigitalTwinWithResponse();
    public abstract void deleteDigitalTwin();
    public abstract void deleteDigitalTwinWithResponse();
    //endregion DigitalTwinSnippets

    //region RelationshipSnippets
    public abstract void createRelationship();
    public abstract void createRelationshipWithResponse();
    public abstract void getRelationship();
    public abstract void getRelationshipWithResponse();
    public abstract void updateRelationship();
    public abstract void updateRelationshipWithResponse();
    public abstract void deleteRelationship();
    public abstract void deleteRelationshipWithResponse();
    public abstract void listRelationships();
    public abstract void listIncomingRelationships();
    //endregion RelationshipSnippets

    //region ModelsSnippets
    public abstract void createModels();
    public abstract void createModelsWithResponse();
    public abstract void getModel();
    public abstract void getModelWithResponse();
    public abstract void listModels();
    public abstract void decommissionModel();
    public abstract void decommissionModelWithResponse();
    public abstract void deleteModel();
    public abstract void deleteModelWithResponse();
    //endregion ModelsSnippets

    //region ComponentSnippets
    public abstract void getComponent();
    public abstract void getComponentWithResponse();
    public abstract void updateComponent();
    public abstract void updateComponentWithResponse();
    //endregion ComponentSnippets

    //region ComponentSnippets
    public abstract void query();
    //endregion ComponentSnippets

    //region EventRouteSnippets
    public abstract void createEventRoute();
    public abstract void createEventRouteWithResponse();
    public abstract void getEventRoute();
    public abstract void getEventRouteWithResponse();
    public abstract void deleteEventRoute();
    public abstract void deleteEventRouteWithResponse();
    public abstract void listEventRoutes();
    //endregion EventRouteSnippets

    //region TelemetrySnippets
    public abstract void publishTelemetry();
    public abstract void publishTelemetryWithResponse();
    public abstract void publishComponentTelemetry();
    public abstract void publishComponentTelemetryWithResponse();
    //endregion TelemetrySnippets

    /**
     * Implementation not provided for this method
     *
     * @return {@code null}
     */
    protected String getTenenatId() {
        return null;
    }

    /**
     * Implementation not provided for this method
     *
     * @return {@code null}
     */
    protected String getClientId() {
        return null;
    }

    /**
     * Implementation not provided for this method
     *
     * @return {@code null}
     */
    protected String getClientSecret() {
        return null;
    }

    /**
     * Implementation not provided for this method
     *
     * @return {@code null}
     */
    protected String getEndpointUrl() {
        return null;
    }

    /**
     * Implementation not provided for this method
     *
     * @return {@code null}
     */
    protected String getDigitalTwinPayload() {
        return null;
    }

    /**
     * Implementation not provided for this method
     *
     * @return {@code null}
     */
    protected String getRelationshipPayload() {
        return null;
    }

    /**
     * Implementation not provided for this method
     *
     * @return {@code null}
     */
    protected String loadModelFromFile(String fileName){
        return null;
    }
}
