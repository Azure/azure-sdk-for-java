param(
    [Parameter(Mandatory=$true)]
    [System.String] $ServiceDirectory,
    # ArtifactsList will be using ('${{ convertToJson(parameters.Artifacts) }}' | ConvertFrom-Json | Select-Object name, groupId, uberJar)
    [Parameter(Mandatory=$false)]
    [AllowEmptyCollection()]
    [array] $ExcludePaths
)

Write-Host "ServiceDirectory=$ServiceDirectory"
if ($ExcludePaths.Length -eq 0) {
    Write-Host "Exclude Paths is empty"
} else {
    Write-Host "ExcludePaths:"
    foreach ($excludePath in $ExcludePaths) {
        Write-Host "    $excludePath"
    }
}