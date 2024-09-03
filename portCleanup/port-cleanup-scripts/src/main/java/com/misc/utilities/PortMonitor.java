package com.misc.utilities;

import io.restassured.RestAssured;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.act.utilities.Act;
import com.act.utilities.Rubicon;
import com.asri.utilities.Asri;
import com.cleanup.services.CleanServices;
import com.jayway.jsonpath.JsonPath;

import io.restassured.response.Response;

public class PortMonitor {

	public ArrayList<String> validateUniForCleanup(String uni) {

		System.out.println("Validating UNI: " + uni + " from PortMonitor");
		ArrayList<String> cleanupUnis = new ArrayList<String>();

		Response response;

		String query = "https://ndf-test-cleanup.kubeodc-test.corp.intranet/getUnidata/" + uni;
		response = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and().when().get(query)
				.then().extract().response();

		int statCode = response.getStatusCode();
		System.out.println("Status code: " + statCode);

//		response.prettyPrint();

		// if status = 500
		if (statCode == 200) {
			String sasiRes = response.asString();
//			System.out.println("Response: " + sasiRes);
			String userComment = JsonPath.read(sasiRes, "$.user_comment");
			System.out.println("User comment: " + userComment);
			if (userComment.equals("CAN BE CLEANED!")) {
				String cleanupDate = JsonPath.read(sasiRes, "$.cleanup_date");
				System.out.println("Cleanup date: " + cleanupDate);
				System.out.println("User comment is :" + userComment+ " hence adding for cleanup");
				cleanupUnis.add(uni);
				System.out.println(
						"+----------------------------------------------------------------------------------+");
			}
			if (userComment.equals("DO NOT CLEAN!")) {
				String cleanupDate = JsonPath.read(sasiRes, "$.cleanup_date");
				System.out.println("Cleanup date: " + cleanupDate);
				// Parse the string date "2024-03-30" into a LocalDate
				LocalDate parsedStringDate = LocalDate.parse(cleanupDate);
				LocalDate currentDate = LocalDate.now();
				int comparisonResult = currentDate.compareTo(parsedStringDate);
				if (comparisonResult > 0) {
					System.out.println(uni + "::Cleanup date has crossed today's date adding for cleanup");
					cleanupUnis.add(uni);
					System.out.println(
							"+----------------------------------------------------------------------------------+");
				} else {
					System.out.println("Cleanup date is greater than today's date not adding for cleanup");
					System.out.println(
							"+----------------------------------------------------------------------------------+");
				}

			}
		}
		if (statCode == 500) {
			cleanupUnis.add(uni);
			System.out.println("No record found in PortMonitor DB\nUpdating UNI::"+uni+" into port UI for Cleanup");
			updatePortMonitorIfUniNotUpdated(uni);
			System.out.println("+--------------------------------------------------------------------------+");
		}

		return cleanupUnis;
	}

	public static void main(String[] args) {
		PortMonitor pm = new PortMonitor();
		Rubicon rubicon = new Rubicon();
		CleanServices cs = new CleanServices();
		Asri asri = new Asri();
		ArrayList<String> cleanupUnis = new ArrayList<String>();
		ArrayList<String> validatedUnis = new ArrayList<String>();

		// fetch devices from Rubicon
		 ArrayList<String> devices = rubicon.listLabDevices();
		 for (String device : devices) {
			 	System.out.println("Device::"+device);
				ArrayList<String> uniList = rubicon.fetchUnisFromDevice(device);
				System.out.println("+------------------List of UNIs on Device::" + device + "------------------------+");
				for (String uni : uniList) {
					System.out.println(uni);
				}
				System.out.println("===============================================================================");

				for (int i = 1; i < uniList.size(); i++) {
					cleanupUnis = pm.validateUniForCleanup(uniList.get(i));
					if (cleanupUnis.size() > 0) {
						validatedUnis.add(uniList.get(i));
					}
				}

				// print the validated UNIs
				System.out.println("+------------------------------VALIDATED UNIs------------------------------------+");
				for (String unis : validatedUnis) {
					System.out.println(unis);
				}
				System.out.println("===============================================================================");

				for (String unis : validatedUnis) {
					System.out.println("############################################################################");
					System.out.println("Cleanup Started for::" + unis);
					ArrayList<String> envs = asri.getServiceEnvironment(unis);

					if (envs.size() == 0) {
						System.out.println(unis + "::No Environment found");
						cleanPortsViaPortMonitorData(unis, "1");
					} else if (envs.size() > 0) {
						for (String env : envs) {
							System.out.println(unis + "====>" + env);
							cleanPortsViaPortMonitorData(unis, env);
						}
					}

				}
		 }
		
		

//		updatePortMonitorIfUniNotUpdated("CO/KXFN/048399/LUMN");
//		updateRecordAfterCleanup("CO/KXFN/048399/LUMN");CO/KXFN/048664/LUMN
//		cleanPortsViaPortMonitorData("CO/KXFN/048664/LUMN", "4");

	}

