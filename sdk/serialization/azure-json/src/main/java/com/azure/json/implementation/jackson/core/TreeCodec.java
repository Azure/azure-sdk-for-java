// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core;

import java.io.IOException;

/**
 * Interface that defines objects that can read and write
 * {@link TreeNode} instances using Streaming API.
 *
 * @since 2.3
 */
public abstract class TreeCodec {

    public abstract void writeTree(JsonGenerator g, TreeNode tree) throws IOException;

}
