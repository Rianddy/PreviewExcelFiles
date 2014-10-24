package uploadFunction;

import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.Connection;
import net.sf.json.JSONArray;
import au.com.bytecode.opencsv.CSVReader;

public class SaveCsv {
	String saveRow = "";
	Connection connection;
	String tableName = "";
	String startRow;
	String startColumn;
	JSONArray updatedColAndStreamNamesJsonArr;
	JSONArray columnsArr;
	boolean judgeTheSheet = true;
	int updatedColAndStreamNamesJsonArrLength=0;
	int rows = 0;// control to show how many rows
	public void readCSVFile(String pstrFileName) {
		CSVReader reader;
		String nextLine[] = new String[1000];
		FileReader fr = null;
		try {
			fr = new FileReader(pstrFileName);
			reader = new CSVReader(fr);
			while ((nextLine = reader.readNext()) != null) {
				if (rows ==(Integer.parseInt(startRow)-1))
				{
					for (int nIndex = (Integer.parseInt(startColumn)-1); nIndex < nextLine.length; nIndex++) {
						if(updatedColAndStreamNamesJsonArr.contains(nextLine[nIndex]))
						{
							updatedColAndStreamNamesJsonArr.remove(updatedColAndStreamNamesJsonArrLength);
							updatedColAndStreamNamesJsonArr.add(updatedColAndStreamNamesJsonArrLength, nIndex);
							updatedColAndStreamNamesJsonArrLength++;
							System.out.println(updatedColAndStreamNamesJsonArr+" and "+ updatedColAndStreamNamesJsonArrLength);
						}
					}
				}
				if(rows >=(Integer.parseInt(startRow)))
				{
					for (int nIndex = (Integer.parseInt(startColumn)-1); nIndex < nextLine.length; nIndex++) {
						if(updatedColAndStreamNamesJsonArr.contains(nIndex))
						{
							if (nIndex == (Integer.parseInt(startColumn)-1)) {
								saveRow = saveRow + "'" + nextLine[nIndex] + "'";
							} else
								saveRow = saveRow + ",'" + nextLine[nIndex] + "'";
							System.out.print(nextLine[nIndex] + ",");
						}
					}
					insertRowInDB(saveRow);
					System.out.println(saveRow);
					saveRow = "";
				}
				rows++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static <T> String getType(T t) {
		if (t instanceof String) {
			return "String";
		} else if (t instanceof Integer) {
			return "Int";
		} else {
			return "String";
		}
	}

	public SaveCsv(String strFilePath, Connection connection, String tableName, JSONArray updatedColAndStreamNamesJsonArr, JSONArray columnsArr) {
		this.connection = connection;
		this.tableName = tableName;
		this.updatedColAndStreamNamesJsonArr = updatedColAndStreamNamesJsonArr;
		this.columnsArr = columnsArr;
		startRow = (String) (((JSONArray) (columnsArr.get(1)))
				.get(0));
		startColumn = (String) (((JSONArray) (columnsArr.get(2)))
				.get(0));
		readCSVFile(strFilePath);
	}

	public void insertRowInDB(String saveRow) {
		try {
			PreparedStatement ps = connection.prepareStatement("INSERT INTO `"
					+ tableName + "` VALUES (" + saveRow + ")");
			int status = ps.executeUpdate();
			System.out.println(status);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void main(String[] args) {
		String strFilePath = "/Users/rianddy/Documents/precip_output_14.csv";
		BigCsvUpload objRCFD = new BigCsvUpload(strFilePath);
	}
}
