/**
 * This file defines the custom comparators for Bids and Offers to be used by their respective queues.
 * @classes BidComparator and OfferComparator
 * Define trade matching priority for Bid and Offer respectively
 *
 * @author Abhishek Anurag
 * @email anurag.abhishek3@gmail.com
 */
package meshX;

import java.util.Comparator;

class BidComparator implements Comparator<LimitOrder>{

    @Override
    public int compare(LimitOrder o1, LimitOrder o2) {
	if(o1.getPrice()>o2.getPrice()){
	    return -1;
	}else if(o1.getPrice()<o2.getPrice()){
	    return 1;
	}
	if(o1.getQuantity()>o2.getQuantity()){
	    return -1;
	}else if(o1.getQuantity()<o2.getQuantity()){
	    return 1;
	}
	if(o1.getTimestamp()<o2.getTimestamp()){
	    return -1;
	}else if(o1.getTimestamp()>o2.getTimestamp()){
	    return 1;
	}
	return 0;
    }

}

class OfferComparator implements Comparator<LimitOrder>{

    @Override
    public int compare(LimitOrder o1, LimitOrder o2) {
	if(o1.getPrice()<o2.getPrice()){
	    return -1;
	}else if(o1.getPrice()>o2.getPrice()){
	    return 1;
	}
	if(o1.getQuantity()>o2.getQuantity()){
	    return -1;
	}else if(o1.getQuantity()<o2.getQuantity()){
	    return 1;
	}
	if(o1.getTimestamp()<o2.getTimestamp()){
	    return -1;
	}else if(o1.getTimestamp()>o2.getTimestamp()){
	    return 1;
	}
	return 0;
    }

}

