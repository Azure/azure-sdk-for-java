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

/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.cosmos.implementation.uuid.impl;

import com.azure.cosmos.implementation.uuid.StringArgGenerator;
import com.azure.cosmos.implementation.uuid.UUIDType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.UUID;

/**
 * Implementation of UUID generator that uses one of name-based generation methods
 * (variants 3 (MD5) and 5 (SHA1)).
 *<p>
 * As all JUG provided implementations, this generator is fully thread-safe; access
 * to digester is synchronized as necessary.
 * 
 * @since 3.0
 */
public class NameBasedGenerator extends StringArgGenerator
{

    private static final Logger logger = LoggerFactory.getLogger(NameBasedGenerator.class);
    
    public final static Charset _utf8;
    static {
        _utf8 = Charset.forName("UTF-8");
    }
    
    /**
     * Namespace used when name is a DNS name.
     */
    public final static UUID NAMESPACE_DNS = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");

    /**
     * Namespace used when name is a URL.
     */
    public final static UUID NAMESPACE_URL = UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8");
    /**
     * Namespace used when name is an OID.
     */
    public final static UUID NAMESPACE_OID = UUID.fromString("6ba7b812-9dad-11d1-80b4-00c04fd430c8");
    /**
     * Namespace used when name is an X500 identifier
     */
    public final static UUID NAMESPACE_X500 = UUID.fromString("6ba7b814-9dad-11d1-80b4-00c04fd430c8");

    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    /**
     * Namespace to use as prefix.
     */
    protected final UUID _namespace;
    
    /**
     * Message digesster to use for hash calculation
     */
    protected final MessageDigest _digester;

    protected final UUIDType _type;
    
    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */

    /**
     * @param namespace of the namespace, as defined by the
     *   spec. UUID has 4 pre-defined "standard" name space strings
     *   that can be passed to UUID constructor (see example below).
     *   Note that this argument is optional; if no namespace is needed
     *   (for example when name includes namespace prefix), null may be passed.
     * @param digester Hashing algorithm to use. 

    */
    public NameBasedGenerator(UUID namespace, MessageDigest digester, UUIDType type)
    {
        _namespace = namespace;
        // And default digester SHA-1
        if (digester == null) {
            
        }
        if (type == null) {
            String typeStr = digester.getAlgorithm();
            if (typeStr.startsWith("MD5")) {
                type = UUIDType.NAME_BASED_MD5;
            } else if (typeStr.startsWith("SHA")) {
                type = UUIDType.NAME_BASED_SHA1;
            } else {
                // Hmmh... error out? Let's default to SHA-1, but log a warning
                type = UUIDType.NAME_BASED_SHA1;
                logger.warn("Could not determine type of Digester from '{}'; assuming 'SHA-1' type", typeStr);
            }
        }
        _digester = digester;
        _type = type;
    }

    /*
    /**********************************************************************
    /* Access to config
    /**********************************************************************
     */

    @Override
    public UUIDType getType() { return _type; }
    
    public UUID getNamespace() { return _namespace; }
    
    /*
    /**********************************************************************
    /* UUID generation
    /**********************************************************************
     */

    @Override
    public UUID generate(String name)
    {
        // !!! TODO: 14-Oct-2010, tatu: can repurpose faster UTF-8 encoding from Jackson
        return generate(name.getBytes(_utf8));
    }
    
    @Override
    public UUID generate(byte[] nameBytes)
    {
        byte[] digest;
        synchronized (_digester) {
            _digester.reset();
            if (_namespace != null) {
                _digester.update(UUIDUtil.asByteArray(_namespace));
            }
            _digester.update(nameBytes);
            digest = _digester.digest();
        }
        return UUIDUtil.constructUUID(_type, digest);
    }
}
