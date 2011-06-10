package de.marcusschiesser.dbpendler.common.resources;


import de.marcusschiesser.dbpendler.common.vo.ConnectionVO;

public interface ConnectionResource {

	public ConnectionVO[] listDaily(String startId, String destinationId );
	public ConnectionVO[] listDay(String startId, String destinationId, String date );

}
