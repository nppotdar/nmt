package nmt.beans;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;

import nmt.db.DBConnect;


public class Item {
	// only item_no is int
	private int item_no;
	private String item_name;
	private String item_description;
	private int item_price;
	private HashMap<String,String> item_classifiers;
	private ItemReviews item_reviews;
	private ItemRatings item_ratings;
	
	public Item() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Item(int item_no){
		DBConnect temp = new DBConnect("SELECT * FROM nmt_menuitemslist where item_no="+item_no);
		ResultSet rs = temp.getRs();
		ResultSetMetaData rsmd = temp.getRsmd();
		try {
				rs.next();
				this.item_no = rs.getInt("item_no");
				this.item_name = rs.getString("item_name");
				this.item_description = rs.getString("item_description");
				this.item_price = rs.getInt("item_price");
				
				item_classifiers = new HashMap<String, String>();
				for(int i = 4; i<=temp.getRsmd().getColumnCount();i++)
					item_classifiers.put(rsmd.getColumnName(i).substring(5),rs.getString(i)); 
				
				this.item_reviews = new ItemReviews(item_no);
				this.item_ratings = new ItemRatings(item_no);
			} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		
	}
public Item(int item_no, String item_name, String item_description, String item_type,
			String item_cuisine, String item_class, int item_price) {
		super();
		this.item_no = item_no;
		this.item_name = item_name;
		this.setItem_description(item_description);
		this.item_price = item_price;
	}
	public int getItem_no() {
		return item_no;
	}
	public void setItem_no(int item_no) {
		this.item_no = item_no;
	}
	public String getItem_name() {
		return item_name;
	}
	public void setItem_name(String item_name) {
		this.item_name = item_name;
	}
	public int getItem_price() {
		return item_price;
	}
	public void setItem_price(int item_price) {
		this.item_price = item_price;
	}
	public ItemReviews getItem_reviews() {
		return item_reviews;
	}
	public void setItem_reviews(ItemReviews item_reviews) {
		this.item_reviews = item_reviews;
	}
	public ItemRatings getItem_ratings() {
		return item_ratings;
	}
	public void setItem_ratings(ItemRatings item_ratings) {
		this.item_ratings = item_ratings;
	}
	public void setItem_description(String item_description) {
		this.item_description = item_description;
	}
	public String getItem_description() {
		return item_description;
	}
	public HashMap<String, String> getItem_classifiers() {
		return item_classifiers;
	}
	public String getClassifierType(String classifier){
		return item_classifiers.get(classifier);
	}
	public void addItemReview(String review){
		DBConnect db = new DBConnect();
		db.runUpdate("INSERT INTO nmt_reviews(item_no, item_review) VALUES("+this.item_no+",'"+review+"')");
		this.item_reviews.addReview(review);
	}
	
	
}
