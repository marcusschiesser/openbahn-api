package de.marcusschiesser.dbpendler.server.bahnwrapper.handler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.marcusschiesser.dbpendler.server.bahnwrapper.Booking.PaymentType;

public class PreCommitValidationHandler extends DefaultHandler {
	
	//private final Logger log = Logger.getLogger(PreCommitValidationHandler.class.getName());

	private String bahncardNumber;
	private String mobileNumber;
	private boolean insideDiv = false;
	private Pattern commitPattern;
	private Double price = null;

	public PreCommitValidationHandler(PaymentType paymentType) {
		if(paymentType==PaymentType.creditCard) {
			commitPattern = Pattern.compile("Bei der Buchung wird die Kreditkarte mit ([0-9,]+) EUR belastet");
		} else {
			commitPattern = Pattern.compile("Bei der Buchung wird Ihr Konto mit ([0-9,]+) EUR belastet");
		}
	}
/*
// TODO: parse this form and check connection data
<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.0//EN" "http://www.wapforum.org/DTD/xhtml-mobile10.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="de">
<head>

<meta http-equiv="cache-control" content="no-cache" />        
<meta http-equiv="pragma" content="no-cache" />

<meta http-equiv="expires" content="0" />
<meta name="viewport" content="width=device-width; initial-scale=1.0;"/>
<!-- Format detection (Zahlen nicht automatisch als Telefonnummern darstellen) -->
<meta name="format-detection" content="telephone=no" />
<!-- Handheld friendly wird von einigen Browsern erkannt -->
<meta name="HandheldFriendly" content="true" />
<meta http-equiv="content-type" content="text/html; charset=iso-8859-1"/>
<meta http-equiv="content-style-type" content="text/css"/>    	
<meta name="copyright" content="2009 Deutsche Bahn AG"/>
<title>DB - Zahlung</title>
<link href="https://reiseauskunft.bahn.de/es/mobileclient/v800/css/bd_notouch_8.00.p06.00.min.css" rel="stylesheet" type="text/css"/>

	    
<!-- Shortcut Icons -->
<link rel="apple-touch-icon" type="image/x-icon" href="https://reiseauskunft.bahn.de/es/mobileclient/v800/img/apple-touch-icon.png"/>
<link rel="shortcut icon" href="https://reiseauskunft.bahn.de/es/mobileclient/v800/img/favicon.ico" />	    
</head>
<body>
<!-- Kopfzeile einbinden -->
<div id="header">
<div class="logo"><img src="https://reiseauskunft.bahn.de/es/mobileclient/v800/img/l_bahnmobile.gif" alt="DB Deutsche Bahn" width="60px" height="15px" /></div>
<div id="branding">
<div class="bar1"> </div>
<div class="bar2"> </div>
</div>
</div>

<div id="content">
<form method="post" name="formular" autocomplete="off" action="/mobile/bu/ks.post?mId=96230d&lang=de">
 <input type="hidden" name="click_id" value="1.402202319426663" />


<h1>Zahlung</h1>



<div class="formular rline">
<div class="fline">
Zahlungsart: Lastschrift        	
</div>

<div class="fline">
Bei der Buchung wird Ihr Konto mit 2,50 EUR belastet.
</div>
</div>


<div class="formular rline">
<div class="fline bold">
bahn.bonus/bahn.comfort:
</div>
<div class="fline">Wenn Sie Prämien- und Statuspunkte für sammelfähige Angebote der DB möchten, geben Sie hier ihre BahnCard-/bahn.bonus Card-Nummer an:</div>
<div class="fline">
7081&nbsp;<input type="text" class="threequarterswidth" name="bahncard.nummerNummer" value="420001794947" maxlength="12" size="17" id="bahncard.nummer" />
</div>
</div>
<div class="haupt">
Es gelten die bei der Anmeldung akzeptierten AGBs und <a href="https://fahrkarten.bahn.de/mobile/bu/nh.ignore.go?mId=96230d&lang=de">Nutzungshinweise</a> in der aktuellen Version.
</div>
<div class="bline rline">
<input type="submit" name="button.reservieren_p" value="Reservieren" class="hauptbtn" title="Reservieren" id="button.reservieren" />
</div>
<div class="bline rline">
<input type="submit" name="button.zurueck_p" value="Zurück" class="nebenbtn" title="Zurück" id="button.zurueck" />
</div>
<div class="bline">
<input type="submit" name="button.abbrechen_p" value="Buchung verwerfen" class="nebenbtn" title="Buchung verwerfen" id="button.abbrechen" />
</div>
</form>
</div>
<!-- Fusszeile einbinden -->

<div class="logout">
<a class="arrowlink" href="https://fahrkarten.bahn.de/mobile/st/lo.post?mId=96230d&lang=de">Logout</a>
</div>
	
<div id="footer">
<ul class="footer">
 
<li class="firstitem">
<a href="https://fahrkarten.bahn.de/mobile/st/pt.go?mId=96230d&lang=de">
Startseite
</a>
</li>
	
</ul>
</div>
 */
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if(localName.equalsIgnoreCase("input") && attributes.getValue("name")!=null && attributes.getValue("name").equalsIgnoreCase("bahncard.nummerNummer")) {
			bahncardNumber = attributes.getValue("value");
		}
		if(localName.equalsIgnoreCase("input") && attributes.getValue("name")!=null && attributes.getValue("name").equalsIgnoreCase("mobilnr")) {
			mobileNumber = attributes.getValue("value");
		}
		if(localName.equalsIgnoreCase("div") && attributes.getValue("class")!=null && attributes.getValue("class").equalsIgnoreCase("fline")) {
			insideDiv = true;
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(localName.equalsIgnoreCase("div")) {
			insideDiv = false;
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if(insideDiv) {
			String s = new String(ch);
			Matcher matcher = commitPattern.matcher(s);
			if(matcher.find()) {
				price = Double.parseDouble(matcher.group(1).replace(',', '.'));
			}
		}
	}

	public boolean isValid() {
		return price!=null;
	}

	public Double getPrice() {
		return price;
	}
	
	public String getMobileNumber() {
		return mobileNumber;
	}

	public String getBahncardNumber() {
		return bahncardNumber;
	}

}