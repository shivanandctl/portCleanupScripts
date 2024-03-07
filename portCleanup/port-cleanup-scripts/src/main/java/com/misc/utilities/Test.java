package com.misc.utilities;
import java.util.ArrayList;

import com.act.utilities.Act;
import com.asri.utilities.Asri;

public class Test {

	public static void main(String[] args) {

		Act act = new Act();
		Asri asri = new Asri();
		
		String service = "CO/KXFN/049527/LUMN";
		String env = "4";
//		ArrayList<String> ReqID_ServiceType_ReqType = new ArrayList<String>();
//		ReqID_ServiceType_ReqType = act.getRequestIDs(service, "4");
//		
//		for (String s : ReqID_ServiceType_ReqType) {
//			System.out.println(s);
//		}
		
//		ArrayList<String> actInfo = new ArrayList<String>();
//		actInfo = act.getActDetailsUsingRequestID("219178249", "4");
//		for (String s : actInfo) {
//			System.out.println(s);
//		}
		
		boolean actCleanupStatus = act.networkCleanup(service, env);
		if (actCleanupStatus) {
			System.out.println("Act Cleanup is successful");
			boolean asriCleanupStatus = asri.inventoryCleanUp(service, env);
			if (asriCleanupStatus) {
				System.out.println("ASRI Cleanup is successful");
			} else {
				System.out.println("ASRI Cleanup is not successful");
			}
		} else {
			System.out.println("Act Cleanup is not successful");
		}
		
		
		
	}

}
