// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.ErrorContext;

interface ErrorContextProvider {
    ErrorContext getContext();
}
