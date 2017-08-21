# Prepare for Azure Management Libraries for Java 1.2.0 #

Steps to migrate code that uses Azure Management Libraries for Java from 1.1 to 1.2 ...

> If this note missed any breaking changes, please open a pull request.


V1.2 is backwards compatible with V1.1 in the APIs intended for public use that reached the general availability (stable) stage in V1.0. 

Some breaking changes were introduced in APIs that were still in Beta in V1.1, as indicated by the `@Beta` annotation.

## Load Balancer API rework

The load balancer API, which has been in Beta, has undergone a major rework to further simplify the user model, to achieve greater consistency with 
the application gateway model, as well as to enable additional configuration scenarios that became possible as a result of more recent enhancements
in the Azure Load Balancer service.

The rework includes naming changes, some API removals, some additions, as well as a reordering of the load balancer definition flow.


### Definition flow changes

#### Simplified required flow

Other than the usual requirement for a region and a resource group at the beginning of a load balancer definition flow, the only other syntactically required element is the definition of at least one load balancing rule (`.defineLoadBalancingRule()`), OR an inbound NAT rule (`.defineInboundNatRule()`), OR an inbound NAT pool (`.defineInboundNatPool()`). Attaching at least one of these child elements results in a minimally functional and useful load balancer configuration. This change also enables the creation of load balancers which only have NAT rules or NAT pools and no LB rules, which was not possible in the earlier versions of the SDK. 

#### Frontends optional

Previously, at least one explicit frontend definition was required near the beginning of a load balancer definition flow, which could then be referred to from load balancing rules, NAT rules, and/or NAT pools. Although a frontend IP configuration is still a required child element of a load balancer under the hood, there are now methods within the LB rule, NAT rule and NAT pool definition flows which create automatically-named frontends implicitly, via a reference to a new or existing public IP address (`.fromExistingPublicIPAddress(pip)` | `.fromNewPublicIPAddress(dnsLabel)`) or to an existing virtual network subnet (`.fromExistingSubnet(network, subnetName)`). The former would result in the creation of a public (Internet-facing) frontend, whereas the latter would create a private internal frontend.

Note that if the same public IP reference is used by two or more rules/pools, they will all be automatically associated with the same frontend IP configuration under the hood, since multiple frontends pointing to the same public IP address are not allowed by the underlying service. Analogous logic applies to subnets.

Hence, frontend definitions are now in the optional ("creatable") section of the load balancer definition. Note however that if any of these \*rules or \*pools reference a frontend *by name*, rather than implicitly by a public IP address or subnet, then a frontend with that name MUST be defined explicitly later in that load balancer definition flow, despite of its "optional" status.

#### Probes optional
Probe definitions (`.defineHttpProbe() | .defineTcpProbe()`) have now been moved into the optional ("creatable") section of the load balancer definition flow as they are no longer required by the underlying service. Default probes are provided by Azure if no explicit probes are defined.

#### Backends optional
Backend definitions (`.defineBackend()`) have now been moved into the optional ("creatable") section of the load balancer definition flow. Backends can be implicitly created by the load balancing or NAT rule definitions by merely referencing their name (`.toBackend(name)`).

#### No more "default" child elements
Previous releases of the load balancer API supported a notion of "default" child elements, such as default frontends, backends, etc. Those elements, when created implicitly, would assume the name "default" and would be the ones that other methods would refer to in implicit ways. In some sense, these "default" child elements were "global" across the load balancer definition.

This entire notion of a "default" child element of a load balancer has now been removed. The name "default" no longer has any special meaning and the methods operating on such default elements have been either removed or reworked. For example, `.withExistingPublicIPAddress()` which would have created a public frontend named "default" no longer appears in the load balancer definition flow. As mentioned earlier, certain child elements still can be created implicitly (e.g. frontends), but the names generated for them in such cases are unique and associated with the context they are created from (i.e., for example a specific load balancing rule).


### Renames

The following naming associated with load balancers has been changed in ways that break backwards compatibility with the previous Beta releases of this API:

