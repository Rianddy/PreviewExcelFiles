package webService;

import java.util.ArrayList;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import model.UploadManager;
import com.google.gson.Gson;
import dto.XlsObjects;
import javax.servlet.http.HttpServletRequest;

@Path("/Preview")
public class BigXlsUploadService {

	@GET
	@Path("/PreviewXls")
	@Produces("application/json")
	public String XlsPreview(@Context HttpServletRequest request) {
		String sid = request.getParameter("sid");
		System.out.println(sid);
		String function = request.getParameter("function");
		if (function == null) {
			String xlsPreviewData = "";
			try {
				ArrayList<XlsObjects> xlsData = null;
				UploadManager uploadManager = new UploadManager();
				xlsData = uploadManager.GetXls(sid);
				for (int i = 0; i < xlsData.size(); i++) {
					if (i == 0)
						xlsPreviewData = "{";
					xlsPreviewData = xlsPreviewData + "\""
							+ xlsData.get(i).getSheetName() + "\":"
							+ xlsData.get(i).getPreviewJson();
					if (i != (xlsData.size() - 1))
						xlsPreviewData = xlsPreviewData + ",";
					else
						xlsPreviewData = xlsPreviewData + "}";
				}
				System.out.print(xlsPreviewData);
				// Gson gson = new Gson();
				// System.out.println(gson.toJson(xlsData));
				// xlsPreviewData = gson.toJson(xlsData);
			} catch (Exception e) {
				System.out.println("error");
			}
			return xlsPreviewData;
		} else {
			String xlsPreviewData = "";
			try {
				ArrayList<XlsObjects> xlsData = null;
				UploadManager uploadManager = new UploadManager();
				xlsData = uploadManager.GetXls(sid);
				for (int i = 0; i < xlsData.size(); i++) {
					if (i == 0)
						xlsPreviewData = "{\"GetSheets\":{\"filename\":\""
								+ xlsData.get(i).getFileName()
								+ "\",\"worksheets\":[";// {\"isSuccessful\":\"true\",\"data\":[

					if (i != (xlsData.size() - 1))
						xlsPreviewData = xlsPreviewData + "\""
								+ xlsData.get(i).getSheetName() + "\",";
					else
						xlsPreviewData = xlsPreviewData + "\""
								+ xlsData.get(i).getSheetName() + "\"" + "]},";// ]}
				}
				for (int i = 0; i < xlsData.size(); i++) {
					if (i == 0)
						xlsPreviewData = xlsPreviewData + "\"Preview\":{";
					xlsPreviewData = xlsPreviewData + "\""
							+ xlsData.get(i).getSheetName() + "\":"
							+ xlsData.get(i).getPreviewJson();
					if (i != (xlsData.size() - 1))
						xlsPreviewData = xlsPreviewData + ",";
					else
						xlsPreviewData = xlsPreviewData + "},";
				}
				for (int i = 0; i < xlsData.size(); i++) {
					if (i == 0){
						if (i != (xlsData.size() - 1))
							xlsPreviewData = xlsPreviewData + "\"ColumnType\":["
									+ xlsData.get(i).getColumnType() + ",";
						else
							xlsPreviewData = xlsPreviewData + "\"ColumnType\":["
									+ xlsData.get(i).getColumnType() + "]}";
						
					}	
					else {
						if (i != (xlsData.size() - 1))
							xlsPreviewData = xlsPreviewData
									+ xlsData.get(i).getColumnType() + ",";
						else
							xlsPreviewData = xlsPreviewData
									+ xlsData.get(i).getColumnType() + "]}";
					}
				}
				System.out.println(xlsPreviewData);
			} catch (Exception e) {
				System.out.println("error");
			}
			return xlsPreviewData;
		}
	}
}
