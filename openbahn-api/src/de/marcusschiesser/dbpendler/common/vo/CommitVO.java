package de.marcusschiesser.dbpendler.common.vo;

import java.io.Serializable;
import java.util.Date;

public class CommitVO implements Serializable {
	
	private static final long serialVersionUID = -6372396313067748985L;

	private StationVO start;
	private StationVO destination;
	private Date date;
	private Date time;
	private String reservation;
	private String orderNumber;

	public CommitVO() {}
	
	public CommitVO(StationVO start, StationVO destination, Date date,
			Date time, String reservation, String orderNumber) {
		this.start = start;
		this.destination = destination;
		this.date = date;
		this.time = time;
		this.reservation = reservation;
		this.orderNumber = orderNumber;
	}

	public StationVO getStart() {
		return start;
	}

	public StationVO getDestination() {
		return destination;
	}

	public Date getDate() {
		return date;
	}

	public Date getTime() {
		return time;
	}

	public String getReservation() {
		return reservation;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

}
