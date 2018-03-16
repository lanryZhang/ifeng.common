package com.ifeng.common.misc;

import java.net.InetAddress;
import java.util.Random;

/**
 * 生成一个唯一ID(重复的可能性极小)。
 * 可以有长格式和短格式两种
 * 长格式类似于UUID算法，可以保证全局(全球)的唯一性。
 * 短格式一般用于本机内的ID
 * @author jinmy
 */
public class IDGenerator {
    // class被调入的时间，一般来说对于不同的JVM都是不同的
    private String prefix;
    private static final String _zeros = "00000000";
    private static final Random _random = new Random();
    private static final int _loadTime = (int)System.currentTimeMillis() << 1;
    private int count;
    private static int ipHash;
    static {
        try {
            ipHash = InetAddress.getLocalHost().hashCode();
        } catch (Exception e) {
            ipHash = _random.nextInt();
        }
    }
    
    /**
     * @param prefix  一个前缀，所有生成的ID都包含这个前缀
     * @param longFormat  是否为长格式
     */
    public IDGenerator(String prefix, boolean longFormat) {
        int t = (int)System.currentTimeMillis() ^ _loadTime;
        if (longFormat) {
            this.prefix = prefix + toHexString(t) + toHexString(ipHash);
        } else {
            this.prefix = prefix;
        }
        this.count = _random.nextInt();
    }

    private static String toHexString(int i) {
        // 防止负数
        String s = Long.toHexString(i & 0xffffffffL);
        return _zeros.substring(0, 8 - s.length()) + s;
    }
    
    public synchronized String nextId() {
        return this.prefix
                + toHexString(_random.nextInt()) + toHexString(count++); 
    }
    
}
