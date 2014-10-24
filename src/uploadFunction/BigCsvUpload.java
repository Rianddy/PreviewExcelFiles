package uploadFunction;

import java.io.FileReader;
import au.com.bytecode.opencsv.CSVReader;

/**
 * Preview large CSV files during uploading process.
 * 
 * @author rianddy
 * 
 */
public class BigCsvUpload {
	String previewJson = "[";
	String[] columnType = new String[10000];
	int[] judgeSame = new int[10000];

	public void readCSVFile(String pstrFileName) {
		CSVReader reader;
		String nextLine[] = new String[1000];
		FileReader fr = null;
		int i = 0;// control to show how many rows
		try {
			fr = new FileReader(pstrFileName);
			reader = new CSVReader(fr);
			while ((nextLine = reader.readNext()) != null) {
				if (i == 20)
					break;
				for (int nIndex = 0; nIndex < nextLine.length; nIndex++) {
					if (i == 0) {
						columnType[nIndex] = getType(nextLine[nIndex]);
						judgeSame[nIndex] = 1;
					} else {
						if ((!columnType[nIndex]
								.equals(getType(nextLine[nIndex])))
								&& judgeSame[nIndex] == 1) {
							columnType[nIndex] = "string";
							judgeSame[nIndex] = 0;
						}
					}
					if (nIndex == 0) {
						if (nIndex != (nextLine.length - 1))
							previewJson = previewJson + "[\""
									+ nextLine[nIndex] + "\"";
						else
							previewJson = previewJson + "[\""
									+ nextLine[nIndex] + "\"]";
					} else if (nIndex == (nextLine.length - 1))
						previewJson = previewJson + ",\"" + nextLine[nIndex]
								+ "\"],";
					else
						previewJson = previewJson + ",\"" + nextLine[nIndex]
								+ "\"";
					System.out.print(nextLine[nIndex] + ",");
				}
				System.out.println();
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static <T> String getType(T t) {
		if (t instanceof String) {
			return "string";
		} else if (t instanceof Integer) {
			return "number";
		} else {
			return "string";
		}
	}

	public String getPrviewJson() {
		return previewJson;
	}

	public String getColumnType() {
		String jsonColumnType = "[\"";
		int i;
		for (i = 0; i < (columnType.length - 1); i++) {
			if (columnType[i] == null)
				break;
			else
				jsonColumnType = jsonColumnType + columnType[i] + "\",\"";
		}
		jsonColumnType = jsonColumnType.substring(0,
				jsonColumnType.length() - 2) + "]";
		return jsonColumnType;
	}

	public BigCsvUpload(String strFilePath) {
		readCSVFile(strFilePath);
	}

	public static void main(String[] args) {
		String strFilePath = "/Users/rianddy/Documents/precip_output_14.csv";
		BigCsvUpload objRCFD = new BigCsvUpload(strFilePath);
	}
}
