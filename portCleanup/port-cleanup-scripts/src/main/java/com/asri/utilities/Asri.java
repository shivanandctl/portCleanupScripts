package com.asri.utilities;

import io.restassured.RestAssured;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.act.utilities.Rubicon;
import com.autopilot.utilities.Autopilot;
import com.jayway.jsonpath.JsonPath;
import com.misc.utilities.Base;

import io.restassured.response.Response;

public class Asri {

	Base base = new Base();
	Autopilot autopilot = new Autopilot();
	String username = base.username;
	String password = base.password;

	public ArrayList<String> getServiceEnvironment(String service) {
		String environment = "";
		String sasiRes = "";
		Response response;

		String test1 = base.TEST1_SASI_ASRI + service;
		String test2 = base.TEST2_SASI_ASRI + service;
		String test4 = base.TEST4_SASI_ASRI + service;

		ArrayList<String> envList = new ArrayList<String>();
		envList.add(test1);
		envList.add(test2);
		envList.add(test4);

		String envCount[] = new String[5];
		ArrayList<String> envCountList = new ArrayList<String>();

		for (String env : envList) {
			response = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and().when().get(env)
					.then().extract().response();
			sasiRes = response.asString();

			if (sasiRes.contains("No services matching")) {
				environment = "Service not found in any environment";
				continue;
			} else {
				if (env.contains("test1")) {
					environment = "TEST1";
					envCount[0] = "TEST1";
					continue;
				} else if (env.contains("test2")) {
					environment = "TEST2";
					envCount[1] = "TEST2";
					continue;
				} else if (env.contains("test4")) {
					environment = "TEST4";
					envCount[2] = "TEST4";
					continue;
				}

				break;
			}
		}

		for (int i = 0; i < envCount.length; i++) {
			if (!(envCount[i] == null)) {
				envCountList.add(envCount[i]);
			}
		}
		return envCountList;

	}

	public LinkedHashMap getParentServices(String service, String environment) {

		String sasiRes = "";
		Response response;
		ArrayList<String> parentServiceName = new ArrayList<String>();
		// linkedhashmap
		LinkedHashMap parentServicesMap = new LinkedHashMap();

		if (environment.contains("1")) {
			autopilot.environment = "1";
			response = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and().when()
					.get(base.TEST1_SASI_ASRI + service).then().extract().response();
			sasiRes = response.asString();
		} else if (environment.contains("2")) {
			autopilot.environment = "2";
			response = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and().when()
					.get(base.TEST2_SASI_ASRI + service).then().extract().response();
			sasiRes = response.asString();
		} else if (environment.contains("4")) {
			autopilot.environment = "4";
			response = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and().when()
					.get(base.TEST4_SASI_ASRI + service).then().extract().response();
			sasiRes = response.asString();
		}
		parentServiceName = JsonPath.read(sasiRes, "$..parentServices[*].name");

		if (parentServiceName.isEmpty()) {
//			System.out.println("+++++++++===================================++++++++");
//			System.out.println("No parent services found for " + service);
//			System.out.println("+++++++++===================================++++++++");
		} else {
//			System.out.println("===================================");
//			System.out.println("Parent services for " + service + " in ENV:"+environment);
//			System.out.println("===================================");
			for (String parent : parentServiceName) {
				if (parent.contains("_")) {
					String ServiceFromEP = parent.split("_")[0];
					ArrayList<String> serviceType = new ArrayList<String>();
					serviceType = getServiceType(ServiceFromEP, environment);
					if (!serviceType.isEmpty()) {
						String serviceTypeName = serviceType.get(0);
						String resName_resType = getReqNameAndReqType(serviceTypeName, environment);
						String resType = resName_resType.split("_")[1];
						parentServicesMap.put(ServiceFromEP, resType);

					}
				}
				{
					ArrayList<String> serviceType = new ArrayList<String>();
					serviceType = getServiceType(parent, environment);
					if (!serviceType.isEmpty()) {
						String serviceTypeName = serviceType.get(0);
						String resName_resType = getReqNameAndReqType(serviceTypeName, environment);
						String resType = resName_resType.split("_")[1];
						parentServicesMap.put(parent, resType);
					}
				}

			}
		}

		return parentServicesMap;

	}

