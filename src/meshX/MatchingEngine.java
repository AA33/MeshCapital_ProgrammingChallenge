/**
 * @class Matching Engine
 * Defines the data structures and methods for running the matching engine.
 * Bids and Offers are read from the file and processed in an online fashion.
 * Outstanding bids and orders are stored in priority queues ordered by the priority defined by LimitOrderComparators.
 *
 * @author Abhishek Anurag
 * @email anurag.abhishek3@gmail.com
 */
package meshX;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.zip.GZIPInputStream;

public class MatchingEngine {

    //stores outstanding Offers in priority order
    private PriorityQueue<Offer> OfferQueue;
    //stores outstanding Bids in priority order
    private PriorityQueue<Bid> BidQueue;
    //stores mapping of traders to their current position
    private HashMap<String, Integer> TraderMap;
    
    private File inputFile;
    private static final String UNZIPPED_FILE_NAME = "trades.csv";
    
    /*
     * Constructor
     * @params: Path to gzip input file
     * @function :Calls readTradedFromFile function to unzip input file and initializes the Bid and Offer priority queues and the TraderMap.  
     */
    public MatchingEngine(String inputFile) {
	super();
	OfferQueue = new PriorityQueue<Offer>(100, new OfferComparator());
	BidQueue = new PriorityQueue<Bid>(100, new BidComparator());
	TraderMap = new HashMap<String, Integer>();
	try {
	    readTradesFromFile(inputFile);
	} catch (IOException e) {
	    System.out.println("IOException occured in reading from gzip file. Please check the path to gzip file containing the trading entries.");
	    e.printStackTrace();
	}
    }
    
    //generic getter
    public HashMap<String, Integer> getTraderMap() {
	return TraderMap;
    }

    /*
     * @params: Path to gzip input file
     * @function :Unzips the input file to allow reading the entries for trading.  
     */
    private void readTradesFromFile(String inputFilePath) throws IOException {
	inputFile = new File(inputFilePath);
	String outFilename = UNZIPPED_FILE_NAME;
	FileInputStream instream = new FileInputStream(inputFile);
	GZIPInputStream ginstream = new GZIPInputStream(instream);
	FileOutputStream outstream = new FileOutputStream(outFilename);
	byte[] buf = new byte[2048];
	int len;
	while ((len = ginstream.read(buf)) > 0) {
	    outstream.write(buf, 0, len);
	}
	ginstream.close();
	outstream.close();
    }
    
    /*
     * @function :Reads line by line from unzipped file,
     *            Creates Bid/Offer object and performs trading by calling processBid/Offer functions, 
     *            Updates TraderMap after every trade,
     *            Deleted unzipped file at the end of operation.  
     */
    public void letsTrade() throws NumberFormatException, IOException {

	FileInputStream fstream = new FileInputStream(UNZIPPED_FILE_NAME);
	DataInputStream in = new DataInputStream(fstream);
	BufferedReader br = new BufferedReader(new InputStreamReader(in));
	String strLine;
	String[] trade = new String[6];
	while ((strLine = br.readLine()) != null) {
	    trade = strLine.split(",");
	    if (trade[5].charAt(0) == 'S') {
		Offer tradeOffer = new Offer(trade[1], (int) (Float.parseFloat(trade[2]) * 100),
			Integer.parseInt(trade[3]), Long.parseLong(trade[4]));
		if (!TraderMap.containsKey(trade[1])) {
		    TraderMap.put(trade[1], new Integer(0));
		}
		processOffer(tradeOffer);
	    } else if(trade[5].charAt(0) == 'B'){
		Bid tradeBid = new Bid(trade[1], (int) (Float.parseFloat(trade[2]) * 100), Integer.parseInt(trade[3]),
			Long.parseLong(trade[4]));
		if (!TraderMap.containsKey(trade[1])) {
		    TraderMap.put(trade[1], new Integer(0));
		}
		processBid(tradeBid);
	    }
	}
	br.close();
	in.close();
	fstream.close();
	File unzippedFile = new File(UNZIPPED_FILE_NAME);
	unzippedFile.delete();
    }
    
    /*
     * @params : Offer to be processed
     * @function : Tries to trade an incoming offer with Bids in queue,
     * 		   Updates Bid and Offer queues if required,
     *             Updates TraderMap if trade takes place.
     */
    public void processOffer(Offer offer) {
	int noOfSharesOnOffer = offer.getQuantity();
	int noOfSharesTraded = 0;
	boolean tradePossible = true;
	while (noOfSharesOnOffer != 0 && tradePossible) {
	    Bid currentBid = null;
	    currentBid = BidQueue.peek();
	    if (currentBid == null) {
		tradePossible = false;
		continue;
	    }
	    if (currentBid.getPrice() >= offer.getPrice()) {
		if (noOfSharesOnOffer > currentBid.getQuantity()) {
		    // currentBid does not satisfy the offer
		    noOfSharesTraded = currentBid.getQuantity();
		    BidQueue.poll(); // 0 is index of currentBid
		    noOfSharesOnOffer = noOfSharesOnOffer - noOfSharesTraded;
		    updateTraderMap(currentBid, offer, noOfSharesTraded);
		} else {
		    // currentBid satisfies the offer
		    noOfSharesTraded = noOfSharesOnOffer;
		    noOfSharesOnOffer = 0; // noOfSharesOnOffer-noOfSharesTraded;
					   // since offer is satisfied
					   // completely
		    updateTraderMap(currentBid, offer, noOfSharesTraded);
		    BidQueue.poll(); // 0 is index of currentBid
		    currentBid.setQuantity(currentBid.getQuantity() - noOfSharesTraded);
		    if (currentBid.getQuantity() > 0) {
			BidQueue.add(currentBid);
		    }
		}
	    } else {
		tradePossible = false;
	    }
	}
	if (noOfSharesOnOffer != 0) {
	    offer.setQuantity(noOfSharesOnOffer);
	    OfferQueue.add(offer);
	}
    }

