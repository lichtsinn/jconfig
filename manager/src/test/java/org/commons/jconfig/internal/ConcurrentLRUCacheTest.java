package org.commons.jconfig.internal;

import org.commons.jconfig.internal.ConcurrentLRUCache;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConcurrentLRUCacheTest {

    private static Boolean mTrue = true;
    private static Boolean mFalse = false;

    @Test
    public void testLRUCache(){
        ConcurrentLRUCache<String, Boolean> cache = new ConcurrentLRUCache<String, Boolean>(3);
        Boolean value = cache.get("true");
        Assert.assertNull(value);
        cache.put("true", mTrue);
        value = cache.get("true");
        Assert.assertEquals(value, mTrue);
        cache.put("true", mFalse);
        value = cache.get("true");
        Assert.assertNotSame(value, mTrue);
    }

    @Test
    public void testLRUCacheReSizeClone() {
        ConcurrentLRUCache<String, Boolean> cache3 = new ConcurrentLRUCache<String, Boolean>(3);

        cache3.put("value1", mTrue);
        cache3.put("value2", mFalse);
        cache3.put("value3", mFalse);

        ConcurrentLRUCache<String, Boolean> cache3new = new ConcurrentLRUCache<String, Boolean>(3, cache3);

        cache3new.put("value4", mFalse); // over size
        cache3new.put("value3", mFalse); // add same key
        cache3new.touch("value2");
        cache3new.put("value5", mFalse); // add new key

        // value2 is still on the cache since it was touched
        Assert.assertEquals(cache3new.get("value2"), mFalse);
        // value4 was the oldest value in the cache
        Assert.assertEquals(cache3new.get("value4"), null);

        Assert.assertEquals(cache3new.size(), 3);

        // Test clear cache
        cache3new.clear();

        Assert.assertEquals(cache3new.size(), 0);
    }

    @Test
    public void testLRUCacheReSizeReduce() {
        ConcurrentLRUCache<String, Boolean> cache3 = new ConcurrentLRUCache<String, Boolean>(3);

        cache3.put("value1", mTrue);
        cache3.put("value2", mFalse);
        cache3.put("value3", mFalse);

        ConcurrentLRUCache<String, Boolean> cache2 = new ConcurrentLRUCache<String, Boolean>(2, cache3);

        Assert.assertEquals(cache2.size(), 2);

        Assert.assertEquals(cache2.get("value1"), null);

        // Test clear cache
        cache2.clear();

        Assert.assertEquals(cache2.size(), 0);
    }

    @Test
    public void testLRUCacheReSizeIncrease() {
        ConcurrentLRUCache<String, Boolean> cache3 = new ConcurrentLRUCache<String, Boolean>(3);

        cache3.put("value1", mTrue);
        cache3.put("value2", mFalse);
        cache3.put("value3", mFalse);

        ConcurrentLRUCache<String, Boolean> cache5 = new ConcurrentLRUCache<String, Boolean>(5, cache3);

        cache5.put("value4", mFalse); // over size
        cache5.put("value3", mFalse); // add same key
        cache5.touch("value2");
        cache5.put("value5", mFalse); // add new key

        // value2 is still on the cache since it was touched
        Assert.assertEquals(cache5.get("value2"), mFalse);
        // value4 was the oldest value in the cache
        Assert.assertEquals(cache5.get("value4"), mFalse);

        Assert.assertEquals(cache5.size(), 5);

        // Test clear cache
        cache5.clear();

        Assert.assertEquals(cache5.size(), 0);
    }

    @Test
    public void testLRUCacheReSizeIncreaseSameCache() {
        ConcurrentLRUCache<String, Boolean> cache3 = new ConcurrentLRUCache<String, Boolean>(3);

        cache3.put("value1", mTrue);
        cache3.put("value2", mFalse);
        cache3.put("value3", mFalse);
        // value1 should be gone
        cache3.put("value4", mFalse);

        cache3.setMaxSize(5);

        cache3.put("value5", mFalse); // add new key
        cache3.put("value6", mFalse); // add new key
        cache3.put("value3", mFalse); // add same key
        cache3.touch("value2");
        cache3.put("value7", mFalse); // add new key

        // value2 is still on the cache since it was touched
        Assert.assertEquals(cache3.get("value2"), mFalse);
        // value5 was the oldest value in the cache
        Assert.assertEquals(cache3.get("value5"), mFalse);

        Assert.assertEquals(cache3.size(), 5);

        // Test clear cache
        cache3.clear();

        Assert.assertEquals(cache3.size(), 0);
    }

    @Test
    public void testLRUCacheSize() {
        ConcurrentLRUCache<String, Boolean> cache = new ConcurrentLRUCache<String, Boolean>(3);

        cache.put("value1", mTrue);
        cache.put("value2", mFalse);
        cache.put("value3", mFalse);
        cache.put("value4", mFalse); // over size
        cache.put("value3", mFalse); // add same key
        cache.touch("value2");
        cache.put("value5", mFalse); // add new key

        // value2 is still on the cache since it was touched
        Assert.assertEquals(cache.get("value2"), mFalse);
        // value4 was the oldest value in the cache
        Assert.assertEquals(cache.get("value4"), null);

        Assert.assertEquals(cache.size(), 3);

        // Test clear cache
        cache.clear();

        Assert.assertEquals(cache.size(), 0);
    }

    ConcurrentLRUCache<String, Boolean> mCache = new ConcurrentLRUCache<String, Boolean>(50);

    @Test(threadPoolSize = 30, invocationCount = 1000, invocationTimeOut = 10000)
    public void testLRUconcurency() {
        String tid  = "" + Thread.currentThread().getId();

        for (int i = 0; i <= 100; i++) {
            mCache.put(tid + "value" + i, mTrue);
            mCache.put("value" + i, mFalse);
        }

        for (int i = 0; i <= 100; i++) {
            Boolean value = mCache.get(tid + "value" + i);
            if (value != null) {
                Assert.assertEquals(value, mTrue);
            } else {
                int size = mCache.size();
                Assert.assertTrue(size >= 20, "size=" + size + ". size should always be large than 48.");
                Assert.assertTrue(size <= 80, "size=" + size + ". Size should always smaller the maxSize + maxthreads.");
            }
        }

        int size = mCache.size();
        Assert.assertTrue(size >= 20, "size=" + size + ". size should always be large than 48.");
        Assert.assertTrue(size <= 80, "size=" + size + ". Size should always smaller the maxSize + maxthreads.");
    }

    @Test
    public void testZ_LRUconcurency() {
        Assert.assertTrue(mCache.getStats().contains("HIT:"), "stats validation");
    }
}
