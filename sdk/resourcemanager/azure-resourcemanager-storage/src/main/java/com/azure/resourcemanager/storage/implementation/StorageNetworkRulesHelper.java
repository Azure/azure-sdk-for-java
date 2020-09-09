// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.implementation;

import com.azure.resourcemanager.storage.models.Action;
import com.azure.resourcemanager.storage.models.Bypass;
import com.azure.resourcemanager.storage.models.DefaultAction;
import com.azure.resourcemanager.storage.models.IpRule;
import com.azure.resourcemanager.storage.models.NetworkRuleSet;
import com.azure.resourcemanager.storage.models.StorageAccountCreateParameters;
import com.azure.resourcemanager.storage.models.StorageAccountUpdateParameters;
import com.azure.resourcemanager.storage.models.VirtualNetworkRule;
import com.azure.resourcemanager.storage.fluent.inner.StorageAccountInner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/** Helper to operate on storage account NetworkRule set {@link StorageAccountInner#networkRuleSet()} property. */
final class StorageNetworkRulesHelper {
    private static final String BYPASS_NONE_STR = Bypass.NONE.toString().toLowerCase(Locale.ROOT);
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
     * @param inner the current state of storage account
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
        if (inner.networkRuleSet() == null || inner.networkRuleSet().defaultAction() == null) {
            return true;
        }
        return inner.networkRuleSet().defaultAction().equals(DefaultAction.ALLOW);
    }

    /**
     * The list of resource id of subnets having access to the given storage account.
     *
     * @param inner the storage account
     * @return list of subnet resource ids
     */
    static List<String> networkSubnetsWithAccess(final StorageAccountInner inner) {
        List<String> subnetIds = new ArrayList<>();
        if (inner.networkRuleSet() != null && inner.networkRuleSet().virtualNetworkRules() != null) {
            for (VirtualNetworkRule rule : inner.networkRuleSet().virtualNetworkRules()) {
                if (rule != null && rule.virtualNetworkResourceId() != null) {
                    subnetIds.add(rule.virtualNetworkResourceId());
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
        if (inner.networkRuleSet() != null && inner.networkRuleSet().ipRules() != null) {
            for (IpRule rule : inner.networkRuleSet().ipRules()) {
                if (rule != null && rule.ipAddressOrRange() != null && !rule.ipAddressOrRange().contains("/")) {
                    ipAddresses.add(rule.ipAddressOrRange());
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
        if (inner.networkRuleSet() != null && inner.networkRuleSet().ipRules() != null) {
            for (IpRule rule : inner.networkRuleSet().ipRules()) {
                if (rule != null && rule.ipAddressOrRange() != null && rule.ipAddressOrRange().contains("/")) {
                    ipAddressRanges.add(rule.ipAddressOrRange());
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
        if (inner.networkRuleSet() != null
            && inner.networkRuleSet().defaultAction() != null
            && inner.networkRuleSet().defaultAction().equals(DefaultAction.DENY)) {
            Set<String> bypassSet = parseBypass(inner.networkRuleSet().bypass());
            return bypassSet.contains(Bypass.LOGGING.toString().toLowerCase(Locale.ROOT));
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
        if (inner.networkRuleSet() != null
            && inner.networkRuleSet().defaultAction() != null
            && inner.networkRuleSet().defaultAction().equals(DefaultAction.DENY)) {
            Set<String> bypassSet = parseBypass(inner.networkRuleSet().bypass());
            return bypassSet.contains(Bypass.METRICS.toString().toLowerCase(Locale.ROOT));
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
        if (inner.networkRuleSet() != null
            && inner.networkRuleSet().defaultAction() != null
            && inner.networkRuleSet().defaultAction().equals(DefaultAction.DENY)) {
            Set<String> bypassSet = parseBypass(inner.networkRuleSet().bypass());
            return bypassSet.contains(Bypass.AZURE_SERVICES.toString().toLowerCase(Locale.ROOT));
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
        networkRuleSet.withDefaultAction(DefaultAction.ALLOW);
        return this;
    }

    /**
     * Specifies that access to the storage account should be allowed only from selected networks.
     *
     * @return StorageNetworkRulesHelper
     */
    StorageNetworkRulesHelper withAccessFromSelectedNetworks() {
        NetworkRuleSet networkRuleSet = this.getNetworkRuleSetConfig(true);
        networkRuleSet.withDefaultAction(DefaultAction.DENY);
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
        if (networkRuleSet.virtualNetworkRules() == null) {
            networkRuleSet.withVirtualNetworkRules(new ArrayList<VirtualNetworkRule>());
        }
        boolean found = false;
        for (VirtualNetworkRule rule : networkRuleSet.virtualNetworkRules()) {
            if (rule.virtualNetworkResourceId().equalsIgnoreCase(subnetId)) {
                found = true;
                break;
            }
        }
        if (!found) {
            networkRuleSet
                .virtualNetworkRules()
                .add(new VirtualNetworkRule().withVirtualNetworkResourceId(subnetId).withAction(Action.ALLOW));
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
            || networkRuleSet.virtualNetworkRules() == null
            || networkRuleSet.virtualNetworkRules().size() == 0) {
            return this;
        }
        int foundIndex = -1;
        int i = 0;
        for (VirtualNetworkRule rule : networkRuleSet.virtualNetworkRules()) {
            if (rule.virtualNetworkResourceId().equalsIgnoreCase(subnetId)) {
                foundIndex = i;
                break;
            }
            i++;
        }
        if (foundIndex != -1) {
            networkRuleSet.virtualNetworkRules().remove(foundIndex);
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
     * Specifies that previously added read access exception to the storage logging from any network should be removed.
     *
     * @return StorageNetworkRulesHelper
     */
    StorageNetworkRulesHelper withoutReadAccessToLoggingFromAnyNetwork() {
        removeFromBypassList(Bypass.LOGGING);
        return this;
    }

    /**
     * Specifies that previously added read access exception to the storage metrics from any network should be removed.
     *
     * @return StorageNetworkRulesHelper
     */
    StorageNetworkRulesHelper withoutReadAccessToMetricsFromAnyNetwork() {
        removeFromBypassList(Bypass.METRICS);
        return this;
    }

    /**
     * Specifies that previously added access exception to the storage account from application running on azure should
     * be removed.
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
        final String bypassStr = bypass.toString().toLowerCase(Locale.ROOT);
        Set<String> bypassSet = parseBypass(networkRuleSet.bypass());
        if (bypassStr.equalsIgnoreCase(BYPASS_NONE_STR)) {
            bypassSet.clear();
            bypassSet.add(BYPASS_NONE_STR);
        } else {
            if (bypassSet.contains(BYPASS_NONE_STR)) {
                bypassSet.remove(BYPASS_NONE_STR);
            }
            bypassSet.add(bypassStr);
        }
        networkRuleSet.withBypass(Bypass.fromString(String.join(", ", bypassSet)));
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
            Set<String> bypassSet = parseBypass(networkRuleSet.bypass());
            String bypassStr = bypass.toString().toLowerCase(Locale.ROOT);
            if (bypassSet.contains(bypassStr)) {
                bypassSet.remove(bypassStr);
            }
            if (bypassSet.isEmpty() && !bypassStr.equalsIgnoreCase(BYPASS_NONE_STR)) {
                bypassSet.add(BYPASS_NONE_STR);
            }
            networkRuleSet.withBypass(Bypass.fromString(String.join(", ", bypassSet)));
        }
    }

    /**
     * The {@link NetworkRuleSet#defaultAction()} is a required property.
     *
     * <p>During create mode, this method sets the default action to DENY if it is already not set by the user and user
     * specifies at least one network rule or choose at least one exception.
     *
     * <p>When in update mode, this method set action to DENY only if there is no existing network rules and exception
     * hence this is the first time user is adding a network rule or exception and action is not explicitly set by user.
     * If there is any existing rules or exception, we honor currently configured action.
     */
    void setDefaultActionIfRequired() {
        if (isInCreateMode) {
            if (createParameters.networkRuleSet() != null) {
                boolean hasAtLeastOneRule = false;

                if (createParameters.networkRuleSet().virtualNetworkRules() != null
                    && createParameters.networkRuleSet().virtualNetworkRules().size() > 0) {
                    hasAtLeastOneRule = true;
                } else if (createParameters.networkRuleSet().ipRules() != null
                    && createParameters.networkRuleSet().ipRules().size() > 0) {
                    hasAtLeastOneRule = true;
                }
                boolean anyException = createParameters.networkRuleSet().bypass() != null;
                if ((hasAtLeastOneRule || anyException) && createParameters.networkRuleSet().defaultAction() == null) {
                    // If user specified at least one network rule or selected any exception
                    // and didn't choose the default access action then "DENY" access from
                    // unknown networks.
                    //
                    createParameters.networkRuleSet().withDefaultAction(DefaultAction.DENY);
                    if (!anyException) {
                        // If user didn't select any by-pass explicitly then disable "all bypass"
                        // if this is not specified then by default service allows access from
                        // "azure-services".
                        //
                        createParameters.networkRuleSet().withBypass(Bypass.NONE);
                    }
                }
            }
        } else {
            NetworkRuleSet currentRuleSet = this.inner.networkRuleSet();

            final boolean hasNoExistingException = currentRuleSet != null && currentRuleSet.bypass() == null;
            boolean hasExistingRules = false;

            if (currentRuleSet != null) {
                if (currentRuleSet.virtualNetworkRules() != null && currentRuleSet.virtualNetworkRules().size() > 0) {
                    hasExistingRules = true;
                } else if (currentRuleSet.ipRules() != null && currentRuleSet.ipRules().size() > 0) {
                    hasExistingRules = true;
                }
            }
            if (!hasExistingRules) {
                if (updateParameters.networkRuleSet() != null) {
                    boolean anyRulesAddedFirstTime = false;

                    if (updateParameters.networkRuleSet().virtualNetworkRules() != null
                        && updateParameters.networkRuleSet().virtualNetworkRules().size() > 0) {
                        anyRulesAddedFirstTime = true;
                    } else if (updateParameters.networkRuleSet().ipRules() != null
                        && updateParameters.networkRuleSet().ipRules().size() > 0) {
                        anyRulesAddedFirstTime = true;
                    }
                    final boolean anyExceptionAddedFirstTime =
                        !hasNoExistingException && updateParameters.networkRuleSet().bypass() != null;
                    if ((anyRulesAddedFirstTime || anyExceptionAddedFirstTime)
                        && updateParameters.networkRuleSet().defaultAction() == null) {
                        // If there was no existing rules & exceptions and if user specified at least one
                        // network rule or exception and didn't choose the default access action for
                        // unknown networks then DENY access from unknown networks.
                        //
                        updateParameters.networkRuleSet().withDefaultAction(DefaultAction.DENY);
                        if (!anyExceptionAddedFirstTime) {
                            // If user didn't select any by-pass explicitly then disable "all bypass"
                            // if this is not specified then by default service allows access from
                            // "azure-services".
                            //
                            createParameters.networkRuleSet().withBypass(Bypass.NONE);
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
        if (networkRuleSet.ipRules() == null) {
            networkRuleSet.withIpRules(new ArrayList<IpRule>());
        }
        boolean found = false;
        for (IpRule rule : networkRuleSet.ipRules()) {
            if (rule.ipAddressOrRange().equalsIgnoreCase(ipAddressOrRange)) {
                found = true;
                break;
            }
        }
        if (!found) {
            networkRuleSet.ipRules().add(new IpRule().withIpAddressOrRange(ipAddressOrRange).withAction(Action.ALLOW));
        }
        return this;
    }

    /**
     * Specifies that existing access to the storage account from the given ip address or ip address range should be
     * removed.
     *
     * @param ipAddressOrRange the ip address or ip address range in cidr format
     * @return StorageNetworkRulesHelper
     */
    private StorageNetworkRulesHelper withoutIpAddressOrRangeAccess(String ipAddressOrRange) {
        NetworkRuleSet networkRuleSet = this.getNetworkRuleSetConfig(false);
        if (networkRuleSet == null || networkRuleSet.ipRules() == null || networkRuleSet.ipRules().size() == 0) {
            return this;
        }
        int foundIndex = -1;
        int i = 0;
        for (IpRule rule : networkRuleSet.ipRules()) {
            if (rule.ipAddressOrRange().equalsIgnoreCase(ipAddressOrRange)) {
                foundIndex = i;
                break;
            }
            i++;
        }
        if (foundIndex != -1) {
            networkRuleSet.ipRules().remove(foundIndex);
        }
        return this;
    }

    /**
     * Gets the network rule set.
     *
     * @param createIfNotExists flag indicating whether to create a network rule set config if it does not exists
     *     already
     * @return the network rule set
     */
    private NetworkRuleSet getNetworkRuleSetConfig(boolean createIfNotExists) {
        if (this.isInCreateMode) {
            if (this.createParameters.networkRuleSet() == null) {
                if (createIfNotExists) {
                    this.createParameters.withNetworkRuleSet(new NetworkRuleSet());
                } else {
                    return null;
                }
            }
            return this.createParameters.networkRuleSet();
        } else {
            if (this.updateParameters.networkRuleSet() == null) {
                if (this.inner.networkRuleSet() == null) {
                    if (createIfNotExists) {
                        this.updateParameters.withNetworkRuleSet(new NetworkRuleSet());
                    } else {
                        return null;
                    }
                } else {
                    // Create clone of current ruleSet
                    //
                    NetworkRuleSet clonedNetworkRuleSet = new NetworkRuleSet();
                    clonedNetworkRuleSet.withDefaultAction(this.inner.networkRuleSet().defaultAction());
                    clonedNetworkRuleSet.withBypass(this.inner.networkRuleSet().bypass());
                    if (this.inner.networkRuleSet().virtualNetworkRules() != null) {
                        clonedNetworkRuleSet.withVirtualNetworkRules(new ArrayList<VirtualNetworkRule>());
                        for (VirtualNetworkRule rule : this.inner.networkRuleSet().virtualNetworkRules()) {
                            VirtualNetworkRule clonedRule =
                                new VirtualNetworkRule()
                                    .withAction(rule.action())
                                    .withVirtualNetworkResourceId(rule.virtualNetworkResourceId());
                            clonedNetworkRuleSet.virtualNetworkRules().add(clonedRule);
                        }
                    }
                    if (this.inner.networkRuleSet().ipRules() != null) {
                        clonedNetworkRuleSet.withIpRules(new ArrayList<IpRule>());
                        for (IpRule rule : this.inner.networkRuleSet().ipRules()) {
                            IpRule clonedRule =
                                new IpRule().withAction(rule.action()).withIpAddressOrRange(rule.ipAddressOrRange());
                            clonedNetworkRuleSet.ipRules().add(clonedRule);
                        }
                    }
                    this.updateParameters.withNetworkRuleSet(clonedNetworkRuleSet);
                }
            }
            return this.updateParameters.networkRuleSet();
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
                s = s.trim().toLowerCase(Locale.ROOT);
                if (!s.isEmpty() && !bypassSet.contains(s)) {
                    bypassSet.add(s);
                }
            }
            return bypassSet;
        }
    }
}
