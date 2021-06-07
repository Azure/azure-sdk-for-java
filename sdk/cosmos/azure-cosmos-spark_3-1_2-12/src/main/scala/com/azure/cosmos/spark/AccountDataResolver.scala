package com.azure.cosmos.spark

trait AccountDataResolver {
    def getAccountDataConfig(linkedServiceName: Option[String]): Map[String, String]
}
