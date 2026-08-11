// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2008-2018, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

/**
 * The `Loan`s apply method implements Java's try-with-resources. You can loan
 * out a resource list to a function `f` and all the resources will be closed when `f`
 * returns regardless of the way `f` returns.
 *
 * See https://docs.oracle.com/javase/specs/jls/se8/html/jls-14.html#jls-14.20.3
 *
 * In the following example `f` is applied to an input stream if it is
 * non-null. If the input stream is null, then the RuntimeException is thrown.
 * If `f` throws an exception, then the input stream is closed and the
 * exception is rethrown.  If furthermore, closing the input stream throw an
 * exception then that exception is suppressed on the exception thrown by `f`.
 *
 * Example:
 * {{{
 *   import scala.util.Loan
 *   import java.io.InputStream
 *
 *   def withResource[A](resource: String)(f: InputStream => A): A =
 *     Loan(Option(getClass.getResourceAsStream(resource)).getOrElse(throw new RuntimeException("Oh no"))) to f
 * }}}
 */

package com.azure.cosmos.spark

// scalastyle:off null
private[spark] object Loan {
 class Loan[A <: AutoCloseable](resourceList: List[Option[A]]) {
  def to[B](block: List[Option[A]] => B): B = {
   var t: Throwable = null
   try {
    block(resourceList)
   } catch {
    case x: Throwable =>
     t = x
     throw x
   } finally {
    if (resourceList != null) {
     if (t != null) {
      resourceList.foreach(resourceOpt => {
       try {
        if (resourceOpt.isDefined) {
         resourceOpt.get.close()
        }
       } catch {
        case y: Throwable =>
         t.addSuppressed(y)
       }
      })
     } else {
      resourceList.foreach(resourceOpt => {
       if (resourceOpt.isDefined) {
        resourceOpt.get.close()
       }
      })
     }
    }
   }
  }
 }
 // scalastyle:on null

 def apply[A <: AutoCloseable](resourceList: List[Option[A]]): Loan[A] = new Loan(resourceList)
}
