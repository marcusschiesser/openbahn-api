package de.marcusschiesser.dbpendler.server.bahnwrapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.htmlparser.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.marcusschiesser.dbpendler.common.vo.CommitVO;
import de.marcusschiesser.dbpendler.common.vo.ConnectionVO;
import de.marcusschiesser.dbpendler.common.vo.StationVO;
import de.marcusschiesser.dbpendler.server.bahnwrapper.handler.CommitValidationHandler;
import de.marcusschiesser.dbpendler.server.bahnwrapper.handler.ConnectionValidationHandler;
import de.marcusschiesser.dbpendler.server.bahnwrapper.handler.HiddenInputFieldHandler;
import de.marcusschiesser.dbpendler.server.bahnwrapper.handler.PreCommitValidationHandler;
import de.marcusschiesser.dbpendler.server.utils.DateUtils;
import de.marcusschiesser.dbpendler.server.utils.ExceptionUtils;
import de.marcusschiesser.dbpendler.server.utils.HTTPSession;
import de.marcusschiesser.dbpendler.server.utils.HTTPUtils;

public class Booking {
	private static Booking instance = new Booking();

	private final Logger log = Logger.getLogger(Booking.class.getName());

	private URL reserveSeatURL;
	private URL bookingDataURL;
	private URL paymentTypeURL;
	private URL confirmBookingURL;
	private URL commitURL;

	private XMLReader reader;
	private HiddenInputFieldHandler sessionHandler;
	
	public enum BookingType { bookonly, bookandreserve, justreserve; };
	public enum BahncardType { none, bc25, bc50, bc100 };
	public enum ClassType { firstClass, secondClass };
	public enum ReservationType { fensterGrossraum, gangGrossraum, fensterAbteil, gangAbteil };
	public enum PaymentType { creditCard, directDebit };
	
	private Booking() {
		sessionHandler = new HiddenInputFieldHandler();
		reader = new org.htmlparser.sax.XMLReader();
		try {
			reserveSeatURL = new URL("https://fahrkarten.bahn.de/mobile/bu/hf.post");
			bookingDataURL = new URL("https://fahrkarten.bahn.de/mobile/bu/rw.post");
			paymentTypeURL = new URL("https://fahrkarten.bahn.de/mobile/bu/bd.post");
			confirmBookingURL = new URL("https://fahrkarten.bahn.de/mobile/bu/za.post");
			commitURL = new URL("https://fahrkarten.bahn.de/mobile/bu/ks.post");
		} catch (MalformedURLException e) {
			log.severe("error parsing URL: " + e.toString());
		}
	}
	
	public static Booking getInstance() {
		return instance ;
	}
	
	public CommitVO doBooking(HTTPSession session, StationVO start,
			StationVO destination, Date date, Date queryTime, final BookingType bookingtype, 
			final ReservationType reservationType) throws IOException, SAXException {
		return doBooking(session, start, destination, date, queryTime, bookingtype, reservationType, PaymentType.directDebit, "");
	}

