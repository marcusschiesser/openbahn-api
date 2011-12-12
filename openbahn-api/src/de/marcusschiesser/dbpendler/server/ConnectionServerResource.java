package de.marcusschiesser.dbpendler.server;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.google.appengine.repackaged.com.google.common.base.Function;
import com.google.appengine.repackaged.com.google.common.collect.Collections2;

import de.marcusschiesser.dbpendler.common.resources.ConnectionResource;
import de.marcusschiesser.dbpendler.common.vo.ConnectionVO;
import de.marcusschiesser.dbpendler.common.vo.StationVO;
import de.marcusschiesser.dbpendler.server.storage.ConnectionStorage;
import de.marcusschiesser.dbpendler.server.storage.StationStorage;
import de.marcusschiesser.dbpendler.server.utils.DateUtils;
import de.marcusschiesser.dbpendler.server.utils.ExceptionUtils;
import de.marcusschiesser.dbpendler.server.utils.LogUtils;

/**
 * The server side implementation of the Jersey resource.
 */
@Path("/connections")
public class ConnectionServerResource implements ConnectionResource {
	
	private final Logger log = Logger.getLogger(ConnectionServerResource.class.getName());

	@GET
	@Path("/list")
	@Produces("application/json; charset=UTF-8")
	public ConnectionVO[] listDay(@QueryParam("start") String s, @QueryParam("destination") String d, @QueryParam("date") String date ) {
		ExceptionUtils.checkRequiredParamters("start",s,"destination",d,"date",date);
		try {
			Date queryDate = DateUtils.getDateFormat().parse(date);
			StationVO start = StationStorage.getInstance().getStations(s).get(0);
			StationVO destination = StationStorage.getInstance().getStations(d).get(0);
			List<ConnectionVO> result = ConnectionStorage.getInstance().getConnection(start, destination, queryDate);
			return result.toArray(new ConnectionVO[result.size()]);
		} catch (ParseException e) {
			log.warning("Can not parse input date: '" + date + "'");
			ExceptionUtils.throwError(e);
			return null;
		}
	}
	
	@GET
	@Path("/daily")
	@Produces("application/json; charset=UTF-8")
	public ConnectionVO[] listDaily(@QueryParam("start") String s, @QueryParam("destination") String d ) {
			ExceptionUtils.checkRequiredParamters("start",s,"destination",d);
			List<StationVO> starts = StationStorage.getInstance().getStations(s);
			List<StationVO> destinations = StationStorage.getInstance().getStations(d);
			// TODO: this unique check does not work - idea: try to check the weight of the first station instead? 
//			if(starts.size()!=1 || destinations.size()!=1) {
//				ExceptionUtils.throwError("Either start or destination is not unique. Use StationService to get a unique station id.");
//			}
			StationVO start = starts.get(0);
			StationVO destination = destinations.get(0);
			// get all connections for this week (7 days)
			Date date = DateUtils.getThisMonday();
			Set<ConnectionVO> result = new LinkedHashSet<ConnectionVO>();
			for (int i = 0; i < 7; i++) {
				Collection<ConnectionVO> connections = ConnectionStorage.getInstance().getConnection(start, destination, date);
				// remove date from result
				connections = Collections2.transform(connections, new Function<ConnectionVO, ConnectionVO>() {
					@Override
					public ConnectionVO apply(ConnectionVO in) {
						in.setDate(null);
						return in;
					}
				});
				if(i==0)
					result.addAll(connections);
				else
					result.retainAll(connections);
				date = DateUtils.addDay(date, 1);
			}
			LogUtils.logConnections(result);
			return result.toArray(new ConnectionVO[result.size()]);
	}


	
}