	public ArrayList<String> getServiceType(String service, String environment) {

		String sasiRes = "";
		Response response;
		ArrayList<String> serviceType = new ArrayList<String>();

		if (environment.contains("1")) {
			autopilot.environment = "1";
			response = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and().when()
					.get(base.TEST1_SASI_ASRI + service).then().extract().response();
			sasiRes = response.asString();
		} else if (environment.contains("2")) {
			autopilot.environment = "2";
			response = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and().when()
					.get(base.TEST2_SASI_ASRI + service).then().extract().response();
			sasiRes = response.asString();
		} else if (environment.contains("4")) {
			autopilot.environment = "4";
			response = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and().when()
					.get(base.TEST4_SASI_ASRI + service).then().extract().response();
			sasiRes = response.asString();
		}
		serviceType = JsonPath.read(sasiRes, "$..resources[0].type");

//		if (serviceType.isEmpty()) {
//			System.out.println("No service type found for " + service);
//		} else {
//			System.out.println("Service type for " + service + " is " + serviceType.toString());
//		}

		return serviceType;

	}

	public static LinkedHashMap consolidateServices(String service, String environment) {
			
			Rubicon rubicon = new Rubicon();
			Asri asri = new Asri();
		    
			LinkedHashMap consolidatedServicesMap = new LinkedHashMap();
			
			//get parent services
			LinkedHashMap parentServices = asri.getParentServices(service, environment);
			for (Object parentService : parentServices.keySet()) {
				String parentServiceName = (String) parentService;
				String parentServiceType = (String) parentServices.get(parentService);
				consolidatedServicesMap.put(parentServiceName, parentServiceType);
			}
			
			//adding the given service to the consolidated services
			ArrayList<String> serviceType = asri.getServiceType(service, environment);
			if (serviceType.size() > 0) {
				String serviceTypeName = serviceType.get(0);
				String resName_resType = asri.getReqNameAndReqType(serviceTypeName, environment);
				String resType = resName_resType.split("_")[1];
				consolidatedServicesMap.put(service, resType);
			}
			
			
			System.out.println("++============================================================++");
			System.out.println("Consolidated Services for " + service + " in ENV:" + environment);
			System.out.println("++============================================================++");
			for (Object consolidatedService : consolidatedServicesMap.keySet()) {
				String consolidatedServiceName = (String) consolidatedService;
				String consolidatedServiceType = (String) consolidatedServicesMap.get(consolidatedService);
				System.out.println(consolidatedServiceName + " : " + consolidatedServiceType);
			}
			
			if (consolidatedServicesMap.isEmpty()) {
				System.out.println("Empty");
			}
			
			System.out.println("++============================================================++");
			
			return consolidatedServicesMap;
	}
	
	public String getReqNameAndReqType(String serviceType, String environment) {
		String resTypeFormatted = serviceType.replaceAll("\\s+", "").toLowerCase();
		String resName = "";
		String resType = "";

		if (resTypeFormatted.equalsIgnoreCase("uni")) {
			resName = "unis";
			resType = "Uni";
		} else if (resTypeFormatted.equalsIgnoreCase("oline")) {
			resName = "olines";
			resType = "OLine";
		} else if (resTypeFormatted.equalsIgnoreCase("internetaccess")) {
			resName = "internetaccesses";
			resType = "InternetAccess";
		} else if (resTypeFormatted.equalsIgnoreCase("ovc")) {
			resName = "ovcs";
			resType = "Ovc";
		} else if (resTypeFormatted.equalsIgnoreCase("evc")) {
			resName = "evcs";
			resType = "Evc";
		} else if (resTypeFormatted.equalsIgnoreCase("mp-evc")) {
			resName = "mpevcs";
			resType = "MpEvc";
		} else if (resTypeFormatted.equalsIgnoreCase("ovcendPoint")) {
			resName = "ovcendpoints";
			resType = "OvcEndpoint";
		} else if (resTypeFormatted.equalsIgnoreCase("evcendPoint")) {
			resName = "evcendpoints";
			resType = "EvcEndpoint";
		} else if (resTypeFormatted.equalsIgnoreCase("mp-evcendpoint")) {
			resName = "mpevcendpoints";
			resType = "MpEvcEndpoint";
		} else if (resTypeFormatted.equalsIgnoreCase("ipvpnendpoint")) {
			resName = "ipvpnendpoints";
			resType = "IpVpnEndpoint";
		}

		String resName_resType = resName + "_" + resType;
		return resName_resType;
	}
	
	
	// Check if MpEvc Service exists
	public static String isMpEvcServiceExists(LinkedHashMap<String, String> servicesMap, String env) {
		String isExist = "false";
		ArrayList<String> services = new ArrayList<String>(servicesMap.values());
		for (String service : services) {
			if (service.equalsIgnoreCase("MpEvc")) {
				isExist = "true$"+service+"$"+env;
				//get key for the service
				for (String key : servicesMap.keySet()) {
					if (servicesMap.get(key).equalsIgnoreCase(service)) {
						isExist = isExist +"$"+ key;
						break;
					}
				}
				
				break;
			}
		}
		return isExist;
	}
	
