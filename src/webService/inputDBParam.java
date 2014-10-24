package webService;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class inputDBParam {
	public String databaseName;
	public String tableName;
	public String filePath;
	public String engine;
	public String username;
	public String password;
	public String server;
	public String port;
	public String sid;
	public String xmlPath;
	public String user_id;
	public String columns;
	public String condition;
	public String updatedColAndStreamNamesJson;
}