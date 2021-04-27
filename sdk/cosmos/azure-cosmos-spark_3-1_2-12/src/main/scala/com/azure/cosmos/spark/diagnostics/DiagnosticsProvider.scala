// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.diagnostics

import com.azure.cosmos.implementation.spark.OperationListener
import com.azure.cosmos.spark.SimpleDiagnostics

import scala.collection.concurrent.TrieMap

trait DiagnosticsProvider {
  def getLogger(classType: Class[_]) : ILogger

  def getOperationListener() : Option[OperationListener] = Option.empty
}

class DefaultDiagnostics extends DiagnosticsProvider {
  override def getLogger(classType: Class[_]): ILogger = new CosmosLogging(classType)
}

private[spark] object DiagnosticsSelector {
  private val providers = TrieMap[String, DiagnosticsProvider]()
  providers.put(classOf[SimpleDiagnostics].getName, new DefaultDiagnostics)

  def getOperationListener(diagnosticsProviderName: String) : DiagnosticsProvider = {
    providers.get(diagnosticsProviderName) match {
      case Some(provider) => {
        provider
      }
      case None => {
        this.synchronized {
          providers.get(diagnosticsProviderName) match {
            case Some(provider) => {
              provider
            }
            case None => {

              val provider: DiagnosticsProvider =
                Class.forName(diagnosticsProviderName).asSubclass(classOf[DiagnosticsProvider]).getDeclaredConstructor().newInstance()

              providers.put(diagnosticsProviderName, provider)
              provider
            }
          }
        }
      }
    }
  }
}