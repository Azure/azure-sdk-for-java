// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;

/**
 * An immutable client-side representation of an Azure Function App.
 */
public interface FunctionAppBasic extends WebSiteBase, Refreshable<FunctionApp> {
}
