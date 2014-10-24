package uploadFunction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import net.sf.json.JSONArray;

/**
 * A rudimentary XLSX -> CSV processor modeled on the POI sample program
 * XLS2CSVmra by Nick Burch from the package
 * org.apache.poi.hssf.eventusermodel.examples. Unlike the HSSF version, this
 * one completely ignores missing rows.
 * <p/>
 * Data sheets are read using a SAX parser to keep the memory footprint
 * relatively small, so this should be able to read enormous workbooks. The
 * styles table and the shared-string table must be kept in memory. The standard
 * POI styles table class is used, but a custom (read-only) class is used for
 * the shared string table because the standard POI SharedStringsTable grows
 * very quickly with the number of unique strings.
 * <p/>
 * Thanks to Eric Smith for a patch that fixes a problem triggered by cells with
 * multiple "t" elements, which is how Excel represents different formats (e.g.,
 * one word plain and one word bold).
 * 
 * @author Chris Lott
 * 
 *         I achieved saving functions based on previous author's work so that
 *         we can save the data of huge xls excel file within limited memory
 *         according to user's requirements. For example the user can specify
 *         which column or which row to start.
 * 
 * @author rianddy
 */
public class SaveXlsx {

	/**
	 * The type of the data value is indicated by an attribute on the cell. The
	 * value is usually in a "v" element within the cell.
	 */
	enum xssfDataType {
		BOOL, ERROR, FORMULA, INLINESTR, SSTINDEX, NUMBER,
	}

	String[][] columnType = new String[100][10000];
	private String[] previewJson = new String[1000000];
	private String[] sheetName = new String[100];
	private int sheetIndex = -1;
	private int maxRowPreview = 0;
	private Connection connection;
	private String tableName;
	String updatedColAndStreamNamesJson;
	String columnNames;

	/**
	 * Derived from http://poi.apache.org/spreadsheet/how-to.html#xssf_sax_api
	 * <p/>
	 * Also see Standard ECMA-376, 1st edition, part 4, pages 1928ff, at
	 * http://www.ecma-international.org/publications/standards/Ecma-376.htm
	 * <p/>
	 * A web-friendly version is http://openiso.org/Ecma/376/Part4
	 */

	class MyXSSFSheetHandler extends DefaultHandler {

		public int twentyRows = 0;
		public boolean same = true;
		public int currentColumn = 0;
		/**
		 * Table with styles
		 */
		private StylesTable stylesTable;

		/**
		 * Table with unique strings
		 */
		private ReadOnlySharedStringsTable sharedStringsTable;

		/**
		 * Destination for data
		 */
		private final PrintStream output;

		/**
		 * Number of columns to read starting with leftmost
		 */
		private final int minColumnCount;

		// Set when V start element is seen
		private boolean vIsOpen;

		// Set when cell start element is seen;
		// used when cell close element is seen.
		private xssfDataType nextDataType;

		// Used to format numeric cell values.
		private short formatIndex;
		private String formatString;
		private final DataFormatter formatter;

		private int thisColumn = -1;
		// The last column printed to the output stream
		private int lastColumnNumber = -1;

		// Gathers characters as they are seen.
		private StringBuffer value;
		// judge which column to store

		public String saveRow = "";

