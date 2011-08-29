package nmt.beans;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import nmt.db.DBConnect;

public class ItemReviews {
	private ArrayList<String> reviews;
	private int number;
	
	
	public ItemReviews(int itemNo) {
		// TODO Auto-generated constructor stub
		DBConnect db = new DBConnect("Select * from nmt_reviews where item_no = "+ itemNo);
		ResultSet rs = db.getRs();
		ArrayList<String> list = new ArrayList<String>();
		
		try {
			while(rs.next())
			{
				list.add(rs.getString(2));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.reviews = list;
		this.number = list.size();
		
	}
	
	public ArrayList<String> getReviews() {
		return reviews;
	}
	public void setReviews(ArrayList<String> reviews) {
		this.reviews = reviews;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public void addReview(String review){
		this.reviews.add(review);
	}
	
	

}
