package com.cleanup.services;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.act.utilities.Act;
import com.act.utilities.Rubicon;
import com.asri.utilities.Asri;


public class CleanServices {

	
	Asri asri;
	Act act;
	public static void main(String[] args) {
			
		Rubicon rubicon = new Rubicon();
		Asri asri = new Asri();
		Act act = new Act();
//		ArrayList<String> labDevices = rubicon.listLabDevices();
//		for (String device : labDevices) {
//			ArrayList<String> unis = rubicon.fetchUnisFromDevice(device);
//			System.out.println("Device: " + device);
//		
//			for (int i = 1; i < unis.size(); i++) {
////				System.out.println(unis.get(i)+"\t"+asri.getServiceEnvironment(unis.get(i)));
//				System.out.println("+--------------------------------------------------------------------+");
//				ArrayList<String> testEnv = new ArrayList<String>();
//				testEnv = asri.getServiceEnvironment(unis.get(i));
//				System.out.println("UNI:>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+unis.get(i)+"<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
//				if (testEnv.size() == 0) {
//					System.out.println(unis.get(i)+"::No Environment found");
//				} else if (testEnv.size() == 1) {
//					System.out.println(unis.get(i)+"====>"+asri.getServiceType(unis.get(i), testEnv.get(0)) + testEnv.get(0));
//					LinkedHashMap parentServicesMap = asri.getParentServices(unis.get(i), testEnv.get(0));
//					if (parentServicesMap.size() == 0) {
//						ArrayList<String> serviceType =  asri.getServiceType(unis.get(i), testEnv.get(0));
//						if (serviceType.size()>0) {
//							String serviceTypeName = serviceType.get(0);
//							String resName_resType = asri.getReqNameAndReqType(serviceTypeName, testEnv.get(0));
//							String resType = resName_resType.split("_")[1];
//                        }
//					} else {
//						
//					}
//					
//				} else if (testEnv.size() >=2) {
//					for (int j = 0; j < testEnv.size(); j++) {
////						System.out.println(unis.get(i)+"====>"+asri.getServiceType(unis.get(i), testEnv.get(j)) + testEnv.get(j));
//						LinkedHashMap parentServicesMap = asri.getParentServices(unis.get(i), testEnv.get(j));
//						if (parentServicesMap.size() == 0) {
//                            System.out.println(unis.get(i) + " has no parent services in " + testEnv.get(j));
//                        } else {
//                            
//                        }
//						
//						
//					}
//				}
//				
//				
//			}
//		}
		
//		readExcelData();
		
		String service = "CO/KXFN/048637/LUMN";
		String env = "4";
		
		ArrayList<String> envs = new ArrayList<String>();
		envs.add("1");
		envs.add("2");
		envs.add("4");
		
		ArrayList<String> services = new ArrayList<String>();
		LinkedHashMap<String, String> servicesMap = asri.consolidateServices(service, env);
		
		if(servicesMap.size()>0) {
			
			services  = asri.getRearragedServices(servicesMap, env);			
			for (Iterator iterator = services.iterator(); iterator.hasNext();) {
				
				String s = (String) iterator.next();				
				System.out.println("Cleanup started for::"+s);
				
				boolean actCleanUpStatus = act.networkCleanup(s, env);
				if (actCleanUpStatus) {
					System.out.println("Act Cleanup is successful");
					
					//cleaning Ips
					if(s.contains("IRXX")) {
						asri.cleanIp(s);
					}
					boolean asriCleanUpStatus = asri.inventoryCleanUp(s, env);
					if (asriCleanUpStatus) {
						System.out.println("ASRI Cleanup is successful");
					} else {
						System.out.println("ASRI Cleanup is not successful");
						break;
					}
				} else {
					System.out.println("Act Cleanup is not successful");
					break;
				}
				servicesMap = asri.consolidateServices(service, env);
				services  = asri.getRearragedServices(servicesMap, env);
				
			}
		}else {
			System.out.println("Service Not found in Inventory");
			System.out.println("Cleaning up in Network/ACT::"+service);
			
			if(service.contains("IRXX")) {
				asri.cleanIp(service);
			}
			
			for (String eachEnv : envs) {
				boolean actCleanUpStatus = act.networkCleanup(service, eachEnv);
				if (actCleanUpStatus) {
					System.out.println("Act Cleanup is successful");
					boolean asriCleanUpStatus = asri.inventoryCleanUp(service, eachEnv);
					if (asriCleanUpStatus) {
						System.out.println("ASRI Cleanup is successful");
					} else {
						System.out.println("ASRI Cleanup is not successful");
					}
				} else {
					System.out.println("Act Cleanup is not successful");
				}
			}
			
					
		}
	}
	
//	public static void readExcelData() {
//		try {
//			File file = new File(System.getProperty("user.dir") + "\\CleanupDriver.xlsx");
//			FileInputStream fis = new FileInputStream(file);
//			// creating Workbook instance that refers to .xlsx file
//			XSSFWorkbook wb = new XSSFWorkbook(fis);
//			XSSFSheet sheet = wb.getSheetAt(0);
//			int rowTotal = sheet.getLastRowNum() + 1;
//			
//			String environment = wb.getSheetAt(0).getRow(1).getCell(1).getStringCellValue();
//			String cleanBothActAsri = wb.getSheetAt(0).getRow(1).getCell(2).getStringCellValue();
//			String cleanOnlyAct = wb.getSheetAt(0).getRow(1).getCell(3).getStringCellValue();
//			String cleanOnlyAsri = wb.getSheetAt(0).getRow(1).getCell(4).getStringCellValue();			
//			String inventoryType = wb.getSheetAt(0).getRow(1).getCell(5).getStringCellValue();
//			
//
//			if(cleanBothActAsri.equalsIgnoreCase("yes")) {
//				System.out.println("Cleanup started for both ACT and ASRI for Environment::"+environment);
//				for (int i = 1; i < rowTotal; i++) {
//					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//					System.out.println("CLEANUP Started for SERVICE ID :: " + wb.getSheetAt(0).getRow(i).getCell(0));
//					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//					String service = wb.getSheetAt(0).getRow(i).getCell(0).getStringCellValue();
//					cleanViaExcelData(service, environment);
//
//				}
//			} 
//			
//			
//			if(cleanOnlyAct.equalsIgnoreCase("yes")) {
//				System.out.println("Cleanup started for ACT only");
//			}
//			
//			if(cleanOnlyAsri.equalsIgnoreCase("yes")) {
//				System.out.println("Cleanup started for Asri only");
//			}
//
//			
//			
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}		
//		
//
//	}
	
	
	public static void cleanViaExcelData(String service, String env) {
		Asri asri = new Asri();
		Act act = new Act();
		ArrayList<String> services = new ArrayList<String>();
		LinkedHashMap<String, String> servicesMap = asri.consolidateServices(service, env);
		
		if(servicesMap.size()>0) {
			
			services  = asri.getRearragedServices(servicesMap, env);			
			for (Iterator iterator = services.iterator(); iterator.hasNext();) {
				
				String s = (String) iterator.next();				
				System.out.println("Cleanup started for::"+s);
				
				boolean actCleanUpStatus = act.networkCleanup(s, env);
				if (actCleanUpStatus) {
					System.out.println("Act Cleanup is successful");
					
					//cleaning Ips
					if(s.contains("IRXX")) {
						asri.cleanIp(s);
					}
					boolean asriCleanUpStatus = asri.inventoryCleanUp(s, env);
					if (asriCleanUpStatus) {
						System.out.println("ASRI Cleanup is successful");
					} else {
						System.out.println("ASRI Cleanup is not successful");
						break;
					}
				} else {
					System.out.println("Act Cleanup is not successful");
					break;
				}
				servicesMap = asri.consolidateServices(service, env);
				services  = asri.getRearragedServices(servicesMap, env);
				
			}
		}else {
			{
				System.out.println("Service Not found in Inventory");
				
				ArrayList<String> envs = new ArrayList<String>();
				envs.add("1");
				envs.add("2");
				envs.add("4");
				
				System.out.println("Cleaning up in Network/ACT::"+service);
				
				if(service.contains("IRXX")) {
					asri.cleanIp(service);
				}
				
				for (String eachEnv : envs) {
					boolean actCleanUpStatus = act.networkCleanup(service, eachEnv);
					if (actCleanUpStatus) {
						System.out.println("Act Cleanup is successful");
						boolean asriCleanUpStatus = asri.inventoryCleanUp(service, eachEnv);
						if (asriCleanUpStatus) {
							System.out.println("ASRI Cleanup is successful");
						} else {
							System.out.println("ASRI Cleanup is not successful");
						}
					} else {
						System.out.println("Act Cleanup is not successful");
					}
				}
				
						
			}
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
