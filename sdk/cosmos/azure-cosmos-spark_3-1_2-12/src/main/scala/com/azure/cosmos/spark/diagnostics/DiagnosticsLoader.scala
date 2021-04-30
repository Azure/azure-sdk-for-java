// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.diagnostics

import com.azure.cosmos.spark.DiagnosticsConfig

import scala.collection.concurrent.TrieMap

private[spark] object DiagnosticsLoader {
  private val defaultDiagnostics = new DefaultDiagnostics
  private val providers = TrieMap[String, DiagnosticsProvider]()
  providers.put(classOf[SimpleDiagnosticsProvider].getName, new SimpleDiagnosticsProvider)

  def getDiagnosticsProvider(cfg: DiagnosticsConfig): DiagnosticsProvider = {
    cfg.mode match {
      case Some(value) => {
        getDiagnosticsProvider(value)
      }
      case None => {
        defaultDiagnostics
      }
    }
  }

  private def getDiagnosticsProvider(diagnosticsProviderName: String): DiagnosticsProvider = {
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
