package tools.bloom;

import java.util.BitSet;

public class SimpleBloomFilter {
    // 设置布隆过滤器的大小
    private static final int DEFAULT_SIZE = 2 << 24;
    // 产生随机数的种子，可产生6个不同的随机数产生器
    private static final int[] seeds = new int[]{7, 11, 13, 31, 37, 61};
    // Java中的按位存储的思想，其算法的具体实现（布隆过滤器）
    private BitSet bits = new BitSet(DEFAULT_SIZE);
    // 根据随机数的种子，创建6个哈希函数
    private SimpleHash[] func = new SimpleHash[seeds.length];

    // 设置布隆过滤器所对应k（6）个哈希函数
    public SimpleBloomFilter() {
        for (int i = 0; i < seeds.length; i++) {
            func[i] = new SimpleHash(DEFAULT_SIZE, seeds[i]);
        }
    }

    // 是否已经包含该URL
    public boolean contains(String value) {
        if (value == null) {
            return false;
        }

        boolean ret = true;
        // 根据此URL得到在布隆过滤器中的对应位，并判断其标志位（6个不同的哈希函数产生6种不同的映射）
        for (SimpleHash f : func) {
            ret = ret && bits.get(f.hash(value));
        }

        return ret;
    }

    //add
    public void addValue(String value) {
        //将字符串value哈希为8个或多个整数，然后在这些整数的bit上变为1
        if (value != null) {
            for (SimpleHash f : func)
                bits.set(f.hash(value), true);
        }

    }
}
