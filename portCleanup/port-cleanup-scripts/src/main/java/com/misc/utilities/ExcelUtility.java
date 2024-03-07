package com.misc.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.act.utilities.Rubicon;
import com.asri.utilities.Asri;

public class ExcelUtility {

	public static XSSFWorkbook workbook;
	public static XSSFWorkbook workbook1;
	public static Sheet sheet;
	public static XSSFRow row;
	public static XSSFRow row1;
	public static XSSFFont defaultFont;
	public static XSSFFont defaultFont1;
	Asri asri = new Asri();

	@SuppressWarnings("unchecked")
	public void fetchAndStoreUnisOnDevices() throws FileNotFoundException, IOException {
		String filePath = System.getProperty("user.dir") + "\\uniList_On_Devices.xlsx";
		workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Sheet1");
		createHeaderRowForUniListOnDevices(sheet);
		Rubicon rubicon = new Rubicon();
		Asri asri = new Asri();

		ArrayList<ArrayList> uniLists = rubicon.fetchUnisFromAllDevices();

		for (ArrayList<String> uniList : uniLists) {
			String device = uniList.get(0);
			if (uniList.size() > 1) {
				System.out.println("\n#-------------------------------------------------------------------#");
				System.out.println("UNI's found on " + device);
				System.out.println("#-------------------------------------------------------------------#");
				for (int i = 1; i < uniList.size(); i++) {
//					System.out.println(uniList.get(i)+"\t"+asri.getServiceEnvironment(uniList.get(i)));
//					System.out.println(uniList.get(i));
				}
				System.out.println("#-------------------------------------------------------------------#\n\n");
			} else {
				System.out.println("No UNI's found on " + device);
			}
		}

//		ArrayList labbrmcow2212 = uniLists.get(0);
//		ArrayList labbrmcow2213 = uniLists.get(1);
		ArrayList labbrmcow2310 = uniLists.get(2);
		ArrayList labbrmcow2311 = uniLists.get(3);
		ArrayList labbrmcoyp301 = uniLists.get(4);
		ArrayList labbrmcoyp305 = uniLists.get(5);
		ArrayList labbrmcoyj302 = uniLists.get(6);
		ArrayList labbrmcoyj304 = uniLists.get(7);
		ArrayList labbrmcozn301 = uniLists.get(8);

		int colCount = 0;

		for (int i = 1; i < 40; i++) {
			row = (XSSFRow) sheet.createRow(i);

			

	      
	        	
	        	if(labbrmcow2310.size()>i) {
	        		Cell cell = row.createCell(2);
	        		cell.setCellValue(labbrmcow2310.get(i).toString());
	        	}
	        	
	        	if(labbrmcow2311.size()>i) {
	        		Cell cell = row.createCell(3);
	        		cell.setCellValue(labbrmcow2311.get(i).toString());
	        	}
	        	
	        	if(labbrmcoyp301.size()>i) {
	        		Cell cell = row.createCell(4);
	        		cell.setCellValue(labbrmcoyp301.get(i).toString());
	        	}
	        	
	        	
	        	if(labbrmcoyp305.size()>i) {
	        		Cell cell = row.createCell(5);
	        		cell.setCellValue(labbrmcoyp305.get(i).toString());
	        	}
	        	
	        	if(labbrmcoyj302.size()>i) {
	        		Cell cell = row.createCell(6);
	        		cell.setCellValue(labbrmcoyj302.get(i).toString());
	        	}
	        	
	        	if(labbrmcoyj304.size()>i) {
	        		Cell cell = row.createCell(7);
	        		cell.setCellValue(labbrmcoyj304.get(i).toString());
	        	}
	        	
	        	if(labbrmcozn301.size()>i) {
	        		Cell cell = row.createCell(8);
	        		cell.setCellValue(labbrmcozn301.get(i).toString());
	        	}
		}

		int noOfColumns = sheet.getRow(0).getPhysicalNumberOfCells();
		for (int i = 0; i < noOfColumns; i++) {
			sheet.autoSizeColumn(i);
		}

		try (FileOutputStream fileOut = new FileOutputStream(new File(filePath))) {
			workbook.write(fileOut);
		}
		workbook.close();
	}

