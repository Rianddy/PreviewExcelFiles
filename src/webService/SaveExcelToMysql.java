package webService;


import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import model.SaveExcelManager;
import javax.servlet.http.HttpServletRequest;
@Path("/Submit")
public class SaveExcelToMysql {
	@POST @Consumes("application/json")
	@Path("/SaveExcelToMysql")
	@Produces("application/json")
	public String SaveExcelToMysql(final inputDBParam  input)
	{
		String databaseName = input.databaseName;
		String tableName = input.tableName;
		String filePath = input.filePath;
		String engine = input.engine;
		String username = input.username;
		String password = input.password;
		String server = input.server;
		String port = input.port;
		String sid = input.sid;
		String xmlPath = input.xmlPath;
		String user_id = input.user_id;
		String columns = input.columns;
		String condition = input.condition;
		String updatedColAndStreamNamesJson = input.updatedColAndStreamNamesJson;
		System.out.println(sid+" "+xmlPath+" "+user_id+" "+columns+" "+condition+" "+ updatedColAndStreamNamesJson);
		try 
		{
			SaveExcelManager saveexcelmanager= new SaveExcelManager(databaseName, tableName, filePath, username, password, server, port, sid, xmlPath, user_id, updatedColAndStreamNamesJson, condition, columns);
			return "Success"+databaseName+tableName+filePath+engine+username+password+server+port;
		} 
		catch (Exception e)
		{
			System.out.println(e);
			return e.toString()+databaseName+tableName+filePath+engine+username+password+server+port;
		}
		
	}
}
