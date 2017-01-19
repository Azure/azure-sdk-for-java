function Build-Solution
{
    Write-Host "Building Event Hubs projects"

    $mvnArgs1 ="mvn package -Dmaven.test.skip=true".replace('-D','`-D')
    Invoke-Expression $mvnArgs1

    Write-Host "Building complete"
}

function Deploy-AzureResources
{
    if ([bool]$env:ClientSecret `
        -and [bool]$env:TenantId `
        -and [bool]$env:AppId `
        -and [bool]$env:APPVEYOR_BUILD_NUMBER)
    {
        Write-Host "Creating Azure resources"

        $ErrorActionPreference = 'Stop'
        Enable-AzureDataCollection -WarningAction SilentlyContinue | Out-Null
        $BuildVersion = ($env:APPVEYOR_BUILD_NUMBER).Replace(".", "")
    
        $env:ResourceGroupName = "eh-java-av-$BuildVersion-rg"
        $NamespaceName = "eh-java-av-$BuildVersion-ns"
        $StorageAccountName = "ehjavaav" + $BuildVersion + "sa"
        $Location = 'westus'

        $Password = ConvertTo-SecureString -AsPlainText -Force $env:ClientSecret
        $Credentials = New-Object `
            -TypeName System.Management.Automation.PSCredential `
            -ArgumentList $env:AppId, $Password

        # https://docs.microsoft.com/en-us/azure/azure-resource-manager/resource-group-authenticate-service-principal
        Add-AzureRmAccount -Credential $Credentials -ServicePrincipal -TenantId $env:TenantId | Out-Null
 
        $ResourceGroup = New-AzureRmResourceGroup -Name $env:ResourceGroupName -Location $Location -Force -WarningAction SilentlyContinue
        Write-Host ("Resource group name: " + $ResourceGroup.ResourceGroupName)

	    $EventHubName = 'EventHub1'
        $ArmParameters = @{
            namespaceName = $NamespaceName;
            eventhubName = 'EventHub1';
            consumerGroupName = 'CGroup1';
            storageAccountName = $StorageAccountName;
        }

        $TemplatePath = "$((Get-Location).path)\templates\azuredeploy.json"
    
        $settings = New-AzureRmResourceGroupDeployment `
           -ResourceGroupName $env:ResourceGroupName `
           -TemplateFile $TemplatePath `
           -TemplateParameterObject $ArmParameters `
           -Force `
           -WarningAction SilentlyContinue

        Write-Host "Event Hubs namespace: $NamespaceName"
        Write-Host "Storage account name: $StorageAccountName"

        $env:EVENT_HUB_CONNECTION_STRING = $settings.Outputs.Get_Item("namespaceConnectionString").Value + ";EntityPath=$EventHubName"
        $env:EPHTESTSTORAGE = $settings.Outputs.Get_Item("storageAccountConnectionString").Value
        $env:PARTITION_COUNT = 4

        Write-Host "Completed creating Azure resources"
    }
    else
    {
        Write-Host "No environment variables present. Skipping Azure deployment."
    }

    # Remove-AzureRmResourceGroup -Name $ResourceGroupName -Force

    # Useful for debugging ARM deployments
    # Get-AzureRmLog -CorrelationId "GUID" -DetailedOutput
}

function Run-UnitTests
{
    if ([bool]$env:EVENT_HUB_CONNECTION_STRING `
        -and [bool]$env:EPHTESTSTORAGE `
        -and [bool]$env:PARTITION_COUNT)
    {
        Write-Host "Running unit tests."

        $mvnArgs1 ="mvn package"
        Invoke-Expression $mvnArgs1
    }
    else
    {
        Write-Host "No environment variables present. Skipping unit tests."
    }
}

function Delete-AzureResources
{
    if ([bool]$env:ClientSecret -and [bool]$env:AppId)
    {
        Write-Host "Deleting Azure resources"

        $ErrorActionPreference = 'Stop'
    
        $Password = ConvertTo-SecureString -AsPlainText -Force $env:ClientSecret
        $Credentials = New-Object `
            -TypeName System.Management.Automation.PSCredential `
            -ArgumentList $env:AppId, $Password

        Remove-AzureRmResourceGroup -Name $env:ResourceGroupName -WarningAction SilentlyContinue -Force | Out-Null

        Write-Host "Completed deleting Azure resources"
    }
    else
    {
        Write-Host "No environment variables present. Skipping Azure resource deletion"
    }
}