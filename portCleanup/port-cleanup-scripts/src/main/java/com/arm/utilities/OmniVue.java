package com.arm.utilities;

import io.restassured.RestAssured;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.jayway.jsonpath.JsonPath;
import com.misc.utilities.Base;

import io.restassured.response.Response;

public class OmniVue {

	Base base = new Base();

	public ArrayList<String> armLabDevices() {
		ArrayList<String> labDevices = new ArrayList<String>();
//		labDevices.add("BRFDCOZS05W-15.07");//4L002
//		labDevices.add("BRFDCOZS02W-15.07");
//		labDevices.add("BRFDCOZS08W-001.01");//4L003
//		labDevices.add("LTTOCOLK01W-00089");//LABBRMCO4M001
		labDevices.add("BRFDCOZS11W-LABBRMCO.001.LAB..016.010");//LABBRMCONA301 
		labDevices.add("BRFDCOZS10W-LABBRMCO.001.LAB..018.017");//LABBRMCONA401
		return labDevices;
	}

	public LinkedHashMap<String, String> getUnisFromDevice(String device, String environment) {
		Response response;
		Response deviceIdResponse;
		String postUrl = "";
		String sasiArmUrl = "";
		String devId = "";
		if (environment.equalsIgnoreCase("test1")) {
			postUrl = base.TEST1_OMNIVUE;
			sasiArmUrl = base.TEST1_SASI_ARM;
		} else if (environment.equalsIgnoreCase("test2")) {
			postUrl = base.TEST2_OMNIVUE;
			sasiArmUrl = base.TEST2_SASI_ARM;
		} else if (environment.equalsIgnoreCase("test4")) {
			postUrl = base.TEST4_OMNIVUE;
			sasiArmUrl = base.TEST4_SASI_ARM;
		}

		deviceIdResponse = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and().when()
				.get(sasiArmUrl.replace("services", "devices") + device).then().extract().response();
		String deviceIdResponseString = deviceIdResponse.asString();
		ArrayList<String> deviceId = JsonPath.read(deviceIdResponse.asString(), "$..id");
		if (deviceId.size() > 0) {
			devId = deviceId.get(0);
			System.out.println(devId);
		} else {
			System.out.println(deviceIdResponseString);
			return null;
		}

		String payload = "{\r\n" + "    \"action\": \"getDetailsForTable\",\r\n" + "    \"module\": \"Device\",\r\n"
				+ "    \"param\": {\r\n" + "        \"deviceID\": " + devId + ",\r\n" + "        \"deviceName\": \""
				+ device + "\",\r\n" + "        \"isEthernetDevice\": true\r\n" + "    }\r\n" + "}";

		response = RestAssured.given().relaxedHTTPSValidation().header("Content-type", "application/json").and().body(payload)
				.when().post(postUrl).then().extract().response();

		ArrayList<String> portNum = JsonPath.read(response.asString(), "$..PORT");
		ArrayList<String> portType = JsonPath.read(response.asString(), "$..PORTTYPE");
		ArrayList<String> portStatus = JsonPath.read(response.asString(), "$..STATUSFORTOOLTIP");
		ArrayList<String> service = JsonPath.read(response.asString(), "$..SERVICE");
		LinkedHashMap<String, String> unis = new LinkedHashMap<String, String>();

		System.out.println("+--------Service and port info for the device::" + device);
		int validPt = portType.size();
		for (int i = 0; i < validPt; i++) {
			if (portType.get(i).equalsIgnoreCase("Pluggable") && portStatus.get(i).equalsIgnoreCase("In Service")) {
				unis.put(portNum.get(i), service.get(i));
			}
		}

		return unis;

	}

	public static void main(String[] args) {
		OmniVue ov = new OmniVue();

		ArrayList<String> devices = ov.armLabDevices();
		for (String device : devices) {
			LinkedHashMap<String, String> map = ov.getUnisFromDevice(device, "Test4");
			if (map == null) {
				
			} else {
				for (Entry<String, String> entry : map.entrySet()) {
					System.out.println("PortName: " + "Pluggable."+entry.getKey() + ", UNI: " + entry.getValue());
//					System.out.println(entry.getValue());
				}
			}

		}
	}
}
