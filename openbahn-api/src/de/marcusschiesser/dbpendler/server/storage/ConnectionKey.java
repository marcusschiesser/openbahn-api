package de.marcusschiesser.dbpendler.server.storage;

import java.io.Serializable;
import java.util.Date;

import de.marcusschiesser.dbpendler.common.vo.StationVO;

class ConnectionKey implements Serializable{
	private static final long serialVersionUID = 8596364161024129560L;
	private StationVO start;
	private StationVO destination;
	private Date date;
	
	public ConnectionKey(StationVO start, StationVO destination, Date date) {
		super();
		this.start = start;
		this.destination = destination;
		this.date = date;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConnectionKey other = (ConnectionKey) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (destination == null) {
			if (other.destination != null)
				return false;
		} else if (!destination.equals(other.destination))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ConnectionKey [start=" + start + ", destination=" + destination + ", date=" + date + "]";
	}
	
}