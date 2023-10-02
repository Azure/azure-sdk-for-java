// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.typespec.json.implementation.jackson.core.util;

import java.io.IOException;

import com.typespec.json.implementation.jackson.core.JsonGenerator;

/**
 * Default linefeed-based indenter, used by {@link DefaultPrettyPrinter} (unless
 * overridden). Uses system-specific linefeeds and 2 spaces for indentation per level.
 * 
 * @since 2.5
 */
public class DefaultIndenter
    extends DefaultPrettyPrinter.NopIndenter
{
    private static final long serialVersionUID = 1L;

    public final static String SYS_LF;
    static {
        String lf;
        try {
            lf = System.getProperty("line.separator");
        } catch (Throwable t) {
            lf = "\n"; // fallback when security manager denies access
        }
        SYS_LF = lf;
    }

    public static final DefaultIndenter SYSTEM_LINEFEED_INSTANCE = new DefaultIndenter("  ", SYS_LF);

    /**
     * We expect to rarely get indentation deeper than this number of levels,
     * and try not to pre-generate more indentations than needed.
     */
    private final static int INDENT_LEVELS = 16;
    private final char[] indents;
    private final int charsPerLevel;
    private final String eol;

    /**
     * Indent with two spaces and the system's default line feed
     */
    public DefaultIndenter() {
        this("  ", SYS_LF);
    }
    
    /**
     * Create an indenter which uses the <code>indent</code> string to indent one level
     * and the <code>eol</code> string to separate lines.
     *
     * @param indent Indentation String to prepend for a single level of indentation
     * @param eol End-of-line marker to use after indented line
     */
    public DefaultIndenter(String indent, String eol)
    {
        charsPerLevel = indent.length();

        indents = new char[indent.length() * INDENT_LEVELS];
        int offset = 0;
        for (int i=0; i<INDENT_LEVELS; i++) {
            indent.getChars(0, indent.length(), indents, offset);
            offset += indent.length();
        }

        this.eol = eol;
    }
    
    public DefaultIndenter withLinefeed(String lf)
    {
        if (lf.equals(eol)) {
            return this;
        }
        return new DefaultIndenter(getIndent(), lf);
    }
    
    public DefaultIndenter withIndent(String indent)
    {
        if (indent.equals(getIndent())) {
            return this;
        }
        return new DefaultIndenter(indent, eol);
    }

    @Override
    public boolean isInline() { return false; }

    @Override
    public void writeIndentation(JsonGenerator jg, int level) throws IOException
    {
        jg.writeRaw(eol);
        if (level > 0) { // should we err on negative values (as there's some flaw?)
            level *= charsPerLevel;
            while (level > indents.length) { // unlike to happen but just in case
                jg.writeRaw(indents, 0, indents.length); 
                level -= indents.length;
            }
            jg.writeRaw(indents, 0, level);
        }
    }
    
    public String getEol() {
        return eol;
    }
    
    public String getIndent() {
        return new String(indents, 0, charsPerLevel);
    }
}