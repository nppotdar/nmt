package nmt.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import nmt.beans.Item;

public class ItemsList {
	
	private ArrayList<Item> list;
	private ArrayList<Item> filteredList;
	private int numberOfItems;
	public ItemsList()
	{
		//get the number of Items
		DBConnect temp = new DBConnect();
		numberOfItems = temp.getRowCount();
		list = new ArrayList<Item>(numberOfItems);
		filteredList = new ArrayList<Item>(numberOfItems);
		for(int i = 1; i<=numberOfItems; i++)
		{
			list.add(new Item(i));
		}
		filteredList.addAll(list);
	}
	
	
	public ArrayList<Item> getItemsByFilter(String typeOfFilter,String filter)
	{
		String filterType = null;
		int index =0;
		
			for(int i = 0; i<filteredList.size();i++)
			{
				Item temp = filteredList.get(i);
				System.out.println(temp.getItem_name()+" Is encountered");
				filterType = temp.getClassifierType(typeOfFilter);
				System.out.println("Filter is :"+filter);
				System.out.println("FilterType is :"+filterType);
				if(!filterType.equalsIgnoreCase(filter))
				{
					System.out.println(temp.getItem_name()+" is removed");
					filteredList.remove(temp);
					i--;
						
				}
			}
		if(filteredList.size() == 0)
			return null;
		else
			return filteredList;
	}
	public void clearAllFilters()
	{
		filteredList.clear();
		filteredList.addAll(list);
	}
	public ArrayList<Item> getList() {
		return list;
	}
    
	public int getNumberOfItems() {
		return numberOfItems;
	}


	public ArrayList<Item> getFilteredList() {
		return filteredList;
	}
	


	
}
