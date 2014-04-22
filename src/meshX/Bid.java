/**
 * @class Bid
 * Derives from LimitOrder
 * Distinguished by 'type' field
 * Overrides toString() for pretty printing.
 *
 * @author Abhishek Anurag
 * @email anurag.abhishek3@gmail.com
 */
package meshX;

public class Bid extends LimitOrder {

    private static char type='B';

    public Bid(String party, int price, int quantity, long timestamp
	    ) {
	super(party, price, quantity, timestamp);
    }

    public char getType() {
        return type;
    }
    
    public boolean equals(Bid b){
	if(this.getParty().equals(b.getParty()) && this.getPrice()==b.getPrice() && this.getQuantity()==b.getQuantity() && this.getTimestamp()==b.getTimestamp()){
	    return true;
	}
	return false;
    }
    public String toString(){
	String tradeType = "buy ";
	return this.getParty() + " wants to " + tradeType + this.getQuantity() + " shares at " +this.getPrice();
    }

}
