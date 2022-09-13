# Databricks notebook source
from pyspark.sql.functions import lit
cosmosEndpoint = "<cosmos-endpoint>"
cosmosMasterKey = "<cosmos-master-key>"
cosmosDatabaseName = "ContosoHospital"
cosmosContainerName = "Doctor"

cfg = {
    "spark.cosmos.accountEndpoint": cosmosEndpoint,
    "spark.cosmos.accountKey": cosmosMasterKey,
    "spark.cosmos.database": cosmosDatabaseName,
    "spark.cosmos.container": cosmosContainerName,
}

spark.conf.set("spark.sql.catalog.cosmosCatalog",
               "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set(
    "spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
spark.conf.set(
    "spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey)


# sample schema for Doctor container

#  {
#      "id": "2e82d21c-379a-4ab5-b56b-9c67a95ae09c",
#      "name": "Elizebeth O'Keefe III",
#      "dob": "2022-31-04",
#      "gender": "female",
#      "department": "Cardiology",
#      "address": "Suite 466 31270 Blanda Glen, Sipesmouth, NE 08025-9135"
#  }

# sample schema for Slot container

#  {
#     "id": "5eec4574-8e3e-47a2-b7b6-21ab1cb5a0c5",
#      "doctorId": "890a061d-4671-428a-8f83-0aef297b645b",
#      "doctorName": "Paris Kihn",
#      "slotDate": "2022-08-07",
#      "startTime": 1659750013234,
#      "endTime": 1659751813234,
#      "department": "Pulmonology",
#      "patientId": "b7a34a39-e77b-4c30-950c-8f30b53bc37e",
#      "patientName": "Loria Hand",
#      "appointmentDate": "2022-08-05"
#  }


# COMMAND ----------

# patch add single


cfgPatch = {"spark.cosmos.accountEndpoint": cosmosEndpoint,
            "spark.cosmos.accountKey": cosmosMasterKey,
            "spark.cosmos.database": cosmosDatabaseName,
            "spark.cosmos.container": cosmosContainerName,
            "spark.cosmos.write.strategy": "ItemPatch",
            "spark.cosmos.write.bulk.enabled": "false",
            "spark.cosmos.write.patch.defaultOperationType": "None",
            "spark.cosmos.write.patch.columnConfigs": "[col(directReports).op(add)]"
            }

id = "<document-id>"
query = "select * from cosmosCatalog.{}.{} where id = '{}';".format(
    cosmosDatabaseName, cosmosContainerName, id)

dfBeforePatch = spark.sql(query)
print("document before patch operation")
dfBeforePatch.show()
df = dfBeforePatch.withColumn("directReports", lit(2))
df.write.format("cosmos.oltp").mode("Append").options(**cfgPatch).save()
dfAfterPatch = spark.sql(query)
print("document after patch operation")
dfAfterPatch.show()

# COMMAND ----------

# patch set operation

cfgSet = {"spark.cosmos.accountEndpoint": cosmosEndpoint,
          "spark.cosmos.accountKey": cosmosMasterKey,
          "spark.cosmos.database": cosmosDatabaseName,
          "spark.cosmos.container": cosmosContainerName,
          "spark.cosmos.write.strategy": "ItemPatch",
          "spark.cosmos.write.bulk.enabled": "false",
          "spark.cosmos.write.patch.defaultOperationType": "Set",
          "spark.cosmos.write.patch.columnConfigs": "[col(name).op(set)]"
          }

id = "<document-id>"
query = "select * from cosmosCatalog.{}.{} where id = '{}';".format(
    cosmosDatabaseName, cosmosContainerName, id)

dfBeforePatch = spark.sql(query)
print("document before patch operation")
dfBeforePatch.show()

data = [{"id": id, "name": "Joel Brakus"}]
patchDf = spark.createDataFrame(data)

patchDf.write.format("cosmos.oltp").mode("Append").options(**cfgSet).save()

dfAfterPatch = spark.sql(query)
print("document after patch operation")
dfAfterPatch.show()

# COMMAND ----------

# patch replace operation

slotContainer = "Slot"
cfgReplace = {"spark.cosmos.accountEndpoint": cosmosEndpoint,
              "spark.cosmos.accountKey": cosmosMasterKey,
              "spark.cosmos.database": cosmosDatabaseName,
              "spark.cosmos.container": slotContainer,
              "spark.cosmos.write.strategy": "ItemPatch",
              "spark.cosmos.write.bulk.enabled": "false",
              "spark.cosmos.write.patch.columnConfigs": "[col(patientName).op(replace)]"
              }

# document id
id = "<document-id>"
# partition key
doctorId = "<partiton-key>"

query = "select * from cosmosCatalog.{}.{} where id = '{}' and doctorId = '{}';".format(
    cosmosDatabaseName, slotContainer, id, doctorId)

dfBeforePatch = spark.sql(query)
print("document before patch operation")
dfBeforePatch.show()

data = [{"id": id, "doctorId": doctorId, "patientName": "Martha Stewart"}]
patchDf = spark.createDataFrame(data)

patchDf.write.format("cosmos.oltp").mode("Append").options(**cfgReplace).save()

dfAfterPatch = spark.sql(query)
print("document after patch operation")
dfAfterPatch.show()

# COMMAND ----------

# patch remove operation

slotContainer = "Slot"
cfgRemove = {"spark.cosmos.accountEndpoint": cosmosEndpoint,
             "spark.cosmos.accountKey": cosmosMasterKey,
             "spark.cosmos.database": cosmosDatabaseName,
             "spark.cosmos.container": slotContainer,
             "spark.cosmos.write.strategy": "ItemPatch",
             "spark.cosmos.write.bulk.enabled": "false",
             "spark.cosmos.write.patch.columnConfigs": "[col(patientName).op(remove)]"
             }

# document id
id = "<document-id>"
# partition key
doctorId = "<partition-key>"

query = "select * from cosmosCatalog.{}.{} where id = '{}' and doctorId = '{}';".format(
    cosmosDatabaseName, slotContainer, id, doctorId)

dfBeforePatch = spark.sql(query)
print("document before patch operation")
dfBeforePatch.show()

dfBeforePatch.write.format("cosmos.oltp").mode(
    "Append").options(**cfgRemove).save()

dfAfterPatch = spark.sql(query)
print("document after patch operation")
dfAfterPatch.show()

# COMMAND ----------

predicate = "from c where c.department = 'Pulmonology'"

cfgCondUpdate = {"spark.cosmos.accountEndpoint": cosmosEndpoint,
                 "spark.cosmos.accountKey": cosmosMasterKey,
                 "spark.cosmos.database": cosmosDatabaseName,
                 "spark.cosmos.container": cosmosContainerName,
                 "spark.cosmos.write.strategy": "ItemPatch",
                 "spark.cosmos.write.bulk.enabled": "false",
                 "spark.cosmos.write.patch.columnConfigs": "[col(vaccinated).op(set)]",
                 "spark.cosmos.write.patch.filter": predicate
                 }

pulmonogyDoctorId = "<pulmonology-doctor-id>"
urologyDoctorId = "<urology-doctor-id>"

# precondition failure will occur if the df doesn't satisfy the predicate
# data = [{"id": pulmonogyDoctorId,  "vaccinated": "false"}, {"id": urologyDoctorId,  "vaccinated": "false"}]

data = [{"id": pulmonogyDoctorId,  "vaccinated": "false"}]
patchDf = spark.createDataFrame(data)

patchDf.write.format("cosmos.oltp").mode(
    "Append").options(**cfgCondUpdate).save()

pulmonogyDoctor = spark.sql("select * from cosmosCatalog.{}.{} where id = '{}';".format(
    cosmosDatabaseName, cosmosContainerName, pulmonogyDoctorId))
urologyDoctor = spark.sql("select * from cosmosCatalog.{}.{} where id = '{}';".format(
    cosmosDatabaseName, cosmosContainerName, urologyDoctorId))

print("vaccination status of pulmonogy doctor")
pulmonogyDoctor.show()

print("vaccination status of urology doctor")
urologyDoctor.show()

# COMMAND ----------

# patch increment operation

cfgIncrement = {"spark.cosmos.accountEndpoint": cosmosEndpoint,
                "spark.cosmos.accountKey": cosmosMasterKey,
                "spark.cosmos.database": cosmosDatabaseName,
                "spark.cosmos.container": cosmosContainerName,
                "spark.cosmos.write.strategy": "ItemPatch",
                "spark.cosmos.write.bulk.enabled": "false",
                "spark.cosmos.write.patch.columnConfigs": "[col(directReports).op(increment)]"
                }

# document id
id = "<document-id>"

query = "select * from cosmosCatalog.{}.{} where id = '{}';".format(
    cosmosDatabaseName, cosmosContainerName, id)

dfBeforePatch = spark.sql(query)
print("document before patch operation")
dfBeforePatch.show()

data = [{"id": id, "directReports": 2}]
patchDf = spark.createDataFrame(data)

patchDf.write.format("cosmos.oltp").mode(
    "Append").options(**cfgIncrement).save()

dfAfterPatch = spark.sql(query)
print("document after patch operation")
dfAfterPatch.show()

# COMMAND ----------

# patch array operation

cfgArray = {"spark.cosmos.accountEndpoint": cosmosEndpoint,
            "spark.cosmos.accountKey": cosmosMasterKey,
            "spark.cosmos.database": cosmosDatabaseName,
            "spark.cosmos.container": cosmosContainerName,
            "spark.cosmos.write.strategy": "ItemPatch",
            "spark.cosmos.write.bulk.enabled": "false",
            "spark.cosmos.write.patch.columnConfigs": "[col(/speciality/0).path(/speciality/0).op(add),col(/speciality/1).path(/speciality/1).op(add)]"
            }

# document id
id = "<document-id>"

query = "select * from cosmosCatalog.{}.{} where id = '{}';".format(
    cosmosDatabaseName, cosmosContainerName, id)

dfBeforePatch = spark.sql(query)
print("document before patch operation")
dfBeforePatch.show()

data = [{"id": id, "/speciality/0": "IVF", "/speciality/1": "Endometriosis"}]
patchDf = spark.createDataFrame(data)

patchDf.write.format("cosmos.oltp").mode("Append").options(**cfgArray).save()

dfAfterPatch = spark.sql(query)
print("document after patch operation")
dfAfterPatch.show()

# COMMAND ----------

# patch bulk example

predicate = "from c where c.department = 'Gynaecology'"

cfgBulk = {"spark.cosmos.accountEndpoint": cosmosEndpoint,
           "spark.cosmos.accountKey": cosmosMasterKey,
           "spark.cosmos.database": cosmosDatabaseName,
           "spark.cosmos.container": cosmosContainerName,
           "spark.cosmos.write.strategy": "ItemPatch",
           "spark.cosmos.write.bulk.enabled": "true",
           "spark.cosmos.write.patch.columnConfigs": "[col(vaccinated).op(add)]",
           "spark.cosmos.write.patch.filter": predicate
           }

data = [
    {"id": "<document-id1>", "vaccinated": "true"},
    {"id": "<document-id2>", "vaccinated": "false"},
    {"id": "<document-id3>", "vaccinated": "true"},
    {"id": "<document-id4>", "vaccinated": "true"},
    {"id": "<document-id5>", "vaccinated": "true"},
    {"id": "<document-id6>", "vaccinated": "true"},
    {"id": "<document-id7>", "vaccinated": "true"},
    {"id": "<document-id8>", "vaccinated": "true"},
    {"id": "<document-id9>", "vaccinated": "true"},
    {"id": "<document-id10>", "vaccinated": "true"},
    {"id": "<document-id11>", "vaccinated": "true"},
    {"id": "<document-id12>", "vaccinated": "true"},
    {"id": "<document-id13>", "vaccinated": "true"},
    {"id": "<document-id14>", "vaccinated": "true"},
    {"id": "<document-id15>", "vaccinated": "true"},
    {"id": "<document-id16>", "vaccinated": "true"}
]
patchDf = spark.createDataFrame(data)

patchDf.write.format("cosmos.oltp").mode("Append").options(**cfgBulk).save()

query = "select * from cosmosCatalog.{}.{} where id = '{}';".format(
    cosmosDatabaseName, cosmosContainerName, "<document-id2>")

dfAfterPatch = spark.sql(query)
print("document after patch operation")
dfAfterPatch.show()

# COMMAND ----------