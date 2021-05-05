# Setting up test resources

To run or record tests, you must set some environment variables.
Instructions can be found at <https://github.com/Azure/azure-sdk-for-java/tree/master/eng/common/TestResources>.

The shorter explanation is that you will need to run the below command from the "eng/common/TestResources" folder in this repo.

```
Connect-AzAccount -Subscription 'YOUR SUBSCRIPTION ID'

.\New-TestResources.ps1 deviceProvisioningServices -BaseName "deviceprovisioningservice-java-gate-resource"
```

The result of this script is a set of environment variables that you will set to your machine before running tests.

For control plane SDK tests, you will only need the AAD authentication variables for Tenant Id, Client Id, Client Secret, and Subscription Id.