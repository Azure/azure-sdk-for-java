// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package org.apache.spark

import org.mockito.Mockito

object MockTaskContext  {
  def setTaskContext(taskContext: TaskContext) : Unit = {
    TaskContext.setTaskContext(taskContext)
  }

  def setAndCreateTaskContext() : Unit = {
    val taskContext = Mockito.mock(classOf[TaskContext])
    TaskContext.setTaskContext(taskContext)
  }
}
