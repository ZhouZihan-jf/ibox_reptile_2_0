package tools.bloom;

public class SimpleHash {
    private int cap;
    private int seed;

    // 默认构造器，哈希表长默认为DEFAULT_SIZE大小，此哈希函数的种子为seed
    public SimpleHash(int cap, int seed) {
        this.cap = cap;
        this.seed = seed;
    }

    public int hash(String value) {
        int result = 0;
        int len = value.length();

        for (int i = 0; i < len; i++) {
            //将此url用hash函数映射成一个值，使用到了集合中的每一个元素
            result = seed * result + value.charAt(i);
        }

        //产生单个信息指纹
        return (cap - 1) & result;
    }
}
