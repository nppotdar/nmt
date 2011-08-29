package nmt.beans;

import java.sql.ResultSet;
import java.sql.SQLException;
import nmt.db.DBConnect;

public class ItemRatings {
	private double rating;
	private int number;
	
	public ItemRatings(int itemNo) {
		// TODO Auto-generated constructor stub
		DBConnect db = new DBConnect("Select * from nmt_ratings where item_no = "+ itemNo);
		ResultSet rs = db.getRs();
		int temp = 0;
		int count = 0;
		try {
			while(rs.next())
			{
				temp += Integer.parseInt(rs.getString(2));
				count++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.rating = temp*1.0/count;
		this.number = count;
	}
	
	
	public double getRate() {
		return rating;
	}
	public void setRate(int rate) {
		this.rating = rate;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	
}
