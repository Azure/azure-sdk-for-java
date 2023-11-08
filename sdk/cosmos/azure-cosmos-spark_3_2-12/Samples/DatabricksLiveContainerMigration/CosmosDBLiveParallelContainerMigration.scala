// Databricks notebook source
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.util.control.NonFatal

case class NotebookData(path: String, timeout: Int, parameters: Map[String, String] = Map.empty[String, String])

def parallelNotebooks(notebooks: Seq[NotebookData], numNotebooksInParallel: Int): Future[Seq[String]] = {
  import scala.concurrent.{Future, blocking, Await}
  import java.util.concurrent.Executors
  import scala.concurrent.ExecutionContext
  import com.databricks.WorkflowException

  // val numNotebooksInParallel = 4 
  // If you create too many notebooks in parallel the driver may crash when you submit all of the jobs at once. 
  // This code limits the number of parallel notebooks.
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(numNotebooksInParallel))
  val ctx = dbutils.notebook.getContext()
  
  Future.sequence(
    notebooks.map { notebook => 
      Future {
        dbutils.notebook.setContext(ctx)
        if (notebook.parameters.nonEmpty)
          dbutils.notebook.run(notebook.path, notebook.timeout, notebook.parameters)
        else
          dbutils.notebook.run(notebook.path, notebook.timeout)
      }
      .recover {
        case NonFatal(e) => s"ERROR: ${e.getMessage}"
      }
    }
  )
}

def parallelNotebook(notebook: NotebookData): Future[String] = {
  import scala.concurrent.{Future, blocking, Await}
  import java.util.concurrent.Executors
  import scala.concurrent.ExecutionContext.Implicits.global
  import com.databricks.WorkflowException

  val ctx = dbutils.notebook.getContext()
  // The simplest interface we can have but doesn't
  // have protection for submitting to many notebooks in parallel at once
  Future {
    dbutils.notebook.setContext(ctx)
    
    if (notebook.parameters.nonEmpty)
      dbutils.notebook.run(notebook.path, notebook.timeout, notebook.parameters)
    else
      dbutils.notebook.run(notebook.path, notebook.timeout)
    
  }
  .recover {
    case NonFatal(e) => s"ERROR: ${e.getMessage}"
  }
}


// COMMAND ----------

val rawFullFilePath = "dbfs:/FileStore/cosmosDBLiveMigrationList.csv"
val df = spark.read.option("header", true).csv(rawFullFilePath)

val schemaList = df.schema.map(_.name).zipWithIndex//get schema list from dataframe

// define some way to generate a sequence of workloads to run
val jobArguments = df.rdd.map(row =>
  //here rec._1 is column name and rce._2 index
  schemaList.map(rec => (rec._1, row(rec._2).toString)).toMap
 ).collect

// COMMAND ----------

val notebookToRun = "CosmosDBLiveContainerMigration"

val notebooks = jobArguments.map { case (args) => NotebookData(notebookToRun, 0, args) }.toSeq

val numNotebooksInParallel = 4
val res = parallelNotebooks(notebooks, numNotebooksInParallel)

Await.result(res, 30 seconds) // this is a blocking call.
res.value