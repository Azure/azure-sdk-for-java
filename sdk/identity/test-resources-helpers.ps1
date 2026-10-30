function Get-IdentityAksLocation([hashtable] $TemplateParameters, [string] $ResourceGroupName) {
    $location = if ($TemplateParameters.ContainsKey('location')) {
        $TemplateParameters['location']
    } else {
        (Get-AzResourceGroup -Name $ResourceGroupName -ErrorAction Stop).Location
    }

    if ([string]::IsNullOrWhiteSpace($location)) {
        throw "The AKS deployment location is missing for resource group '$ResourceGroupName'."
    }

    return $location
}

function Get-LatestIdentityAksVersion([object] $Versions, [string] $Location) {
    $patchVersions = @(
        foreach ($version in $Versions.values) {
            if ($version.isPreview -eq $true -or $null -eq $version.patchVersions) {
                continue
            }

            foreach ($patch in $version.patchVersions.PSObject.Properties) {
                if ($patch.Value.isPreview -ne $true) {
                    [version] $patch.Name
                }
            }
        }
    )

    if ($patchVersions.Count -eq 0) {
        throw "No non-preview AKS patch versions are available in '$Location'."
    }

    return ($patchVersions | Sort-Object -Descending | Select-Object -First 1).ToString()
}
