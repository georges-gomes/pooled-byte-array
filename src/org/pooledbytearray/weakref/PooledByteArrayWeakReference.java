package org.pooledbytearray.weakref;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;


public class PooledByteArrayWeakReference extends WeakReference<PooledByteArray>
{
    byte[] bytes;

    public PooledByteArrayWeakReference(PooledByteArray pba, byte[] bytes)
    {
        super(pba);
        this.bytes = bytes;
    }

    @Override
    public boolean equals(Object o)
    {
        return this == o;
    }
}