		/**
		 * Accepts objects needed while parsing.
		 * 
		 * @param styles
		 *            Table of styles
		 * @param strings
		 *            Table of shared strings
		 * @param cols
		 *            Minimum number of columns to show
		 * @param target
		 *            Sink for output
		 */
		public MyXSSFSheetHandler(StylesTable styles,
				ReadOnlySharedStringsTable strings, int cols, PrintStream target) {
			this.stylesTable = styles;
			this.sharedStringsTable = strings;
			this.minColumnCount = cols;
			this.output = target;
			this.value = new StringBuffer();
			this.nextDataType = xssfDataType.NUMBER;
			this.formatter = new DataFormatter();
			// twentyRows=twentyRows+1;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
		 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */

		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {

			if ("inlineStr".equals(name) || "v".equals(name)) {
				vIsOpen = true;
				// Clear contents cache
				value.setLength(0);
			}
			// c => cell
			else if ("c".equals(name)) {
				// Get the cell reference
				String r = attributes.getValue("r");
				int firstDigit = -1;
				for (int c = 0; c < r.length(); ++c) {
					if (Character.isDigit(r.charAt(c))) {
						firstDigit = c;
						break;
					}
				}
				thisColumn = nameToColumn(r.substring(0, firstDigit));

				// Set up defaults.
				this.nextDataType = xssfDataType.NUMBER;
				this.formatIndex = -1;
				this.formatString = null;
				String cellType = attributes.getValue("t");
				String cellStyleStr = attributes.getValue("s");
				if ("b".equals(cellType))
					nextDataType = xssfDataType.BOOL;
				else if ("e".equals(cellType))
					nextDataType = xssfDataType.ERROR;
				else if ("inlineStr".equals(cellType))
					nextDataType = xssfDataType.INLINESTR;
				else if ("s".equals(cellType))
					nextDataType = xssfDataType.SSTINDEX;
				else if ("str".equals(cellType))
					nextDataType = xssfDataType.FORMULA;
				else if (cellStyleStr != null) {
					// It's a number, but almost certainly one
					// with a special style or format
					int styleIndex = Integer.parseInt(cellStyleStr);
					XSSFCellStyle style = stylesTable.getStyleAt(styleIndex);
					this.formatIndex = style.getDataFormat();
					this.formatString = style.getDataFormatString();
					if (this.formatString == null)
						this.formatString = BuiltinFormats
								.getBuiltinFormat(this.formatIndex);
				}
			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
		 * java.lang.String, java.lang.String)
		 */
		public void endElement(String uri, String localName, String name)
				throws SAXException {
			String thisStr = null;
			// v => contents of a cell
			if ("v".equals(name)) {
				// Process the value contents as required.
				// Do now, as characters() may be called more than once
				switch (nextDataType) {

				case BOOL:
					char first = value.charAt(0);
					thisStr = first == '0' ? "FALSE" : "TRUE";
					break;

				case ERROR:
					thisStr = "\"ERROR:" + value.toString() + '"';
					break;

				case FORMULA:
					// A formula could result in a string value,
					// so always add double-quote characters.
					thisStr = '"' + value.toString() + '"';
					break;

				case INLINESTR:
					// TODO: have seen an example of this, so it's untested.
					XSSFRichTextString rtsi = new XSSFRichTextString(
							value.toString());
					thisStr = '"' + rtsi.toString() + '"';
					break;

				case SSTINDEX:
					String sstIndex = value.toString();
					try {
						int idx = Integer.parseInt(sstIndex);
						XSSFRichTextString rtss = new XSSFRichTextString(
								sharedStringsTable.getEntryAt(idx));
						thisStr = rtss.toString();
					} catch (NumberFormatException ex) {
						output.println("Failed to parse SST index '" + sstIndex
								+ "': " + ex.toString());
					}
					break;

				case NUMBER:
					String n = value.toString();
					if (this.formatString != null)
						thisStr = formatter.formatRawCellContents(
								Double.parseDouble(n), this.formatIndex,
								this.formatString);
					else
						thisStr = n;
					break;

				default:
					thisStr = "(TODO: Unexpected type: " + nextDataType + ")";
					break;
				}

				// Output after we've seen the string contents
				// Emit commas for any fields that were missing on this row
				if (lastColumnNumber == -1) {
					lastColumnNumber = 0;
				}
				for (int i = lastColumnNumber; i < thisColumn; ++i) {
				}
				if (thisColumn >= (Integer.parseInt(startColumn) - 1)
						&& thisRow == (Integer.parseInt(startRow) - 1)) {
					if (updatedColAndStreamNamesJsonArr.contains(thisStr)) {
						updatedColAndStreamNamesJsonArr
								.remove(updatedColAndStreamNamesJsonArrLength);
						updatedColAndStreamNamesJsonArr.add(
								updatedColAndStreamNamesJsonArrLength,
								thisColumn);
						updatedColAndStreamNamesJsonArrLength++;// .get(0)=thisColumn;
						System.out.println(updatedColAndStreamNamesJsonArr);
					}
				}
				if (thisColumn >= (Integer.parseInt(startColumn) - 1)
						&& thisRow >= (Integer.parseInt(startRow))
						&& updatedColAndStreamNamesJsonArr.contains(thisColumn)) {
					if (thisColumn == (Integer.parseInt(startColumn) - 1)) {
						saveRow += "'" + thisStr + "'";
						output.print("\"'" + thisStr + "'");
					} else {
						saveRow += "," + "'" + thisStr + "'";
						output.print(",'" + thisStr + "'");
					}

				}
				currentColumn += 1;
				// Update column
				if (thisColumn > -1)
					lastColumnNumber = thisColumn;
			} else if ("row".equals(name)) {
				if (minColumns > 0) {
					if (lastColumnNumber == -1) {
						lastColumnNumber = 0;
					}
					for (int i = lastColumnNumber; i < (this.minColumnCount); i++) {
					}
				}
				if (!saveRow.equals("")) {
					// We're onto a new row
					output.print("\"");
					output.println();
					lastColumnNumber = -1;
					currentColumn = 0;
					thisRow++;
					insertRowInDB(saveRow);
					saveRow = "";
				} else {
					lastColumnNumber = -1;
					currentColumn = 0;
					thisRow++;
				}
			}
		}

		/**
		 * Captures characters only if a suitable element is open. Originally
		 * was just "v"; extended for inlineStr also.
		 */
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (vIsOpen)
				value.append(ch, start, length);
		}

		/**
		 * Converts an Excel column name like "C" to a zero-based index.
		 * 
		 * @param name
		 * @return Index corresponding to the specified name
		 */
		private int nameToColumn(String name) {
			int column = -1;
			for (int i = 0; i < name.length(); ++i) {
				int c = name.charAt(i);
				column = (column + 1) * 26 + c - 'A';
			}
			return column;
		}

	}

	// /////////////////////////////////////

