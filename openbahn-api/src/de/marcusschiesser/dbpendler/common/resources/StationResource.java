package de.marcusschiesser.dbpendler.common.resources;

import de.marcusschiesser.dbpendler.common.vo.StationVO;

public interface StationResource {
	StationVO[] getList(String startsWith);
	StationVO get(String id);
}