	public void fetchAndStoreUniDeltailsFromUniDataSource() {
		try {

			// reading excel file
			String filePath = System.getProperty("user.dir") + "\\uniList_On_Devices.xlsx";
			File file = new File(filePath);
			FileInputStream fis = new FileInputStream(file);
			XSSFWorkbook wb = new XSSFWorkbook(fis);
			XSSFSheet sheet = wb.getSheetAt(0);
			
			
			//
			String uniDetailsFilePath = System.getProperty("user.dir") + "\\uniDetails.xlsx";
			workbook1 = new XSSFWorkbook();
			Sheet uniDetailsSheet = workbook1.createSheet("uniDetails");
			createHeaderRowForUniDetails(uniDetailsSheet);
			Rubicon rubicon = new Rubicon();
			Asri asri = new Asri();

			// Get the number of rows and columns
			int numRows = sheet.getLastRowNum() + 1;
			int numCols = sheet.getRow(0).getLastCellNum();

			// Read the data column-wise
			int entries = 1;
			for (int col = 0; col < numCols; col++) {
				for (int row = 1; row < numRows; row++) {
					Cell cell = sheet.getRow(row).getCell(col);
					
					if (!(cell == null)) {
						row1 = (XSSFRow) uniDetailsSheet.createRow(entries);
						String service = cell.getStringCellValue();
						
						Cell wCell = row1.createCell(1);
						wCell.setCellValue(service);
						
						ArrayList<String> envList = asri.getServiceEnvironment(service);
						String environment = null;
						if(envList.size()>0) {
							
							for (String env : envList) {
								environment = env;
								ArrayList<String> parentServices = new ArrayList<String>();
//								ArrayList<String> parentServices = asri.getParentServices(service, env);
								if (parentServices.size() > 0) {
									for (String parent : parentServices) {
//										row1 = (XSSFRow) uniDetailsSheet.createRow(entries);
										ArrayList<String> resTypeList = asri.getServiceType(parent, env);
										String resTypeFormatted = null;
										if (resTypeList.size() > 0) {
											resTypeFormatted = resTypeList.get(0).replaceAll("\\s+", "").toLowerCase();
											
											Cell envCell = row1.createCell(0);
											envCell.setCellValue(environment);

											Cell uniService = row1.createCell(1);
											uniService.setCellValue(service);

//											Cell olineService = row1.createCell(2);
//											olineService.setCellValue("null");
//
//											Cell ipvpnService = row1.createCell(3);
//											ipvpnService.setCellValue("null");
//
//											Cell diaService = row1.createCell(4);
//											diaService.setCellValue("null");
//
//											Cell ovcService = row1.createCell(5);
//											ovcService.setCellValue("null");
//
//											Cell mpevcService = row1.createCell(6);
//											mpevcService.setCellValue("null");
//
//											Cell evcService = row1.createCell(7);
//											evcService.setCellValue("null");
//
//											Cell ovcServiceEp = row1.createCell(8);
//											ovcServiceEp.setCellValue("null");
//											
//											Cell evcServiceEp = row1.createCell(9);
//											evcServiceEp.setCellValue("null");
//											
//											Cell mpevcServiceEp = row1.createCell(10);
//											mpevcServiceEp.setCellValue("null");
											
											
											if(resTypeFormatted.equalsIgnoreCase("oline")) {
												Cell olineService = row1.createCell(2);
												olineService.setCellValue(parent);
											}else if(resTypeFormatted.equalsIgnoreCase("internetaccess")) {
												Cell diaService = row1.createCell(4);
												diaService.setCellValue(parent);
											}else if(resTypeFormatted.equalsIgnoreCase("ovc")) {
												Cell ovcService = row1.createCell(5);
												ovcService.setCellValue(parent);
											}else if(resTypeFormatted.equalsIgnoreCase("evc")) {
												Cell evcService = row1.createCell(7);
												evcService.setCellValue(parent);
											}else if(resTypeFormatted.equalsIgnoreCase("mp-evc")) {
												Cell mpevcService = row1.createCell(6);
												mpevcService.setCellValue(parent);
											}else if(resTypeFormatted.equalsIgnoreCase("ovcendPoint")) {
												Cell ovcServiceEp = row1.createCell(8);
												ovcServiceEp.setCellValue(parent);
											}else if(resTypeFormatted.equalsIgnoreCase("evcendPoint")) {
												Cell evcServiceEp = row1.createCell(9);
												evcServiceEp.setCellValue(parent);
											}else if(resTypeFormatted.equalsIgnoreCase("mp-evcendpoint")) {
												Cell mpevcServiceEp = row1.createCell(10);
												mpevcServiceEp.setCellValue(parent);
											}else if(resTypeFormatted.equalsIgnoreCase("ipvpnendpoint")) {
												Cell ipvpnService = row1.createCell(3);
												ipvpnService.setCellValue(parent);
											}
											
											
										}

									entries++;
									}
								}else {//if no parents services
									Cell envCell = row1.createCell(0);
									envCell.setCellValue(environment);

									Cell uniService = row1.createCell(1);
									uniService.setCellValue(service);

//									Cell olineService = row1.createCell(2);
//									olineService.setCellValue("null");
//
//									Cell ipvpnService = row1.createCell(3);
//									ipvpnService.setCellValue("null");
//
//									Cell diaService = row1.createCell(4);
//									diaService.setCellValue("null");
//
//									Cell ovcService = row1.createCell(5);
//									ovcService.setCellValue("null");
//
//									Cell mpevcService = row1.createCell(6);
//									mpevcService.setCellValue("null");
//
//									Cell evcService = row1.createCell(7);
//									evcService.setCellValue("null");
//
//									Cell ovcServiceEp = row1.createCell(8);
//									ovcServiceEp.setCellValue("null");
//									
//									Cell evcServiceEp = row1.createCell(9);
//									evcServiceEp.setCellValue("null");
//									
//									Cell mpevcServiceEp = row1.createCell(10);
//									mpevcServiceEp.setCellValue("null");
								}
							}
						}else {//if envList is empty
							Cell envCell = row1.createCell(0);
							envCell.setCellValue("null");

							Cell uniService = row1.createCell(1);
							uniService.setCellValue(service);

//							Cell olineService = row1.createCell(2);
//							olineService.setCellValue("null");
//
//							Cell ipvpnService = row1.createCell(3);
//							ipvpnService.setCellValue("null");
//
//							Cell diaService = row1.createCell(4);
//							diaService.setCellValue("null");
//
//							Cell ovcService = row1.createCell(5);
//							ovcService.setCellValue("null");
//
//							Cell mpevcService = row1.createCell(6);
//							mpevcService.setCellValue("null");
//
//							Cell evcService = row1.createCell(7);
//							evcService.setCellValue("null");
//
//							Cell ovcServiceEp = row1.createCell(8);
//							ovcServiceEp.setCellValue("null");
//							
//							Cell evcServiceEp = row1.createCell(9);
//							evcServiceEp.setCellValue("null");
//							
//							Cell mpevcServiceEp = row1.createCell(10);
//							mpevcServiceEp.setCellValue("null");
						}

					entries++;
					}

				}
			}

			// Close the read workbook and the file
			wb.close();
			fis.close();
			
			//
			int noOfColumns = uniDetailsSheet.getRow(0).getPhysicalNumberOfCells();
			for (int i = 0; i < noOfColumns; i++) {
				uniDetailsSheet.autoSizeColumn(i);
			}

			try (FileOutputStream fileOut = new FileOutputStream(new File(uniDetailsFilePath))) {
				workbook1.write(fileOut);
			}
			workbook1.close();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static void createHeaderRowForUniListOnDevices(Sheet sheet) {

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		defaultFont = workbook.createFont();
		defaultFont.setFontName("Arial");
		defaultFont.setColor(IndexedColors.DARK_RED.getIndex());
		defaultFont.setBold(true);
		cellStyle.setFont(defaultFont);

		Row row = sheet.createRow(0);
		Cell cellSERVICE = row.createCell(0);
		cellSERVICE.setCellStyle(cellStyle);
		cellSERVICE.setCellValue("LABBRMCOW2212");

		Cell cellENVIRONMENT = row.createCell(1);
		cellENVIRONMENT.setCellStyle(cellStyle);
		cellENVIRONMENT.setCellValue("LABBRMCOW2213");

		Cell cellACT_STATUS = row.createCell(2);
		cellACT_STATUS.setCellStyle(cellStyle);
		cellACT_STATUS.setCellValue("LABBRMCOW2310");

		Cell cellASRI_STATUS = row.createCell(3);
		cellASRI_STATUS.setCellStyle(cellStyle);
		cellASRI_STATUS.setCellValue("LABBRMCOW2311");

		Cell cellIP_STATUS = row.createCell(4);
		cellIP_STATUS.setCellStyle(cellStyle);
		cellIP_STATUS.setCellValue("LABBRMCOYP301");

		Cell cellREQUEST_ID = row.createCell(5);
		cellREQUEST_ID.setCellStyle(cellStyle);
		cellREQUEST_ID.setCellValue("LABBRMCOYP305");

		Cell cellDEACTIVATE_JOB_ID = row.createCell(6);
		cellDEACTIVATE_JOB_ID.setCellStyle(cellStyle);
		cellDEACTIVATE_JOB_ID.setCellValue("LABBRMCOYJ302");

		Cell cellACT_ID = row.createCell(7);
		cellACT_ID.setCellStyle(cellStyle);
		cellACT_ID.setCellValue("LABBRMCOYJ304");

		Cell cellERROR = row.createCell(8);
		cellERROR.setCellStyle(cellStyle);
		cellERROR.setCellValue("LABBRMCOZN301");

	}

	private static void createHeaderRowForUniDetails(Sheet sheet) {

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		defaultFont1 = workbook1.createFont();
		defaultFont1.setFontName("Arial");
		defaultFont1.setColor(IndexedColors.DARK_RED.getIndex());
		defaultFont1.setBold(true);
		cellStyle.setFont(defaultFont1);

		Row row = sheet.createRow(0);

		Cell env = row.createCell(0);
		env.setCellStyle(cellStyle);
		env.setCellValue("ENVIRONMENT");

		Cell uniService = row.createCell(1);
		uniService.setCellStyle(cellStyle);
		uniService.setCellValue("UNI");

		Cell olineService = row.createCell(2);
		olineService.setCellStyle(cellStyle);
		olineService.setCellValue("OLINE");

		Cell ipvpnService = row.createCell(3);
		ipvpnService.setCellStyle(cellStyle);
		ipvpnService.setCellValue("IPVPN_SERVICE");

		Cell cellIP_STATUS = row.createCell(4);
		cellIP_STATUS.setCellStyle(cellStyle);
		cellIP_STATUS.setCellValue("DIA_SERVICE");

		Cell ovcService = row.createCell(5);
		ovcService.setCellStyle(cellStyle);
		ovcService.setCellValue("OVC_SERVICE");

		Cell mpevcService = row.createCell(6);
		mpevcService.setCellStyle(cellStyle);
		mpevcService.setCellValue("MPEVC_SERVICE");

		Cell evcService = row.createCell(7);
		evcService.setCellStyle(cellStyle);
		evcService.setCellValue("EVC_SERVICE");

		Cell ovcServiceEp = row.createCell(8);
		ovcServiceEp.setCellStyle(cellStyle);
		ovcServiceEp.setCellValue("OVC_SERVICE_EP");

		Cell evcServiceEp = row.createCell(9);
		evcServiceEp.setCellStyle(cellStyle);
		evcServiceEp.setCellValue("EVC_SERVICE_EP");

		Cell mpevcServiceEp = row.createCell(10);
		mpevcServiceEp.setCellStyle(cellStyle);
		mpevcServiceEp.setCellValue("MPEVC_SERVICE_EP");

	}

	public static void main(String[] args) throws IOException {

		ExcelUtility eu = new ExcelUtility();
		eu.fetchAndStoreUnisOnDevices();
		eu.fetchAndStoreUniDeltailsFromUniDataSource();

	}
}
