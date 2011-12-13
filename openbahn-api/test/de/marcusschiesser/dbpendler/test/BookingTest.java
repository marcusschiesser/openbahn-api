package de.marcusschiesser.dbpendler.test;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import de.marcusschiesser.dbpendler.common.vo.StationVO;
import de.marcusschiesser.dbpendler.server.bahnwrapper.Booking;
import de.marcusschiesser.dbpendler.server.bahnwrapper.Booking.BookingType;
import de.marcusschiesser.dbpendler.server.bahnwrapper.Booking.PaymentType;
import de.marcusschiesser.dbpendler.server.bahnwrapper.Booking.ReservationType;
import de.marcusschiesser.dbpendler.server.bahnwrapper.Login;
import de.marcusschiesser.dbpendler.server.bahnwrapper.handler.PreCommitValidationHandler;
import de.marcusschiesser.dbpendler.server.utils.DateUtils;
import de.marcusschiesser.dbpendler.server.utils.HTTPSession;

public class BookingTest {

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	@Before
	public void setUp() {
		helper.setUp();
	}

	@After
	public void tearDown() {
		helper.tearDown();
	}
	
	@Test
	public void testReservation() throws SAXException, IOException, ParseException {
		double price = doPreBooking(BookingType.justreserve);
		Assert.assertEquals("just a reservation costs only 2.50", 2.5d, price, 0);
	}
	
	@Test
	public void testBookingAndReserve() throws SAXException, IOException, ParseException {
		double price = doPreBooking(BookingType.bookandreserve);
		Assert.assertEquals("just a reservation costs 39.50", 39.50d, price, 0);
	}

	@Test
	public void testBooking() throws SAXException, IOException, ParseException {
		double price = doPreBooking(BookingType.bookonly);
		Assert.assertEquals("booking costs 37.00", 37.00d, price, 0);
	}

	private double doPreBooking(BookingType bookingType) throws SAXException, IOException, ParseException {
		PreCommitValidationHandler handler = null;
		for (ReservationType reservationType : ReservationType.values()) {
			for (PaymentType paymentType : PaymentType.values()) {
				HTTPSession session = Login.getInstance().login(LoginTest.TEST_USER_LOGIN, LoginTest.TEST_USER_PIN);
				Date date = DateUtils.getDateFormat().parse("25.07.2011");
				Date queryTime = DateUtils.getTimeFormat().parse("08:00");
				handler = Booking.getInstance().doPreBooking(session, new StationVO("Karlsruhe Hbf"), new StationVO("Frankfurt(Main)Hbf"), 
						date, queryTime, bookingType, reservationType, paymentType);
				Assert.assertTrue("precommit price must not be undefined for payment:" + paymentType.name() + " and reservation: " + reservationType.name(), handler.getPrice()!=null);
			}
		}
		return handler.getPrice();
	}
	
}
