package com.microsoft.azure.management.resources.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Taggable;
import com.microsoft.azure.management.resources.fluentcore.model.*;
import com.microsoft.azure.management.resources.models.implementation.api.*;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

public interface Deployment extends
        Indexable,
        Refreshable<Deployment>,
        Wrapper<DeploymentExtendedInner> {

    /***********************************************************
     * Getters
     ***********************************************************/

    String provisioningState();
    String correlationid();
    DateTime timestamp();
    Object outputs();
    List<Provider> providers();
    List<Dependency> dependencies();
    Object template();
    TemplateLink templateLink();
    Object parameters();
    ParametersLink parametersLink();
    DeploymentMode mode();

    /**************************************************************
     * Setters (fluent interface)
     **************************************************************/

    interface DefinitionBlank {
        DefinitionWithTemplate withTemplate(Object template);
        DefinitionWithTemplate withTemplate(JsonNode template);
        DefinitionWithTemplate withTemplateLink(String uri, String contentVersion);
    }

    interface DefinitionWithTemplate {
        DefinitionProvisionable withParameters(Object parameters);
        DefinitionProvisionable withParameters(JsonNode parameters);
        DefinitionProvisionable withParametersLink(String uri, String contentVersion);
    }

    interface DefinitionProvisionable extends Provisionable<Deployment> {
        DefinitionProvisionable withMode(DeploymentMode mode);
    }
}
