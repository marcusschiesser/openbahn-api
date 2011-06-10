package de.marcusschiesser.dbpendler.common.vo;

import java.io.Serializable;
import java.util.Date;

public class ConnectionVO implements Serializable {
	private static final long serialVersionUID = 8594894128306553971L;
	
	private StationVO start;
	private StationVO destination;
	private Date startTime;
	private Date destinationTime;
	private Date date;
	private String link;
	private Double price;

	public ConnectionVO() {}
	
	public ConnectionVO(StationVO start, StationVO destination, Date startTime,
			Date destinationTime) {
		this.start = start;
		this.destination = destination;
		this.startTime = startTime;
		this.destinationTime = destinationTime;
		this.date = null;
	}

	public ConnectionVO(StationVO start, StationVO destination, Date startTime,
			Date destinationTime, Date date, Double price, String link) {
		this.start = start;
		this.destination = destination;
		this.startTime = startTime;
		this.destinationTime = destinationTime;
		this.date = date;
		this.price = price;
		this.link = link.replace("&amp;", "&");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + ((destinationTime == null) ? 0 : destinationTime.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
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
		ConnectionVO other = (ConnectionVO) obj;
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
		if (destinationTime == null) {
			if (other.destinationTime != null)
				return false;
		} else if (!destinationTime.equals(other.destinationTime))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		return true;
	}

	public String getLink() {
		return link;
	}

	public Double getPrice() {
		return price;
	}

	public StationVO getStart() {
		return start;
	}

	public StationVO getDestination() {
		return destination;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Date getDestinationTime() {
		return destinationTime;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}
	
}