	// Check if MpEvc Endpoint exists
	public static String isMpEvcEndpointExists(LinkedHashMap<String, String> servicesMap, String env) {
		String isExist = "false";
		ArrayList<String> services = new ArrayList<String>(servicesMap.values());
		for (String service : services) {
			if (service.equalsIgnoreCase("MpEvcEndpoint")) {
				isExist = "true$"+service+"$"+env;
				//get key for the service
				for (String key : servicesMap.keySet()) {
					if (servicesMap.get(key).equalsIgnoreCase(service)) {
						isExist = isExist + "$" + key;
						break;
					}
				}
				break;
			}
		}
		return isExist;
	}
	
	// Check if Evc Service exists
	public static String isEvcServiceExists(LinkedHashMap<String, String> servicesMap, String env) {
		String isExist = "false";
		ArrayList<String> services = new ArrayList<String>(servicesMap.values());
		for (String service : services) {
			if (service.equalsIgnoreCase("Evc")) {
				isExist = "true$"+service+"$"+env;
				//get key for the service
				for (String key : servicesMap.keySet()) {
					if (servicesMap.get(key).equalsIgnoreCase(service)) {
						isExist = isExist + "$" + key;
						break;
					}
				}
				break;
			}
		}
		return isExist;
	}
	
	// Check if Evc Endpoint exists
	public static String isEvcEndpointExists(LinkedHashMap<String, String> servicesMap, String env) {
		String isExist = "false";
		ArrayList<String> services = new ArrayList<String>(servicesMap.values());
		for (String service : services) {
			if (service.equalsIgnoreCase("EvcEndpoint")) {
				isExist = "true$"+service+"$"+env;
				//get key for the service
				for (String key : servicesMap.keySet()) {
					if (servicesMap.get(key).equalsIgnoreCase(service)) {
						isExist = isExist + "$" + key;
						break;
					}
				}
				break;
			}
		}
		return isExist;
	}
	
	// Check if Ovc Service exists
	public static String isOvcServiceExists(LinkedHashMap<String, String> servicesMap, String env) {
		String isExist = "false";
		ArrayList<String> services = new ArrayList<String>(servicesMap.values());
		for (String service : services) {
			if (service.equalsIgnoreCase("Ovc")) {
				isExist = "true$"+service+"$"+env;
				//get key for the service
				for (String key : servicesMap.keySet()) {
					if (servicesMap.get(key).equalsIgnoreCase(service)) {
						isExist = isExist + "$" + key;
						break;
					}
				}
				break;
			}
		}
		return isExist;
	}
	
	// Check if Ovc Endpoint exists
	public static String isOvcEndpointExists(LinkedHashMap<String, String> servicesMap, String env) {
		String isExist = "false";
		ArrayList<String> services = new ArrayList<String>(servicesMap.values());
		for (String service : services) {
			if (service.equalsIgnoreCase("OvcEndpoint")) {
				isExist = "true$"+service+"$"+env;
				//get key for the service
				for (String key : servicesMap.keySet()) {
					if (servicesMap.get(key).equalsIgnoreCase(service)) {
						isExist = isExist + "$" + key;
						break;
					}
				}
				break;
			}
		}
		return isExist;
	}
	
	
	// Check if IPVPN Service exists
	public static String isIpvpnServiceExists(LinkedHashMap<String, String> servicesMap, String env) {
		String isExist = "false";
		ArrayList<String> services = new ArrayList<String>(servicesMap.values());
		for (String service : services) {
			if (service.equalsIgnoreCase("IPVPN")) {
				isExist = "true$"+service+"$"+env;
				//get key for the service
				for (String key : servicesMap.keySet()) {
					if (servicesMap.get(key).equalsIgnoreCase(service)) {
						isExist = isExist + "$" + key;
						break;
					}
				}
				break;
			}
		}
		return isExist;
	}
	
	// Check if IPVPN Endpoint exists
	public static String isIpvpnEndpointExists(LinkedHashMap<String, String> servicesMap, String env) {
		String isExist = "false";
		ArrayList<String> services = new ArrayList<String>(servicesMap.values());
		for (String service : services) {
			if (service.equalsIgnoreCase("IpVpnEndpoint")) {
				isExist = "true$"+service+"$"+env;
				//get key for the service
				for (String key : servicesMap.keySet()) {
					if (servicesMap.get(key).equalsIgnoreCase(service)) {
						isExist = isExist + "$" + key;
						break;
					}
				}
				break;
			}
		}
		return isExist;
	}
	
