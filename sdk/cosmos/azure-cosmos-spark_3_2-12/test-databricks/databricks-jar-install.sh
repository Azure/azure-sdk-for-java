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

echo "DATABRICKS_RUNTIME_VERSION: $DATABRICKS_RUNTIME_VERSION"
if [ $DATABRICKS_RUNTIME_VERSION > 15 ]; then
  # Upload to workspace and install from there
  echo "Using Workspace library installation for DBR $DBR_VERSION"
  echo "Deleting files in /Workspace/tmp/libraries/$JARFILE"
  databricks workspace delete /tmp/libraries/$JARFILE

  echo "Copying files to $JARPATH/$JARFILE"
  databricks workspace import --format AUTO "$JARPATH/$JARFILE" "/Workspace/tmp/libraries/$JARFILE" --overwrite
  databricks libraries install --cluster-id $CLUSTER_ID --jar "/Workspace/tmp/libraries/$JARFILE"
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