	public static void cleanPortsViaPortMonitorData(String service, String env) {
		Asri asri = new Asri();
		Act act = new Act();
		ArrayList<String> services = new ArrayList<String>();
		LinkedHashMap<String, String> servicesMap = asri.consolidateServices(service, env);

		if (servicesMap.size() > 0) {

			services = asri.getRearragedServices(servicesMap, env);
			for (Iterator iterator = services.iterator(); iterator.hasNext();) {

				String s = (String) iterator.next();
				System.out.println("Cleanup started for::" + s);

				boolean actCleanUpStatus = act.networkCleanup(s, env);
				if (actCleanUpStatus) {
					System.out.println("Act Cleanup is successful");

					// cleaning Ips
					if (s.contains("IRXX")) {
						asri.cleanIp(s);
					}
					boolean asriCleanUpStatus = asri.inventoryCleanUp(s, env);
					if (asriCleanUpStatus) {
						System.out.println("ASRI Cleanup is successful");
						// update the record in portmonitor db
						if (s.contains("KXFN")) {
							updateRecordAfterCleanup(s);
						}

					} else {
						System.out.println("ASRI Cleanup is not successful");
						break;
					}
				} else {
					System.out.println("Act Cleanup is not successful");
					break;
				}
				servicesMap = asri.consolidateServices(service, env);
				services = asri.getRearragedServices(servicesMap, env);

			}
		} else {
			{
				System.out.println("Service Not found in Inventory");

				ArrayList<String> envs = new ArrayList<String>();
				envs.add("1");
				envs.add("2");
				envs.add("4");
//				
//				System.out.println("Cleaning up in Network/ACT::"+service);
//				
//				if(service.contains("IRXX")) {
//					asri.cleanIp(service);
//				}
//				
//				for (String eachEnv : envs) {
//					boolean actCleanUpStatus = act.networkCleanup(service, eachEnv);
//					if (actCleanUpStatus) {
//						System.out.println("Act Cleanup is successful");
//						boolean asriCleanUpStatus = asri.inventoryCleanUp(service, eachEnv);
//						if (asriCleanUpStatus) {
//							System.out.println("ASRI Cleanup is successful");
//						} else {
//							System.out.println("ASRI Cleanup is not successful");
//						}
//					} else {
//						System.out.println("Act Cleanup is not successful");
//					}
//				}
				
				System.out.println("Checking for no services found in all the Inventory try cleaning in Network");
				//if no services found in Inventory try cleaning in Network
				LinkedHashMap<String, String> servicesMap1 = asri.consolidateServices(service, "1");
				LinkedHashMap<String, String> servicesMap2 = asri.consolidateServices(service, "2");
				LinkedHashMap<String, String> servicesMap4 = asri.consolidateServices(service, "4");
				if (servicesMap1.size()==0&&servicesMap2.size()==0&&servicesMap4.size()==0) {
					System.out.println("No services found in all the Inventory try cleaning in Network");
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
				}else {
					for (String eachEnv : envs) {
						services = new ArrayList<String>();
						servicesMap = asri.consolidateServices(service, eachEnv);

						if (servicesMap.size() > 0) {

							services = asri.getRearragedServices(servicesMap, eachEnv);
							for (Iterator iterator = services.iterator(); iterator.hasNext();) {

								String s = (String) iterator.next();
								System.out.println("Cleanup started for::" + s);

								boolean actCleanUpStatus = act.networkCleanup(s, eachEnv);
								if (actCleanUpStatus) {
									System.out.println("Act Cleanup is successful");

									// cleaning Ips
									if (s.contains("IRXX")) {
										asri.cleanIp(s);
									}
									boolean asriCleanUpStatus = asri.inventoryCleanUp(s, eachEnv);
									if (asriCleanUpStatus) {
										System.out.println("ASRI Cleanup is successful");
										// update the record in portmonitor db
										if (s.contains("KXFN")) {
											updateRecordAfterCleanup(s);
										}

									} else {
										System.out.println("ASRI Cleanup is not successful");
										break;
									}
								} else {
									System.out.println("Act Cleanup is not successful");
									break;
								}
								servicesMap = asri.consolidateServices(service, eachEnv);
								services = asri.getRearragedServices(servicesMap, eachEnv);

							}
						}
					}
				}
			}
		}

	}

