package uploadFunction;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

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
 * 
 *         Get sheets information from excel files based on previous author's
 *         work.
 * 
 * @author Rianddy
 * 
 */
public class GetXlsSheets implements HSSFListener {
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
	private String[] cellType = new String[100];
	// store the sheet name of the xls file
	private String sheetsName = "worksheets:[";
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
	public GetXlsSheets(final POIFSFileSystem fs, final PrintStream output,
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
	public GetXlsSheets(final String filename, final int minColumns)
			throws IOException, FileNotFoundException {
		this(new POIFSFileSystem(new FileInputStream(filename)), System.out,
				minColumns);
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

	public String GetSuggestColumnType() {
		String jsonSuggestColumnType = "";
		for (int i = 0; i < cellType.length; i++) {
			if (cellType[i] == null)
				break;
			else
				jsonSuggestColumnType += cellType[i];
		}
		return jsonSuggestColumnType;
	}

	public int GetSheetIndex() {
		return this.sheetIndex + 1;
	}

	public String GetSheetName() {
		return this.sheetsName;
	}

	/**
	 * Main HSSFListener method, processes events, and outputs the CSV as the
	 * file is processed.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void processRecord(final Record record) {
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
				output.println(orderedBSRs[sheetIndex].getSheetname() + " ["
						+ (sheetIndex + 1) + "]:");
				sheetsName = sheetsName + "\""
						+ orderedBSRs[sheetIndex].getSheetname() + "\",";
				System.out.println(sheetsName);
			}
			break;
		default:
			break;
		}
	}

	public static void main(final String[] args) throws Exception {
		int minColumns = -1;
		final GetXlsSheets xls2csv = new GetXlsSheets(
				"/Users/rianddy/Documents/testXls.xls", minColumns);
		xls2csv.process();
	}
}