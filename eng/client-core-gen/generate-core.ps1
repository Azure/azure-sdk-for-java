# This is a list of the modules we care about for the client-core generation.
$modules = @(
    "azure-core",
    "azure-core-http-netty",
    "azure-json"
)

$modulesNewNames = @(
    "client-core",
    "client-core-http-netty",
    "client-core-json"
)

# Conditionally clean up existing directory, if it exists
if (Test-Path ..\..\sdk\client-core) {
    Write-Host "Deleting existing client-core directory"
    Remove-Item ..\..\sdk\client-core -Recurse -Force
}

# Copy everything from azure-core directory as the starting point for our rewriting
Write-Host "Copying azure-core directory to sdk-core directory"
Copy-Item -Path "..\..\sdk\core\" -Destination "..\..\sdk\client-core" -Recurse

# Concatenate the modules array above into a comma-separated string that can be passed to Maven
$modulesString = $modules -join ","
Write-Host "Modules to rewrite: $modulesString"

# Run maven to build the client-core-gen library (used by OpenRewrite in the next step)
Write-Host "Building client-core-gen"
mvn -f pom.xml clean install

# Run maven with a project list consisting of just the modules we care about
Write-Host "Running OpenRewrite on sub-projects"
mvn -f ..\..\sdk\client-core\pom.xml -Popenrewrite -pl "$modulesString" rewrite:run

# Run OpenRewrite on the top-level pom.xml to rewrite the module list
# Write-Host "Running OpenRewrite on top-level pom.xml"
# mvn -f ..\..\sdk\client-core\pom.xml -U org.openrewrite.maven:rewrite-maven-plugin:run `
#   "-Drewrite.recipeArtifactCoordinates=com.azure:client-core-gen:1.0.0" `
#   "-Drewrite.activeRecipes=com.client.core.gen.MavenModulesRewrite"

# Delete all the folders that are not in the modules list
Write-Host "Deleting folders that are not in the modules list"
Get-ChildItem -Path ..\..\sdk\client-core\ -Directory | Where-Object { $_.Name -notin $modules } | Remove-Item -Recurse -Force

# Rename the folders to match the new names
Write-Host "Renaming folders to match new names"
for ($i = 0; $i -lt $modules.Count; $i++) {
    Rename-Item -Path "..\..\sdk\client-core\$($modules[$i])" -NewName "$($modulesNewNames[$i])"
}

# Delete the empty directories, which are left-over from the com.azure -> com.client.core.gen rewrite
Write-Host "Deleting empty directories"
Get-ChildItem -Path "..\..\sdk\client-core" -Force -Recurse -Directory |
    Where-Object { (Get-ChildItem -Path $_.FullName -Recurse -File -EA SilentlyContinue |
        Measure-Object).Count -eq 0 } | Remove-Item -Force -Recurse

# Run the Maven build on the new libraries
mvn -f ..\..\sdk\client-core\pom.xml -pl "$modulesString" install --errors
