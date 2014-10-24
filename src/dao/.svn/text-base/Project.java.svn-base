package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import dto.FeedObjects;


public class Project {
	
	
	public ArrayList<FeedObjects> GetFeeds(Connection connection) throws Exception
	{
		ArrayList<FeedObjects> feedData = new ArrayList<FeedObjects>();
		try
		{
			//String uname = request.getParameter("uname");
			PreparedStatement ps = connection.prepareStatement("SELECT title,description,url FROM website ORDER BY id DESC");
			//ps.setString(1,uname);
			ResultSet rs = ps.executeQuery();
			while(rs.next())
			{
				FeedObjects feedObject = new FeedObjects();
				feedObject.setTitle(rs.getString("title"));
				feedObject.setDescription(rs.getString("description"));
				feedObject.setUrl(rs.getString("url"));
				feedData.add(feedObject);
			}
			return feedData;
		}
		catch(Exception e)
		{
			throw e;
		}
	}
	
}
