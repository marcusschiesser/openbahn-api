package de.marcusschiesser.dbpendler.server.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;

import com.google.appengine.api.memcache.stdimpl.GCacheFactory;

import de.marcusschiesser.dbpendler.common.vo.StationVO;
import de.marcusschiesser.dbpendler.server.bahnwrapper.StationParser;

public class StationStorage {
	private static StationStorage instance = new StationStorage();
	private StationParser parser;
	private Map<String, List<StationVO>> cache;

	public static final Logger log = Logger.getLogger(StationStorage.class.getName());

	@SuppressWarnings("unchecked")
	private StationStorage() {
		parser = StationParser.getInstance();

		try {
			@SuppressWarnings("rawtypes")
			Map props = new HashMap();
			props.put(GCacheFactory.EXPIRATION_DELTA, 3600*24); // cache expires in one day
			CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
			cache = cacheFactory.createCache(props);
		} catch (CacheException e) {
			log.severe("Error initialising station storage. Cache is not used!: " + e.toString());
		}
	}

	public static StationStorage getInstance() {
		return instance;
	}

	public List<StationVO> getStations(String contains) {
		if (!cache.containsKey(contains)) {
			log.info("Cache miss for: " + contains);
			List<StationVO> stations = parser.getStations(contains);
			cache.put(contains, stations);
		}
		return cache.get(contains);
	}
	
	public boolean isUnique(String stationName) {
		List<StationVO> stations = getStations(stationName);
		return stations.size()==1;
	}

}
