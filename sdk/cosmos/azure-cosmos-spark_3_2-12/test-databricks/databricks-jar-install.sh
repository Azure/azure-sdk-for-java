#!/bin/bash

CLUSTER_NAME=$1
AVOID_DBFS=$2
JARPATH=$3
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
echo "Avoid DBFS: $AVOID_DBFS"
# DATABRICKS_RUNTIME_VERSION is not populated in the environment and version comparison is messy in bash
# Using cluster name for the cluster that was created with 16.4
if [ $AVOID_DBFS == "true" ]; then
  echo "Importing files from $JARPATH/$JARFILE to /Workspace/libs/$JARFILE"
  databricks workspace files upload --local-path "$JARPATH/$JARFILE" --workspace-path "/Workspace/libs/$JARFILE" --overwrite
  if [$? -ne 0]; then
      echo "Failed to upload JAR to Workspace Files."
      echo $?
      exit $?
  fi
  echo "Successfully uploaded JAR to Workspace."
  echo "Installing $JARFILE in $CLUSTER_ID"
  databricks libraries install --cluster-id "$CLUSTER_ID" --workspace-file "/Workspace/libs/$JARFILE"
  if [ $? -ne 0 ]; then
        echo "Failed to install JAR to cluster."
        echo $?
        exit $?
  fi
  echo "Successfully installed JAR to cluster"
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
