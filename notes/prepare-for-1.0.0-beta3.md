# Prepare for Azure Management Libraries for Java 1.0.0-beta3#

Steps to migrate code that uses Azure Management Libraries for Java from beta 2 to beta 3 …

# Replace Import Statement #

<table>
  <tr>
    <th>Replace</th>
    <th>With</th>
    <th>Ref</th>
  </tr>
  <tr>
    <td>`import com.microsoft.azure.Azure`</td>
    <td>`import com.microsoft.azure.management.Azure`</td>
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
    <td>`Subnet.networkSecurityGroup()`</td>
    <td>`Subnet.getNetworkSecurityGroup()`</td>
    <td>#1140</td>
  </tr>
  <tr>
    <td>`Network.dsnServerIPs()`</td>
    <td>`Network.dnsServerIps()`</td>
    <td>#1140</td>
  </tr>
  <tr>
    <td>`NicIpConfiguration.publicIpAddress()`</td>
    <td>`NicIpConfiguration.getPublicIpAddress()`</td>
    <td>#1083</td>
  </tr>
  <tr>
    <td>`NicIpConfiguration.network()`</td>
    <td>`NicIpConfiguration.getNetwork()`</td>
    <td>#1083</td>
  </tr>
  <tr>
    <td>`NetworkInterface.networkSecurityGroup()`</td>
    <td>`NetworkInterface.getNetworkSecurityGroup()`</td>
    <td>#1065</td>
  </tr>
  <tr>
    <td>`NicIpConfiguration.privateIp()`</td>
    <td>`NicIpConfiguration.privateIpAddress()`</td>
    <td>#1055</td>
  </tr>
  <tr>
    <td>`VirtualMachine.primaryPublicIpAddress()`</td>
    <td>`VirtualMachine.getPrimaryPublicIpAddress()`</td>
    <td>#1090</td>
  </tr>
  <tr>
    <td>`StorageAccount.refreshKeys()`</td>
    <td>`StorageAccount.getKeys()`</td>
    <td>#1090</td>
  </tr>
  <tr>
    <td>`NetworkInterface.primaryNetwork()`</td>
    <td>`NetworkInterface.getPrimaryNetwork()`</td>
    <td>#1090</td>
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
    <td>`List`</td>
    <td>`Map`</td>
    <td>`NetworkInterface.ipConfigurations()`</td>
    <td>1055</td>
  </tr>
  <tr>
    <td>`List`</td>
    <td>`Map`</td>
    <td>`VirtualMachine.resources()`</td>
    <td>1045</td>
  </tr>
  <tr>
    <td>`List`</td>
    <td>`Map`</td>
    <td>`NetworkSecurityGroup.securityRules()`</td>
    <td>970</td>
  </tr>
  <tr>
    <td>`List`</td>
    <td>`Map`</td>
    <td>`NetworkSecurityGroup.defaultSecurityRules()`</td>
    <td>970</td>
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
    <td>`NetworkInterface.primarySubnetId()`</td>
    <td>`NetworkInterface.primaryIpConfiguration().subnetId()`</td>
    <td>1090</td>
  </tr>
  <tr>
    <td>`NicIpConfiguration.subnetId()`</td>
    <td>Use `NicIpConfiguration.subnetName()` for the name of the subnet, and `.networkId()` for its parent virtual network ID. Or simply call `.getNetwork()` for the actual associated Network instance and look up the subnet using `Network.subnets().get(subnetName)`</td>
    <td>1090</td>
  </tr>
  <tr>
    <td>`NetworkInterface.primaryPublicIpAddress()`</td>
    <td>`NetworkInterface.primaryIpConfiguration().getPublicIpAddress()`</td>
    <td>1090</td>
  </tr>
  <tr>
    <td>`StorageAccount.keys()`</td>
    <td>`StorageAccount.getKeys()`</td>
    <td>1090</td>
  </tr>
</table>

# Add Property #

Add another property `graphURL=https\://graph.windows.net/` to the experimental Azure Auth file #1107.


