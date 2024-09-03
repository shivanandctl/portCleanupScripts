package com.arm.utilities;

import io.restassured.RestAssured;
import java.util.ArrayList;
import com.misc.utilities.Base;
import io.restassured.response.Response;

public class Arm {

	Base base = new Base();

	public String getServiceEnvironment(String service) {
		String environment = "";
		String sasiRes = "";
		Response response;

		String test1 = base.TEST1_SASI_ARM + service;
		String test2 = base.TEST2_SASI_ARM + service;
		String test4 = base.TEST4_SASI_ARM + service;

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
		return envCountList.toString();
	}
	
	public static void main(String[] args) {
		Arm arm = new Arm();
		System.out.println(arm.getServiceEnvironment("29/KXGS/660204//MS"));
	}
}
