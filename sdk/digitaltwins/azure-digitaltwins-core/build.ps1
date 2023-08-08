# Builds the azure-digitaltwins-core project
# The output will be in the target directory.
# You can use the generated azure-digitaltwins-core-x-sources.jar to upload to the API review tool at https://apiview.dev/

param (
    
    [Parameter(Mandatory = $false)]
    [switch] $skipTests
)

if($skipTests) {
    mvn install -DskipTests
}
else {
    mvn install
}