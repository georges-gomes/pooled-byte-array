package org.pooledbytearray;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class PooledByteArrayFactoryTest
{
    private int array_size;
    private int prealloc_size;
    private PooledByteArrayFactory pbaf = null;

    @Parameterized.Parameters
    public static Collection factorySettings() {
        return Arrays.asList(new Object[][]{
                {256, 100},
                {1024, 10},
                {1<<16, 0},
        });
    }

    public PooledByteArrayFactoryTest(int as, int pas)
    {
        super();
        array_size = as;
        prealloc_size = pas;
        pbaf = new PooledByteArrayFactory(array_size, prealloc_size);
    }

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void testSingleNewByteArray() throws Exception
    {
        PooledByteArray pba = pbaf.newByteArray();
        Assert.assertNotNull(pba);
        Assert.assertEquals(pba.length(), array_size);
    }

    @Test
    public void testMultipleNewByteArray() throws Exception
    {
        for(int i=0; i<(prealloc_size+1)*2; i++)
        {
            PooledByteArray pba = pbaf.newByteArray();
            Assert.assertNotNull(pba);
            Assert.assertEquals(pba.length(), array_size);
        }
    }

}