	public static void updatePortMonitorIfUniNotUpdated(String uni) {

		Response response;
		String query = "https://sasi-test1.kubeodc-test.corp.intranet/inventory/v1/asri/services?name=" + uni;
		response = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and().when().get(query)
				.then().extract().response();

		int statCode = response.getStatusCode();
		if (statCode == 200) {
			String environment = "Test1";
			System.out.println("UNI: " + uni + " is updated in PortMonitor in Test1");
			getSasiDetails(response, environment);
			triggerUpdateDbApi(getSasiDetails(response, environment));
		} else {
			query = "https://sasi-test2.kubeodc-test.corp.intranet/inventory/v1/asri/services?name=" + uni;
			response = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and().when()
					.get(query).then().extract().response();
			statCode = response.getStatusCode();
			if (statCode == 200) {
				String environment = "Test2";
				System.out.println("UNI: " + uni + " is updated in PortMonitor in Test2");
				getSasiDetails(response, environment);
				triggerUpdateDbApi(getSasiDetails(response, environment));
			} else {
				query = "https://sasi-test4.kubeodc-test.corp.intranet/inventory/v1/asri/services?name=" + uni;
				response = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and().when()
						.get(query).then().extract().response();
				statCode = response.getStatusCode();
				if (statCode == 200) {
					String environment = "Test4";
					System.out.println("UNI: " + uni + " is updated in PortMonitor in Test4");
					getSasiDetails(response, environment);
					triggerUpdateDbApi(getSasiDetails(response, environment));
				} else {
					System.out.println("UNI: " + uni
							+ " is not updated in PortMonitor\n as it is not found in ASRI Test1, Test2 and Test4");
				}
			}
		}
	}

	public static String getSasiDetails(Response response, String env) {

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date currentDate = new Date();
		String formattedDate = formatter.format(currentDate);

		String sasiRes = response.asString();
//		response.prettyPrint();

		ArrayList<String> unialias = JsonPath.read(sasiRes, "$..resources[0].name");

		String environment = env;
		ArrayList<String> createddate = JsonPath.read(sasiRes,
				"$..resources[0].attributes[?(@.name=='CREATEDDATE')].value");

		ArrayList<String> device = JsonPath.read(sasiRes, "$..resources[0].zend.device.name");
		String deviceName;
		if (device.size() > 0) {
			deviceName = device.get(0);
		} else {
			deviceName = "NULL";
		}

		ArrayList<String> portnum = JsonPath.read(sasiRes, "$..resources[0].zend.port.name");
		String portName;
		if (portnum.size() > 0) {
			portName = portnum.get(0);
		} else {
			portName = "NULL";
		}

		String prodType = "UNI";
		String team = "Team_Auto";
		ArrayList<String> orderId = JsonPath.read(sasiRes,
				"$..resources[0].attributes[?(@.name=='EXTERNAL_ORDER_ID')].value");
		String orderIdValue;
		if (orderId.size() > 0) {
			orderIdValue = orderId.get(0);
		} else {
			orderIdValue = "NULL";
		}

		String createdBy = "Jenkins_Auto";
		String user_comment = "CAN BE CLEANED!";
		String cleanup_date = formattedDate;

		String sasiDetails = unialias.get(0) + "," + environment + "," + createddate.get(0).split(" ")[0] + ","
				+ deviceName + "," + portName + "," + prodType + "," + team + "," + orderIdValue + "," + createdBy + ","
				+ user_comment + "," + cleanup_date;

//		for (String s : sasiDetails.split(",")) {
//			System.out.println(s);
//		}

		return sasiDetails;

	}