    /*
     * @params : Bid to be processed
     * @function : Tries to trade an incoming Bid with Offers in queue,
     * 		   Updates Bid and Offer queues if required,
     *             Updates TraderMap if trade takes place.
     */
    public void processBid(Bid bid) {
	int noOfSharesOnBid = bid.getQuantity();
	int noOfSharesTraded = 0;
	boolean tradePossible = true;
	while (noOfSharesOnBid != 0 && tradePossible) {
	    Offer currentOffer = null;
	    currentOffer = OfferQueue.peek();
	    if (currentOffer == null) {
		tradePossible = false;
		continue;
	    }

	    if (currentOffer.getPrice() <= bid.getPrice()) {
		if (noOfSharesOnBid > currentOffer.getQuantity()) {
		    // when the currentOffer does not satisfy the bid fully
		    noOfSharesTraded = currentOffer.getQuantity();
		    OfferQueue.poll(); // 0 is index of currentOffer
		    noOfSharesOnBid = noOfSharesOnBid - noOfSharesTraded;
		    updateTraderMap(bid, currentOffer, noOfSharesTraded);
		} else {
		    // when the currentOffer fully satisfies the bid
		    noOfSharesTraded = noOfSharesOnBid;
		    noOfSharesOnBid = 0; // noOfSharesOnBid-noOfSharesTraded;
					 // since bid is satisfied completely
		    updateTraderMap(bid, currentOffer, noOfSharesTraded);
		    OfferQueue.poll(); // 0 is index of currentOffer
		    currentOffer.setQuantity(currentOffer.getQuantity() - noOfSharesTraded);
		    if (currentOffer.getQuantity() > 0) {
			OfferQueue.add(currentOffer);
		    }
		}
	    } else {
		tradePossible = false;
	    }

	}
	if (noOfSharesOnBid != 0) {
	    bid.setQuantity(noOfSharesOnBid);
	    BidQueue.add(bid);
	}
    }

    /*
     * @params : The Bid and Offer that have traded and the number of shares traded
     * @function: Updates TraderMap of latest trade,
     *            Skips processing if a trader trades with him/herself.
     */
    public void updateTraderMap(Bid currentBid, Offer offer, int noOfSharesTraded) {

	String biddingParty = currentBid.getParty();
	String offeringParty = offer.getParty();
	if (biddingParty.equals(offeringParty)) {
	    return;
	}
	Integer currentBidderPos = TraderMap.get(biddingParty);
	TraderMap.put(biddingParty, new Integer(currentBidderPos.intValue() + noOfSharesTraded));

	Integer currentOffererPos = TraderMap.get(offeringParty);
	TraderMap.put(offeringParty, new Integer(currentOffererPos.intValue() - noOfSharesTraded));
    }

    /*
     * Test function: to get positions of all traders.
     */
    public void showTradersInfo() {
	Iterator<String> traderIterator = TraderMap.keySet().iterator();
	while (traderIterator.hasNext()) {
	    String tempTrader = traderIterator.next();
	    System.out.println(tempTrader + ": " + TraderMap.get(tempTrader).intValue());
	}
    }

    /*
     * Test function : to show all Bids and Offers currently on the book in priority order.
     */
    public void showOrderBookStatus() {
	System.out.println("Bids are:");
	Bid b = BidQueue.poll();
	while (b != null) {
	    System.out.println(b.getTimestamp() + ":By " + b.getParty() + " for " + b.getQuantity() + " shares at "
		    + b.getPrice());
	    b = BidQueue.poll();
	}
	System.out.println("Offers are:");
	Offer o = OfferQueue.poll();
	while (o != null) {
	    System.out.println(o.getTimestamp() + ":By " + o.getParty() + " for " + o.getQuantity() + " shares at "
		    + o.getPrice());
	    o = OfferQueue.poll();
	}

    }

    /*
     * Main function : Takes path to gzip input file as program argument,
     * 		       Creates MatchingEngine object to perform trading,
     *                 Displays Kaylee's net position and processing time in seconds.
     */
    public static void main(String args[]) {
	if (args[0] != null) {
	    Date d = new Date();
	    long startTime = d.getTime();
	    MatchingEngine me = new MatchingEngine(args[0]);
	    try {
		me.letsTrade();
	    } catch (NumberFormatException e) {
		System.out.println("Input file contains unparseable entries.");
		e.printStackTrace();
	    } catch (IOException e) {
		System.out.println("Error reading from input file.");
		e.printStackTrace();
	    }
	    System.out.println("Kaylee:" + me.getTraderMap().get("Kaylee"));
	    d = new Date();
	    long endTime = d.getTime();
	    System.out.println("Runtime=" + (endTime - startTime) / 1000 + "s");
	} else {
	    System.out.println("Please provide complete path to the gzip file containing trading entries as program argument.");
	}
    }
}