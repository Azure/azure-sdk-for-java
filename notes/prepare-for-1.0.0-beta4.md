# Prepare for Azure Management Libraries for Java 1.0.0-beta4#

Steps to migrate code that uses Azure Management Libraries for Java from beta 3 to beta 4 …

> If this note missed any breaking changes, please open a pull request.

# Change Method Names #

<table>
  <tr>
    <th>From</th>
    <th>To</th>
    <th>Ref</th>
  </tr>
    <tr>
    <td><code>VirtualMachine.disableVmAgent()</code></td>
    <td><code>VirtualMachine.withoutVmAgent()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1249">#1249</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.disableAutoUpdate()</code></td>
    <td><code>VirtualMachine.withoutAutoUpdate()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1249">#1249</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.withRootUserName()</code></td>
    <td><code>VirtualMachine.withRootUsername()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1249">#1249</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.withAdminUserName()</code></td>
    <td><code>VirtualMachine.withAdminUsername()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1249">#1249</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachine.withPassword()</code></td>
    <td><code>VirtualMachine.withRootPassword()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1249">#1249</a></td>
  </tr>
    <tr>
    <td><code>VirtualMachine.withPassword()</code></td>
    <td><code>VirtualMachine.withAdminPassword()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1249">#1249</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachineScaleSet.withPrimaryInternetFacingLoadBalancer()</code></td>
    <td><code>VirtualMachineScaleSet.withExistingPrimaryInternetFacingLoadBalancer()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1266">#1266</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachineScaleSet.withPrimaryInternalLoadBalancer()</code></td>
    <td><code>VirtualMachineScaleSet.withExistingPrimaryInternalLoadBalancer()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1266">#1266</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachineScaleSet.withAdminUserName()</code></td>
    <td><code>VirtualMachineScaleSet.withAdminUsername()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1266">#1266</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachineScaleSet.withRootUserName()</code></td>
    <td><code>VirtualMachineScaleSet.withRootUsername()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1266">#1266</a></td>
  </tr>
  <tr>
    <td><code>VirtualMachineScaleSet.withPassword()</code></td>
    <td>
    Windows:
    <br/>
    <code>VirtualMachineScaleSet.withAdminPassword()</code>
    <br/>
    Linux:
    <br/>
    <code>VirtualMachineScaleSet.withRootPassword()</code><br/>
    </td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1266">#1266</a></td>
  </tr>
  <tr>
    <td><code>LoadBalancer.Frontend()</code></td>
    <td><code>LoadBalancer.LoadBalancerFrontend()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1178">#1178</a></td>
  </tr>
  <tr>
    <td><code>LoadBalancer.Probe()</code></td>
    <td><code>LoadBalancer.LoadBalancerProbe()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1178">#1178</a></td>
  </tr>
  <tr>
    <td><code>LoadBalancer.TcpProbe()</code></td>
    <td><code>LoadBalancer.LoadBalancerTcpProbe()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1178">#1178</a></td>
  </tr>
  <tr>
    <td><code>LoadBalancer.HttpProbe()</code></td>
    <td><code>LoadBalancer.LoadBalancerHttpProbe()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1178">#1178</a></td>
  </tr>
  <tr>
    <td><code>LoadBalancer.Backend()</code></td>
    <td><code>LoadBalancer.LoadBalancerBackend()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1178">#1178</a></td>
  </tr>
  <tr>
    <td><code>LoadBalancer.withExistingSubnet()</code></td>
    <td><code>LoadBalancer.withFrontendSubnet()</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1245">#1245</a></td>
  </tr>

  <tr>
    <td><code>ResourceGroups.delete(String id)</code></td>
    <td><code>ResourceGroups().deleteByName(String name)</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1236">#1236</a></td>
  </tr>
  <tr>
    <td>
      <code>{ResourceCollection}.delete(String id)</code>
      <br/>
      e.g.
      <br/>
      <code>VirtualMachines.delete(String id)</code>
      <br/>
      <code>Networks.delete(String id)</code>
      <br/>
      <code>StorageAccounts.delete(String id)</code>
      <br/>
      ...
    </td>
    <td>
      <code>{ResourceCollection}.deleteById(String id)</code>
      <br/>
      <br/>
      <code>VirtualMachines.deleteById(String id)</code>
      <br/>
      <code>Networks.deleteById(String id)</code>
      <br/>
      <code>StorageAccounts.deleteById(String id)</code>
      <br/>
      <br/>
    </td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1236">#1236</a></td>
  </tr>
  <tr>
    <td><code>{ResourceCollection}.delete(String groupName, String name)</code>
      <br/>
      e.g.
      <br/>
      <code>VirtualMachines.delete(String groupName, String name)</code>
      <br/>
      <code>Networks.delete(String groupName, String name)</code>
      <br/>
      <code>StorageAccounts.delete(String groupName, String name)</code>
      <br/>
      ...
      </td>
    <td><code>{ResourceCollection}.deleteByGroup(String groupName, String name)</code>
      <br/>
      <br/>
      <code>VirtualMachines.deleteByGroup(String groupName, String name)</code>
      <br/>
      <code>Networks.deleteByGroup(String groupName, String name)</code>
      <br/>
      <code>StorageAccounts.deleteByGroup(String groupName, String name)</code>
      <br/>
      <br/>
    </td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1236">#1236</a></td>
  </tr>
