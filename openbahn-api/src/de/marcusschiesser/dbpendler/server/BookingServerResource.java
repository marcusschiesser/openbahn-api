package de.marcusschiesser.dbpendler.server;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.xml.sax.SAXException;

import de.marcusschiesser.dbpendler.common.resources.BookingResource;
import de.marcusschiesser.dbpendler.common.vo.CommitVO;
import de.marcusschiesser.dbpendler.common.vo.StationVO;
import de.marcusschiesser.dbpendler.server.bahnwrapper.Booking;
import de.marcusschiesser.dbpendler.server.bahnwrapper.Booking.BookingType;
import de.marcusschiesser.dbpendler.server.bahnwrapper.Booking.PaymentType;
import de.marcusschiesser.dbpendler.server.bahnwrapper.Booking.ReservationType;
import de.marcusschiesser.dbpendler.server.bahnwrapper.Login;
import de.marcusschiesser.dbpendler.server.storage.StationStorage;
import de.marcusschiesser.dbpendler.server.utils.DateUtils;
import de.marcusschiesser.dbpendler.server.utils.ExceptionUtils;
import de.marcusschiesser.dbpendler.server.utils.HTTPSession;

/**
 * The server side implementation of the Jersey resource.
 */
@Path("/book")
public class BookingServerResource implements BookingResource {
	
//	private final Logger log = Logger.getLogger(BookingServerResource.class.getName());

	@GET
	@Produces("application/json; charset=UTF-8")
	public CommitVO book(@QueryParam("login") String login, @QueryParam("pin") String pin, 
			@QueryParam("start") String s, @QueryParam("destination") String d,
			@QueryParam("date") String date, @QueryParam("time") String time,
			@QueryParam("bookType") BookingType bookingType,
			@QueryParam("reservationType") ReservationType reservationType,
			@QueryParam("paymentType") PaymentType paymentType,
			@QueryParam("creditCardCheckNumber") String creditCardCheckNumber
			) {
		// set default params
		if(bookingType==null) {
			bookingType = BookingType.bookandreserve;
		}
		if(reservationType==null) {
			reservationType = ReservationType.gangGrossraum;
		}
		if(paymentType==null) {
			paymentType = PaymentType.directDebit;
		}
		// check params
		if(paymentType==PaymentType.creditCard && creditCardCheckNumber==null) {
			ExceptionUtils.throwError("Credit card check number must be set for payment type 'Credit card'");
		}
		ExceptionUtils.checkRequiredParamters("login",login,"pin",pin,"date",date,"time",time,"start",s,"destination",d);
		List<StationVO> starts = StationStorage.getInstance().getStations(s);
		List<StationVO> destinations = StationStorage.getInstance().getStations(d);
		// this does not work (see ConnectionServerResource)
//		if(starts.size()!=1 || destinations.size()!=1) {
//			ExceptionUtils.throwError("Either start or destination is not unique. Use StationService to get a unique station id.");
//		}
		StationVO start = starts.get(0);
		StationVO destination = destinations.get(0);
		// do transaction
		try {
			HTTPSession session = Login.getInstance().login(login, pin);
			Date bookDate = DateUtils.getDateFormat().parse(date);
			Date bookTime = DateUtils.getTimeFormat().parse(time);
			CommitVO commitData = Booking.getInstance().doBooking(session, start, destination, 
					bookDate, bookTime, bookingType, reservationType, paymentType,
					creditCardCheckNumber); 
			Login.getInstance().logout(session);
			return commitData;
		} catch (ParseException e) {
			ExceptionUtils.throwError(e);
			return null;
		} catch (IOException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		} catch (SAXException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		}
	}
	
}
