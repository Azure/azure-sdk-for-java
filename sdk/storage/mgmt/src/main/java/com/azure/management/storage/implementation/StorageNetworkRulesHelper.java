/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage.implementation;

import com.azure.management.storage.Action;
import com.azure.management.storage.Bypass;
import com.azure.management.storage.DefaultAction;
import com.azure.management.storage.IPRule;
import com.azure.management.storage.NetworkRuleSet;
import com.azure.management.storage.StorageAccountCreateParameters;
import com.azure.management.storage.StorageAccountUpdateParameters;
import com.azure.management.storage.VirtualNetworkRule;
import com.azure.management.storage.models.StorageAccountInner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Helper to operate on storage account NetworkRule set {@link StorageAccountInner#getNetworkRuleSet} property.
 */
final class StorageNetworkRulesHelper {
    private static final String BYPASS_NONE_STR = Bypass.NONE.toString().toLowerCase();
    private final boolean isInCreateMode;
    private final StorageAccountInner inner;
    private final StorageAccountCreateParameters createParameters;
    private final StorageAccountUpdateParameters updateParameters;

    /**
     * Creates StorageNetworkRulesHelper.
     *
     * @param createParameters the model representing payload for storage account create.
     */
    StorageNetworkRulesHelper(StorageAccountCreateParameters createParameters) {
        this.isInCreateMode = true;
        this.createParameters = createParameters;
        this.updateParameters = null;
        this.inner = null;
    }

    /**
     * Creates StorageNetworkRulesHelper.
     *
     * @param updateParameters the model representing payload for storage account update
     * @param inner            the current state of storage account
     */
    StorageNetworkRulesHelper(StorageAccountUpdateParameters updateParameters, final StorageAccountInner inner) {
        this.isInCreateMode = false;
        this.createParameters = null;
        this.updateParameters = updateParameters;
        this.inner = inner;

    }

    /**
     * Checks whether access to the given storage account is allowed from all networks.
     *
     * @param inner the storage account
     * @return true if access allowed from all networks, false otherwise
     */
    static boolean isAccessAllowedFromAllNetworks(final StorageAccountInner inner) {
        if (inner.getNetworkRuleSet() == null || inner.getNetworkRuleSet().getDefaultAction() == null) {
            return true;
        }
        return inner.getNetworkRuleSet().getDefaultAction().equals(DefaultAction.ALLOW);
    }

    /**
     * The list of resource id of subnets having access to the given storage account.
     *
     * @param inner the storage account
     * @return list of subnet resource ids
     */
    static List<String> networkSubnetsWithAccess(final StorageAccountInner inner) {
        List<String> subnetIds = new ArrayList<>();
        if (inner.getNetworkRuleSet() != null
                && inner.getNetworkRuleSet().getVirtualNetworkRules() != null) {
            for (VirtualNetworkRule rule : inner.getNetworkRuleSet().getVirtualNetworkRules()) {
                if (rule != null && rule.getVirtualNetworkResourceId() != null) {
                    subnetIds.add(rule.getVirtualNetworkResourceId());
                }
            }
        }
        return subnetIds;
    }

    /**
     * The list of ipv4 addresses having access to the given storage account.
     *
     * @param inner the storage account
     * @return list of ip addresses
     */
    static List<String> ipAddressesWithAccess(final StorageAccountInner inner) {
        List<String> ipAddresses = new ArrayList<>();
        if (inner.getNetworkRuleSet() != null
                && inner.getNetworkRuleSet().getIpRules() != null) {
            for (IPRule rule : inner.getNetworkRuleSet().getIpRules()) {
                if (rule != null
                        && rule.getIPAddressOrRange() != null
                        && !rule.getIPAddressOrRange().contains("/")) {
                    ipAddresses.add(rule.getIPAddressOrRange());
                }
            }
        }
        return ipAddresses;
    }

    /**
     * The list of CIDR formatted ip address ranges having access to the given storage account.
     *
     * @param inner the storage account
     * @return list of ip address ranges in cidr format
     */
    static List<String> ipAddressRangesWithAccess(final StorageAccountInner inner) {
        List<String> ipAddressRanges = new ArrayList<>();
        if (inner.getNetworkRuleSet() != null
                && inner.getNetworkRuleSet().getIpRules() != null) {
            for (IPRule rule : inner.getNetworkRuleSet().getIpRules()) {
                if (rule != null
                        && rule.getIPAddressOrRange() != null
                        && rule.getIPAddressOrRange().contains("/")) {
                    ipAddressRanges.add(rule.getIPAddressOrRange());
                }
            }
        }
        return ipAddressRanges;
    }

    /**
     * Checks storage log entries can be read from any network.
     *
     * @param inner the storage account
     * @return true if storage log entries can be read from any network, false otherwise
     */
    static boolean canReadLogEntriesFromAnyNetwork(final StorageAccountInner inner) {
        if (inner.getNetworkRuleSet() != null
                && inner.getNetworkRuleSet().getDefaultAction() != null
                && inner.getNetworkRuleSet().getDefaultAction().equals(DefaultAction.DENY)) {
            Set<String> bypassSet = parseBypass(inner.getNetworkRuleSet().getBypass());
            return bypassSet.contains(Bypass.LOGGING.toString().toLowerCase());
        }
        return true;
    }

    /**
     * Checks storage metrics can be read from any network.
     *
     * @param inner the storage account
     * @return true if storage metrics can be read from any network, false otherwise
     */
    static boolean canReadMetricsFromAnyNetwork(final StorageAccountInner inner) {
        if (inner.getNetworkRuleSet() != null
                && inner.getNetworkRuleSet().getDefaultAction() != null
                && inner.getNetworkRuleSet().getDefaultAction().equals(DefaultAction.DENY)) {
            Set<String> bypassSet = parseBypass(inner.getNetworkRuleSet().getBypass());
            return bypassSet.contains(Bypass.METRICS.toString().toLowerCase());
        }
        return true;
    }

    /**
     * Checks storage account can be accessed from applications running on azure.
     *
     * @param inner the storage account
     * @return true if storage can be accessed from application running on azure, false otherwise
     */
    static boolean canAccessFromAzureServices(final StorageAccountInner inner) {
        if (inner.getNetworkRuleSet() != null
                && inner.getNetworkRuleSet().getDefaultAction() != null
                && inner.getNetworkRuleSet().getDefaultAction().equals(DefaultAction.DENY)) {
            Set<String> bypassSet = parseBypass(inner.getNetworkRuleSet().getBypass());
            return bypassSet.contains(Bypass.AZURE_SERVICES.toString().toLowerCase());
        }
        return true;
    }

    /**
     * Specifies that access to the storage account should be allowed from all networks.
     *
     * @return StorageNetworkRulesHelper
     */
    StorageNetworkRulesHelper withAccessFromAllNetworks() {
        NetworkRuleSet networkRuleSet = this.getNetworkRuleSetConfig(true);
        networkRuleSet.setDefaultAction(DefaultAction.ALLOW);
        return this;
    }

    /**
     * Specifies that access to the storage account should be allowed only from selected networks.
     *
     * @return StorageNetworkRulesHelper
     */
    StorageNetworkRulesHelper withAccessFromSelectedNetworks() {
        NetworkRuleSet networkRuleSet = this.getNetworkRuleSetConfig(true);
        networkRuleSet.setDefaultAction(DefaultAction.DENY);
        return this;
    }

    /**
     * Specifies that access to the storage account from the given network subnet should be allowed.
     *
     * @param subnetId the network subnet resource id
     * @return StorageNetworkRulesHelper
     */
    StorageNetworkRulesHelper withAccessFromNetworkSubnet(String subnetId) {
        NetworkRuleSet networkRuleSet = this.getNetworkRuleSetConfig(true);
        if (networkRuleSet.getVirtualNetworkRules() == null) {
            networkRuleSet.setVirtualNetworkRules(new ArrayList<VirtualNetworkRule>());
        }
        boolean found = false;
        for (VirtualNetworkRule rule : networkRuleSet.getVirtualNetworkRules()) {
            if (rule.getVirtualNetworkResourceId().equalsIgnoreCase(subnetId)) {
                found = true;
                break;
            }
        }
        if (!found) {
            networkRuleSet.getVirtualNetworkRules().add(new VirtualNetworkRule()
                    .setVirtualNetworkResourceId(subnetId)
                    .setAction(Action.ALLOW));
        }
        return this;
    }

    /**
     * Specifies that access to the storage account from the given ip address should be allowed.
     *
     * @param ipAddress the ip address
     * @return StorageNetworkRulesHelper
     */
    StorageNetworkRulesHelper withAccessFromIpAddress(String ipAddress) {
        return withAccessAllowedFromIpAddressOrRange(ipAddress);
    }

    /**
     * Specifies that access to the storage account from the given ip address range should be allowed.
     *
     * @param ipAddressCidr the ip address range in cidr format
     * @return StorageNetworkRulesHelper
     */
    StorageNetworkRulesHelper withAccessFromIpAddressRange(String ipAddressCidr) {
        return withAccessAllowedFromIpAddressOrRange(ipAddressCidr);
    }

    /**
     * Specifies that read access to the storage account logging should be allowed from all networks.
     *
     * @return StorageNetworkRulesHelper
     */
    StorageNetworkRulesHelper withReadAccessToLoggingFromAnyNetwork() {
        addToBypassList(Bypass.LOGGING);
        return this;
    }

    /**
     * Specifies that read access to the storage account metrics should be allowed from all networks.
     *
     * @return StorageNetworkRulesHelper
     */
    StorageNetworkRulesHelper withReadAccessToMetricsFromAnyNetwork() {
        addToBypassList(Bypass.METRICS);
        return this;
    }

    /**
     * Specifies that access to the storage account from application running on Azure should be allowed.
     *
     * @return StorageNetworkRulesHelper
     */
    StorageNetworkRulesHelper withAccessAllowedFromAzureServices() {
        addToBypassList(Bypass.AZURE_SERVICES);
        return this;
    }

    /**
     * Specifies that existing access to the storage account from the given subnet should be removed.
     *
     * @param subnetId the network subnet resource id
     * @return StorageNetworkRulesHelper
     */
    StorageNetworkRulesHelper withoutNetworkSubnetAccess(String subnetId) {
        NetworkRuleSet networkRuleSet = this.getNetworkRuleSetConfig(false);
        if (networkRuleSet == null
                || networkRuleSet.getVirtualNetworkRules() == null
                || networkRuleSet.getVirtualNetworkRules().size() == 0) {
            return this;
        }
        int foundIndex = -1;
        int i = 0;
        for (VirtualNetworkRule rule : networkRuleSet.getVirtualNetworkRules()) {
            if (rule.getVirtualNetworkResourceId().equalsIgnoreCase(subnetId)) {
                foundIndex = i;
                break;
            }
            i++;
        }
        if (foundIndex != -1) {
            networkRuleSet.getVirtualNetworkRules().remove(foundIndex);
        }
        return this;
    }

    /**
     * Specifies that existing access to the storage account from the given ip address should be removed.
     *
     * @param ipAddress the ip address
     * @return StorageNetworkRulesHelper
     */
    StorageNetworkRulesHelper withoutIpAddressAccess(String ipAddress) {
        return withoutIpAddressOrRangeAccess(ipAddress);
    }

    /**
     * Specifies that existing access to the storage account from the given ip address range should be removed.
     *
     * @param ipAddressCidr the ip address range in cidr format
     * @return StorageNetworkRulesHelper
     */
    StorageNetworkRulesHelper withoutIpAddressRangeAccess(String ipAddressCidr) {
        return withoutIpAddressOrRangeAccess(ipAddressCidr);
    }

    /**
     * Specifies that previously added read access exception to the storage logging from any network
     * should be removed.
     *
     * @return StorageNetworkRulesHelper
     */
    StorageNetworkRulesHelper withoutReadAccessToLoggingFromAnyNetwork() {
        removeFromBypassList(Bypass.LOGGING);
        return this;
    }

    /**
     * Specifies that previously added read access exception to the storage metrics from any network
     * should be removed.
     *
     * @return StorageNetworkRulesHelper
     */
    StorageNetworkRulesHelper withoutReadAccessToMetricsFromAnyNetwork() {
        removeFromBypassList(Bypass.METRICS);
        return this;
    }

    /**
     * Specifies that previously added access exception to the storage account from application
     * running on azure should be removed.
     *
     * @return StorageNetworkRulesHelper
     */
    StorageNetworkRulesHelper withoutAccessFromAzureServices() {
        removeFromBypassList(Bypass.AZURE_SERVICES);
        return this;
    }

    /**
     * Add the given bypass to the list of bypass configured for the storage account.
     *
     * @param bypass access type to which default network access action is not applied.
     */
    private void addToBypassList(Bypass bypass) {
        NetworkRuleSet networkRuleSet = this.getNetworkRuleSetConfig(true);
        final String bypassStr = bypass.toString().toLowerCase();
        Set<String> bypassSet = parseBypass(networkRuleSet.getBypass());
        if (bypassStr.equalsIgnoreCase(BYPASS_NONE_STR)) {
            bypassSet.clear();
            bypassSet.add(BYPASS_NONE_STR);
        } else {
            if (bypassSet.contains(BYPASS_NONE_STR)) {
                bypassSet.remove(BYPASS_NONE_STR);
            }
            bypassSet.add(bypassStr);
        }
        networkRuleSet.setBypass(Bypass.fromString(String.join(", ", bypassSet)));
    }

    /**
     * Removes the given bypass from the list of bypass configured for the storage account.
     *
     * @param bypass access type to which default network access action is not applied.
     */
    private void removeFromBypassList(Bypass bypass) {
        NetworkRuleSet networkRuleSet = this.getNetworkRuleSetConfig(false);
        if (networkRuleSet == null) {
            return;
        } else {
            Set<String> bypassSet = parseBypass(networkRuleSet.getBypass());
            String bypassStr = bypass.toString().toLowerCase();
            if (bypassSet.contains(bypassStr)) {
                bypassSet.remove(bypassStr);
            }
            if (bypassSet.isEmpty() && !bypassStr.equalsIgnoreCase(BYPASS_NONE_STR)) {
                bypassSet.add(BYPASS_NONE_STR);
            }
            networkRuleSet.setBypass(Bypass.fromString(String.join(", ", bypassSet)));
        }
    }

    /**
     * The {@link NetworkRuleSet#getDefaultAction()} is a required property.
     * <p>
     * During create mode, this method sets the default action to DENY if it is already not set by the user
     * and user specifies at least one network rule or choose at least one exception.
     * <p>
     * When in update mode, this method set action to DENY only if there is no existing network rules and exception
     * hence this is the first time user is adding a network rule or exception and action is not explicitly set by user.
     * If there is any existing rules or exception, we honor currently configured action.
     */
    void setDefaultActionIfRequired() {
        if (isInCreateMode) {
            if (createParameters.getNetworkRuleSet() != null) {
                boolean hasAtLeastOneRule = false;

                if (createParameters.getNetworkRuleSet().getVirtualNetworkRules() != null
                        && createParameters.getNetworkRuleSet().getVirtualNetworkRules().size() > 0) {
                    hasAtLeastOneRule = true;
                } else if (createParameters.getNetworkRuleSet().getIpRules() != null
                        && createParameters.getNetworkRuleSet().getIpRules().size() > 0) {
                    hasAtLeastOneRule = true;
                }
                boolean anyException = createParameters.getNetworkRuleSet().getBypass() != null;
                if ((hasAtLeastOneRule || anyException) && createParameters.getNetworkRuleSet().getDefaultAction() == null) {
                    // If user specified at least one network rule or selected any exception
                    // and didn't choose the default access action then "DENY" access from
                    // unknown networks.
                    //
                    createParameters.getNetworkRuleSet().setDefaultAction(DefaultAction.DENY);
                    if (!anyException) {
                        // If user didn't select any by-pass explicitly then disable "all bypass"
                        // if this is not specified then by default service allows access from
                        // "azure-services".
                        //
                        createParameters.getNetworkRuleSet().setBypass(Bypass.NONE);
                    }
                }
            }
        } else {
            NetworkRuleSet currentRuleSet = this.inner.getNetworkRuleSet();

            final boolean hasNoExistingException = currentRuleSet != null && currentRuleSet.getBypass() == null;
            boolean hasExistingRules = false;

            if (currentRuleSet != null) {
                if (currentRuleSet.getVirtualNetworkRules() != null
                        && currentRuleSet.getVirtualNetworkRules().size() > 0) {
                    hasExistingRules = true;
                } else if (currentRuleSet.getIpRules() != null
                        && currentRuleSet.getIpRules().size() > 0) {
                    hasExistingRules = true;
                }
            }
            if (!hasExistingRules) {
                if (updateParameters.getNetworkRuleSet() != null) {
                    boolean anyRulesAddedFirstTime = false;

                    if (updateParameters.getNetworkRuleSet().getVirtualNetworkRules() != null
                            && updateParameters.getNetworkRuleSet().getVirtualNetworkRules().size() > 0) {
                        anyRulesAddedFirstTime = true;
                    } else if (updateParameters.getNetworkRuleSet().getIpRules() != null
                            && updateParameters.getNetworkRuleSet().getIpRules().size() > 0) {
                        anyRulesAddedFirstTime = true;
                    }
                    final boolean anyExceptionAddedFirstTime = !hasNoExistingException && updateParameters.getNetworkRuleSet().getBypass() != null;
                    if ((anyRulesAddedFirstTime || anyExceptionAddedFirstTime)
                            && updateParameters.getNetworkRuleSet().getDefaultAction() == null) {
                        // If there was no existing rules & exceptions and if user specified at least one
                        // network rule or exception and didn't choose the default access action for
                        // unknown networks then DENY access from unknown networks.
                        //
                        updateParameters.getNetworkRuleSet().setDefaultAction(DefaultAction.DENY);
                        if (!anyExceptionAddedFirstTime) {
                            // If user didn't select any by-pass explicitly then disable "all bypass"
                            // if this is not specified then by default service allows access from
                            // "azure-services".
                            //
                            createParameters.getNetworkRuleSet().setBypass(Bypass.NONE);
                        }
                    }
                }
            }
        }
    }

    /**
     * Specifies that access to the storage account should be allowed from the given ip address or ip address range.
     *
     * @param ipAddressOrRange the ip address or ip address range in cidr format
     * @return StorageNetworkRulesHelper
     */
    private StorageNetworkRulesHelper withAccessAllowedFromIpAddressOrRange(String ipAddressOrRange) {
        NetworkRuleSet networkRuleSet = this.getNetworkRuleSetConfig(true);
        if (networkRuleSet.getIpRules() == null) {
            networkRuleSet.setIpRules(new ArrayList<IPRule>());
        }
        boolean found = false;
        for (IPRule rule : networkRuleSet.getIpRules()) {
            if (rule.getIPAddressOrRange().equalsIgnoreCase(ipAddressOrRange)) {
                found = true;
                break;
            }
        }
        if (!found) {
            networkRuleSet.getIpRules().add(new IPRule()
                    .setIPAddressOrRange(ipAddressOrRange)
                    .setAction(Action.ALLOW));
        }
        return this;
    }

    /**
     * Specifies that existing access to the storage account from the given ip address or ip address range should be removed.
     *
     * @param ipAddressOrRange the ip address or ip address range in cidr format
     * @return StorageNetworkRulesHelper
     */
    private StorageNetworkRulesHelper withoutIpAddressOrRangeAccess(String ipAddressOrRange) {
        NetworkRuleSet networkRuleSet = this.getNetworkRuleSetConfig(false);
        if (networkRuleSet == null
                || networkRuleSet.getIpRules() == null
                || networkRuleSet.getIpRules().size() == 0) {
            return this;
        }
        int foundIndex = -1;
        int i = 0;
        for (IPRule rule : networkRuleSet.getIpRules()) {
            if (rule.getIPAddressOrRange().equalsIgnoreCase(ipAddressOrRange)) {
                foundIndex = i;
                break;
            }
            i++;
        }
        if (foundIndex != -1) {
            networkRuleSet.getIpRules().remove(foundIndex);
        }
        return this;
    }

    /**
     * Gets the network rule set.
     *
     * @param createIfNotExists flag indicating whether to create a network rule set config if it does not exists already
     * @return the network rule set
     */
    private NetworkRuleSet getNetworkRuleSetConfig(boolean createIfNotExists) {
        if (this.isInCreateMode) {
            if (this.createParameters.getNetworkRuleSet() == null) {
                if (createIfNotExists) {
                    this.createParameters.setNetworkRuleSet(new NetworkRuleSet());
                } else {
                    return null;
                }
            }
            return this.createParameters.getNetworkRuleSet();
        } else {
            if (this.updateParameters.getNetworkRuleSet() == null) {
                if (this.inner.getNetworkRuleSet() == null) {
                    if (createIfNotExists) {
                        this.updateParameters.setNetworkRuleSet(new NetworkRuleSet());
                    } else {
                        return null;
                    }
                } else {
                    // Create clone of current ruleSet
                    //
                    NetworkRuleSet clonedNetworkRuleSet = new NetworkRuleSet();
                    clonedNetworkRuleSet.setDefaultAction(this.inner.getNetworkRuleSet().getDefaultAction());
                    clonedNetworkRuleSet.setBypass(this.inner.getNetworkRuleSet().getBypass());
                    if (this.inner.getNetworkRuleSet().getVirtualNetworkRules() != null) {
                        clonedNetworkRuleSet.setVirtualNetworkRules(new ArrayList<VirtualNetworkRule>());
                        for (VirtualNetworkRule rule : this.inner.getNetworkRuleSet().getVirtualNetworkRules()) {
                            VirtualNetworkRule clonedRule = new VirtualNetworkRule()
                                    .setAction(rule.getAction())
                                    .setVirtualNetworkResourceId(rule.getVirtualNetworkResourceId());
                            clonedNetworkRuleSet.getVirtualNetworkRules().add(clonedRule);
                        }
                    }
                    if (this.inner.getNetworkRuleSet().getIpRules() != null) {
                        clonedNetworkRuleSet.setIpRules(new ArrayList<IPRule>());
                        for (IPRule rule : this.inner.getNetworkRuleSet().getIpRules()) {
                            IPRule clonedRule = new IPRule()
                                    .setAction(rule.getAction())
                                    .setIPAddressOrRange(rule.getIPAddressOrRange());
                            clonedNetworkRuleSet.getIpRules().add(clonedRule);
                        }
                    }
                    this.updateParameters.setNetworkRuleSet(clonedNetworkRuleSet);
                }
            }
            return this.updateParameters.getNetworkRuleSet();
        }
    }

    /**
     * Parses the given comma separated traffic sources to bypass and convert them to list.
     *
     * @param bypass comma separated traffic sources to bypass.
     * @return the bypass list
     */
    private static Set<String> parseBypass(Bypass bypass) {
        if (bypass == null) {
            return new TreeSet<>();
        } else {
            Set<String> bypassSet = new TreeSet<>();
            List<String> bypassStrList = Arrays.asList(bypass.toString().split(","));
            for (String s : bypassStrList) {
                s = s.trim().toLowerCase();
                if (!s.isEmpty() && !bypassSet.contains(s)) {
                    bypassSet.add(s);
                }
            }
            return bypassSet;
        }
    }
}