	// Check if DIA/internet Service exists
	public static String isDiaServiceExists(LinkedHashMap<String, String> servicesMap, String env) {
		String isExist = "false";
		ArrayList<String> services = new ArrayList<String>(servicesMap.values());
		for (String service : services) {
			if (service.equalsIgnoreCase("InternetAccess")) {
				isExist = "true$"+service+"$"+env;
				//get key for the service
				for (String key : servicesMap.keySet()) {
					if (servicesMap.get(key).equalsIgnoreCase(service)) {
						isExist = isExist + "$" + key;
						break;
					}
				}
				break;
			}
		}
		return isExist;
	}
	
	// Check if Oline Service exists
	public static String isOlineServiceExists(LinkedHashMap<String, String> servicesMap, String env) {
		String isExist = "false";
		ArrayList<String> services = new ArrayList<String>(servicesMap.values());
		for (String service : services) {
			if (service.equalsIgnoreCase("OLine")) {
				isExist = "true$"+service+"$"+env;
				//get key for the service
				for (String key : servicesMap.keySet()) {
					if (servicesMap.get(key).equalsIgnoreCase(service)) {
						isExist = isExist + "$" + key;
						break;
					}
				}
				break;
			}
		}
		return isExist;
	}
	
	// Check if Unis Service exists
	public static String isUnisServiceExists(LinkedHashMap<String, String> servicesMap, String env) {
		String isExist = "false";
		ArrayList<String> services = new ArrayList<String>(servicesMap.values());
		for (String service : services) {
			if (service.equalsIgnoreCase("Uni")) {
				isExist = "true$"+service+"$"+env;
				//get key for the service
				for (String key : servicesMap.keySet()) {
					if (servicesMap.get(key).equalsIgnoreCase(service)) {
						isExist = isExist + "$" + key;
						break;
					}
				}
				break;
			}
		}
		return isExist;
	}	

