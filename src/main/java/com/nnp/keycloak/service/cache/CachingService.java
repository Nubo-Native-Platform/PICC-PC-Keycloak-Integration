package com.nnp.keycloak.service.cache;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CachingService {

	@Autowired
	CacheManager cacheManager;

	public void evictAllCaches() {
		cacheManager.getCacheNames()
		.parallelStream()
		.forEach(cacheName -> cacheManager.getCache(cacheName).clear());
	}

	@Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
	public void evictAllcachesAtIntervals() {
		evictAllCaches();
	}

	@CacheEvict(value = "accessToken", allEntries = true)
	public void evictAllCacheValues() {
	}

	public void evictAllCacheValues(String cacheName) {
		cacheManager.getCache(cacheName).clear();
	}

	@CacheEvict(value = "accessToken", key = "#cacheKey")
	public void evictSingleCacheValue(String cacheKey) {
	}

	public void evictSingleCacheValue(String cacheName, String cacheKey) {
		cacheManager.getCache(cacheName).evict(cacheKey);
	}

	public String getFromCache(String cacheName, String key) {
		String value = null;
		if (cacheManager.getCache(cacheName).get(key) != null) {
			value = cacheManager.getCache(cacheName).get(key).get().toString();
		}
		return value;
	}

	public void putToCache(String cacheName, String key, String value) {
		cacheManager.getCache(cacheName).put(key, value);
	}

}
