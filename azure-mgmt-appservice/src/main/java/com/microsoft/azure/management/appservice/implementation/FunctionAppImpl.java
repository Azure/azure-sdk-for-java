/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.AppServicePricingTier;
import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azure.management.appservice.WebAppBase;

/**
 * The implementation for FunctionApp.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
class FunctionAppImpl
    extends AppServiceBaseImpl<FunctionApp, FunctionAppImpl, FunctionApp.DefinitionStages.WithCreate, FunctionApp.Update>
    implements
        FunctionApp,
        FunctionApp.Definition,
        FunctionApp.Update,
        FunctionApp.UpdateStages.WithNewAppServicePlan,
        WebAppBase.DefinitionStages.WithWebContainer<FunctionApp.DefinitionStages.WithCreate>,
        WebAppBase.UpdateStages.WithWebContainer<FunctionApp.Update> {

    FunctionAppImpl(String name, SiteInner innerObject, SiteConfigInner configObject, AppServiceManager manager) {
        super(name, innerObject, configObject, manager);
        innerObject.withKind("functionapp");
    }

    @Override
    public FunctionAppImpl withDynamicPricingTier() {
        return withPricingTier(new AppServicePricingTier("Dynamic", "Y1"));
    }
}