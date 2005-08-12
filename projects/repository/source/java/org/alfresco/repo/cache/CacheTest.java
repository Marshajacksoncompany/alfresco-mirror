/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.cache;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import net.sf.ehcache.CacheManager;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

/**
 * @see org.alfresco.repo.cache.EhCacheAdapter
 * 
 * @author Derek Hulley
 */
public class CacheTest extends TestCase
{
    private static ApplicationContext ctx =new ClassPathXmlApplicationContext(
            new String[] {"classpath:cache-test-context.xml", ApplicationContextHelper.CONFIG_LOCATIONS[0]}
            );
    
    private ServiceRegistry serviceRegistry;
    private SimpleCache standaloneCache;
    private SimpleCache backingCache;
    private SimpleCache transactionalCache;
    
    @Override
    public void setUp() throws Exception
    {
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        standaloneCache = (SimpleCache) ctx.getBean("ehCache1");
        backingCache = (SimpleCache) ctx.getBean("backingCache");
        transactionalCache = (SimpleCache) ctx.getBean("transactionalCache");
    }
    
    public void testSetUp() throws Exception
    {
        CacheManager cacheManager = (CacheManager) ctx.getBean("ehCacheManager");
        assertNotNull(cacheManager);
        CacheManager cacheManagerCheck = (CacheManager) ctx.getBean("ehCacheManager");
        assertTrue(cacheManager == cacheManagerCheck);
    
        assertNotNull(serviceRegistry);
        assertNotNull(backingCache);
        assertNotNull(standaloneCache);
        assertNotNull(transactionalCache);
    }
    
    public void testEhcacheAdaptors() throws Exception
    {
        backingCache.put("A", "AAA");
        assertNull("Second cache should not have first's present", standaloneCache.get("A"));
        
        assertEquals("AAA", backingCache.get("A"));
        
        backingCache.remove("A");
        assertNull(backingCache.get("A"));
    }
    
    public void testTransactionalCacheNoTxn() throws Exception
    {
        String key = "B";
        String value = "BBB";
        // no transaction - do a put
        transactionalCache.put(key, value);
        // check that the value appears in the backing cache, backingCache
        assertEquals("Backing cache not used for put when no transaction present", value, backingCache.get(key));
        
        // remove the value from the backing cache and check that it is removed from the transaction cache
        backingCache.remove(key);
        assertNull("Backing cache not used for removed when no transaction present", transactionalCache.get(key));
        
        // add value into backing cache
        backingCache.put(key, value);
        // remove it from the transactional cache
        transactionalCache.remove(key);
        // check that it is gone from the backing cache
        assertNull("Non-transactional remove didn't go to backing cache", backingCache.get(key));
    }
    
    public void testTransactionalCacheWithTxn() throws Throwable
    {
        String newGlobalOne = "new_global_one";
        String newGlobalTwo = "new_global_two";
        String newGlobalThree = "new_global_three";
        String updatedTxnThree = "updated_txn_three";
        
        // add item to global cache
        backingCache.put(newGlobalOne, newGlobalOne);
        backingCache.put(newGlobalTwo, newGlobalTwo);
        backingCache.put(newGlobalThree, newGlobalThree);
        
        UserTransaction txn = serviceRegistry.getUserTransaction();
        // begin a transaction
        txn.begin();
        
        try
        {
            // remove 1 from the cache
            transactionalCache.remove(newGlobalOne);
            assertFalse("Item was not removed from txn cache", transactionalCache.contains(newGlobalOne));
            assertTrue("Item was removed from backing cache", backingCache.contains(newGlobalOne));
            
            // update 3 in the cache
            transactionalCache.put(updatedTxnThree, "XXX");
            assertEquals("Item not updated in txn cache", "XXX", transactionalCache.get(updatedTxnThree));
            assertFalse("Item was put into backing cache", backingCache.contains(updatedTxnThree));
            
            // commit the transaction
            txn.commit();
            
            // check that backing cache was updated with the in-transaction changes
            assertFalse("Item was not removed from backing cache", backingCache.contains(newGlobalOne));
            assertEquals("Item not updated in backing cache", "XXX", backingCache.get(updatedTxnThree));
        }
        catch (Throwable e)
        {
            if (txn.getStatus() == Status.STATUS_ACTIVE)
            {
                txn.rollback();
            }
            throw e;
        }
    }
    
    /**
     * Preloads the cache, then performs a simultaneous addition of N new values and
     * removal of the N preloaded values.
     * 
     * @param cache
     * @param objectCount
     * @return Returns the time it took in <b>nanoseconds</b>.
     */
    public long runPerformanceTestOnCache(SimpleCache cache, int objectCount)
    {
        // preload
        for (int i = 0; i < objectCount; i++)
        {
            String key = Integer.toString(i);
            Integer value = new Integer(i);
            cache.put(key, value);
        }
        
        // start timer
        long start = System.nanoTime();
        for (int i = 0; i < objectCount; i++)
        {
            String key = Integer.toString(i);
            cache.remove(key);
            // add a new value
            key = Integer.toString(i + objectCount);
            Integer value = new Integer(i + objectCount);
            cache.put(key, value);
        }
        // stop
        long stop = System.nanoTime();
        
        return (stop - start);
    }
    
    /**
     * Tests a straight Ehcache adapter against a transactional cache both in and out
     * of a transaction.  This is done repeatedly, pushing the count up.
     */
    public void testPerformance() throws Exception
    {
        for (int i = 0; i < 5; i++)
        {
            int count = (int) Math.pow(10D, (double)i);
            // clear caches
            standaloneCache.clear();
            backingCache.clear();
            transactionalCache.clear();
            
            // test standalone
            long timePlain = runPerformanceTestOnCache(standaloneCache, count);
            
            // test transactional outside of transaction
            long timeNoTxn = runPerformanceTestOnCache(transactionalCache, count);
            
            // clear the transactional cache and backing cache
            backingCache.clear();
            transactionalCache.clear();
            // do transactional cache in a transaction
            UserTransaction txn = serviceRegistry.getUserTransaction();
            txn.begin();
            long timeTxn = runPerformanceTestOnCache(transactionalCache, count);
            long commitStart = System.nanoTime();
            txn.commit();
            long commitEnd = System.nanoTime();
            long commitTime = (commitEnd - commitStart);
            // add this to the cache's performance overhead
            timeTxn += commitTime;
            
            // report
            System.out.printf("Cache performance test: \n" +
                    "   count: " + count + "\n" +
                    "   direct: " + timePlain/((long)count) + " ns\\count \n" + 
                    "   no transaction: " + timeNoTxn/((long)count) + " ns\\count \n" + 
                    "   transaction: " + timeTxn/((long)count) + " ns\\count \n"); 
        }
    }
}
