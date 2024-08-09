// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
/* Woodstox Lite ("wool") XML processor
 *
 * Copyright (c) 2006- Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in the file LICENSE which is
 * included with the source code.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.azure.xml.implementation.aalto;

import javax.xml.stream.*;

/**
 * Unchecked exception that has to be used for methods of Stax API that
 * do not allow throwing checked XMLStreamException, but where internal
 * methods that need to be called may throw such an exception.
 * It is, then, essentially used to overcome design flaws in Stax API.
 *<p>
 * This class generally tries to forward all requests to the underlying
 * exception class, in the hopes that it can for the most part look just
 * like if the underlying message was thrown. This may be confusing in
 * some ways ("Why did my catch (XMLStreamException ..) clause catch
 * this exception?"), but usually should make sense. 
 */
@SuppressWarnings("serial")
public class UncheckedStreamException extends RuntimeException {
    private UncheckedStreamException(XMLStreamException orig) {
        super(orig);
    }

    public static UncheckedStreamException createFrom(XMLStreamException sex) {
        return new UncheckedStreamException(sex);
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return getCause().getStackTrace();
    }

    @Override
    public String getMessage() {
        return getCause().getMessage();
    }

    @Override
    public String getLocalizedMessage() {
        return getCause().getLocalizedMessage();
    }

    @Override
    public void printStackTrace() {
        getCause().printStackTrace();
    }

    @Override
    public void printStackTrace(java.io.PrintStream p) {
        getCause().printStackTrace(p);
    }

    @Override
    public void printStackTrace(java.io.PrintWriter p) {
        getCause().printStackTrace(p);
    }

    @Override
    public String toString() {
        return getCause().toString();
    }
}
