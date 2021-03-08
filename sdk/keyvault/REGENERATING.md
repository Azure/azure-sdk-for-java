# Regenerating SDK

The code in the azure-keyvault package is generated via AutoREST. After code generation, there are currently several manual modifications that need to be made for the build to succeed. In the root of the azure-keyvault-java repo, there is a Python script called "regenerate_and_fix_sdk.py" which will automatically regenerate the code and apply the necessary fixes. To regenerate the code using this script:

- Install AutoREST: https://github.com/Azure/autorest/blob/master/docs/install/readme.md
- Open the script in your favorite editor and change "swagger_source_branch" to contain the branch of the Azure REST spec API repo that you'd like the API spec to be sourced from – likely either "master" or "keyvault_preview"
  - If there is a tag you'd like to pass to AutoREST – set the "autorest_tag" branch accordingly.
- Using Python 3.6+, run the script from the root of the checked out repository. Verify that no errors appear, and then build/test the SDK.



The changes made by the script post-regeneration are as follows:

* The generated webkey models will be removed


* Any generated model class which has a corresponding custom class in the "models/custom" folder will be changed to inherit from the custom class.
* Any class which utilizes webkey models will have the correct imports from the azure-keyvault-webkey package added
* AutoREST occasionally generates broken parameter declarations in the Javadoc comments - these are repaired.
