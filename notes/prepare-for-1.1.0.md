# Prepare for Azure Management Libraries for Java 1.1.0 #

Steps to migrate code that uses Azure Management Libraries for Java from 1.0 to 1.1 ...

> If this note missed any breaking changes, please open a pull request.


V1.1 is backwards compatible with V1.0 in the APIs intended for public use that reached the general availability (stable) stage in V1.0. 

Some breaking changes were introduced in APIs that were still in Beta in V1.0, as indicated by the `@Beta` annotation.

## GA'd APIs in V1.1

Some of the APIs that were still in Beta in V1.0 are now GA in V1.1, in particular:
- async methods
- all methods in CDN that were previously in Beta
- all methods and interfaces in Application Gateways that were previously in Beta


## Naming Changes ##

<table>
  <tr>
    <th align=left>Area</th>
    <th align=left>From</th>
    <th align=left>To</th>
    <th align=left>Ref</th>
  </tr>
  <tr>
      <td>Graph RBAC</td>
      <td><code>User</code></td>
      <td><code>ActiveDirectoryUser</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1655">#1655</a></td>
  </tr>
  <tr>
      <td>Graph RBAC</td>
      <td><code>Users</code></td>
      <td><code>ActiveDirectoryUsers</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1655">#1655</a></td>
  </tr>
  <tr>
      <td>Graph RBAC</td>
      <td><code>GraphRbacManager.users()</code></td>
      <td><code>GraphRbacManager.activeDirectoryUsers()</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1655">#1655</a></td>
  </tr>
</table>



## Changes in Return or Input Parameter Types ##

<table>
  <tr>
    <th align=left>Area</th>
    <th align=left>Method</th>
    <th align=left>From</th>
    <th align=left>To</th>
    <th align=left>Ref</th>
  </tr>
  <tr>
    <td>Networking</td>
    <td><code>ApplicationGatewayBackend.addresses()</code></td>
    <td><code>List&lt;&gt;</code></td>
    <td><code>Collection&lt;&gt;</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1694">#1694</a></td>
  </tr>
  <tr>
    <td>Networking</td>
    <td><code> ApplicationGatewayRequestRoutingRule.backendAddresses()</code></td>
    <td><code>List&lt;&gt;</code></td>
    <td><code>Collection&lt;&gt;</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1694">#1694</a></td>
  </tr>

  <tr>
    <td>CDN</td>
    <td><code>CdnEndpoint.contentTypesToCompress()</code></td>
    <td><code>List&lt;String&gt;</code></td>
    <td><code>Set&lt;String&gt;</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1634">#1634</a></td>
  </tr>
  <tr>
    <td>CDN</td>
    <td><code>CdnEndpoint.withContentTypesToCompress()</code></td>
    <td><code>List&lt;String&gt;</code></td>
    <td><code>Set&lt;String&gt;</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1634">#1634</a></td>
  </tr>
  <tr>
    <td>CDN</td>
    <td><code>CdnEndpoint.customDomains()</code></td>
    <td><code>List&lt;String&gt;</code></td>
    <td><code>Set&lt;String&gt;</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1634">#1634</a></td>
  </tr>
  <tr>
    <td>CDN</td>
    <td><code>CdnEndpoint.purgeContent() /  .purgeContentAsync()</code></td>
    <td><code>List&lt;String&gt;</code></td>
    <td><code>Set&lt;String&gt;</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1634">#1634</a></td>
  </tr>
  <tr>
    <td>CDN</td>
    <td><code>CdnEndpoint.loadContent() / .loadContentAsync()</code></td>
    <td><code>List&lt;String&gt;</code></td>
    <td><code>Set&lt;String&gt;</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1634">#1634</a></td>
  </tr>
  <tr>
    <td>CDN</td>
    <td><code>CdnEndpoint.withGeoFilters()</code></td>
    <td><code>List&lt;&gt;</code></td>
    <td><code>Collection&lt;&gt;</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1634">#1634</a></td>
  </tr>
  <tr>
    <td>CDN</td>
    <td><code>CdnEndpoint.withGeoFilter()</code></td>
    <td><code>List&lt;&gt;</code></td>
    <td><code>Collection&lt;&gt;</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1634">#1634</a></td>
  </tr>
  <tr>
    <td>CDN</td>
    <td><code>CdnProfile.purgeEndpointContent() / .purgeEndpointContentAsync()</code></td>
    <td><code>List&lt;String&gt;</code></td>
    <td><code>Set&lt;String&gt;</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1634">#1634</a></td>
  </tr>
  <tr>
    <td>CDN</td>
    <td><code>CdnProfile.loadEndpointContent() / .loadEndpointContentAsync()</code></td>
    <td><code>List&lt;String&gt;</code></td>
    <td><code>Set&lt;String&gt;</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1634">#1634</a></td>
  </tr>
  <tr>
    <td>KeyVault</td>
    <td><code>Vault.defineAccessPolicy().forUser()</code></td>
    <td><code>User</code></td>
    <td><code>ActiveDirectoryUser</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1655">#1655</a></td>
  </tr>
</table>


## API Removals ##

<table>
  <tr>
    <th>Removed</th>
    <th>Alternate to switch to</th>
    <th>PR</th>
  </tr>
  <tr>
    <td><code>ApplicationGateway.sslPolicy()</code></td>
    <td><code>ApplicationGateway.disabledSslProtocols()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1703">#1703</a></td>
  </tr>
  <tr>
    <td><code>RuntimeStack.NODEJS_6_9_3</code></td>
    <td><code>RuntimeStack.NODEJS_6_9</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1739">#1739</a></td>
  </tr>
  <tr>
    <td><code>RuntimeStack.NODEJS_6_6_0</code></td>
    <td><code>RuntimeStack.NODEJS_6_6</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1739">#1739</a></td>
  </tr>
  <tr>
    <td><code>RuntimeStack.NODEJS_6_2_2</code></td>
    <td><code>RuntimeStack.NODEJS_6_2</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1739">#1739</a></td>
  </tr>
  <tr>
    <td><code>RuntimeStack.NODEJS_4_5_0</code></td>
    <td><code>RuntimeStack.NODEJS_4_5</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1739">#1739</a></td>
  </tr>
  <tr>
    <td><code>RuntimeStack.NODEJS_4_4_7</code></td>
    <td><code>RuntimeStack.NODEJS_4_4</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1739">#1739</a></td>
  </tr>
  <tr>
    <td><code>RuntimeStack.PHP_5_6_23</code></td>
    <td><code>RuntimeStack.PHP_5_6</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1739">#1739</a></td>
  </tr>
  <tr>
    <td><code>RuntimeStack.PHP_7_0_6</code></td>
    <td><code>RuntimeStack.PHP_7_0</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1739">#1739</a></td>
  </tr>
</table>

