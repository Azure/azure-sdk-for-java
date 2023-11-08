// Databricks notebook source
import org.apache.spark.sql.functions.lit

val cosmosEndpoint = "<cosmos-endpoint>"
val cosmosMasterKey = "<cosmos-master-key>"
val cosmosDatabaseName = "ContosoHospital"
val cosmosContainerName = "Doctor"

val cfg = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
  "spark.cosmos.accountKey" -> cosmosMasterKey,
  "spark.cosmos.database" -> cosmosDatabaseName,
  "spark.cosmos.container" -> cosmosContainerName
)

//sample schema for Doctor container

// {
//     "id": "2e82d21c-379a-4ab5-b56b-9c67a95ae09c",
//     "name": "Elizebeth O'Keefe III",
//     "dob": "2022-31-04",
//     "gender": "female",
//     "department": "Cardiology",
//     "address": "Suite 466 31270 Blanda Glen, Sipesmouth, NE 08025-9135"
// }

//sample schema for Slot container

// {
//     "id": "5eec4574-8e3e-47a2-b7b6-21ab1cb5a0c5",
//     "doctorId": "890a061d-4671-428a-8f83-0aef297b645b",
//     "doctorName": "Paris Kihn",
//     "slotDate": "2022-08-07",
//     "startTime": 1659750013234,
//     "endTime": 1659751813234,
//     "department": "Pulmonology",
//     "patientId": "b7a34a39-e77b-4c30-950c-8f30b53bc37e",
//     "patientName": "Loria Hand",
//     "appointmentDate": "2022-08-05"
// }

// COMMAND ----------

// create Cosmos Database and Cosmos Container using Catalog APIs
spark.conf.set(s"spark.sql.catalog.cosmosCatalog", "com.azure.cosmos.spark.CosmosCatalog")
spark.conf.set(s"spark.sql.catalog.cosmosCatalog.spark.cosmos.accountEndpoint", cosmosEndpoint)
spark.conf.set(s"spark.sql.catalog.cosmosCatalog.spark.cosmos.accountKey", cosmosMasterKey)

// COMMAND ----------


// patch add operation

import org.apache.spark.sql.functions.lit

val cfgPatch = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabaseName,
        "spark.cosmos.container" -> cosmosContainerName,
        "spark.cosmos.write.strategy" -> "ItemPatch",
        "spark.cosmos.write.bulk.enabled" -> "false",
        "spark.cosmos.write.patch.defaultOperationType" -> "None",
        "spark.cosmos.write.patch.columnConfigs" -> "[col(directReports).op(add)]"
      )

// if id is the partition key, then we can only provide id otherwise both id and 
// partition keys are required to uniquely identify a document in cosmos db
val id = "<document-id>"
val query = s"select * from cosmosCatalog.${cosmosDatabaseName}.${cosmosContainerName} where id = '$id';"

val dfBeforePatch = spark.sql(query)
println("document before patch operation")
dfBeforePatch.show()
val df = dfBeforePatch.withColumn("directReports", lit(1))
df.write.format("cosmos.oltp").mode("Append").options(cfgPatch).save()
val dfAfterPatch = spark.sql(query)
println("document after patch operation")
dfAfterPatch.show()

// COMMAND ----------

// patch set operation

val cfgSet = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabaseName,
        "spark.cosmos.container" -> cosmosContainerName,
        "spark.cosmos.write.strategy" -> "ItemPatch",
        "spark.cosmos.write.bulk.enabled" -> "false",
         
        "spark.cosmos.write.patch.columnConfigs" -> "[col(name).op(set)]"
      )

val id = "<document-id>"
val query = s"select * from cosmosCatalog.${cosmosDatabaseName}.${cosmosContainerName} where id = '$id';"

val dfBeforePatch = spark.sql(query)
println("document before patch operation")
dfBeforePatch.show()
val patchDf =  Seq(
        (id,  "Joel Brakus")
      ).toDF("id", "name")

patchDf.write.format("cosmos.oltp").mode("Append").options(cfgPatch).save()
val dfAfterPatch = spark.sql(query)
println("document after patch operation")
dfAfterPatch.show()


// patch replace operation 

val slotContainer = "Slot"
val cfgReplace = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabaseName,
        "spark.cosmos.container" -> slotContainer,
        "spark.cosmos.write.strategy" -> "ItemPatch",
        "spark.cosmos.write.bulk.enabled" -> "false",
        "spark.cosmos.write.patch.columnConfigs" -> "[col(patientName).op(replace)]"
      )

// document id
val id = "<document-id>"
//partition key 
val doctorId = "<partition-key>"

val query = s"select * from cosmosCatalog.${cosmosDatabaseName}.${slotContainer} where id ='$id' and doctorId = '$doctorId';"
val dfBeforePatch = spark.sql(query)
print("document before patch operation")
dfBeforePatch.show()

 val patchDf = Seq(
        (id, doctorId, "Joel Brakus")
      ).toDF("id", "doctorId", "patientName")

patchDf.write.format("cosmos.oltp").mode("Append").options(cfgReplace).save()

val dfAfterPatch = spark.sql(query)
print("document after patch operation")
dfAfterPatch.show()

// COMMAND ----------

// patch remove operation 

val slotContainer = "Slot"

//removing any column which doesn't exist will cause error
val cfgRemove = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabaseName,
        "spark.cosmos.container" -> slotContainer,
        "spark.cosmos.write.strategy" -> "ItemPatch",
        "spark.cosmos.write.bulk.enabled" -> "false",
        "spark.cosmos.write.patch.columnConfigs" -> "[col(patientName).op(remove)]"
      )

// document id
val id = "<document-id>"
//partition key 
val doctorId = "<partition-key>"

