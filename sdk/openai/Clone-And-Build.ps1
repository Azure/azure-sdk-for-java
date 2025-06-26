##################################################################
# This script is used for testing openai-build.yml locally only. #
##################################################################

# Define repository details
$repoUrl = "https://github.com/openai/openai-java.git"
$branchName = "release-please--branches--main--changes--next"

# Clone the repository and checkout the specified branch
Write-Host "Cloning repository: $repoUrl"
git clone $repoUrl openai-java
cd openai-java
Write-Host "Checking out branch: $branchName"
git checkout $branchName

# Define the path to the build.gradle.kts file
$buildGradlePath = './build.gradle.kts'

# Read the contents of the build.gradle.kts file
$buildGradleContent = Get-Content -Path $buildGradlePath

# Extract the version number from the build.gradle.kts file
$versionLine = $buildGradleContent | Select-String -Pattern 'version\s*=\s*\"([^\"]+)\"'
if ($versionLine -match 'version\s*=\s*\"([^\"]+)\"') {
    $version = $matches[1]
    Write-Host "Current version: $version"
} else {
    Write-Host "Version not found"
    exit 1
}

# Define the new version with '-SNAPSHOT' appended
$newVersion = "$version-SNAPSHOT"
Write-Host "New version: $newVersion"

# Update the version in the build.gradle.kts file
$updatedContent = $buildGradleContent -replace 'version\s*=\s*\"([^\"]+)\"', "version = `"$newVersion`""
Set-Content -Path $buildGradlePath -Value $updatedContent

# Build the project and publish to the local Maven repository
Write-Host "Building the project and publishing to the local Maven repository"
./gradlew clean build -x test publishToMavenLocal

# Define the path to the external_dependencies.txt file
$dependenciesFilePath = './../../../eng/versioning/external_dependencies.txt'

# Update the version in the external_dependencies.txt file
$newVersionInExternalDependenciesFile = "com.openai:openai-java;$newVersion"
$dependenciesContent = Get-Content -Path $dependenciesFilePath -Raw
$updatedDependenciesContent = $dependenciesContent -replace 'com.openai:openai-java;[0-9.]+', $newVersionInExternalDependenciesFile
Set-Content -Path $dependenciesFilePath -Value $updatedDependenciesContent

# Run the version update script
Write-Host "Running the version update script"
cd ../../../
python eng/versioning/update_versions.py --skip-readme

# Navigate back to the sdk/openai directory
cd .\sdk\openai
