# Prepare for Azure Management Libraries for Java 1.0.0-beta3#

Steps to migrate code that uses Azure Management Libraries for Java from beta 2 to beta 3 …

> If this note missed any breaking changes, please open a pull request.

# Replace Import Statement #

<table>
  <tr>
    <th>Replace</th>
    <th>With</th>
    <th>Ref</th>
  </tr>
  <tr>
    <td><code>import com.microsoft.azure.Azure</code></td>
    <td><code>import com.microsoft.azure.management.Azure</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1144">#1144</a></td>
  </tr>
</table>

# Change Method Names #

<table>
  <tr>
    <th>From</th>
    <th>To</th>
    <th>Ref</th>
  </tr>
  <tr>
    <td><code>Subnet.networkSecurityGroup()</code></td>
    <td><code>Subnet.getNetworkSecurityGroup()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1140">#1140</a></td>
  </tr>
  <tr>
    <td><code>Network.dsnServerIPs()</code></td>
    <td><code>Network.dnsServerIps()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1140">#1140</a></td>
  </tr>
  <tr>
    <td><code>NicIpConfiguration.publicIpAddress()</code></td>
    <td><code>NicIpConfiguration.getPublicIpAddress()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1083">#1083</a></td>
  </tr>
  <tr>
    <td><code>NicIpConfiguration.network()</code></td>
    <td><code>NicIpConfiguration.getNetwork()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1083">#1083</a></td>
  </tr>
  <tr>
    <td><code>NetworkInterface.networkSecurityGroup()</code></td>
    <td><code>NetworkInterface.getNetworkSecurityGroup()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1065">#1065</a></td>
  </tr>
  <tr>
    <td><code>NicIpConfiguration.privateIp()</code></td>
    <td><code>NicIpConfiguration.privateIpAddress()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1055">#1055</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.primaryPublicIpAddress()</code></td>
    <td><code>VirtualMachine.getPrimaryPublicIpAddress()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1090">#1090</a></td>
  </tr>
  <tr>
    <td><code>StorageAccount.refreshKeys()</code></td>
    <td><code>StorageAccount.getKeys()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1090">#1090</a></td>
  </tr>
  <tr>
    <td><code>NetworkInterface.primaryNetwork()</code></td>
    <td><code>NetworkInterface.getPrimaryNetwork()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1090">#1090</a></td>
  </tr>
</table>

# Change Receiving Variable Type #

<table>
  <tr>
    <th>From</th>
    <th>To</th>
    <th>For Method</th>
    <th>Ref</th>
  </tr>
  <tr>
    <td><code>List</code></td>
    <td><code>Map</code></td>
    <td><code>NetworkInterface.ipConfigurations()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1055">#1055</a></td>
  </tr>
  <tr>
    <td><code>List</code></td>
    <td><code>Map</code></td>
    <td><code>VirtualMachine.resources()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1045">#1045</a></td>
  </tr>
  <tr>
    <td><code>List</code></td>
    <td><code>Map</code></td>
    <td><code>NetworkSecurityGroup.securityRules()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/970">#970</a></td>
  </tr>
  <tr>
    <td><code>List</code></td>
    <td><code>Map</code></td>
    <td><code>NetworkSecurityGroup.defaultSecurityRules()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/970">#970</a></td>
  </tr>
</table>

# Drop Method Usage or Use Alternate #

There are alternate ways to achieve the same thing:

<table>
  <tr>
    <th>Drop Method</th>
    <th>Use Alternate</th>
    <th>Ref</th>
  </tr>
  <tr>
    <td><code>NetworkInterface.primarySubnetId()</code></td>
    <td><code>NetworkInterface.primaryIpConfiguration().subnetId()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1090">#1090</a></td>
  </tr>
  <tr>
    <td><code>NicIpConfiguration.subnetId()</code></td>
    <td>Use <code>NicIpConfiguration.subnetName()</code> for the name of the subnet, and <code>.networkId()</code> for its parent virtual network ID. Or simply call <code>.getNetwork()</code> for the actual associated Network instance and look up the subnet using <code>Network.subnets().get(subnetName)</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1090">#1090</a></td>
  </tr>
  <tr>
    <td><code>NetworkInterface.primaryPublicIpAddress()</code></td>
    <td><code>NetworkInterface.primaryIpConfiguration().getPublicIpAddress()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1090">#1090</a></td>
  </tr>
  <tr>
    <td><code>StorageAccount.keys()</code></td>
    <td><code>StorageAccount.getKeys()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1090">#1090</a></td>
  </tr>
</table>

# Add Property #

Add another property <code>graphURL=https\://graph.windows.net/</code> to the experimental Azure Auth file [#1107](https://github.com/Azure/azure-sdk-for-java/pull/1107).


