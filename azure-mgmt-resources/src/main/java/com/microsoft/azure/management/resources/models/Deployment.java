package com.microsoft.azure.management.resources.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.management.resources.DeploymentOperations;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Provisionable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.models.implementation.api.*;
import org.joda.time.DateTime;

import java.util.List;

public interface Deployment extends
        Indexable,
        Refreshable<Deployment>,
        Wrapper<DeploymentExtendedInner> {

    /***********************************************************
     * Getters
     ***********************************************************/

    String resourceGroupName();
    String name();
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

    DeploymentOperations deploymentOperations();

    /**************************************************************
     * Setters (fluent interface)
     **************************************************************/

    interface DefinitionBlank {
        DefinitionWithGroup withNewResourceGroup(String resourceGroupName, Region location) throws Exception;
        DefinitionWithGroup withExistingResourceGroup(String resourceGroupName);
    }

    interface DefinitionWithGroup {
        DefinitionWithTemplate withTemplate(Object template);
        DefinitionWithTemplate withTemplate(JsonNode template);
        DefinitionWithTemplate withTemplateLink(String uri, String contentVersion);
    }

    interface DefinitionWithTemplate {
        DefinitionWithParameters withParameters(Object parameters);
        DefinitionWithParameters withParameters(JsonNode parameters);
        DefinitionWithParameters withParametersLink(String uri, String contentVersion);
    }

    interface DefinitionWithParameters {
        DefinitionProvisionable withMode(DeploymentMode mode);
    }

    interface DefinitionProvisionable extends Provisionable<Deployment> {
    }
}
