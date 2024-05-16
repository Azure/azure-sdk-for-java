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

import com.azure.cosmos.implementation.uuid.UUIDType;

import java.util.UUID;

public class UUIDUtil
{
    public final static int BYTE_OFFSET_CLOCK_LO = 0;
    public final static int BYTE_OFFSET_CLOCK_MID = 4;
    public final static int BYTE_OFFSET_CLOCK_HI = 6;

    // note: clock-hi and type occupy same byte (different bits)
    public final static int BYTE_OFFSET_TYPE = 6;

    // similarly, clock sequence and variant are multiplexed
    public final static int BYTE_OFFSET_CLOCK_SEQUENCE = 8;
    public final static int BYTE_OFFSET_VARIATION = 8;
	
    /*
    /**********************************************************************
    /* Construction (can instantiate, although usually not necessary)
    /**********************************************************************
     */

    // note: left public just for convenience; all functionality available
    // via static methods
    public UUIDUtil() { }

    /*
    /**********************************************************************
    /* Factory methods
    /**********************************************************************
     */
	
    /**
     * Factory method for creating UUIDs from the canonical string
     * representation.
     *
     * @param id String that contains the canonical representation of
     *   the UUID to build; 36-char string (see UUID specs for details).
     *   Hex-chars may be in upper-case too; UUID class will always output
     *   them in lowercase.
     */
    public static UUID uuid(String id)
    {
        if (id == null) {
            throw new NullPointerException();
        }
        if (id.length() != 36) {
            throw new NumberFormatException("UUID has to be represented by the standard 36-char representation");
        }

        long lo, hi;
        lo = hi = 0;
        
        for (int i = 0, j = 0; i < 36; ++j) {
        	
            // Need to bypass hyphens:
            switch (i) {
            case 8:
            case 13:
            case 18:
            case 23:
                if (id.charAt(i) != '-') {
                    throw new NumberFormatException("UUID has to be represented by the standard 36-char representation");
                }
                ++i;
            }
            int curr;
            char c = id.charAt(i);

            if (c >= '0' && c <= '9') {
                curr = (c - '0');
            } else if (c >= 'a' && c <= 'f') {
                curr = (c - 'a' + 10);
            } else if (c >= 'A' && c <= 'F') {
                curr = (c - 'A' + 10);
            } else {
                throw new NumberFormatException("Non-hex character at #"+i+": '"+c
                        +"' (value 0x"+Integer.toHexString(c)+")");
            }
            curr = (curr << 4);

            c = id.charAt(++i);

            if (c >= '0' && c <= '9') {
                curr |= (c - '0');
            } else if (c >= 'a' && c <= 'f') {
                curr |= (c - 'a' + 10);
            } else if (c >= 'A' && c <= 'F') {
                curr |= (c - 'A' + 10);
            } else {
                throw new NumberFormatException("Non-hex character at #"+i+": '"+c
                        +"' (value 0x"+Integer.toHexString(c)+")");
            }
            if (j < 8) {
            	hi = (hi << 8) | curr;
            } else {
            	lo = (lo << 8) | curr;
            }
            ++i;
        }		
        return new UUID(hi, lo);
    }

    /**
     * Factory method for constructing {@link UUID} instance from given
     * 16 bytes.
     * NOTE: since absolutely no validation is done for contents, this method should
     * only be used if contents are known to be valid.
     */
    public static UUID uuid(byte[] bytes)
    {
        _checkUUIDByteArray(bytes, 0);
        long l1 = gatherLong(bytes, 0);
        long l2 = gatherLong(bytes, 8);
        return new UUID(l1, l2);
    }

    /**
     * Factory method for constructing {@link UUID} instance from given
     * 16 bytes.
     * NOTE: since absolutely no validation is done for contents, this method should
     * only be used if contents are known to be valid.
     * 
     * @param bytes Array that contains sequence of 16 bytes that contain a valid UUID
     * @param offset Offset of the first of 16 bytes
     */
    public static UUID uuid(byte[] bytes, int offset)
    {
        _checkUUIDByteArray(bytes, offset);
        return new UUID(gatherLong(bytes, offset), gatherLong(bytes, offset+8));
    }

    /**
     * Helper method for constructing UUID instances with appropriate type
     */
    public static UUID constructUUID(UUIDType type, byte[] uuidBytes)
    {
        // first, ensure type is ok
        int b = uuidBytes[BYTE_OFFSET_TYPE] & 0xF; // clear out high nibble
        b |= type.raw() << 4;
        uuidBytes[BYTE_OFFSET_TYPE] = (byte) b;
        // second, ensure variant is properly set too
        b = uuidBytes[UUIDUtil.BYTE_OFFSET_VARIATION] & 0x3F; // remove 2 MSB
        b |= 0x80; // set as '10'
        uuidBytes[BYTE_OFFSET_VARIATION] = (byte) b;
        return uuid(uuidBytes);
    }
    