<table>
  <tr>
    <th align=left>From</th>
    <th align=left>To</th>
    <th align=left>Ref</th>
  </tr>
  <tr>
      <td><code>LoadBalancer.updateInternetFrontend()</code></td>
      <td><code>LoadBalancer.updatePublicFrontend()</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1773/commits/1c147f279776b12bbfca8009795f2b49041bf25b">bf25b</a></td>
  </tr>
  <tr>
      <td><code>LoadBalancer.updateInternalFrontend()</code></td>
      <td><code>LoadBalancer.updatePrivateFrontend()</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1773/commits/1c147f279776b12bbfca8009795f2b49041bf25b">bf25b</a></td>
  </tr>
  <tr>
      <td><code>LoadBalancingRule.withFrontend()</code></td>
      <td><code>LoadBalancingRule.fromFrontend()</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1773/commits/687fef73af7cd1921a7d3da224a317a1152bd408">bd408</a></td>
  </tr>
  <tr>
      <td><code>LoadBalancerInboundNatRule.withFrontend()</code></td>
      <td><code>LoadBalancerRuleInboundNatRule.fromFrontend()</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1773/commits/687fef73af7cd1921a7d3da224a317a1152bd408">bd408</a></td>
  </tr>
  <tr>
      <td><code>LoadBalancerInboundNatPool.withFrontend()</code></td>
      <td><code>LoadBalancerRuleInboundNatPool.fromFrontend()</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1773/commits/687fef73af7cd1921a7d3da224a317a1152bd408">bd408</a></td>
  </tr>
  <tr>
      <td><code>LoadBalancingRule.withBackend()</code></td>
      <td><code>LoadBalancingRule.toBackend()</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1773/commits/6fe1735480480b08c0733d707338fbb981b1e97e">1e97e</a></td>
  </tr>
  <tr>
      <td><code>LoadBalancingRule.withBackendPort()</code></td>
      <td><code>LoadBalancingRule.toBackendPort()</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1773/commits/9299b33b0c04b11b35030d2b881fbd2651e047e9">047e9</a></td>
  </tr>
  <tr>
      <td><code>LoadBalancerInboundNatRule.withBackendPort()</code></td>
      <td><code>LoadBalancerInboundNatRule.toBackendPort()</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1773/commits/9299b33b0c04b11b35030d2b881fbd2651e047e9">047e9</a></td>
  </tr>
  <tr>
      <td><code>LoadBalancerInboundNatPool.withBackendPort()</code></td>
      <td><code>LoadBalancerInboundNatPool.toBackendPort()</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1773/commits/9299b33b0c04b11b35030d2b881fbd2651e047e9">047e9</a></td>
  </tr>
  <tr>
      <td><code>LoadBalancingRule.withFrontendPort()</code></td>
      <td><code>LoadBalancingRule.fromFrontendPort()</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1773/commits/e495e2ae2ab1c28b744d49f2be0fc42daa5951b2">951b2</a></td>
  </tr>
  <tr>
      <td><code>LoadBalancerInboundNatRule.withFrontendPort()</code></td>
      <td><code>LoadBalancerInboundNatRule.fromFrontendPort()</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1773/commits/e495e2ae2ab1c28b744d49f2be0fc42daa5951b2">951b2</a></td>
  </tr>
  <tr>
      <td><code>LoadBalancerInboundNatPool.withFrontendPortRange()</code></td>
      <td><code>LoadBalancerInboundNatPool.fromFrontendPortRange()</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1773/commits/7fff98060f6a72462d266d716be6d8f995f52da0">52da0</a></td>
  </tr>
  <tr>
      <td><code>LoadBalancingRule.withExistingPublicIPAddress()</code></td>
      <td><code>LoadBalancingRule.fromExistingPublicIPAddress()</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1814/commits/bb3df1a20834397d0ccc0279ab25a8e9c937ef84">7ef84</a></td>
  </tr>
  <tr>
      <td><code>LoadBalancerInboundNatRule.withExistingPublicIPAddress()</code></td>
      <td><code>LoadBalancerInboundNatRule.fromExistingPublicIPAddress()</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1814/commits/bb3df1a20834397d0ccc0279ab25a8e9c937ef84">7ef84</a></td>
  </tr>
  <tr>
      <td><code>LoadBalancerInboundNatPool.withExistingPublicIPAddress()</code></td>
      <td><code>LoadBalancerInboundNatPool.fromExistingPublicIPAddress()</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1814/commits/bb3df1a20834397d0ccc0279ab25a8e9c937ef84">7ef84</a></td>
  </tr>
 
</table>

### API Removals

