#!/bin/bash

CLUSTER_NAME=$1
AVOID_DBFS=$2
JARPATH=$3
STORAGE_ACCOUNT_KEY=$4
[[ -z "$CLUSTER_NAME" ]] && exit 1
[[ -z "$JARPATH" ]] && exit 1

echo "Looking for cluster '$CLUSTER_NAME' - Avoid DBFS '$AVOID_DBFS'"

CLUSTER_ID=$(databricks clusters list --output json | jq -r --arg N "$CLUSTER_NAME" '.[] | select(.cluster_name == $N) | .cluster_id')

if [[ -z "$CLUSTER_ID" ]]
then
	echo "Cannot find a cluster named '$CLUSTER_NAME'"
	exit 1
fi

for file in $JARPATH/*.jar
do
	filename=${file##*/}
	if [[ "$filename" != *"original"* && "$filename" != *"sources"* && "$filename" != *"javadoc"* ]]
	then
		JARFILE=$filename
	fi
done

if [[ -z "$JARFILE" ]]
then
	echo "Cannot find Jar files at $JARPATH"
	exit 1
fi

echo "CLUSTER_NAME: $CLUSTER_NAME"
echo "Avoid DBFS: $AVOID_DBFS"
# DATABRICKS_RUNTIME_VERSION is not populated in the environment and version comparison is messy in bash
# Using cluster name for the cluster that was created with 16.4
if [[ "${AVOID_DBFS,,}" == "true" ]]; then
  account=oltpsparkcijarstore

  echo "Uploading jar '$JARPATH/$JARFILE' to Azure Storage account oltpsparkcijarstore (ephemeral tenant) container jarstore BLOB jars/azure-cosmos-spark_3-5_2-12-latest-ci-candidate.jar"
  az storage blob upload --account-name oltpsparkcijarstore --account-key $STORAGE_ACCOUNT_KEY --container-name jarstore --name jars/azure-cosmos-spark_3-5_2-12-latest-ci-candidate.jar --file $JARPATH/$JARFILE --type block --overwrite true --only-show-errors

  if [ $? -eq 0 ]; then
    echo "Successfully uploaded JAR to oltpsparkcijarstore (ephemeral tenant)."
    echo "Rebooting cluster to install new library via init script"
  else
    echo "Failed to upload JAR to Workspace Files."
    echo $?
    exit $?
  fi
else
  echo "Uninstalling libraries in $CLUSTER_ID"
  LIBRARIES=$(databricks libraries cluster-status $CLUSTER_ID | jq -r '.[] | .library.jar')
  for library in $LIBRARIES
  do
    databricks -v
  	echo "Uninstalling $library"
  	databricks libraries uninstall --json "{\"cluster_id\": \"$CLUSTER_ID\", \"libraries\": [{\"jar\": \"$library\"}]}"
  done

  bash sdk/cosmos/azure-cosmos-spark_3_2-12/test-databricks/databricks-cluster-restart.sh $CLUSTER_ID

  # For older runtimes: Use DBFS path
  echo "Using DBFS library installation for DBR $DBR_VERSION"
  echo "Deleting files in dbfs:/tmp/libraries/$JARFILE"
  databricks fs rm dbfs:/tmp/libraries/$JARFILE

  echo "Copying files to DBFS $JARPATH/$JARFILE"
  databricks fs cp $JARPATH/$JARFILE dbfs:/tmp/libraries/$JARFILE --overwrite

  echo "Installing $JARFILE in $CLUSTER_ID"
  databricks libraries install --json "{\"cluster_id\": \"$CLUSTER_ID\", \"libraries\": [{\"jar\": \"dbfs:/tmp/libraries/$JARFILE\"}]}"
fi

bash sdk/cosmos/azure-cosmos-spark_3_2-12/test-databricks/databricks-cluster-restart.sh $CLUSTER_ID
