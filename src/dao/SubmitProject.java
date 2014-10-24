package dao;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.List;

import model.ProjectManager;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import uploadFunction.SaveXls;
import uploadFunction.SaveXlsx;
import uploadFunction.SaveCsv; 
import net.sf.json.JSONArray;   
public class SubmitProject {
	String fileName;
	public SubmitProject(Connection connection, String tableName, String filePath, String sid, String ktrPath, String user_id, String updatedColAndStreamNamesJson, String condition, String columns) throws Exception
	{
		int judgeFileType=JudgeFileType(filePath);
		JSONArray jsArr = JSONArray.fromObject(updatedColAndStreamNamesJson);
		String columnNumber=jsArr.size()+"";
		JSONArray columnsArr = JSONArray.fromObject(columns);
		ProjectManager projectManager= new ProjectManager();
		ProjectManager.setSid(sid);
		ProjectManager.setFilepath(getSubString(ktrPath));
		ProjectManager.setUser_id(user_id);
		ProjectManager.setTable_name(fileName);
		ProjectManager.setColumns(columnNumber);
		ProjectManager.setCondition(condition);
		try {
			switch (judgeFileType) {
			case 1:
				FileInputStream fileInputStream = new FileInputStream(filePath);
				SaveXls savexls = new SaveXls(fileInputStream, -1, connection, tableName, jsArr, columnsArr);
				savexls.process();
				projectManager.testOPM1();
				projectManager.testOPM2();
				projectManager.testOPM3();
				projectManager.testOPM4();
				break;
			case 2:
				File xlsxFile = new File(filePath);
				OPCPackage p = OPCPackage.open(xlsxFile.getPath(),
						PackageAccess.READ);
				SaveXlsx savexlsx = new SaveXlsx(p, System.out, -1, connection, tableName, jsArr, columnsArr);
				savexlsx.process();
				projectManager.testOPM1();
				projectManager.testOPM2();
				projectManager.testOPM3();
				projectManager.testOPM4();
				break;
			case 3:
				SaveCsv csv = new SaveCsv(filePath,connection,tableName, jsArr, columnsArr);
				projectManager.testOPM1();
				projectManager.testOPM2();
				projectManager.testOPM3();
				projectManager.testOPM4();
				break;
			default:
				System.out.print("default");
				break;
			}
		} catch (Exception e) {
			System.out.println(e);
			throw e;
		}
	}
	public int JudgeFileType(String sid) {
		File file = new File(sid);
		fileName = file.getName();
		String prefix = fileName.substring(fileName.lastIndexOf(".") + 1);
		if (prefix.equals("xls"))
			return 1;
		else if (prefix.equals("xlsx"))
			return 2;
		else
			return 3;
	}
	public String getSubString(String ktrPath) {
		File file = new File(ktrPath);
		String ktrName = file.getName();
		return ktrPath.substring(0, ktrPath.length()-ktrName.length());
	}
}
