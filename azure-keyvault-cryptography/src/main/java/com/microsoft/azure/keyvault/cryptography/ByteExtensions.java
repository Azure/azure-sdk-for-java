/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.cryptography;

public final class ByteExtensions {

    public static boolean sequenceEqualConstantTime( byte[] self, byte[] other )
    {
        if ( self == null )
            throw new IllegalArgumentException( "self" );

        if ( other == null )
            throw new IllegalArgumentException( "other" );

        // Constant time comparison of two byte arrays
        long difference = ( self.length & 0xffffffffl ) ^ ( other.length & 0xffffffffl );

        for ( int i = 0; i < self.length && i < other.length; i++ )
        {
            difference |= ( self[i] ^ other[i] ) & 0xffffffffl;
        }

        return difference == 0;
    }

    public static byte[] or( byte[] self, byte[] other )
    {
        return or( self, other, 0 );
    }

    public static byte[] or( byte[] self, byte[] other, int offset )
    {
        if ( self == null )
            throw new IllegalArgumentException( "self" );

        if ( other == null )
            throw new IllegalArgumentException( "other" );

        if ( self.length > other.length - offset )
            throw new IllegalArgumentException( "self and other lengths do not match" );

        byte[] result = new byte[self.length];

        for ( int i = 0; i < self.length; i++ )
        {
            result[i] = (byte)( self[i] | other[offset + i] );
        }

        return result;
    }
    
    public static byte[] xor( byte[] self, byte[] other ) {
    	return xor( self, other, 0, false );
    }

    public static byte[] xor( byte[] self, byte[] other, boolean inPlace )
    {
        return xor( self, other, 0, inPlace );
    }
    
    public static byte[] xor( byte[] self, byte[] other, int offset ) {
    	return xor( self, other, 0, false );
    }

    public static byte[] xor( byte[] self, byte[] other, int offset, boolean inPlace )
    {
        if ( self == null )
            throw new IllegalArgumentException( "self" );

        if ( other == null )
            throw new IllegalArgumentException( "other" );

        if ( self.length > other.length - offset )
            throw new IllegalArgumentException( "self and other lengths do not match" );

        if ( inPlace )
        {
            for ( int i = 0; i < self.length; i++ )
            {
                self[i] = (byte)( self[i] ^ other[offset + i] );
            }

            return self;
        }
        else
        {
            byte[] result = new byte[self.length];

            for ( int i = 0; i < self.length; i++ )
            {
                result[i] = (byte)( self[i] ^ other[offset + i] );
            }

            return result;
        }
    }

    public static byte[] take( byte[] self, int count )
    {
        return ByteExtensions.take( self, 0, count );
    }

    
    /**
     * Takes the first count bytes from the source and
     * returns a new array containing those bytes.
     * 
     * @param self The source of the bytes.
     * @param offset The starting offset.
     * @param count The number of bytes to take.
     * @return count bytes from the source as a new array.
     */
    public static byte[] take( byte[] self, int offset, int count )
    {
        if ( self == null )
            throw new IllegalArgumentException( "self" );

        if ( offset < 0 )
            throw new IllegalArgumentException( "offset cannot be < 0" );

        if ( count <= 0 )
            throw new IllegalArgumentException( "count cannot be <= 0" );

        if ( offset + count > self.length )
            throw new IllegalArgumentException( "offset + count cannot be > self.Length" );

        byte[] result = new byte[count];

        System.arraycopy( self, offset, result, 0, count );

        return result;
    }

    public static void zero( byte[] self )
    {
        if ( self == null )
            throw new IllegalArgumentException( "self" );

        for ( int i = 0; i < self.length; i++ ) {
        	self[i] = 0;
        }
    }
}
