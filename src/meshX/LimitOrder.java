/**
 * @class LimitOrder
 * Defines a generic limit order with trader,price(stored as int for comparison efficiency),quantity and timestamp as data fields
 * Acts as parent to Bid and Offer classes.
 *
 * @author Abhishek Anurag
 * @email anurag.abhishek3@gmail.com
 */
package meshX;

public class LimitOrder {

	private String party;
	private int price;
	private int quantity;
	private long timestamp;
	
	
	public LimitOrder(String party, int price, int quantity,
		long timestamp) {
	    super();
	    this.party = party;
	    this.price = price;
	    this.quantity = quantity;
	    this.timestamp = timestamp;
	}
	public String getParty() {
		return party;
	}
	public void setParty(String party) {
		this.party = party;
	}
	public float getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	
}

