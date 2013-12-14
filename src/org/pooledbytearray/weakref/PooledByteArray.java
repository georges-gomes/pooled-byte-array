package org.pooledbytearray.weakref;

import org.pooledbytearray.finalizer.PooledByteArrayFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.ReferenceQueue;

public class PooledByteArray
{
    private byte[] bytes = null;

    PooledByteArray(byte[] bytes)
    {
        this.bytes = bytes;
    }

    public int length()
    {
        return bytes.length;
    }

    public byte getAt(int index)
    {
        return bytes[index];
    }

    public void setAt(int index, byte b)
    {
        bytes[index] = b;
    }

    public void copyTo(byte[] dst, int dstPos, int offset, int len)
    {
        System.arraycopy(bytes, offset, dst, dstPos, len);
    }

    public void copyFrom(byte[] src, int srcPos, int offset, int len)
    {
        System.arraycopy(src, srcPos, bytes, offset, len);
    }

    public void writeTo(OutputStream out, int offset, int len) throws IOException
    {
        out.write(bytes, offset, len);
    }

    public int readFrom(InputStream in, int offset, int len) throws IOException
    {
        return in.read(bytes, offset, len);
    }
}
