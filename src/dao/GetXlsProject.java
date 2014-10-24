package dao;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;

import dto.XlsObjects;
import uploadFunction.BigXlsUpload;
import uploadFunction.BigXlsxUpload;
import uploadFunction.BigCsvUpload;

public class GetXlsProject {
	String fileName;

	public ArrayList<XlsObjects> GetXls(String sid) throws Exception {
		ArrayList<XlsObjects> xlsObjectArray = new ArrayList<XlsObjects>();
		try {
			switch (JudgeFileType(sid)) {
			case 1:
				FileInputStream fileInputStream = new FileInputStream(sid);
				BigXlsUpload xls = new BigXlsUpload(fileInputStream, -1);
				xls.process();
				for (int i = 0; i < xls.GetSheetIndex(); i++) {
					XlsObjects xlsObject = new XlsObjects();
					if(xls.GetPreviewJson()[i].length()==1)
						xlsObject.setPreviewJson("[]");
					else
						xlsObject
								.setPreviewJson((xls.GetPreviewJson()[i].substring(
										0, xls.GetPreviewJson()[i].length() - 1))
										+ "]");// +xls.GetSuggestColumnType());
					xlsObject.setSheetName(xls.GetSheetName()[i]);
					xlsObject.setColumnType(xls.GetColumnType(i));
					System.out.println(xls.GetPreviewJson()[i]);
					System.out.println(xls.GetColumnType(i));
					xlsObjectArray.add(xlsObject);
				}
				return xlsObjectArray;
			case 2:
				File xlsxFile = new File(sid);
				OPCPackage p = OPCPackage.open(xlsxFile.getPath(),
						PackageAccess.READ);
				BigXlsxUpload xlsx = new BigXlsxUpload(p, System.out, -1);
				xlsx.process();
				for (int i = 0; i < xlsx.GetSheetIndex(); i++) {
					XlsObjects xlsxObject = new XlsObjects();
					if(xlsx.GetPreviewJson()[i].length()==1)
						xlsxObject.setPreviewJson("[]");
					else
						xlsxObject
								.setPreviewJson((xlsx.GetPreviewJson()[i].substring(
										0, xlsx.GetPreviewJson()[i].length() - 1))
										+ "]");
					xlsxObject.setSheetName(xlsx.GetSheetName()[i]);
					xlsxObject.setColumnType(xlsx.GetColumnType(i));
					System.out.println(xlsx.GetPreviewJson());
					xlsObjectArray.add(xlsxObject);
				}
				return xlsObjectArray;
			case 3:
				BigCsvUpload csv = new BigCsvUpload(sid);
				XlsObjects xlsxObject = new XlsObjects();
				xlsxObject
						.setPreviewJson((csv.getPrviewJson().substring(
								0, csv.getPrviewJson().length() - 1))
								+ "]");
				xlsxObject.setSheetName("FileContent");
				xlsxObject.setColumnType(csv.getColumnType());
				System.out.println(csv.getPrviewJson());
				xlsObjectArray.add(xlsxObject);
				return xlsObjectArray;
			default:
				return xlsObjectArray;
			}

		} catch (Exception e) {
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
}
