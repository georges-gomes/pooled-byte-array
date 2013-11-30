package org.pooledbytearray;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class PooledByteArrayFactory
{
    private Deque<byte[]> pool = new ConcurrentLinkedDeque<>();
    private final int arraySize;

    public PooledByteArrayFactory(int arraySize, int preAllocSize)
    {
        this.arraySize = arraySize;

        for(int i=0; i<preAllocSize; i++)
        {
            pool.push(new byte[arraySize]);
        }
    }

    /**
     * @return Ready to use PooledByteArray. Be careful not reset!
     */
    public PooledByteArray getByteArray()
    {
        if (pool.isEmpty())
        {
            return new PooledByteArray(this, new byte[arraySize]);
        }

        return new PooledByteArray(this, pool.pop());
    }

    /**
     * Automatically called by Pooled o finalizer
     * Default modifier for internal call only
     * @param bytes to recycle
     */
    void recycle(byte[] bytes)
    {
        pool.push(bytes);
    }

}
