// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

/***
 * The action string of ExceptionReceivedEventArts will be one of this. 
 * They describe what activity was taking place when the exception occurred.  
 */
public final class EventProcessorHostActionStrings {
    public static final String ACQUIRING_LEASE = "Acquiring Lease";
    public static final String CHECKING_CHECKPOINT_STORE = "Checking Checpoint Store Existence";
    public static final String CHECKING_LEASES = "Checking Leases";
    public static final String CHECKING_LEASE_STORE = "Checking Lease Store Existence";
    public static final String CLOSING_EVENT_PROCESSOR = "Closing Event Processor";
    public static final String CREATING_CHECKPOINTS = "Creating Checkpoint Holders";
    public static final String CREATING_CHECKPOINT_STORE = "Creating Checkpoint Store";
    public static final String CREATING_EVENT_HUB_CLIENT = "Creating Event Hub Client";
    public static final String CREATING_EVENT_PROCESSOR = "Creating Event Processor";
    public static final String CREATING_LEASES = "Creating Leases";
    public static final String CREATING_LEASE_STORE = "Creating Lease Store";
    public static final String DELETING_LEASE = "Deleting Lease";
    public static final String GETTING_CHECKPOINT = "Getting Checkpoint Details";
    public static final String GETTING_LEASE = "Getting Lease Details";
    public static final String INITIALIZING_STORES = "Initializing Stores";
    public static final String OPENING_EVENT_PROCESSOR = "Opening Event Processor";
    public static final String PARTITION_MANAGER_CLEANUP = "Partition Manager Cleanup";
    public static final String PARTITION_MANAGER_MAIN_LOOP = "Partition Manager Main Loop";
    public static final String RELEASING_LEASE = "Releasing Lease";
    public static final String RENEWING_LEASE = "Renewing Lease";
    public static final String STEALING_LEASE = "Stealing Lease";
    public static final String UPDATING_CHECKPOINT = "Updating Checkpoint";
    public static final String UPDATING_LEASE = "Updating Lease";
}
