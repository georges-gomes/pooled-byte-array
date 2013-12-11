package org.pooledbytearray;

import org.pooledbytearray.finalizer.PooledByteArray;
import org.pooledbytearray.finalizer.PooledByteArrayFactory;

import java.util.HashMap;
import java.util.Map;


public class Bench extends Thread
{
    final static int BUFFERSIZE = 64*1024;

    public static void main (String[] args)
    {
        Bench bench = new Bench(BUFFERSIZE);
        bench.start();

        try
        {
            bench.join();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }



    private Map<String,String> object;
    private PooledByteArrayFactory factory;

    public Bench(int allocationSize)
    {
        object = new HashMap<>();
        object.put("field1", "titi");
        object.put("field2", "tutu");
        object.put("field3", "toto");
        object.put("field4", "tata");
        object.put("field5", "tete");

        factory = new PooledByteArrayFactory(BUFFERSIZE, 10000);
    }

    public void run()
    {
        try
        {
            // Warmup
            for(int i=0; i<1000; i++)
            {
                runBench();
            }
            System.out.println("Warm up done");

            System.gc();

            // Pause 1s
            Thread.currentThread().sleep(1000);

            // Bench
            long start = System.nanoTime();
            for(int i=0; i<100000; i++)
            {
                runBench();
            }
            long end = System.nanoTime();
            System.out.println("time = " + ((end-start)/1000));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void runBench()
    {
        PooledByteArray pb = factory.getByteArray();
        //byte[] pb = new byte[BUFFERSIZE];

        int len = serialize(object, pb);

        // Consume young
        byte[] tmp = new byte[1024];
        tmp[0] = 'a';
    }

    private int serialize(Map<String, String> object, byte[] b)
    {
        int pos = 0;

        for(Map.Entry<String, String> entry : object.entrySet())
        {
            byte[] key = entry.getKey().getBytes();
            byte[] value = entry.getValue().getBytes();

            System.arraycopy(key, 0, b, pos, key.length);
            pos += key.length;

            b[pos++] = '=';
            b[pos++] = '=';

            System.arraycopy(value, 0, b, pos, value.length);
            pos += value.length;

            b[pos++] = '<';
            b[pos++] = '>';
        }

        return pos;
    }

    private int serialize(Map<String, String> object, PooledByteArray b)
    {
        int pos = 0;

        for(Map.Entry<String, String> entry : object.entrySet())
        {
            byte[] key = entry.getKey().getBytes();
            byte[] value = entry.getValue().getBytes();

            b.copyFrom(key, 0, pos, key.length);
            pos += key.length;

            b.setAt(pos++, (byte)'=');
            b.setAt(pos++, (byte)'=');

            b.copyFrom(value, 0, pos, value.length);
            pos += value.length;

            b.setAt(pos++, (byte)'<');
            b.setAt(pos++, (byte)'>');
        }

        return pos;
    }

}
