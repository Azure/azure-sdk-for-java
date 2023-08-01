<#

.SYNOPSIS
This script helps release batch SDK.

.DESCRIPTION
This script will do the following things:
1) Create 2 commits in github.com/Azure/azure-batch-sdk-for-java
2) Tag the 1st one as the released hash
3) Put the jars and pom in \\adxsdkbuilder\builds\azure-batch-{version}

After these are done, you still need to run Jenkins job http://azuresdkci.cloudapp.net/view/5-Signing%20and%20Publishing/job/sign-and-publish-jar.

.PARAMETER version
The version to release, e.g. 1.0.0-rc

.PARAMETER tag
The tag name in GitHub, e.g. v1.0.0-rc

.PARAMETER snapshot
The next development snapshot version, e.g. 1.0.0-rc2-SNAPSHOT

.EXAMPLE
./release-prepare-maven.ps1 1.2.3 v1.2.3 1.2.4-SNAPSHOT

#>

[CmdletBinding()]
Param(
    [Parameter(Mandatory=$True, Position=0)]
    [string]$version,

    [Parameter(Mandatory=$True, Position=1)]
    [string]$tag,

    [Parameter(Mandatory=$True, Position=2)]
    [string]$snapshot
)

$branch = "master"

# Check branch
echo "Checking if you are on $branch branch..."
If ($(git rev-parse --abbrev-ref HEAD) -ne $branch) {
    echo "You are not on $branch branch."
    exit(1)
}

# Check diff
echo "Checking if your $branch is in sync..."
git remote add release https://github.com/Azure/azure-batch-sdk-for-java.git
git fetch release
If ($(git diff release/$branch) -ne $null) {
    echo "Your $branch branch is different from https://github.com/Azure/azure-batch-sdk-for-java.git, please sync before running this script."
    git remote remove release
    exit(1)
}
git remote remove release

# Check write access
echo "Checking if you have write access to the repo..."
git push origin HEAD:$branch
If (!$?) {
    echo "Doesn't have write access to the repo."
    exit(1)
}

# Check if tag exists
echo "Checking if tag $tag already exists..."
$tags=git ls-remote --tags origin
If ($tags -like "*refs/tags/$tag*") {
    echo "Tag $tag already exists."
    exit(1)
}

# Run mvn
$cmd = "mvn -Dtag=`"$tag`" -DreleaseVersion=`"$version`" -DdevelopmentVersion=`"$snapshot`" release:prepare -Dresume=false"
echo "Invoking $cmd..."
Invoke-Expression $cmd | Out-Null

If (!$?) {
    echo "Failed to create tag or release commits.."
    exit(1)
}

echo "Release preparation done. Going to tag $tag to build artifacts..."
Invoke-Expression "git checkout `"$tag`"" | Out-Null

echo "Building artifacts..."
mvn clean source:jar javadoc:jar package -DskipTests | Out-Null

echo "Copying artifacts to \\adxsdkbuilder\builds\azure-batch-$version..."
New-Item \\adxsdkbuilder\builds\azure-batch-$version -ItemType Directory
Copy-Item pom.xml \\adxsdkbuilder\builds\azure-batch-$version\azure-batch-$version.pom

$files = Get-ChildItem azure-batch-$version*.jar -Recurse
foreach ($file in $files) {
    Copy-Item $file \\adxsdkbuilder\builds\azure-batch-$version
}

git checkout $branch | Out-Null

echo "The job is done. Please go to http://azuresdkci.cloudapp.net/view/5-Signing%20and%20Publishing/job/sign-and-publish-jar/build?delay=0sec \
    and build with location parameter `"\\adxsdkbuilder\builds\azure-batch-$version`"."