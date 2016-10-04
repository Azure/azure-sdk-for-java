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
    <td>import com.microsoft.azure.Azure</td>
    <td>import com.microsoft.azure.management.Azure</td>
    <td>#1144</td>
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
    <td>List</td>
    <td>Map</td>
    <td>NetworkInterface.ipConfigurations()</td>
    <td>#1055</td>
  </tr>
  <tr>
    <td>List</td>
    <td>Map</td>
    <td>VirtualMachine.resources()</td>
    <td>#1045</td>
  </tr>
  <tr>
    <td>List</td>
    <td>Map</td>
    <td>NetworkSecurityGroup.securityRules()</td>
    <td>#970</td>
  </tr>
  <tr>
    <td>List</td>
    <td>Map</td>
    <td>NetworkSecurityGroup.defaultSecurityRules()</td>
    <td>#970</td>
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
    <td>NetworkInterface.primarySubnetId()</td>
    <td>NetworkInterface.primaryIpConfiguration().subnetId()</td>
    <td>#1090</td>
  </tr>
  <tr>
    <td>NicIpConfiguration.subnetId()</td>
    <td>Use NicIpConfiguration.subnetName() for the name of the subnet, and .networkId() for its parent virtual network ID. Or simply call .getNetwork() for the actual associated Network instance and look up the subnet using Network.subnets().get(subnetName)</td>
    <td>#1090</td>
  </tr>
  <tr>
    <td>NetworkInterface.primaryPublicIpAddress()</td>
    <td>NetworkInterface.primaryIpConfiguration().getPublicIpAddress()</td>
    <td>#1090</td>
  </tr>
  <tr>
    <td>StorageAccount.keys()</td>
    <td>StorageAccount.getKeys()</td>
    <td>#1090</td>
  </tr>
</table>

# Add Property #

Add another property graphURL=https\://graph.windows.net/ to the experimental Azure Auth file #1107.


