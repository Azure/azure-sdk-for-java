/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.FunctionApp;

/**
 * The implementation for FunctionApp.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
class FunctionAppImpl
    extends AppServiceBaseImpl<FunctionApp, FunctionAppImpl>
    implements
        FunctionApp,
        FunctionApp.Definition,
        FunctionApp.Update,
        FunctionApp.UpdateStages.WithNewAppServicePlan {

    FunctionAppImpl(String name, SiteInner innerObject, SiteConfigInner configObject, AppServiceManager manager) {
        super(name, innerObject, configObject, manager);
        innerObject.withKind("functionapp");
    }
}