# This tag must match one of the tags defined in this file: https://github.com/Azure/azure-rest-api-specs/blob/master/specification/iothub/resource-manager/readme.md
# You can change this tag's value in this script if you want to generate for a different service API version
$tag = "package-2021-07"
Write-Host "Generating control plane SDK from tag" $tag

# This is the version of the control plane SDK itself
$newVersion = "1.1.0"
Write-Host "Generated control plane SDK will have version" $newVersion

autorest https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/iothub/resource-manager/readme.md `
	--version=3.1.3 `
	--use=@autorest/java@4.0.31 `
	--java.azure-libraries-for-java-folder=../.. `
	--java.output-folder=./azure-resourcemanager-iothub `
	--java.namespace=com.azure.resourcemanager.iothub `
	--tag=$tag `
	--package-version=$newVersion `
	--java `
	--pipeline.modelerfour.additional-checks=false `
	--pipeline.modelerfour.lenient-model-deduplication=true `
	--azure-arm `
	--verbose `
	--sdk-integration `
	--fluent=lite `
	--java.fluent=lite `
	--java.license-header=MICROSOFT_MIT_SMALL `
	--generate-samples