</table>

# Change interface Names #

<table>
  <tr>
    <th>From</th>
    <th>To</th>
    <th>Ref</th>
  </tr>
   <tr>
    <td><code>com.microsoft.azure.management.compute.WithAdminUserName</code></td>
    <td><code>com.microsoft.azure.management.compute.WithWindowsAdminUsername</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1249">#1249</a></td>
  </tr>
   <tr>
    <td><code>com.microsoft.azure.management.compute.WithRootUserName</code></td>
    <td><code>com.microsoft.azure.management.compute.WithLinuxRootUsername</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1249">#1249</a></td>
  </tr>
   <tr>
    <td><code>com.microsoft.azure.management.compute.WithPassword</code></td>
    <td>
    Windows:
    <br/>
    <code>com.microsoft.azure.management.compute.WithWindowsAdminPassword</code>
    <br/>
    Linux:
    <br/>
    <code>com.microsoft.azure.management.compute.WithLinuxRootPasswordOrPublicKey</code>
    </td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1249">#1249</a></td>
  </tr>
  <tr>
    <td><code>com.microsoft.azure.management.network.HttpProbe</code></td>
    <td><code>com.microsoft.azure.management.network.LoadBalancerHttpProbe</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1178">#1178</a></td>
  </tr>
  <tr>
    <td><code>com.microsoft.azure.management.network.TcpProbe</code></td>
    <td><code>com.microsoft.azure.management.network.LoadBalancerTcpProbe</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1178">#1178</a></td>
  </tr>
  <tr>
    <td><code>com.microsoft.azure.management.network.Probe</code></td>
    <td><code>com.microsoft.azure.management.network.LoadBalancerProbe</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1178">#1178</a></td>
  </tr>
  <tr>
    <td><code>com.microsoft.azure.management.network.PrivateFrontend</code></td>
    <td><code>com.microsoft.azure.management.network.LoadBalancerPrivateFrontend</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1245">#1245</a></td>
  </tr>
  <tr>
    <td><code>com.microsoft.azure.management.network.PublicFrontend</code></td>
    <td><code>com.microsoft.azure.management.network.LoadBalancerPublicFrontend</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1245">#1245</a></td>
  </tr>
  <tr>
    <td><code>com.microsoft.azure.management.network.InboundNatRule</code></td>
    <td><code>com.microsoft.azure.management.network.LoadBalancerInboundNatRule</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1245">#1245</a></td>
  </tr>
  <tr>
    <td><code>com.microsoft.azure.management.network.InboundNatPool</code></td>
    <td><code>com.microsoft.azure.management.network.LoadBalancerInboundNatPool</code></td>
    <td><a href="https://github.com/Azure/azure-sdk-for-java/pull/1245">#1245</a></td>
  </tr>
</table>