val query = s"select * from cosmosCatalog.${cosmosDatabaseName}.${slotContainer} where id ='$id' and doctorId = '$doctorId';"
val dfBeforePatch = spark.sql(query)
print("document before patch operation")
dfBeforePatch.show()

dfBeforePatch.write.format("cosmos.oltp").mode("Append").options(cfgRemove).save()

val dfAfterPatch = spark.sql(query)
print("document after patch operation")
dfAfterPatch.show()


// COMMAND ----------

// demonstrates how to use a predicate for conditional update
val predicate = "from c where c.department = 'Pulmonology'"

val cfgCondUpdate = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabaseName,
        "spark.cosmos.container" -> cosmosContainerName,
        "spark.cosmos.write.strategy" -> "ItemPatch",
        "spark.cosmos.write.bulk.enabled" -> "false",
        "spark.cosmos.write.patch.columnConfigs" -> "[col(vaccinated).op(set)]",
        "spark.cosmos.write.patch.filter" -> predicate
      )

val pulmonogyDoctorId = "fc6ccf65-c3f2-460b-8e07-93854074d1b5"
val urologyDoctorId = "aae645f7-f56f-4929-9c27-9ff247b60f63"

//precondition failure will occur if the df doesn't satisfy the predicate
// val patchDf = Seq(
//         (pulmonogyDoctorId, "false"), (urologyDoctorId, "true")
//       ).toDF("id", "vaccinated")

val patchDf = Seq(
        (pulmonogyDoctorId, "false")
      ).toDF("id", "vaccinated")

patchDf.write.format("cosmos.oltp").mode("Append").options(cfgCondUpdate).save()

val pulmonogyDoctor = spark.sql(s"select * from cosmosCatalog.${cosmosDatabaseName}.${cosmosContainerName} where id = '$pulmonogyDoctorId';")
val urologyDoctor = spark.sql(s"select * from cosmosCatalog.${cosmosDatabaseName}.${cosmosContainerName} where id = '$urologyDoctorId';")

println("vaccination status of pulmonogy doctor")
pulmonogyDoctor.show()

//only pulmonology doctor's vaccination status is updated
println("vaccination status of urology doctor")
urologyDoctor.show()

// COMMAND ----------

// patch increment 

val cfgPatch = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabaseName,
        "spark.cosmos.container" -> cosmosContainerName,
        "spark.cosmos.write.strategy" -> "ItemPatch",
        "spark.cosmos.write.bulk.enabled" -> "false",
        "spark.cosmos.write.patch.columnConfigs" -> "[col(directReports).op(increment)]"
      )

val id = "<document-id>"
val query = s"select * from cosmosCatalog.${cosmosDatabaseName}.${cosmosContainerName} where id = '$id';"

val dfBeforePatch = spark.sql(query)
println("document before patch operation")
dfBeforePatch.show()

val patchDf = Seq(
        (id, 2)
      ).toDF("id", "directReports")

patchDf.write.format("cosmos.oltp").mode("Append").options(cfgPatch).save()
val dfAfterPatch = spark.sql(query)
println("document after patch operation")
dfAfterPatch.show()

// COMMAND ----------

// patch add array

import org.apache.spark.sql.functions.lit

val cfgPatch = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabaseName,
        "spark.cosmos.container" -> cosmosContainerName,
        "spark.cosmos.write.strategy" -> "ItemPatch",
        "spark.cosmos.write.bulk.enabled" -> "false",
        "spark.cosmos.write.patch.columnConfigs" -> "[col(/speciality/0).path(/speciality/0).op(add),col(/speciality/1).path(/speciality/1).op(add)]"
      )



val id = "<document-id>"
val query = s"select * from cosmosCatalog.${cosmosDatabaseName}.${cosmosContainerName} where id = '$id';"

val dfBeforePatch = spark.sql(query)
println("document before patch operation")
dfBeforePatch.show()

val patchDf = Seq(
        (id,"IVF", "endometriosis")
      ).toDF("id", "/speciality/0", "/speciality/1")

patchDf.write.format("cosmos.oltp").mode("Append").options(cfgPatch).save()
val dfAfterPatch = spark.sql(query)
println("document after patch operation")
dfAfterPatch.show()

// COMMAND ----------

// patch bulk example 
val predicate = "from c where c.department = 'Gynaecology'"

val cfgBulkPatch = Map("spark.cosmos.accountEndpoint" -> cosmosEndpoint,
        "spark.cosmos.accountKey" -> cosmosMasterKey,
        "spark.cosmos.database" -> cosmosDatabaseName,
        "spark.cosmos.container" -> cosmosContainerName,
        "spark.cosmos.write.strategy" -> "ItemPatch",
        "spark.cosmos.write.bulk.enabled" -> "true",
        "spark.cosmos.write.patch.columnConfigs" -> "[col(vaccinated).op(add)]",
        "spark.cosmos.write.patch.filter" -> predicate
      )


val patchDf = Seq(
("<document-id1>","true"),
("<document-id2>","true"),
("<document-id3>","true"),
("<document-id4>","true"),
("<document-id5>","true"),
("<document-id6>","true"),
("<document-id7>","true"),
("<document-id8>","true"),
("<document-id9>","true"),
("<document-id10>","true"),
("<document-id11>","true"),
("<document-id12>","true"),
("<document-id13>","true"),
("<document-id14>","true"),
("<document-id15>","true"),
("<document-id16>","true")
      ).toDF("id", "vaccinated")

patchDf.write.format("cosmos.oltp").mode("Append").options(cfgBulkPatch).save()
val dfAfterPatch = spark.sql(s"select * from cosmosCatalog.${cosmosDatabaseName}.${cosmosContainerName} where id ='<document-id>';")
println("document after patch operation")
dfAfterPatch.show()

// COMMAND ----------