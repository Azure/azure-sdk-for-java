#!/bin/bash

CLUSTER_NAME=$1
JARPATH=$2
[[ -z "$CLUSTER_NAME" ]] && exit 1
[[ -z "$JARPATH" ]] && exit 1

echo "Looking for cluster '$CLUSTER_NAME'"

CLUSTER_ID=$(databricks clusters list --output json | jq -r --arg N "$CLUSTER_NAME" '.clusters[] | select(.cluster_name == $N) | .cluster_id')

if [[ -z "$CLUSTER_ID" ]]
then
	echo "Cannot find a cluster named '$CLUSTER_NAME'"
	exit 1
fi

echo "Uninstalling libraries in $CLUSTER_ID"
LIBRARIES=$(databricks libraries list --cluster-id $CLUSTER_ID | jq -r '.library_statuses[] | .library.jar')
for library in $LIBRARIES
do
	echo "Uninstalling $library"
	databricks libraries uninstall --cluster-id $CLUSTER_ID --jar $library
done

bash sdk/cosmos/azure-cosmos-spark_3_2-12/test-databricks/databricks-cluster-restart.sh $CLUSTER_ID

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
# DATABRICKS_RUNTIME_VERSION is not populated in the environment and version comparison is messy in bash
# Using cluster name for the cluster that was created with 16.4
if [ $CLUSTER_NAME == "oltp-ci-spark35-2workers-ds3v2-16.4" ]; then
  echo "Copying files from $JARPATH/$JARFILE to /Workspace/tmp/libraries/$JARFILE"
  databricks fs cp $JARPATH/$JARFILE /Workspace/tmp/libraries/$JARFILE --overwrite
  echo $0
  echo "Installing $JARFILE in $CLUSTER_ID"
  databricks libraries install --cluster-id $CLUSTER_ID --jar /Workspace/tmp/libraries/$JARFILE
  echo $0
else
  # For older runtimes: Use DBFS path
  echo "Using DBFS library installation for DBR $DBR_VERSION"
  echo "Deleting files in dbfs:/tmp/libraries/$JARFILE"
  dbfs rm dbfs:/tmp/libraries/$JARFILE
  dbfs ls dbfs:/tmp/libraries/

  echo "Copying files to DBFS $JARPATH/$JARFILE"
  dbfs cp $JARPATH/$JARFILE dbfs:/tmp/libraries/$JARFILE --overwrite
  dbfs ls dbfs:/tmp/libraries/

  echo "Installing $JARFILE in $CLUSTER_ID"
  databricks libraries install --cluster-id $CLUSTER_ID --jar dbfs:/tmp/libraries/$JARFILE
fi

bash sdk/cosmos/azure-cosmos-spark_3_2-12/test-databricks/databricks-cluster-restart.sh $CLUSTER_ID
