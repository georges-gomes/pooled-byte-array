pooled-byte-array
=================

byte[] pool - Automatically recycled by Garbage Collector


Assumptions (for relatively big byte[])
--------------
- byte[] are expensive to allocate (zeroed)
- consume young gen unnecessary => increase frequency of minor GC
- if promoted => increase minor GC pause because of copy between gens

byte[] can be reused manually but in algorithms that don't control the full reference or life cycle of the byte[], it's nice to let the garbage collector do his job and find out when the byte[] is not used anymore.
Reusing byte[] manually, sometimes means thread-safety/lock issues or over-provisionning.

This experimental approach try to cover these problems.



Usage
-----

```java
// Instanciate the factory once
// (Here with array of 1024 bytes and 100 of them pre-allocated)
PooledByteArrayFactory factory = new PooledByteArrayFactory(1024, 100);
```

```java
// Anywhere in the code
// Instead of 
// byte[] buffer = new buffer[1024];
// you call the factory for a new byte buffer
PooledByteArray buffer = factory.getByteArray();
```

**You don't need to manualy recycle the PoolByteArray object back into the pool.
The wrapped byte[] in PooledByteArray will be recycled in the factory automatically
when the Garbage Colector will reclaim the PooledByteArray.**

You can then use the PooledByteArray for all sort of operation:

```java
public class PooledByteArray
{
    ...
    public int length()
    public byte getAt(int index)
    public void setAt(int index, byte b)
    public void copyTo(byte[] dst, int dstPos, int offset, int len)
    public void copyFrom(byte[] src, int srcPos, int offset, int len)
    public void writeTo(OutputStream out, int offset, int len) throws IOException
    public int readFrom(InputStream in, int offset, int len) throws IOException
    ...
}
```
To be enriched...



Results
-------

###Basic / standard implementation

```java
private void runBench()
{
    // Allocation
    byte[] pb = new byte[4096];

    // Basic serialization to buffer
    int len = serialize(object, pb);

    // Consume some young
    byte[] tmp = new byte[1024];
    tmp[0] = 'a';
}
```

Result for a loop of 100 000:

```
-XX:NewSize=64m -XX:MaxNewSize=64m -Xms1g -Xmx1g -XX:CompileThreshold=100 -XX:+PrintGCDetails

Warmup done

[GC [DefNew: 52480K->0K(59008K), 0.0004690 secs] 93291K->40812K(1042048K), 0.0004860 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0002950 secs] 93292K->40812K(1042048K), 0.0003090 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->4K(59008K), 0.0003010 secs] 93292K->40816K(1042048K), 0.0003150 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52484K->0K(59008K), 0.0002800 secs] 93296K->40812K(1042048K), 0.0002950 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0002820 secs] 93292K->40812K(1042048K), 0.0002960 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0003110 secs] 93292K->40812K(1042048K), 0.0003270 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0002830 secs] 93292K->40812K(1042048K), 0.0002960 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0002870 secs] 93292K->40812K(1042048K), 0.0003020 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->4K(59008K), 0.0002860 secs] 93292K->40816K(1042048K), 0.0002990 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52484K->0K(59008K), 0.0002770 secs] 93296K->40812K(1042048K), 0.0002890 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 

time = 99547us
```

###PooledByteArray with preallocated buffers (!)

```java
private void runBench()
{
    // Allocation
    PooledByteArray pb = factory.getByteArray();  // 4096 factory

    // Basic serialization to buffer
    int len = serialize(object, pb);

    // Consume some young
    byte[] tmp = new byte[1024];
    tmp[0] = 'a';
}
```

Result for a loop of 100 000:

```
-XX:NewSize=64m -XX:MaxNewSize=64m -Xms1g -Xmx1g -XX:CompileThreshold=100 -XX:+PrintGCDetails

Warm up done

[GC [DefNew: 52480K->6524K(59008K), 0.0191530 secs] 93331K->65566K(1042048K), 0.0191730 secs] [Times: user=0.01 sys=0.00, real=0.02 secs] 
[GC [DefNew: 59004K->6526K(59008K), 0.0191460 secs] 118046K->83820K(1042048K), 0.0191650 secs] [Times: user=0.02 sys=0.01, real=0.02 secs] 
[GC [DefNew: 59006K->6525K(59008K), 0.0136640 secs] 136300K->96609K(1042048K), 0.0136830 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
[GC [DefNew: 59005K->6527K(59008K), 0.0148630 secs] 149089K->105654K(1042048K), 0.0148930 secs] [Times: user=0.01 sys=0.01, real=0.01 secs]

time = 213258us
```

Less minor GC but they are MUCH bigger! finalize() cost way too much.