	private OPCPackage xlsxPackage;
	private int minColumns;
	private PrintStream output;
	int thisRow = 0;
	String startRow;
	String startColumn;
	int starSaveDataLocation = 0;
	JSONArray updatedColAndStreamNamesJsonArr;
	int updatedColAndStreamNamesJsonArrLength = 0;
	JSONArray columnsArr;

	/**
	 * Creates a new XLSX -> CSV converter
	 * 
	 * @param pkg
	 *            The XLSX package to process
	 * @param output
	 *            The PrintStream to output the CSV to
	 * @param minColumns
	 *            The minimum number of columns to output, or -1 for no minimum
	 */

	public SaveXlsx(OPCPackage pkg, PrintStream output, int minColumns,
			Connection connection, String tableName,
			JSONArray updatedColAndStreamNamesJsonArr, JSONArray columnsArr) {
		this.xlsxPackage = pkg;
		this.output = output;
		this.minColumns = minColumns;
		this.connection = connection;
		this.tableName = tableName;
		this.updatedColAndStreamNamesJsonArr = updatedColAndStreamNamesJsonArr;
		this.columnsArr = columnsArr;
	}

	/**
	 * Parses and shows the content of one sheet using the specified styles and
	 * shared-strings tables.
	 * 
	 * @param styles
	 * @param strings
	 * @param sheetInputStream
	 */
	public void processSheet(StylesTable styles,
			ReadOnlySharedStringsTable strings, InputStream sheetInputStream)
			throws IOException, ParserConfigurationException, SAXException {

		InputSource sheetSource = new InputSource(sheetInputStream);
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		SAXParser saxParser = saxFactory.newSAXParser();
		XMLReader sheetParser = saxParser.getXMLReader();
		ContentHandler handler = new MyXSSFSheetHandler(styles, strings,
				this.minColumns, this.output);
		sheetParser.setContentHandler(handler);
		sheetParser.parse(sheetSource);
	}

	/**
	 * Initiates the processing of the XLS workbook file to CSV.
	 * 
	 * @throws IOException
	 * @throws OpenXML4JException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public void process() throws IOException, OpenXML4JException,
			ParserConfigurationException, SAXException {

		ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(
				this.xlsxPackage);
		XSSFReader xssfReader = new XSSFReader(this.xlsxPackage);
		StylesTable styles = xssfReader.getStylesTable();
		XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader
				.getSheetsData();
		int index = 0;

		while (iter.hasNext()) {
			sheetIndex++;
			InputStream stream = iter.next();
			String sheetName = iter.getSheetName();
			this.output.println();
			this.output.println(sheetName + " [index=" + index + "]:");
			this.previewJson[sheetIndex] = "[";
			this.sheetName[sheetIndex] = sheetName;
			if (((JSONArray) (columnsArr.get(0))).contains(sheetName)) {
				startRow = (String) (((JSONArray) (columnsArr.get(1)))
						.get(starSaveDataLocation));
				startColumn = (String) (((JSONArray) (columnsArr.get(2)))
						.get(starSaveDataLocation));
				System.out.println(startRow + startColumn);
				maxRowPreview = 0;
				processSheet(styles, strings, stream);
				stream.close();
				starSaveDataLocation++;
			}
			thisRow = 0;
			++index;
		}
	}

	public String[] GetPreviewJson() {
		return this.previewJson;
	}

	public int GetSheetIndex() {
		return this.sheetIndex + 1;
	}

	public String[] GetSheetName() {
		return this.sheetName;
	}

	public String GetColumnType(int sheetIndex) {
		String jsonColumnType = "[\"";
		int i;
		for (i = 0; i < (columnType[sheetIndex].length - 1); i++) {
			if (columnType[sheetIndex][i] == null)
				break;
			else
				jsonColumnType = jsonColumnType + columnType[sheetIndex][i]
						+ "\",\"";
		}
		jsonColumnType = jsonColumnType.substring(0,
				jsonColumnType.length() - 2) + "]";
		return jsonColumnType;
	}

	public void insertRowInDB(String saveRow) {
		try {
			System.out.println();
			System.out.println(saveRow);
			PreparedStatement ps = connection.prepareStatement("INSERT INTO `"
					+ tableName + "` VALUES (" + saveRow + ")");
			int status = ps.executeUpdate();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void main(String[] args) throws Exception {
		File xlsxFile = new File(
				"/Users/rianddy/Documents/precip_output_3.xlsx");
		if (!xlsxFile.exists()) {
			System.err
					.println("Not found or not a file: " + xlsxFile.getPath());
			return;
		}
		int minColumns = -1;
		if (args.length >= 2)
			minColumns = Integer.parseInt(args[1]);

		// The package open is instantaneous, as it should be.
		OPCPackage p = OPCPackage.open(xlsxFile.getPath(), PackageAccess.READ);
		BigXlsxUpload xlsx2csv = new BigXlsxUpload(p, System.out, minColumns);
		xlsx2csv.process();
		for (int i = 0; i < xlsx2csv.GetSheetIndex(); i++) {
			System.out.println(xlsx2csv.GetPreviewJson()[i]);
		}
	}
}