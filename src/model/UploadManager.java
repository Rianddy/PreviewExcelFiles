package model;

import java.sql.Connection;
import java.util.ArrayList;

import dao.Database;

import dao.GetXlsProject;
import dto.XlsObjects;

public class UploadManager {
	
	
	public ArrayList<XlsObjects> GetXls(String sid) throws Exception {
		ArrayList<XlsObjects> xls = null;
		try {
			    //Database database= new Database();
			    //Connection connection = database.Get_Connection();
				GetXlsProject project= new GetXlsProject();
				xls=project.GetXls(sid);
		} catch (Exception e) {
			throw e;
		}
		return xls;
	}
}
