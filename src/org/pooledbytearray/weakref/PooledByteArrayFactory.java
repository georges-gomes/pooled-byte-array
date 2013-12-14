package org.pooledbytearray.weakref;

import java.lang.ref.ReferenceQueue;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class PooledByteArrayFactory
{
    private Deque<byte[]> pool = new ConcurrentLinkedDeque<>();
    private List<PooledByteArrayWeakReference> list = new ArrayList<>(20000);
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
        byte[] array;

        if (pool.isEmpty())
        {
            tryToFindArrays();
        }

        if (pool.isEmpty())
        {
            array = new byte[arraySize];
        }
        else
        {
            array = pool.pop();
        }

        PooledByteArray pba = new PooledByteArray(array);
        list.add(new PooledByteArrayWeakReference(pba, array));
        return pba;
    }

    private void tryToFindArrays()
    {
        Iterator<PooledByteArrayWeakReference> i = list.iterator();
        while (i.hasNext())
        {
            PooledByteArrayWeakReference wr = i.next();
            if (wr.get()==null)
            {
                pool.push(wr.bytes);
                i.remove();
            }
        }
    }
}