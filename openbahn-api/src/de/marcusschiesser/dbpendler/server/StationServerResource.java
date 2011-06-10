package de.marcusschiesser.dbpendler.server;

import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import de.marcusschiesser.dbpendler.common.resources.StationResource;
import de.marcusschiesser.dbpendler.common.vo.StationVO;
import de.marcusschiesser.dbpendler.server.storage.StationStorage;

/**
 * The server side implementation of the Jersey resource.
 */
@Path("/stations")
public class StationServerResource implements
        StationResource {

	public static final Logger log = Logger.getLogger(StationServerResource.class.getName());
	
	@GET
    @Path("/list")
    @Produces( {MediaType.APPLICATION_JSON } )
	public StationVO[] getList(@QueryParam("contains") String contains)  {
		List<StationVO> stations = StationStorage.getInstance().getStations(contains);
		return stations.toArray(new StationVO[0]);
	}
	
	@GET
    @Path("/id/{id}")
    @Produces( {MediaType.APPLICATION_JSON } )
	public StationVO get(@PathParam("id") String id) {
		// TODO Auto-generated method stub
		return null;
	}

}
