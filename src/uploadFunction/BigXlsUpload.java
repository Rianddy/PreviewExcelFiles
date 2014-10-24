package uploadFunction;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import org.apache.poi.hssf.eventusermodel.EventWorkbookBuilder.SheetRecordCollectingListener;
import org.apache.poi.hssf.eventusermodel.FormatTrackingHSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.eventusermodel.MissingRecordAwareHSSFListener;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.BoolErrRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NoteRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.RKRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * A XLS -> CSV processor, that uses the MissingRecordAware EventModel code to
 * ensure it outputs all columns and rows.
 * 
 * @author Nick Burch
 * 
 *         I achieved preview functions based on previous author's work so that we
 *         can preview huge xls excel file's data within limited memory. And then 
 *         send those data back with JSON format.
 * 
 * @author Rianddy
 */
public class BigXlsUpload implements HSSFListener {
	private int minColumns;
	private POIFSFileSystem fs;
	private PrintStream output;

	private int lastRowNumber;
	private int lastColumnNumber;

	/** Should we output the formula, or the value it has? */
	private boolean outputFormulaValues = true;

	/** For parsing Formulas */
	private SheetRecordCollectingListener workbookBuildingListener;
	private HSSFWorkbook stubWorkbook;

	// Records we pick up as we process
	private SSTRecord sstRecord;
	private FormatTrackingHSSFListener formatListener;

	/** So we known which sheet we're on */
	@SuppressWarnings("unused")
	private int sheetIndex = -1;
	private BoundSheetRecord[] orderedBSRs;
	@SuppressWarnings("rawtypes")
	private ArrayList boundSheetRecords = new ArrayList();

	// For handling formulas with string results
	private int nextRow;
	private int nextColumn;

	// store the type of each column
	private String[][] cellType = new String[100][10000];
	// store the sheet name of the xls file
	private String[] sheetName = new String[100];
	private String[] previewJson = new String[1000000];
	private int maxRowPreview = 0;
	private boolean outputNextStringRecord;

	/**
	 * Creates a new XLS -> CSV converter
	 * 
	 * @param fs
	 *            The POIFSFileSystem to process
	 * @param output
	 *            The PrintStream to output the CSV to
	 * @param minColumns
	 *            The minimum number of columns to output, or -1 for no minimum
	 */
	public BigXlsUpload(final POIFSFileSystem fs, final PrintStream output,
			final int minColumns) {
		this.fs = fs;
		this.output = output;
		this.minColumns = minColumns;
	}

	/**
	 * Creates a new XLS -> CSV converter
	 * 
	 * @param filename
	 *            The file to process
	 * @param minColumns
	 *            The minimum number of columns to output, or -1 for no minimum
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public BigXlsUpload(final FileInputStream filename, final int minColumns)
			throws IOException, FileNotFoundException {
		this(new POIFSFileSystem(new BufferedInputStream(filename)),
				System.out, minColumns);
	}

	/**
	 * Initiates the processing of the XLS file to CSV
	 */
	public void process() throws IOException {
		final MissingRecordAwareHSSFListener listener = new MissingRecordAwareHSSFListener(
				this);
		formatListener = new FormatTrackingHSSFListener(listener);

		final HSSFEventFactory factory = new HSSFEventFactory();
		final HSSFRequest request = new HSSFRequest();

		if (outputFormulaValues) {
			request.addListenerForAllRecords(formatListener);
		} else {
			workbookBuildingListener = new SheetRecordCollectingListener(
					formatListener);
			request.addListenerForAllRecords(workbookBuildingListener);
		}

		factory.processWorkbookEvents(request, fs);
	}

	public String[] GetPreviewJson() {
		return this.previewJson;
	}

