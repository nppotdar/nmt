package nmt.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnect
{
	private ResultSet rs = null;
	private ResultSetMetaData rsmd = null;
	
	private String dbName;
	private String userid;
	private String password;
	private String url;
	private String driver;
	private int noOfRows;
	
	public DBConnect()
	{
		Connection con = null;
		dbName = "nmt_restaurant";
		userid = "root";
		password = "sumitra";
		url = "jdbc:mysql://127.0.0.1/" + dbName;
		driver = "com.mysql.jdbc.Driver";
		try
		{
			Class.forName(driver).newInstance();
			con = DriverManager.getConnection(url, userid, password);

			Statement st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			rs = st.executeQuery("Select * from nmt_menuitemslist");
			rsmd= rs.getMetaData();
			rs.last();
			noOfRows = rs.getRow();
			rs.beforeFirst();
		} catch (Exception e)
		{

			e.printStackTrace();
			System.out.println("error in connection");
		}
		
	}
	public DBConnect(String sql)
	{
		Connection con = null;
		dbName = "nmt_restaurant";
		userid = "root";
		password = "sumitra";
		url = "jdbc:mysql://127.0.0.1/" + dbName;
		driver = "com.mysql.jdbc.Driver";
		try
		{
			Class.forName(driver).newInstance();
			con = DriverManager.getConnection(url, userid, password);

			Statement st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			rs = st.executeQuery(sql);
			rsmd= rs.getMetaData();
			rs.last();
			noOfRows = rs.getRow();
			rs.beforeFirst();
			
		} catch (Exception e)
		{

			e.printStackTrace();
			System.out.println("error in connection");
		}
		
	}

	
	public void runUpdate(String sql)
	{
		try
		{
			Class.forName(driver).newInstance();
			Connection con = DriverManager.getConnection(url, userid, password);

			Statement st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			st.execute(sql);
			} catch (Exception e)
		{

			e.printStackTrace();
			System.out.println("error in connection");
		}
		
	}
	

	public void test()
	{
		try
		{
			while (rs.next())
			{
				System.out.println("Test Passed");
				System.out.println(rsmd.getColumnCount());
			}
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			System.out.print("Test failed");
			e.printStackTrace();
		}
	}

	public ResultSet getRs()
	{
		return rs;
	}
	
	
	public ResultSetMetaData getRsmd()
	{
		return rsmd;
	}
	public int getRowCount()
	{
		return noOfRows;
	}

	
}