package de.marcusschiesser.dbpendler.server.storage;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;

import com.google.appengine.api.memcache.stdimpl.GCacheFactory;

import de.marcusschiesser.dbpendler.common.vo.ConnectionVO;
import de.marcusschiesser.dbpendler.common.vo.StationVO;
import de.marcusschiesser.dbpendler.server.bahnwrapper.ConnectionParser;

public class ConnectionStorage {
	private static ConnectionStorage instance = new ConnectionStorage();
	private ConnectionParser parser;
	private Map<ConnectionKey, List<ConnectionVO>> cache;

	public static final Logger log = Logger.getLogger(ConnectionStorage.class.getName());
	
	@SuppressWarnings("unchecked")
	private ConnectionStorage() {
		parser = ConnectionParser.getInstance();

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

	public static ConnectionStorage getInstance() {
		return instance;
	}

	public List<ConnectionVO> getConnection(StationVO start, StationVO destination, Date date) {
		ConnectionKey key = new ConnectionKey(start, destination, date);
		if (!cache.containsKey(key)) {
			log.info("Cache miss for: " + key.toString());
			List<ConnectionVO> connections = parser.getConnection(start, destination, date);
			cache.put(key, connections);
		}
		return cache.get(key);
	}

}
