/* JUG Java Uuid Generator
 *
 * Copyright (c) 2002- Tatu Saloranta, tatu.saloranta@iki.fi
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

package com.azure.cosmos.implementation.uuid.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper we (only) need to support CLI usage (see {@link com.azure.cosmos.implementation.uuid.Jug}
 * wherein we do not actually  have logger package included; in which case we
 * will print warning(s) out to {@code System.err}.
 * For normal embedded usage no benefits, except if someone forgot their SLF4j API
 * package. :)
 *
 * @since 4.1
 */
public class LoggerFacade {
    private final Class<?> _forClass;

    private WrappedLogger _logger;
    
    private LoggerFacade(Class<?> forClass) {
        _forClass = forClass;
    }

    public static LoggerFacade getLogger(Class<?> forClass) {
        return new LoggerFacade(forClass);
    }

    public void warn(String msg) {
        _warn(msg);
    }

    public void warn(String msg, Object arg) {
        _warn(String.format(msg, arg));
    }

    public void warn(String msg, Object arg, Object arg2) {
        _warn(String.format(msg, arg, arg2));
    }

    private synchronized void _warn(String message) {
        if (_logger == null) {
            _logger = WrappedLogger.logger(_forClass);
        }
        _logger.warn(message);
    }

    private static class WrappedLogger {
        private final Logger _logger;

        private WrappedLogger(Logger l) {
            _logger = l;
        }

        public static WrappedLogger logger(Class<?> forClass) {
            // Why all these contortions? To support case where Slf4j API missing
            // (or, if it ever fails for not having impl) to just print to STDERR
            try {
                return new WrappedLogger(LoggerFactory.getLogger(forClass));
            } catch (Throwable t) {
                return new WrappedLogger(null);
            }
        }

        public void warn(String message) {
            if (_logger != null) {
                _logger.warn(message);
            } else {
                System.err.println("WARN: "+message);
            }
        }
    }
}
