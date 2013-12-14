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

With options: 
```
-XX:NewSize=64m -XX:MaxNewSize=64m -Xms1g -Xmx1g -XX:CompileThreshold=100 -XX:+PrintGCDetails
```

###Basic new byte[] implementation

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


Loop 100 000 - Buffer 4096:

```
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

Loop 100 000 - Buffer 32768:

```
Warm up done

[GC [DefNew: 52480K->0K(59008K), 0.0019210 secs] 373289K->320809K(1042048K), 0.0019420 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0016740 secs] 373289K->320809K(1042048K), 0.0016910 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0014300 secs] 373289K->320809K(1042048K), 0.0014430 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0014040 secs] 373289K->320809K(1042048K), 0.0014180 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0013900 secs] 373289K->320809K(1042048K), 0.0014020 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
...
40 lines removed
...
[GC [DefNew: 52480K->0K(59008K), 0.0014070 secs] 373289K->320809K(1042048K), 0.0014210 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0014530 secs] 373289K->320809K(1042048K), 0.0014690 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0019650 secs] 373289K->320809K(1042048K), 0.0020040 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0014320 secs] 373289K->320809K(1042048K), 0.0014490 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0016540 secs] 373289K->320809K(1042048K), 0.0016780 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0014480 secs] 373289K->320809K(1042048K), 0.0014630 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0014440 secs] 373289K->320809K(1042048K), 0.0014610 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0015030 secs] 373289K->320809K(1042048K), 0.0015280 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0014530 secs] 373289K->320809K(1042048K), 0.0014720 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0014560 secs] 373289K->320809K(1042048K), 0.0014760 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0017740 secs] 373289K->320809K(1042048K), 0.0018010 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0014600 secs] 373289K->320809K(1042048K), 0.0014850 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0014440 secs] 373289K->320809K(1042048K), 0.0014610 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0017920 secs] 373289K->320809K(1042048K), 0.0018180 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0014150 secs] 373289K->320809K(1042048K), 0.0014300 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0014930 secs] 373289K->320809K(1042048K), 0.0015130 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0014610 secs] 373289K->320809K(1042048K), 0.0014780 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0014160 secs] 373289K->320809K(1042048K), 0.0014300 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC [DefNew: 52480K->0K(59008K), 0.0014120 secs] 373289K->320809K(1042048K), 0.0014240 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 

time = 365047us
```

Loop 100 000 - Buffer 65536:

```
...
time = 1038956us
```


###PooledByteArray with Finalizer implementation (10000 prealloc)

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

Loop 100 000 - Buffer 4096:

```
Warm up done

[GC [DefNew: 52480K->6524K(59008K), 0.0191530 secs] 93331K->65566K(1042048K), 0.0191730 secs] [Times: user=0.01 sys=0.00, real=0.02 secs] 
[GC [DefNew: 59004K->6526K(59008K), 0.0191460 secs] 118046K->83820K(1042048K), 0.0191650 secs] [Times: user=0.02 sys=0.01, real=0.02 secs] 
[GC [DefNew: 59006K->6525K(59008K), 0.0136640 secs] 136300K->96609K(1042048K), 0.0136830 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
[GC [DefNew: 59005K->6527K(59008K), 0.0148630 secs] 149089K->105654K(1042048K), 0.0148930 secs] [Times: user=0.01 sys=0.01, real=0.01 secs]

time = 213258us
```



Loop 100 000 - Buffer 32768:

```
Warm up done

[GC [DefNew: 52480K->6517K(59008K), 0.0223240 secs] 373329K->354931K(1042048K), 0.0223440 secs] [Times: user=0.02 sys=0.01, real=0.02 secs] 
[GC [DefNew: 58978K->6514K(59008K), 0.0224960 secs] 407393K->386689K(1042048K), 0.0225220 secs] [Times: user=0.02 sys=0.01, real=0.02 secs] 
[GC [DefNew: 58994K->6509K(59008K), 0.0207540 secs] 439169K->416746K(1042048K), 0.0207760 secs] [Times: user=0.01 sys=0.01, real=0.02 secs] 
[GC [DefNew: 58989K->6501K(59008K), 0.0197010 secs] 469226K->445488K(1042048K), 0.0197210 secs] [Times: user=0.02 sys=0.01, real=0.02 secs] 
[GC [DefNew: 58981K->6522K(59008K), 0.0199160 secs] 497968K->472818K(1042048K), 0.0199380 secs] [Times: user=0.02 sys=0.00, real=0.02 secs] 
[GC [DefNew: 59002K->6509K(59008K), 0.0195460 secs] 525298K->498802K(1042048K), 0.0195660 secs] [Times: user=0.01 sys=0.01, real=0.02 secs] 
[GC [DefNew: 58989K->6525K(59008K), 0.0183120 secs] 551282K->523534K(1042048K), 0.0183310 secs] [Times: user=0.02 sys=0.01, real=0.02 secs] 

time = 312211us
```

Loop 100 000 - Buffer 65536:

```
Warm up done

[GC [DefNew: 52432K->6483K(59008K), 0.0202150 secs] 693261K->675608K(1042048K), 0.0202370 secs] [Times: user=0.02 sys=0.01, real=0.02 secs] 
[GC [DefNew: 58932K->6516K(59008K), 0.0225640 secs] 728057K->709057K(1042048K), 0.0225850 secs] [Times: user=0.02 sys=0.01, real=0.03 secs] 
[GC [DefNew: 58972K->6484K(59008K), 0.0223650 secs] 761513K->741801K(1042048K), 0.0223880 secs] [Times: user=0.02 sys=0.01, real=0.02 secs] 
[GC [DefNew: 58911K->6515K(59008K), 0.0208900 secs] 794228K->773648K(1042048K), 0.0209100 secs] [Times: user=0.02 sys=0.00, real=0.02 secs] 
[GC [DefNew: 58958K->6481K(59008K), 0.0208940 secs] 826091K->804725K(1042048K), 0.0209130 secs] [Times: user=0.02 sys=0.01, real=0.02 secs] 
[GC [DefNew: 58961K->6511K(59008K), 0.0237490 secs] 857205K->834970K(1042048K), 0.0237700 secs] [Times: user=0.01 sys=0.01, real=0.03 secs] 
[GC [DefNew: 58991K->6475K(59008K), 0.0227720 secs] 887450K->864382K(1042048K), 0.0227930 secs] [Times: user=0.01 sys=0.01, real=0.02 secs] 
[GC [DefNew: 58955K->6503K(59008K), 0.0219240 secs] 916862K->893153K(1042048K), 0.0219470 secs] [Times: user=0.02 sys=0.01, real=0.02 secs] 

time = 338505us
```


