package nmt.db;

import java.sql.ResultSet;
import java.util.List;

import nmt.beans.Item;

public class ItemReview {

		public static List<String> getItemReviews(Item item)
		{
			
			DBConnect db = new DBConnect("Select * from nmt_reviews");;
			ResultSet rs = db.getRs();
			
			return null;
			
		}
		
}
