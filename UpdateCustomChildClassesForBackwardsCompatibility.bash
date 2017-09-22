#!/bin/bash
custom_folder_name="custom"
search_dir="azure-keyvault/src/main/java/com/microsoft/azure/keyvault/models/$custom_folder_name/"
custom_package_name="com.microsoft.azure.keyvault.models.$custom_folder_name."
model_name_regex="models\/([a-zA-Z]+).java"

for entry in "$search_dir"*
do
    model_file_name=${entry/${custom_folder_name}\//}
    [[ $model_file_name =~ $model_name_regex ]]
    model_name=${BASH_REMATCH[1]}
    echo $model_name
    if ! grep -q "extends $custom_package_name" "$model_file_name"; then
        sed -i "s/public class ${model_name}/public class ${model_name} extends ${custom_package_name}${model_name}/g" $model_file_name
    fi
done

echo "Extended for backwards compatibility...DONE"
