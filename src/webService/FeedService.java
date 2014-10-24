package webService;

import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import model.ProjectManager;

import com.google.gson.Gson;

import dto.FeedObjects;
import javax.servlet.http.HttpServletRequest;

@Path("/WebService")
public class FeedService {
	
	@GET
	@Path("/GetFeeds")
	@Produces("application/json")
	public String feed(@Context HttpServletRequest request) 
	{
		String feeds = null;
		try 
		{
			String sid=request.getParameter("sid");
			String filepath= request.getParameter("filepath");
			String user_id=request.getParameter("user_id");
			String table_name= request.getParameter("table_name");
	        String columns= request.getParameter("columns");
	        String condition= request.getParameter("condition");
	        
	
			ProjectManager projectManager= new ProjectManager();
			ProjectManager.setSid(sid);
			ProjectManager.setFilepath(filepath);
			ProjectManager.setUser_id(user_id);
			ProjectManager.setTable_name(table_name);
			ProjectManager.setColumns(columns);
			ProjectManager.setCondition(condition);
			
			projectManager.testOPM1();
			projectManager.testOPM2();
			projectManager.testOPM3();
			projectManager.testOPM4();
			Gson gson = new Gson();
			feeds="Done successfully";
			//feedData = projectManager.GetFeeds();
			//StringBuffer sb = new StringBuffer();
			//Gson gson = new Gson();
			//System.out.println(gson.toJson(feedData));
			//feeds = gson.toJson(feedData);
		
		} catch (Exception e)
		{
			System.out.println("error");
		}
		return feeds;
	}

}