	public PreCommitValidationHandler doPreBooking(HTTPSession session, StationVO start,
			StationVO destination, Date date, Date queryTime, final BookingType bookingtype, 
			final ReservationType reservationType, final PaymentType paymentType) throws IOException, SAXException {
		// TODO: set bahncard type
		// TODO: set klasse
		
		// Not needed: session.postMethod(new URL("https://mobile.bahn.de/bin/mobil/query.exe/dox?lang=de"), "button.auskunft_p=Ticket/Reservierung");
		List<ConnectionVO> connections = ConnectionParser.getInstance().getConnectionTime(session, start, destination, date, queryTime);

		if(connections.size()<1) {
			ExceptionUtils.throwError("Error parsing the connection."); 
		}
		ConnectionVO connection = connections.get(0);
		String timeLink = connection.getLink();
		log.info("Following this connection link: " + timeLink);
		String response = session.getMethod(new URL(timeLink));

		// parse ld & ident
		InputStream inputStream = HTTPUtils.stringToStream(response);
		reader.setContentHandler(sessionHandler);
		reader.parse(new InputSource(inputStream));
		
		String ident = sessionHandler.getValues().get("ident");
		String ld = sessionHandler.getValues().get("ld");
		String seqnr = sessionHandler.getValues().get("seqnr");
		
		String reserveSeatParam = "ident=" + ident +
		"&lang=de" +
		"&ld=" + ld + 
		"&oCID=C1-0" +
		"&sTID=C1-0.0%401" +
		"&seqnr=" + seqnr;
		
		if(bookingtype==BookingType.justreserve) {
			reserveSeatParam += "&order=Nur%20Reservierung&uc=2";
		} else {
			reserveSeatParam += "&order=Ticket%2FReservierung&uc=0";
		}
		// click on reserve ticket button
		response = session.postMethod(
				reserveSeatURL,
				// this is not needed: "hafasContinueUrl=https%253A%252F%252Fmobile.bahn.de%252Fbin%252Fmobil%252Fquery2.exe%252Fdox%253Fprotocol%253Dhttps%253A%2526ident%253Dfe.01302927.1305560218%2526seqnr%253D22%2526changeOutwardJourney%253Dyes%2526ld%253D9627&hdbh=e5bf994b256ea3efbafc69759e3cfaef&" + 
				reserveSeatParam
		);
		
		String reserveParam;
		switch(reservationType) {
		case fensterAbteil:
			reserveParam = "Platzlage=fenster&Wagenart=abteil";
			break;
		case fensterGrossraum:
			reserveParam = "Platzlage=fenster&Wagenart=grossraum";
			break;
		case gangAbteil:
			reserveParam = "Platzlage=gang&Wagenart=abteil";
			break;
		case gangGrossraum:
		default:
			reserveParam = "Platzlage=gang&Wagenart=grossraum";
			break;
		}
		
		String bookingDataParam = "lang=de&" +
		"AnzahlPersonen=1&" + reserveParam +
		"&button.weiter_p=Weiter&";
		switch(bookingtype) {
		case justreserve:
			bookingDataParam += "Klasse=2"; // oder klasse 1
		break;
		case bookandreserve:
			bookingDataParam += "sitzplatzres.reservierung=0"; 
		break;
		case bookonly:
			bookingDataParam += "sitzplatzres.reservierung=1"; 
		break;
		}
			
		response = session.postMethod(bookingDataURL, 
			bookingDataParam);
		inputStream = HTTPUtils.stringToStream(response);
		ConnectionValidationHandler handler = new ConnectionValidationHandler();
		reader.setContentHandler(handler);
		reader.parse(new InputSource(inputStream));
		// TODO: check also whether the time is correct
		if(!handler.isValid(connection)) {
			String msg = "The parser seems to have an error. The input connection does not fit to the parsed connection returned from the server. Please either report or fix this problem.";
			ExceptionUtils.throwError(msg);
			log.severe(msg);
		}

		// zahlungsart
		response = session.postMethod(paymentTypeURL, 
				//"mId=21299d_x&" +
				"lang=de&button.weiter_p=Weiter"
				);
		
		
		response = session.postMethod(confirmBookingURL, 
				"lang=de&" +
				"Zahlungsart=" + ((paymentType==PaymentType.creditCard) ? "kk" : "ls") +
				"&button.weiter_p=Weiter");
		
		inputStream = HTTPUtils.stringToStream(response);
		PreCommitValidationHandler preCommitHandler = new PreCommitValidationHandler(paymentType);
		reader.setContentHandler(preCommitHandler);
		reader.parse(new InputSource(inputStream));
		return preCommitHandler;
	}
	
	public CommitVO doBooking(HTTPSession session, StationVO start,
			StationVO destination, Date date, Date time, final BookingType bookingtype, 
			final ReservationType reservationType, final PaymentType paymentType,
				String creditCardCheckNumber) throws IOException, SAXException {
		Date actTime = new Date();
		Date bookTime = DateUtils.unifyDateTime(date, time);
		if(bookTime.before(actTime)) {
			String msg = "Can not book tickets for the past.";
			ExceptionUtils.throwError(msg);
		}
		PreCommitValidationHandler preCommitHandler = doPreBooking(session, start, destination, date, time, bookingtype, reservationType, paymentType);
		if(!preCommitHandler.isValid()) {
			String msg = "The parser seems to have an error. The pre commit response does not look valid. Please either report or fix this problem.";
			ExceptionUtils.throwError(msg);
			log.severe(msg);
		}
		String mobileNumber = preCommitHandler.getMobileNumber();
		String bahncardNumber = preCommitHandler.getBahncardNumber();
		
		String commitParam = "lang=de" +
		"&mobilnr=" + mobileNumber + 
		((paymentType==PaymentType.creditCard) ? "&kreditkartePruefziffer="+creditCardCheckNumber : "")+
		"&bahncard.nummerNummer=" + bahncardNumber;
		if(bookingtype==BookingType.justreserve) {
			commitParam += "&button.reservieren_p=Reservieren";
		} else {
			commitParam += "&button.ticketbuchen_p=Ticket buchen";
		}
		
		String response = session.postMethod(commitURL, commitParam);
		InputStream inputStream = HTTPUtils.stringToStream(response);
		CommitValidationHandler commitHandler = new CommitValidationHandler();
		reader.setContentHandler(commitHandler);
		reader.parse(new InputSource(inputStream));
		
		return commitHandler.getCommitData();
	}

}
