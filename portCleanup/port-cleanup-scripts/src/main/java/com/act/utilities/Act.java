package com.act.utilities;

import io.restassured.RestAssured;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.json.XML;

import com.jayway.jsonpath.JsonPath;
import com.misc.utilities.Base;

import io.restassured.response.Response;

import com.autopilot.utilities.Autopilot;

public class Act {
	Base base = new Base();
	String username = base.username;
	String password = base.password;

	/**
	 * Cleans up the network for a specific service and environment.
	 * If the network is cleaned up successfully, returns true.
	 * Otherwise, returns false.
	 *
	 * @param service the service to clean up the network for
	 * @param environment the environment in which the network cleanup should be performed
	 * @return true if the network cleanup is successful, false otherwise
	 */
	public boolean networkCleanup(String service, String environment) {
		
		// Logic to clean up the network
		// If network is cleaned up successfully, return true
		// Else return false
		boolean actCleanupStatus = false;
		Autopilot autopilot = new Autopilot();
		//set the environment
		autopilot.environment = environment;
		
		
		//get the request ids
		ArrayList<String> ReqID_ServiceType_ReqType = new ArrayList<String>();
		ReqID_ServiceType_ReqType = getRequestIDs(service, environment);
		if (ReqID_ServiceType_ReqType.size() == 0) {
			System.out.println("No request found for the given service::" + service +" in the "+environment+" environment");
			return actCleanupStatus;
		}else if (ReqID_ServiceType_ReqType.size() > 0) {
			if(ReqID_ServiceType_ReqType.get(0).contains("delete")) {
				ArrayList<String> actInfo = new ArrayList<String>();
				actInfo = getActDetailsUsingRequestID(ReqID_ServiceType_ReqType.get(0).split("&&")[0], environment);
				if (actInfo.size() > 0) {
					String identifier_id = actInfo.get(0);
					String header_identifier = actInfo.get(1);
					// cleanup the network
					System.out.println("Network cleanup already done for ServiceID::"+service+ "\nidentifier_id::" + identifier_id + "\nheader_identifier::" + header_identifier);
					actCleanupStatus = true;
				}
			} else {
				// get the act details using request id
				ArrayList<String> actInfo = new ArrayList<String>();
				actInfo = getActDetailsUsingRequestID(ReqID_ServiceType_ReqType.get(0).split("&&")[0], environment);
				if (actInfo.size() > 0) {
					String identifier_id = actInfo.get(0);
					String header_identifier = actInfo.get(1);
					// cleanup the network
					System.out.println("Network cleanup is in progress for ServiceID::"+service+ "\nidentifier_id::" + identifier_id + "\nheader_identifier::" + header_identifier);
					
					//perform network cleanup
					
					String token = autopilot.getToken(username, password);
//					System.out.println("Logging into Autopilot\nToken generated::"+token+"\nLogged in Successfully");
//					System.out.println("Logging into Autopilot\nLogged in Successfully");
					String jobid_ = autopilot.triggerWorkflow(identifier_id, header_identifier, "LNAAS_DELETE_TRANSACTION_ACT_TL_V1",
							token);
					System.out.println("Triggering workflow::\"LNAAS_DELETE_TRANSACTION_ACT_TL_V1\"\nJob id::" + jobid_);
					try {
						System.out.println("Is Workflow completed?::" + autopilot.getWorkflowStatus(jobid_, token));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					try {
						String errorString = autopilot.getTaskDetail(jobid_, "e5b9", "$..outgoing.return_value", token);
						if (errorString != null) {
							System.out.println("Delete Transaction from ACT completed Successfully");
							String successDelActId = autopilot.getTaskDetail(jobid_, "14bc", "$..actIdentifierId", token);
							actCleanupStatus = true;
						} else {
							errorString = autopilot.getTaskDetail(jobid_, "51ed", "$..outgoing..message", token);
							String delActId = autopilot.getTaskDetail(jobid_, "51ed", "$..outgoing..actID", token);
							System.out.println("DELETE Transaction failed with error::");
							System.out.println(
									"+-----------+---------+-----------+---------+-----------+---------+-----------+---------+-----------+---------+");
							System.out.println(errorString);
							System.out.println(
									"+-----------+---------+-----------+---------+-----------+---------+-----------+---------+-----------+---------+");
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					
					
				}
			}
		}
		

		return actCleanupStatus;

	}

	/** This method performs authentication and returns a list of cookies.
	*The authentication is done using the provided [environment] parameter.
	*The [environment] parameter determines the URL to be used for authentication.
	* The method uses basic authentication with the provided username and password.
	* It returns a list of cookies as a list of maps, where each map represents a cookie.
	* Each map contains key-value pairs representing the cookie name and value.
	* If an error occurs during the authentication process, an exception is thrown.
	* 
	* Example usage:
	* 
	* ArrayList<Map<String, String>> cookies = actAuthentication("1");
	* 
	*
	* @param environment The environment for authentication.
	* @return A list of cookies as a list of maps.
	* @throws Exception If an error occurs during the authentication process.
	*/
	public static ArrayList<Map<String, String>> actAuthentication(String environment) {
		
		// Set basic authentication header
		Base base = new Base();
		String auth = base.username + ":" + base.password;
		String authHeader = "";
		byte[] encodedAuth;
		String Test_authUrl = "";

		if (environment.contains("1")) {
			Test_authUrl = "http://act-env1.idc1.level3.com:8081/ac-ip-rs-web/rs/auth";
		} else if (environment.contains("2")) {
			Test_authUrl = "http://act-env2.idc1.level3.com:8081/ac-ip-rs-web/rs/auth";
		} else if (environment.contains("4")) {
			Test_authUrl = "http://act-env4.idc1.level3.com:8081/ac-ip-rs-web/rs/auth";
		}
		ArrayList<Map<String, String>> cookiesMap = new ArrayList<Map<String, String>>();
		try {
			encodedAuth = Base64.encodeBase64(auth.getBytes("UTF-8"));
			authHeader = new String(encodedAuth);
			Response Test_response = RestAssured.given().relaxedHTTPSValidation().auth().preemptive().basic(base.username, base.password)
					.header("authorization", authHeader).get(Test_authUrl);
			Map<String, String> Test_cook = Test_response.cookies();

			cookiesMap.add(Test_cook);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return cookiesMap;
	}

	/// Retrieves the request IDs based on the service alias and environment.
	/// 
	/// The method takes in the [serviceAlias] and [environment] as parameters and returns a list of request IDs.
	/// It performs authentication using the [actAuthentication] method and retrieves the JSON response from the specified environment URL.
	/// The JSON response is then converted to XML using the `XML.toJSONObject` method.
	/// The XML is then parsed using JsonPath to extract the request IDs and other relevant data.
	/// The extracted request IDs are stored in a LinkedHashMap along with their corresponding service type and request type.
	/// Finally, the method returns a list of request IDs along with their service type and request type.
	/// 
	/// Example usage:
	/// ```dart
	/// ArrayList<String> requestIDs = getRequestIDs("serviceAlias", "environment");
	/// ```
	///
	/// @param serviceAlias The service alias for which to retrieve the request IDs.
	/// @param environment The environment in which to perform the request.
	/// @return A list of request IDs along with their service type and request type.
	/// @throws Exception If an error occurs during the request process.
	
	public ArrayList<String> getRequestIDs(String serviceAlias, String environment) {

		ArrayList<Map<String, String>> cookiesMap = actAuthentication(environment);
		ArrayList<String> ReqID_ServiceType_ReqType = new ArrayList<String>();
		String actJsonResponse = null;

		if (environment.contains("1")) {
			actJsonResponse = RestAssured.given().cookies(cookiesMap.get(0)).when()
					.get("http://act-env1.idc1.level3.com:8081/ac-ip-rs-web/rs/view/default/data?q0=" + serviceAlias)
					.body().asString();
		} else if (environment.contains("2")) {
			actJsonResponse = RestAssured.given().cookies(cookiesMap.get(0)).when()
					.get("http://act-env2.idc1.level3.com:8081/ac-ip-rs-web/rs/view/default/data?q0=" + serviceAlias)
					.body().asString();
		} else if (environment.contains("4")) {
			actJsonResponse = RestAssured.given().cookies(cookiesMap.get(0)).when()
					.get("http://act-env4.idc1.level3.com:8081/ac-ip-rs-web/rs/view/default/data?q0=" + serviceAlias)
					.body().asString();
		}

		JSONObject xmlJSONObj = XML.toJSONObject(actJsonResponse);
		String jsonPrettyPrintString = xmlJSONObj.toString(4);

		ArrayList requestIdLength = JsonPath.read(jsonPrettyPrintString, "$..requestID");
		LinkedHashMap<Integer, String> newRequestIdMap = new LinkedHashMap<Integer, String>();
		Integer requestId = null;
		if (requestIdLength.size() == 1) {
			{
				ArrayList isRequestType = JsonPath.read(jsonPrettyPrintString, "$..data[3].data");
				ArrayList isRequestComplete = JsonPath.read(jsonPrettyPrintString, "$..data[0].data");
				ArrayList<String> serviceType = JsonPath.read(jsonPrettyPrintString, "$..data[5].data");
				ArrayList idNewRequest = null;
				String reqType = null;
				String reqComplete = null;
				String serviceTypeName = null;

				if (isRequestType.size() > 0) {
					reqType = (String) isRequestType.get(0);
					reqComplete = (String) isRequestComplete.get(0);
					serviceTypeName = (String) serviceType.get(0);
					if (reqType.equalsIgnoreCase("new") && reqComplete.equalsIgnoreCase("complete")) {
						idNewRequest = JsonPath.read(jsonPrettyPrintString, "$..requestID");
						if (idNewRequest.size() > 0) {
							requestId = (Integer) idNewRequest.get(0);
							newRequestIdMap.put(requestId, serviceTypeName + "_" + reqType);
							ReqID_ServiceType_ReqType
									.add(requestId.toString() + "&&" + serviceTypeName + "&&" + reqType);
						}
					}
				}
			}
		} else {
			ArrayList givenServiceType = JsonPath.read(jsonPrettyPrintString, "$..data[5].data");
			int serviceTypeSize = givenServiceType.size();
			if (serviceTypeSize > 0) {
				String serviceTypeName = (String) givenServiceType.get(serviceTypeSize - 1);
				// System.out.println("Given input is of Type::"+serviceTypeName);

				for (int i = 0; i < requestIdLength.size(); i++) {
					ArrayList isRequestType = JsonPath.read(jsonPrettyPrintString, "$..rows[" + i + "]..data[3].data");
					ArrayList isRequestComplete = JsonPath.read(jsonPrettyPrintString,
							"$..rows[" + i + "]..data[0].data");
					ArrayList<String> serviceType = JsonPath.read(jsonPrettyPrintString,
							"$..rows[" + i + "]..data[5].data");

					ArrayList idNewRequest = null;
					String reqType = null;
					String reqComplete = null;
					String innerServiceTypeName = null;

					if (isRequestType.size() > 0) {
						reqType = (String) isRequestType.get(0);
						reqComplete = (String) isRequestComplete.get(0);
						innerServiceTypeName = (String) serviceType.get(0);
						// if (reqType.equalsIgnoreCase(activity) &&
						// reqComplete.equalsIgnoreCase("complete")) {
						if (reqComplete.equalsIgnoreCase("complete")&& innerServiceTypeName.equalsIgnoreCase(serviceTypeName)&& (reqType.equalsIgnoreCase("new")||reqType.equalsIgnoreCase("delete"))) {
//						if (reqComplete.equalsIgnoreCase("complete")&& innerServiceTypeName.equalsIgnoreCase(serviceTypeName)) {
							idNewRequest = JsonPath.read(jsonPrettyPrintString, "$..rows[" + i + "]..requestID");
							if (idNewRequest.size() > 0) {
								// System.out.println("Given input is of Type::"+serviceTypeName);
								requestId = (Integer) idNewRequest.get(0);
								newRequestIdMap.put(requestId, innerServiceTypeName + "_" + reqType);
								ReqID_ServiceType_ReqType
										.add(requestId.toString() + "&&" + innerServiceTypeName + "&&" + reqType);
								// System.out.println(serviceTypeName+"::"+requestId+"::"+reqType);
							}
						}
					}
				}
			}

		}

		return ReqID_ServiceType_ReqType;

	}
	
	
	/// Retrieves the ACT details using the provided request ID and environment.
	/// 
	/// The method makes a GET request to the ACT service based on the environment parameter.
	/// It retrieves the ACT JSON response and converts it to a JSON object.
	/// The JSON object is then parsed to extract the values of the 'identifier_id' and 'header.identifier' fields.
	/// If both fields have values, they are added to the 'actInfo' ArrayList.
	/// Finally, the 'actInfo' ArrayList is returned.
	///
	/// Parameters:
	/// - requestId: The ID of the request.
	/// - environment: The environment to use for the request (e.g., "1", "2", "4").
	///
	/// Returns:
	/// - An ArrayList containing the 'identifier_id' and 'header.identifier' values, if available.
	///   Otherwise, an empty ArrayList is returned.
	
	public ArrayList<String> getActDetailsUsingRequestID(String requestId, String environment) {
		ArrayList<String> actInfo = new ArrayList<String>();
		String actJsonResponse = null;
		String identifier_id = null;
		String header_identifier = null;

		if (environment.contains("1")) {
			actJsonResponse = RestAssured.given().when()
					.get("http://act-env1.idc1.level3.com:8081/ac-ip-rs-web/rs/requestPayload?requestID=" + requestId)
					.body().asString();
		} else if (environment.contains("2")) {
			actJsonResponse = RestAssured.given().when()
					.get("http://act-env2.idc1.level3.com:8081/ac-ip-rs-web/rs/requestPayload?requestID=" + requestId)
					.body().asString();
		} else if (environment.contains("4")) {
			actJsonResponse = RestAssured.given().when()
					.get("http://act-env4.idc1.level3.com:8081/ac-ip-rs-web/rs/requestPayload?requestID=" + requestId)
					.body().asString();
		}
		JSONObject xmlJSONObj = XML.toJSONObject(actJsonResponse);
		String jsonPrettyPrintString = xmlJSONObj.toString(4);

		ArrayList identifier_id_list     = JsonPath.read(jsonPrettyPrintString,"$..item[?(@.name=='identifier_id')].value");
		ArrayList header_identifier_list = JsonPath.read(jsonPrettyPrintString,"$..item[?(@.name=='header.identifier')].value");
		if (identifier_id_list.size() > 0 && header_identifier_list.size() > 0) {
			identifier_id = (String) identifier_id_list.get(0);
			header_identifier = String.valueOf(header_identifier_list.get(0));
			actInfo.add(identifier_id);
			actInfo.add(header_identifier);
		}

		return actInfo;
	}

}
