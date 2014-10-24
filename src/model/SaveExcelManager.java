package model;

import java.sql.Connection;
import dao.Database;
import dao.SubmitProject;

public class SaveExcelManager {
	
	
	public SaveExcelManager(String databaseName, String tableName, String filePath, String username, String password, String server, String port , String sid, String ktrPath, String user_id, String updatedColAndStreamNamesJson, String condition, String columns) throws Exception {
		try {
			    Database database= new Database();
			    Connection connection = database.Get_Connection(databaseName, username, password, server,port);
				SubmitProject project = new SubmitProject(connection, tableName, filePath, sid, ktrPath, user_id, updatedColAndStreamNamesJson, condition, columns);
		} catch (Exception e) {
			throw e;
		}
	}
}
