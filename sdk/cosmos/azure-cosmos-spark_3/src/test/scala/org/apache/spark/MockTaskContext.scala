// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package org.apache.spark

import org.mockito.Mockito

object MockTaskContext  {
  def mockTaskContext(): TaskContext = {
    Mockito.mock(classOf[TaskContext])
  }
}