	public boolean inventoryCleanUp(String service, String environment) {
		System.out.println("+------------Inventory Cleanup Start---------------+");
		boolean inventoryCleanUpStatus = false;		
		
		ArrayList<String> serviceTypeList = getServiceType(service, environment);
		if (!serviceTypeList.isEmpty()) {
			String serviceType = getServiceType(service, environment).get(0);
			String resName_resType = getReqNameAndReqType(serviceType, environment);
			
			System.out.println("Cleanup Started for Service::" + service);
			System.out.println("Service type::" + serviceType);
			System.out.println("Resource Name and Resource Type::" + resName_resType);
			System.out.println("Environment::" + environment);

			String token = autopilot.getToken(username, password);
			String jobid_ = autopilot.triggerWorkflow(resName_resType, service, "Delete_ResourceByFilter_ASRI_AL", token);
			System.out.println("Triggering workflow::\"Delete_ResourceByFilter_ASRI_AL\"\nJob id::" + jobid_);

			String delResBody = "";
			try {
				System.out.println("Is Workflow completed?::" + autopilot.getWorkflowStatus(jobid_, token));
				delResBody = autopilot.getTaskDetail(jobid_, "7858", "$..outgoing..return_data", token);
				if (delResBody != null && delResBody.contains("successfully")) {
					inventoryCleanUpStatus = true;
				} else {
					inventoryCleanUpStatus = false;
					delResBody = autopilot.getTaskDetail(jobid_, "69f8", "$..error.response", token);
					System.out.println("Error::" + delResBody);
					
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("+------------Inventory Cleanup End-----------------+");
		} else {
			System.out.println("Service type not found for " + service);
			inventoryCleanUpStatus = true;
		}		
		return inventoryCleanUpStatus;
	}
	
	
	public ArrayList<String> getRearragedServices(LinkedHashMap<String, String> servicesMap, String env){
		
		ArrayList<String> rearragedServices = new ArrayList<String>();
		
		String isMpEvcEndpointExists = isMpEvcEndpointExists(servicesMap, env);
		String isMpEvcServiceExists = isMpEvcServiceExists(servicesMap, env);
		
		String isEvcEndpointExists = isEvcEndpointExists(servicesMap, env);
		String isEvcServiceExists = isEvcServiceExists(servicesMap, env);
		
		String isOvcEndpointExists = isOvcEndpointExists(servicesMap, env);
		String isOvcServiceExists = isOvcServiceExists(servicesMap, env);
		
		String isIpvpnEndpointExists = isIpvpnEndpointExists(servicesMap, env);
		String isIpvpnServiceExists = isIpvpnServiceExists(servicesMap, env);
		
		String isDiaServiceExists = isDiaServiceExists(servicesMap, env);
		String isOlineServiceExists = isOlineServiceExists(servicesMap, env);
		String isUnisServiceExists = isUnisServiceExists(servicesMap, env);
		
		
		if (isMpEvcEndpointExists.contains("true")) {
			rearragedServices.add(isMpEvcEndpointExists.split("\\$")[3]);
		}
		if (isMpEvcServiceExists.contains("true")) {
			rearragedServices.add(isMpEvcServiceExists.split("\\$")[3]);
		}
		
		
		if (isEvcEndpointExists.contains("true")) {
			rearragedServices.add(isEvcEndpointExists.split("\\$")[3]);
		}
		if (isEvcServiceExists.contains("true")) {
			rearragedServices.add(isEvcServiceExists.split("\\$")[3]);
		}
		
		if (isOvcEndpointExists.contains("true")) {
			rearragedServices.add(isOvcEndpointExists.split("\\$")[3]);
		}
		if (isOvcServiceExists.contains("true")) {
			rearragedServices.add(isOvcServiceExists.split("\\$")[3]);
		}
		
		
		if (isIpvpnEndpointExists.contains("true")) {
			rearragedServices.add(isIpvpnEndpointExists.split("\\$")[3]);
		}
		if (isIpvpnServiceExists.contains("true")) {
			rearragedServices.add(isIpvpnServiceExists.split("\\$")[3]);
		}
		
		if (isDiaServiceExists.contains("true")) {
			rearragedServices.add(isDiaServiceExists.split("\\$")[3]);
		}
		if (isOlineServiceExists.contains("true")) {
			rearragedServices.add(isOlineServiceExists.split("\\$")[3]);
		}
		if (isUnisServiceExists.contains("true")) {
			rearragedServices.add(isUnisServiceExists.split("\\$")[3]);
		}
		
		return rearragedServices;
	}

	public static boolean cleanIp(String serviceID) {
		String Test_Get_IPs = "https://sasi-sasiwrap-test1.kubeodc.corp.intranet/wrappers/nisws/ipBlocks?circuitId=";
		String Test_IP_Release = "https://sasi-sasiwrap-test1.kubeodc.corp.intranet/wrappers/nisws/ipRelease";
		
		System.out.println("==============================================IP CLEANUP START=================================================");
		boolean isIpCleaned = false;
		String resolvedIpUrl =  Test_Get_IPs + serviceID;
		String iPResBody = RestAssured.given().relaxedHTTPSValidation().get(resolvedIpUrl).body().asString();
		ArrayList<String> ipList = JsonPath.read(iPResBody, "$..ipBlock..cidrRange");
		if (ipList.size() > 0) {
			System.out.println("IPs Found!!\nNumber of IPs occupied by " + serviceID + " is::" + ipList.size());
			for (String ip : ipList) {
				System.out.println("Releasing IP::" + ip);
				String ipReleasePayload = "{\r\n" + "    \"circuitId\" : \"" + serviceID + "\",\r\n"
						+ "    \"cidrRange\" : \"" + ip + "\"\r\n" + "}";
				System.out.println(ipReleasePayload);
				String ipReleaseResponse = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json")
						.and().body(ipReleasePayload).when().post(Test_IP_Release).then().extract().response()
						.asString();
				System.out.println(ipReleaseResponse);
				ArrayList<String> ipReleaseList = JsonPath.read(ipReleaseResponse, "$..errorMessage");
				if (ipReleaseList.size() > 0) {
					System.out.println(ipReleaseList.get(0));
					ipReleaseList.get(0).equalsIgnoreCase("SUCCESS");
				}
			}
		} else {
			System.out.println("No IPs occupied by the service::" + serviceID);
		}
		System.out.println("==============================================IP CLEANUP END===================================================\n\n");
		return isIpCleaned;

	}
	
	public static void main(String[] args) {
		Asri asri = new Asri();
		
		String service = "CO/KXFN/048520/LUMN";
		String env = "4";
		
		LinkedHashMap<String, String> servicesMap = asri.consolidateServices(service, env);
		ArrayList<String> rearragedServices = asri.getRearragedServices(servicesMap, env);
		
		for (String service_ : rearragedServices) {
			System.out.println(service_);
		}

	}

}
