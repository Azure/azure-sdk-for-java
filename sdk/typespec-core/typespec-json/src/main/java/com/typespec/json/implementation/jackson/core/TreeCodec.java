// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core;

import java.io.IOException;

/**
 * Interface that defines objects that can read and write
 * {@link TreeNode} instances using Streaming API.
 * 
 * @since 2.3
 */
public abstract class TreeCodec
{
    public abstract <T extends TreeNode> T readTree(JsonParser p) throws IOException, JsonProcessingException;
    public abstract void writeTree(JsonGenerator g, TreeNode tree) throws IOException, JsonProcessingException;

    /**
     * @return Node that represents "missing" node during traversal: something
     *   referenced but that does not exist in content model
     * 
     * @since 2.10
     */
    public TreeNode missingNode() {
        return null;
    }

    /**
     * @return Node that represents explict {@code null} value in content
     * 
     * @since 2.10
     */
    public TreeNode nullNode() {
        return null;
    }

    public abstract TreeNode createArrayNode();
    public abstract TreeNode createObjectNode();
    public abstract JsonParser treeAsTokens(TreeNode node);
}
