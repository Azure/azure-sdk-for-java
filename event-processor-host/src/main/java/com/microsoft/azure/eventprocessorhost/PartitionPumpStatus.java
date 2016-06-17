/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

enum PartitionPumpStatus { PP_UNINITIALIZED, PP_OPENING, PP_OPENFAILED, PP_RUNNING, PP_ERRORED, PP_CLOSING, PP_CLOSED };