<table>
  <tr>
    <th>Removed</th>
    <th>Alternate to switch to</th>
    <th>PR</th>
  </tr>
  <tr>
    <td><code>LoadBalancer.withNewPublicIPAddress()</code></td>
    <td><code>LoadBalancerPublicFrontend.withNewPublicIPAddress()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1814/commits/07267a06a7b5d54687e5ce7ddbb6fe0f54d378d0">378d0</a></td>
  </tr>
  <tr>
    <td><code>LoadBalancer.withExistingPublicIPAddress()</code></td>
    <td><code>LoadBalancerPublicFrontend.withExistingPublicIPAddress()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1814/commits/07267a06a7b5d54687e5ce7ddbb6fe0f54d378d0">378d0</a></td>
  </tr>
  <tr>
    <td><code>LoadBalancer.withFrontendSubnet()</code></td>
    <td><code>LoadBalancerPrivateFrontend.withExistingSubnet()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1814/commits/7435a71181b5d8506b9f2d54c57b19da3d7da5cf">378d0</a></td>
  </tr>  
  <tr>
    <td><code>LoadBalancer.withLoadBalancingRule()</code></td>
    <td><code>LoadBalancer.defineLoadBalancingRule()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1773/commits/4d05d7793ac4db9cd409b071e260da08d15dc191">dc191</a></td>
  </tr>
  <tr>
    <td><code>LoadBalancer.withHttpProbe()</code></td>
    <td><code>LoadBalancer.defineHttpProbe()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1773/commits/68bddde8fd8298ee76f7f712ab92d2fb5d90b802">0b802</a></td>
  </tr>
  <tr>
    <td><code>LoadBalancer.withTcpProbe()</code></td>
    <td><code>LoadBalancer.defineTcpProbe()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1773/commits/68bddde8fd8298ee76f7f712ab92d2fb5d90b802">0b802</a></td>
  </tr>
  <tr>
    <td><code>LoadBalancer.withExistingVirtualMachines()</code></td>
    <td><code>LoadBalancerBackend.withExistingVirtualMachines()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1814/commits/40696a75aa6c3395f323d242d13fb0ded867a80c">7a80c</a></td>
  </tr>
</table>

## DocumentDB API renamed to CosmosDB.

The document db API which was in the com.microsoft.azure.management.documentdb namespace has been moved to com.microsoft.azure.management.cosmosdb. The POM artifact id has been changed from azure-mgmt-documentdb to azure-mgmt-cosmosdb. 

### Renames

The following naming associated with document db has been changed in ways that break backwards compatibility with the previous releases of this API:

<table>
  <tr>
    <th align=left>From</th>
    <th align=left>To</th>
    <th align=left>Ref</th>
  </tr>
  <tr>
      <td><code>DocumentDBAccount</code></td>
      <td><code>CosmosDBAccount</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1856">#1856</a></td>
  </tr>
  <tr>
      <td><code>DocumentDBAccounts</code></td>
      <td><code>CosmosDBAccounts</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1856">#1856</a></td>
  </tr>
  <tr>
      <td><code>DocumentDBManager.databaseAccounts</code></td>
      <td><code>CosmosDBManager.databaseAccounts</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1856">#1856</a></td>
  </tr>
</table>

## Other changes
### Naming changes

<table>
  <tr>
    <th align=left>Area</th>
    <th align=left>From</th>
    <th align=left>To</th>
    <th align=left>Ref</th>
  </tr>
  <tr>
      <td>TBD</td>
      <td><code>TBD</code></td>
      <td><code>TBD</code></td>
      <td><a href="">TBD</a></td>
  </tr>
</table>


### Changes in return or input parameter types

<table>
  <tr>
    <th align=left>Area</th>
    <th align=left>Method</th>
    <th align=left>From</th>
    <th align=left>To</th>
    <th align=left>Ref</th>
  </tr>
  <tr>
    <td>TBD</td>
    <td><code>TBD</code></td>
    <td><code>TBD</code></td>
    <td><code>TBD</code></td>
    <td><a href="">TBD</a></td>
  </tr>
</table>


### API Removals

<table>
  <tr>
    <th>Removed</th>
    <th>Alternate to switch to</th>
    <th>PR</th>
  </tr>
  <tr>
    <td><code>TBD</code></td>
    <td><code>TBD</code></td>
    <td><a href="">TBD</a></td>
  </tr>
</table>

