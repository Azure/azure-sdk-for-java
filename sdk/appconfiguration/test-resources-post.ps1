# cspell:ignore Drevapi, Djacoco, Dcodesnippet, Dcheckstyle, Dspotbugs, Dspotless, Dcheckstyle, Dfunction, Dresource, vmcontainer, OIDC, oidc, usermgdid, azfunc, kubelet

param (
    [hashtable] $DeploymentOutputs
)
function getVariable {
    param (
        $name
    )
    if ($null -ne $DeploymentOutputs -and $DeploymentOutputs.ContainsKey($name)) {
        return $DeploymentOutputs[$name]
    }
    if (test-path "env:\$name") {
        return (Get-Item "env:\$name").Value
    }
    Write-Error "Could not find value for $name"
    exit 1
}

$azIdentityRoot = "$PSScriptRoot/../identity/azure-identity/pom.xml" | Resolve-Path

mvn clean install "-DskipTests" "-Drevapi.skip=true" "-Dcheckstyle.skip=true" "-Dcodesnippet.skip=true" "-Dspotbugs.skip=true" "-Dmaven.javadoc.skip=true" "-Dspotless.check.skip=true" "-Dspotless.apply.skip=true" "-Djacoco.skip=true" -f $azIdentityRootPom | Write-Host




