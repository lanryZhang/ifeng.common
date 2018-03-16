package com.ifeng.common.misc;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;



/**
 * 具有时间限制的Map，最后访问时间或生存期超过一定时间的元素自动删除。
 * 它是一个线程安全的Map，无需在外边再加同步
 * <p>
 * 如果需要监听Map的变化(即删除时通知)，可以利用commons collection的AbstractMapDecorator的机制：
 * <pre>
 * TimeLimitedMap map = new TimeLimitedMap(new AbstractMapDecorator(new HashMap()) {
 *     public Object remove(Object key) {
 *         // do something here
 *         return super.remove(key);
 *     }
 * }, ......);
 * </pre>
 * @author jinmy
 */
public class TimeLimitedMap implements Map, Serializable {
	private static Logger logger = Logger.getLogger(TimeLimitedMap.class);
	private static final long serialVersionUID = -7347776598951280571L;
	private Map innerMap;
    /**
     * key: same key of innerMap; value: timertask
     */
    private Map timerMap = new HashMap();
    private int timeLimit;
    private boolean fullLife;
    private static final Timer timer = TimerManager.getTimer("TimeLimitedMap Timer");
    private static Thread timerThread;

    /**
     * 缺省fullLife=true
     */
    public TimeLimitedMap(Map innerMap, int timeLimit) {
        this(innerMap, timeLimit, true);
    }

    /**
     * @param innerMap  要加上时间限制的Map
     * @param timeLimit  时间限制，单位毫秒
     * @param fullLife  时间限制是指put之后的整个生存期，还是最近访问(指get，不计values访问的)
     */
    public TimeLimitedMap(Map innerMap, int timeLimit, boolean fullLife) {
        this.innerMap = innerMap;
        this.timeLimit = timeLimit;
        this.fullLife = fullLife;
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public synchronized int size() {
        return this.innerMap.size();
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public synchronized void clear() {
        this.innerMap.clear();
        this.timerMap.clear();
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public synchronized boolean isEmpty() {
        return this.innerMap.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public synchronized boolean containsKey(Object key) {
        return this.innerMap.containsKey(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public synchronized boolean containsValue(Object value) {
        return this.innerMap.containsValue(value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    public synchronized Collection values() {
        return this.innerMap.values();
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    public synchronized void putAll(Map t) {
        // 不直接delegate，而是调用put
        for (Iterator it = t.entrySet().iterator(); it.hasNext(); ) {
            Entry entry = (Entry)it.next();
            put(entry.getKey(), entry.getValue());
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public synchronized Set entrySet() {
        return this.innerMap.entrySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public synchronized Set keySet() {
        return this.innerMap.keySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public synchronized Object get(Object key) {
        if (!this.fullLife) {
            refreshTime(key);
        }
        return this.innerMap.get(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public synchronized Object remove(Object key) {
        TimerTask oldTask = (TimerTask)this.timerMap.remove(key);
        if (oldTask != null) {
            oldTask.cancel();
        }
        return this.innerMap.remove(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public synchronized Object put(Object key, Object value) {
        Object result = this.innerMap.put(key, value);
        refreshTime0(key);
        return result;
    }

    private void refreshTime0(final Object key) {
        TimerTask oldTask = (TimerTask)this.timerMap.get(key);
        if (oldTask != null) {
            oldTask.cancel();
        }
        TimerTask task = new TimerTask() {
            public void run() {
                try {
                    timerThread = Thread.currentThread();
                    removeOnTimeout(key);
                } catch (Throwable e) {
                	logger.error("Error in TimeLimitedMap timer", e);
                }
            }
        };
        this.timerMap.put(key, task);
        timer.schedule(task, this.timeLimit);
    }

    synchronized void removeOnTimeout(Object key) {
    	TimerTask oldTask = (TimerTask)this.timerMap.remove(key);
        if (oldTask != null) {
            if(oldTask.cancel()) {
            	logger.warn("removeOnTimeout key=" + key+" cancel another task.");
 
            }
        }
        //this.timerMap.remove(key);
        if (this.innerMap.containsKey(key)) {
            this.innerMap.remove(key);
        }
    }

    /**
     * 刷新key对应的时间
     */
    public synchronized void refreshTime(Object key) {
        if (this.innerMap.containsKey(key)) {
            refreshTime0(key);
        }
    }

    public synchronized boolean equals(Object obj) {
        return this.innerMap.equals(obj);
    }

    public synchronized int hashCode() {
        return this.innerMap.hashCode();
    }

    public synchronized String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append('{');
        for (Iterator it = this.innerMap.entrySet().iterator(); it.hasNext(); ) {
            Entry e = (Entry)it.next();
            buf.append(this.timerMap.get(e.getKey()) + ":" + e.getKey() + '='
                    + e.getValue() + ' ');
        }
        buf.append('}');
        return buf.toString();
    }

    /**
     * 供使用者判断innerMap的remove是否来自timer
     */
    public static boolean isInTimer() {
        return Thread.currentThread() == timerThread;
    }

}
