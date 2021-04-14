// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.spark.diagnostics

import com.azure.cosmos.implementation.spark.OperationListener
import com.azure.cosmos.spark.SimpleOperationLogger

import scala.collection.concurrent.TrieMap

object OperationListenerFactory {
  private val listeners = TrieMap[String, OperationListener]()
  listeners.put(classOf[SimpleOperationLogger].getSimpleName, new SimpleOperationLogger)

  def getOperationListener(listenerName: String) : OperationListener = {
    listeners.get(listenerName) match {
      case Some(listener) => {
        listener
      }
      case None => {
        this.synchronized {
          listeners.get(listenerName) match {
            case Some(listener) => {
              listener
            }
            case None => {

              val listener: OperationListener =
                Class.forName(listenerName).asSubclass(classOf[OperationListener]).getDeclaredConstructor().newInstance()

              listeners.put(listenerName, listener)
              listener
            }
          }
        }
      }
    }
  }
}