    public static UUID constructUUID(UUIDType type, long l1, long l2)
    {
        // first, ensure type is ok
        l1 &= ~0xF000L; // remove high nibble of 6th byte
        l1 |= (long) (type.raw() << 12);
        // second, ensure variant is properly set too (8th byte; most-sig byte of second long)
        l2 = ((l2 << 2) >>> 2); // remove 2 MSB
        l2 |= (2L << 62); // set 2 MSB to '10'
        return new UUID(l1, l2);
    }

    public static long initUUIDFirstLong(long l1, UUIDType type)
    {
        return initUUIDFirstLong(l1, type.raw());
    }

    public static long initUUIDFirstLong(long l1, int rawType)
    {
        l1 &= ~0xF000L; // remove high nibble of 6th byte
        l1 |= (long) (rawType << 12);
        return l1;
    }
    
    public static long initUUIDSecondLong(long l2)
    {
        l2 = ((l2 << 2) >>> 2); // remove 2 MSB
        l2 |= (2L << 62); // set 2 MSB to '10'
        return l2;
    }
    
    /*
    /***********************************************************************
    /* Type introspection
    /***********************************************************************
     */

    /**
     * Method for determining which type of UUID given UUID is.
     * Returns null if type can not be determined.
     * 
     * @param uuid UUID to check
     * 
     * @return Null if UUID is null or type can not be determined (== invalid UUID);
     *   otherwise type
     */
    public static UUIDType typeOf(UUID uuid)
    {
        if (uuid == null) {
            return null;
        }
        // Ok: so 4 MSB of byte at offset 6...
        long l = uuid.getMostSignificantBits();
        int typeNibble = (((int) l) >> 12) & 0xF;
        switch (typeNibble) {
        case 0:
            // possibly null?
            if (l == 0L && uuid.getLeastSignificantBits() == l) {
                return UUIDType.UNKNOWN;
            }
            break;
        case 1:
            return UUIDType.TIME_BASED;
        case 2:
            return UUIDType.DCE;
        case 3:
            return UUIDType.NAME_BASED_MD5;
        case 4:
            return UUIDType.RANDOM_BASED;
        case 5:
            return UUIDType.NAME_BASED_SHA1;
        }
        // not recognized: return null
        return null;
    }
	
    /*
    /***********************************************************************
    /* Conversions to other types
    /***********************************************************************
     */
	
    public static byte[] asByteArray(UUID uuid)
    {
        long hi = uuid.getMostSignificantBits();
        long lo = uuid.getLeastSignificantBits();
        byte[] result = new byte[16];
        _appendInt((int) (hi >> 32), result, 0);
        _appendInt((int) hi, result, 4);
        _appendInt((int) (lo >> 32), result, 8);
        _appendInt((int) lo, result, 12);
        return result;
    }

    public static void toByteArray(UUID uuid, byte[] buffer) {
        toByteArray(uuid, buffer, 0);
    }

    public static void toByteArray(UUID uuid, byte[] buffer, int offset)
    {
        _checkUUIDByteArray(buffer, offset);
        long hi = uuid.getMostSignificantBits();
        long lo = uuid.getLeastSignificantBits();
        _appendInt((int) (hi >> 32), buffer, offset);
        _appendInt((int) hi, buffer, offset+4);
        _appendInt((int) (lo >> 32), buffer, offset+8);
        _appendInt((int) lo, buffer, offset+12);
    }

    /*
    /******************************************************************************** 
    /* Package helper methods
    /******************************************************************************** 
     */
    
    //private final static long MASK_LOW_INT = 0x0FFFFFFFF;

    protected final static long gatherLong(byte[] buffer, int offset)
    {
        long hi = ((long) _gatherInt(buffer, offset)) << 32;
        //long lo = ((long) _gatherInt(buffer, offset+4)) & MASK_LOW_INT;
        long lo = (((long) _gatherInt(buffer, offset+4)) << 32) >>> 32;
        return hi | lo;
    }
    
    /*
    /******************************************************************************** 
    /* Internal helper methods
    /******************************************************************************** 
     */

    private final static void _appendInt(int value, byte[] buffer, int offset)
    {
        buffer[offset++] = (byte) (value >> 24);
        buffer[offset++] = (byte) (value >> 16);
        buffer[offset++] = (byte) (value >> 8);
        buffer[offset] = (byte) value;
    }
	
    private final static int _gatherInt(byte[] buffer, int offset)
    {
        return (buffer[offset] << 24) | ((buffer[offset+1] & 0xFF) << 16)
            | ((buffer[offset+2] & 0xFF) << 8) | (buffer[offset+3] & 0xFF);
    }

    private final static void _checkUUIDByteArray(byte[] bytes, int offset)
    {
        if (bytes == null) {
            throw new IllegalArgumentException("Invalid byte[] passed: can not be null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Invalid offset ("+offset+") passed: can not be negative");
        }
        if ((offset + 16) > bytes.length) {
            throw new IllegalArgumentException("Invalid offset ("+offset+") passed: not enough room in byte array (need 16 bytes)");
        }
    }
}
