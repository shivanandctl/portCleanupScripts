package com.misc.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class Base {

	// TEST1 VARIABLES
	public static String TEST1_SASI_ASRI = "";
	public static String TEST1_SASI_ARM = "";
	
	public static String TEST1_AUTH_URL = "";
	public static String TEST1_FETCH_URL = "";
	public static String TEST1_FETCH_DETAILS_FROM_REQ_ID = "";
	
	public static String TEST1_OMNIVUE = "";

	// TEST2 VARIABLES
	public static String TEST2_SASI_ASRI = "";
	public static String TEST2_SASI_ARM = "";
	
	public static String TEST2_AUTH_URL = "";
	public static String TEST2_FETCH_URL = "";
	public static String TEST2_FETCH_DETAILS_FROM_REQ_ID = "";
	
	public static String TEST2_OMNIVUE = "";

	// TEST4 VARIABLES
	public static String TEST4_SASI_ASRI = "";
	public static String TEST4_SASI_ARM = "";
	
	public static String TEST4_AUTH_URL = "";
	public static String TEST4_FETCH_URL = "";
	public static String TEST4_FETCH_DETAILS_FROM_REQ_ID = "";
	
	public static String TEST4_OMNIVUE = "";

	// IP VARIABLES
	public static String TEST_GET_IP = "";
	public static String TEST_IP_RELEASE = "";

	// LAB RUBICON VARIABLES
	public static String RUBICON_INT_DESCRIPTION_URL = "";
	public static String RUBICON_CHECKPORTS_URL = "";
	public static String username = "AC70068";
	public static String password = "Arshi1994#";

	public Base() {
		configMethod();
	}

	public static HashMap<String, String> configMethod() {
		String configFilePath = System.getProperty("user.dir") + "\\environment.Properties";
		// System.out.println(configFilePath);

		File configFile = new File(configFilePath);
		HashMap<String, String> envDetails = new HashMap<String, String>();

		try {
			FileReader reader = new FileReader(configFile);
			Properties props = new Properties();
			props.load(reader);

			// TEST1
			TEST1_SASI_ASRI = props.getProperty("TEST1_SASI_ASRI");
			TEST1_SASI_ARM = props.getProperty("TEST1_SASI_ARM");
			TEST1_AUTH_URL = props.getProperty("TEST1_AUTH_URL");
			TEST1_FETCH_URL = props.getProperty("TEST1_FETCH_URL");
			TEST1_FETCH_DETAILS_FROM_REQ_ID = props.getProperty("TEST1_FETCH_DETAILS_FROM_REQ_ID");
			
			TEST1_OMNIVUE = props.getProperty("TEST1_OMNIVUE");

			// TEST2
			TEST2_SASI_ASRI = props.getProperty("TEST2_SASI_ASRI");
			TEST2_SASI_ARM = props.getProperty("TEST2_SASI_ARM");
			TEST2_AUTH_URL = props.getProperty("TEST2_AUTH_URL");
			TEST2_FETCH_URL = props.getProperty("TEST2_FETCH_URL");
			TEST2_FETCH_DETAILS_FROM_REQ_ID = props.getProperty("TEST2_FETCH_DETAILS_FROM_REQ_ID");
			
			TEST2_OMNIVUE = props.getProperty("TEST2_OMNIVUE");

			// TEST4
			TEST4_SASI_ASRI = props.getProperty("TEST4_SASI_ASRI");
			TEST4_SASI_ARM = props.getProperty("TEST4_SASI_ARM");
			
			TEST4_AUTH_URL = props.getProperty("TEST4_AUTH_URL");
			TEST4_FETCH_URL = props.getProperty("TEST4_FETCH_URL");
			TEST4_FETCH_DETAILS_FROM_REQ_ID = props.getProperty("TEST4_FETCH_DETAILS_FROM_REQ_ID");
			
			TEST4_OMNIVUE = props.getProperty("TEST4_OMNIVUE");

			// IP
			TEST_GET_IP = props.getProperty("TEST_GET_IP");
			TEST_IP_RELEASE = props.getProperty("TEST_IP_RELEASE");

			// LAB RUBICON
			RUBICON_INT_DESCRIPTION_URL = props.getProperty("RUBICON_INT_DESCRIPTION_URL");
			RUBICON_CHECKPORTS_URL = props.getProperty("RUBICON_CHECKPORTS_URL");
			
			
			envDetails.put("TEST1_SASI_ASRI", TEST1_SASI_ASRI);
			envDetails.put("TEST1_SASI_ARM", TEST1_SASI_ARM);
			envDetails.put("TEST1_AUTH_URL", TEST1_AUTH_URL);
			envDetails.put("TEST1_FETCH_URL", TEST1_FETCH_URL);
			envDetails.put("TEST1_FETCH_DETAILS_FROM_REQ_ID", TEST1_FETCH_DETAILS_FROM_REQ_ID);
			
			envDetails.put("TEST1_OMNIVUE", TEST1_OMNIVUE);

			envDetails.put("TEST2_SASI_ASRI", TEST2_SASI_ASRI);
			envDetails.put("TEST2_SASI_ARM", TEST2_SASI_ARM);
			envDetails.put("TEST2_AUTH_URL", TEST2_AUTH_URL);
			envDetails.put("TEST2_FETCH_URL", TEST2_FETCH_URL);
			envDetails.put("TEST2_FETCH_DETAILS_FROM_REQ_ID", TEST2_FETCH_DETAILS_FROM_REQ_ID);
			
			envDetails.put("TEST2_OMNIVUE", TEST2_OMNIVUE);

			envDetails.put("TEST4_SASI_ASRI", TEST4_SASI_ASRI);
			envDetails.put("TEST4_SASI_ARM", TEST4_SASI_ARM);
			envDetails.put("TEST4_AUTH_URL", TEST4_AUTH_URL);
			envDetails.put("TEST4_FETCH_URL", TEST4_FETCH_URL);
			envDetails.put("TEST4_FETCH_DETAILS_FROM_REQ_ID", TEST4_FETCH_DETAILS_FROM_REQ_ID);
			
			envDetails.put("TEST4_OMNIVUE", TEST4_OMNIVUE);

			envDetails.put("TEST_GET_IP", TEST_GET_IP);
			envDetails.put("TEST_IP_RELEASE", TEST_IP_RELEASE);

			envDetails.put("RUBICON_INT_DESCRIPTION_URL", RUBICON_INT_DESCRIPTION_URL);
			envDetails.put("RUBICON_CHECKPORTS_URL", RUBICON_CHECKPORTS_URL);			
			

			reader.close();
		} catch (FileNotFoundException ex) {
			System.out.println("File not found::" + ex);
			System.out.println("Cannot find file :: environment.Properties");
		} catch (IOException ex) {
			System.out.println("IO Exception::" + ex);
		}
		return envDetails;
	}
}