	public String GetSuggestColumnType() {
		String jsonSuggestColumnType = "";
		for (int i = 0; i < cellType.length; i++) {
			if (cellType[i] == null)
				break;
			else {
				jsonSuggestColumnType += cellType[i];
			}
		}
		return jsonSuggestColumnType;
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
		for (i = 0; i < (cellType[sheetIndex].length - 1); i++) {
			if (cellType[sheetIndex][i] == null)
				break;
			else
				jsonColumnType = jsonColumnType + cellType[sheetIndex][i]
						+ "\",\"";
		}
		if (!jsonColumnType.equals("[\""))
			jsonColumnType = jsonColumnType.substring(0,
					jsonColumnType.length() - 2)
					+ "]";
		else
			jsonColumnType = jsonColumnType.substring(0,
					jsonColumnType.length() - 1)
					+ "]";
		return jsonColumnType;
	}

	/**
	 * Main HSSFListener method, processes events, and outputs the CSV as the
	 * file is processed.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void processRecord(final Record record) {
		if (maxRowPreview < 21 || (record.getSid() == BOFRecord.sid)) {
			int thisRow = -1;
			int thisColumn = -1;
			String thisStr = null;
			switch (record.getSid()) {
			case BoundSheetRecord.sid:
				boundSheetRecords.add(record);
				break;
			case BOFRecord.sid:
				final BOFRecord br = (BOFRecord) record;
				maxRowPreview = 0;
				if (br.getType() == BOFRecord.TYPE_WORKSHEET) {
					// Create sub workbook if required
					if ((workbookBuildingListener != null)
							&& (stubWorkbook == null)) {
						stubWorkbook = workbookBuildingListener
								.getStubHSSFWorkbook();
					}

					// Output the worksheet name
					// Works by ordering the BSRs by the location of
					// their BOFRecords, and then knowing that we
					// process BOFRecords in byte offset order
					sheetIndex++;
					if (orderedBSRs == null) {
						orderedBSRs = BoundSheetRecord
								.orderByBofPosition(boundSheetRecords);
					}
					// uncomment the below if you want to output the worksheet
					// name
					output.println();
					output.println(orderedBSRs[sheetIndex].getSheetname()
							+ " [" + (sheetIndex + 1) + "]:");
					sheetName[sheetIndex] = orderedBSRs[sheetIndex]
							.getSheetname();
					previewJson[sheetIndex] = "[";
				}
				break;

			case SSTRecord.sid:
				sstRecord = (SSTRecord) record;
				break;

			case BlankRecord.sid:
				final BlankRecord brec = (BlankRecord) record;

				thisRow = brec.getRow();
				thisColumn = brec.getColumn();
				thisStr = "";
				break;
			case BoolErrRecord.sid:
				final BoolErrRecord berec = (BoolErrRecord) record;

				thisRow = berec.getRow();
				thisColumn = berec.getColumn();
				thisStr = "";
				break;

			case FormulaRecord.sid:
				final FormulaRecord frec = (FormulaRecord) record;

				thisRow = frec.getRow();
				thisColumn = frec.getColumn();

				if (outputFormulaValues) {
					if (Double.isNaN(frec.getValue())) {
						// Formula result is a string
						// This is stored in the next record
						outputNextStringRecord = true;
						nextRow = frec.getRow();
						nextColumn = frec.getColumn();
					} else {
						thisStr = formatListener.formatNumberDateCell(frec);
					}
				} else {
					thisStr = '"' + HSSFFormulaParser.toFormulaString(
							stubWorkbook, frec.getParsedExpression()) + '"';
				}
				break;
			case StringRecord.sid:
				if (outputNextStringRecord) {
					// String for formula
					final StringRecord srec = (StringRecord) record;
					thisStr = srec.getString();
					thisRow = nextRow;
					thisColumn = nextColumn;
					outputNextStringRecord = false;
				}
				break;

			case LabelRecord.sid:
				final LabelRecord lrec = (LabelRecord) record;

				thisRow = lrec.getRow();
				thisColumn = lrec.getColumn();
				thisStr = '"' + lrec.getValue() + '"';
				break;
			case LabelSSTRecord.sid:
				final LabelSSTRecord lsrec = (LabelSSTRecord) record;

				thisRow = lsrec.getRow();
				thisColumn = lsrec.getColumn();
				if (sstRecord == null) {
					thisStr = '"' + "(No SST Record, can't identify string)" + '"';
				} else {
					// thisStr = '"' +
					// sstRecord.getString(lsrec.getSSTIndex()).toString() +
					// '"'+"String";
					thisStr = sstRecord.getString(lsrec.getSSTIndex())
							.toString();
					if (thisRow == 1) {
						cellType[sheetIndex][thisColumn] = "String";
					} else if (thisRow > 1
							&& !cellType[sheetIndex][thisColumn]
									.equals("String")) {
						cellType[sheetIndex][thisColumn] = "String";
					}
				}
				break;
			case NoteRecord.sid:
				final NoteRecord nrec = (NoteRecord) record;

				thisRow = nrec.getRow();
				thisColumn = nrec.getColumn();
				// TODO: Find object to match nrec.getShapeId()
				thisStr = '"' + "(TODO)" + '"';
				break;
			case NumberRecord.sid:
				final NumberRecord numrec = (NumberRecord) record;

				thisRow = numrec.getRow();
				thisColumn = numrec.getColumn();

				// Format
				thisStr = formatListener.formatNumberDateCell(numrec);
				if (thisRow == 1) {
					cellType[sheetIndex][thisColumn] = "Number";
				} else if (thisRow > 1
						&& !cellType[sheetIndex][thisColumn].equals("Number")) {
					cellType[sheetIndex][thisColumn] = "String";
				}
				break;
			case RKRecord.sid:
				final RKRecord rkrec = (RKRecord) record;

				thisRow = rkrec.getRow();
				thisColumn = rkrec.getColumn();
				thisStr = '"' + "(TODO)" + '"';
				break;
			default:
				break;
			}

			// Handle new row
			if ((thisRow != -1) && (thisRow != lastRowNumber)) {
				lastColumnNumber = -1;
			}

			// Handle missing column
			if (record instanceof MissingCellDummyRecord) {
				final MissingCellDummyRecord mc = (MissingCellDummyRecord) record;
				thisRow = mc.getRow();
				thisColumn = mc.getColumn();
				thisStr = "";
			}

			// If we got something to print out, do so
			if (thisStr != null) {
				if (thisColumn > 0) {
					output.print(',');
					previewJson[sheetIndex] = previewJson[sheetIndex] + ",\""
							+ thisStr + "\"";
				} else
					previewJson[sheetIndex] = previewJson[sheetIndex] + "["
							+ "\"" + thisStr + "\"";

				output.print(thisStr);
			}
			// Update column and row count
			if (thisRow > -1) {
				lastRowNumber = thisRow;
			}
			if (thisColumn > -1)
				lastColumnNumber = thisColumn;

			// Handle end of row
			if (record instanceof LastCellOfRowDummyRecord) {
				// Print out any missing commas if needed
				if (minColumns > 0) {
					// Columns are 0 based
					if (lastColumnNumber == -1) {
						lastColumnNumber = 0;
					}
					for (int i = lastColumnNumber; i < (minColumns); i++) {
						output.print(',');
					}
				}
				// We're onto a new row
				lastColumnNumber = -1;
				previewJson[sheetIndex] = previewJson[sheetIndex] + "],";
				maxRowPreview++;
				// End the row
				output.println();
			}
		}
	}

	public static void main(final String[] args) throws Exception {
		int minColumns = -1;
		FileInputStream fileInputStream = new FileInputStream(
				"/Users/rianddy/Documents/testXls.xls");
		final BigXlsUpload xls2csv = new BigXlsUpload(fileInputStream,
				minColumns);
		xls2csv.process();
	}
}