# Builds the azure-digitaltwins-core project
# The output will be in the target directory.
# You can use the generated azure-digitaltwins-core-x-sources.jar to upload to the API review tool at https://apiview.dev/

param (
<<<<<<< HEAD

=======
    
>>>>>>> 95a27a56ad7e94c066c6b4113935ad5901940c61
    [Parameter(Mandatory = $false)]
    [switch] $skipTests
)

if($skipTests) {
    mvn install -DskipTests
}
else {
    mvn install
<<<<<<< HEAD
}
=======
}
>>>>>>> 95a27a56ad7e94c066c6b4113935ad5901940c61
