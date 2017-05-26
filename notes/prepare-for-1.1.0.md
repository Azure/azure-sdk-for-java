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
      <td><code>Application</code></td>
      <td><code>ActiveDirectoryApplication</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1699">#1699</a></td>
  </tr>
  <tr>
      <td>Graph RBAC</td>
      <td><code>Applications</code></td>
      <td><code>ActiveDirectoryApplications</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1699">#1699</a></td>
  </tr>
  <tr>
      <td>Graph RBAC</td>
      <td><code>Azure.Authenticated.applications()</code></td>
      <td><code>Azure.Authenticated.activeDirectoryApplications()</code></td>
      <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1699">#1699</a></td>
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
</table>