	// This function is used to trigger the update db api to update the portmonitor
	// db with the sasi details
	public static void triggerUpdateDbApi(String sasiDetails) {
		Response response;
		String query = "https://ndf-test-cleanup.kubeodc-test.corp.intranet/updateUnidetailsInDbFromSoa";
		String jsonBody = "{\r\n" + "    \"unialias\": \"" + sasiDetails.split(",")[0] + "\",\r\n"
				+ "    \"environment\": \"" + sasiDetails.split(",")[1] + "\",\r\n" + "    \"createddate\": \""
				+ sasiDetails.split(",")[2] + "\",\r\n" + "    \"device\": \"" + sasiDetails.split(",")[3] + "\",\r\n"
				+ "    \"portnum\": \"" + sasiDetails.split(",")[4] + "\",\r\n" + "    \"prodType\": \""
				+ sasiDetails.split(",")[5] + "\",\r\n" + "    \"team\": \"" + sasiDetails.split(",")[6] + "\",\r\n"
				+ "    \"orderId\": \"" + sasiDetails.split(",")[7] + "\",\r\n" + "    \"createdBy\": \""
				+ sasiDetails.split(",")[8] + "\",\r\n" + "    \"user_comment\": \"" + sasiDetails.split(",")[9]
				+ "\",\r\n" + "    \"cleanup_date\": \"" + sasiDetails.split(",")[10] + "\"\r\n" + "}";

//        make a post call to update the db
		response = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and().body(jsonBody)
				.when().post(query).then().extract().response();

		int statCode = response.getStatusCode();
		if (statCode == 200) {
			System.out.println("PortMonitor DB updated successfully");
		} else {
			System.out.println("PortMonitor DB not updated");
		}
	}

	// This function is used to update the record in portmonitor db after cleanup is
	// done in network and inventory
	public static void updateRecordAfterCleanup(String uni) {

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date currentDate = new Date();
		String formattedDate = formatter.format(currentDate);

		Response response;
		String query = "https://ndf-test-cleanup.kubeodc-test.corp.intranet/getUnidata/" + uni;
		response = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and().when().get(query)
				.then().extract().response();

		int statCode = response.getStatusCode();
		System.out.println("Status code: " + statCode);
		String sasiRes = response.asString();
		response.prettyPrint();
//		System.out.println("+++++++----------------------------------------------+++++++++++++");
//		for (String s : sasiRes.split(",")) {
//			System.out.println(s);
//		}

		String ss[] = sasiRes.split(",");
		ss[9] = "\"createdBy\":\"Jenkins_Auto\"";
		ss[10] = "\"user_comment\":\"CLEANED\"";
		ss[12] = "\"cleanedDate\":\"" + formattedDate + "\"}";
		System.out.println("+++++++----------------------------------------------+++++++++++++");

		String modifiedPayload = "";
		for (String m : ss) {
			System.out.println(m);
			if (!m.contains("cleanedDate")) {
				modifiedPayload = modifiedPayload + m + ",";
			} else {
				modifiedPayload = modifiedPayload + m;
			}

		}

		System.out.println("+++++++----------------------------------------------+++++++++++++");

		System.out.println("PortMonitor DB UPDATE PAYLOAD::\n"+modifiedPayload);

		// update the record
		String updateQuery = "https://ndf-test-cleanup.kubeodc-test.corp.intranet/updateUnidetailsInDb";
		response = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and()
				.body(modifiedPayload).when().post(updateQuery).then().extract().response();

		statCode = response.getStatusCode();
		System.out.println("Status code: " + statCode);
		if (statCode == 200 || statCode == 201) {
			System.out.println("PortMonitor DB updated successfully");
		} else {
			System.out.println("PortMonitor DB not updated");
		}
	}
}
