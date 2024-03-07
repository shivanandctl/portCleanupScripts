package com.act.utilities;

import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.asri.utilities.Asri;
import com.misc.utilities.Base;

import io.restassured.response.Response;

public class Rubicon {

	Base base = new Base();

	// List of Lab Devices
	public ArrayList<String> listLabDevices() {
		ArrayList<String> labDevices = new ArrayList<String>();
//		labDevices.add("LABBRMCOW2212");
//		labDevices.add("LABBRMCOW2213");
		labDevices.add("LABBRMCOW2310");
		labDevices.add("LABBRMCOW2311");
		labDevices.add("LABBRMCOYP301");
		labDevices.add("LABBRMCOYP305");
		labDevices.add("LABBRMCOYJ302");
		labDevices.add("LABBRMCOYJ304");
		labDevices.add("LABBRMCOZN301");
		labDevices.add("LABBRMCOW2302");
		return labDevices;
	}

	// fetch uni's on a lab device
	public ArrayList<String> fetchUnisFromDevice(String device) {
		ArrayList<String> uniList = new ArrayList<String>();
		uniList.clear();
		uniList.add(device);
		
		String username = base.username;
		String password = base.password;

		// read from environment.properties file
		String rubiconURL = base.RUBICON_INT_DESCRIPTION_URL + device;
		Response response;

		response = given().relaxedHTTPSValidation().header("Content-type", "application/json").and().when()
				.get(rubiconURL).then().extract().response();
		String rubRes = response.asString();
		
		response.getStatusLine();
//		 System.out.println("Status Line:: "+response.getStatusLine());

		Pattern pattern = Pattern.compile("CO/KXFN/\\d+/LUMN");
		Matcher matcher = pattern.matcher(rubRes);
		while (matcher.find()) {
			uniList.add(matcher.group());
		}
		
		Pattern pattern1 = Pattern.compile("!!/KXFN/\\d+/LUMN");
		Matcher matcher1 = pattern1.matcher(rubRes);
		while (matcher1.find()) {
			uniList.add(matcher1.group());
		}

		return uniList;

	}
	
	//fetch uni's on all lab device
	
	public ArrayList<ArrayList> fetchUnisFromAllDevices(){
		Rubicon rubicon = new Rubicon();
		rubicon.listLabDevices();
		
		//create arraylist for all devices
		ArrayList<ArrayList> uniListAllDevices = new ArrayList<ArrayList>();
		
		//create arraylist for each device

		ArrayList<String> uniList_LABBRMCOW2212 = new ArrayList<String>();
		ArrayList<String> uniList_LABBRMCOW2213 = new ArrayList<String>();
		ArrayList<String> uniList_LABBRMCOW2310 = new ArrayList<String>();
		ArrayList<String> uniList_LABBRMCOW2311 = new ArrayList<String>();
		ArrayList<String> uniList_LABBRMCOYP301 = new ArrayList<String>();
		ArrayList<String> uniList_LABBRMCOYP305 = new ArrayList<String>();
		ArrayList<String> uniList_LABBRMCOYJ302 = new ArrayList<String>();
		ArrayList<String> uniList_LABBRMCOYJ304 = new ArrayList<String>();
		ArrayList<String> uniList_LABBRMCOZN301 = new ArrayList<String>();
		ArrayList<String> uniList_LABBRMCOW2302 = new ArrayList<String>();

		for (String device : rubicon.listLabDevices()) {
			
			if (device.contains("LABBRMCOW2212")) {
				System.out.println("Fetching UNIs on " + device);
				uniList_LABBRMCOW2212 = rubicon.fetchUnisFromDevice(device);
				uniListAllDevices.add(uniList_LABBRMCOW2212);
			} else if (device.contains("LABBRMCOW2213")) {
				System.out.println("Fetching UNIs on " + device);
				uniList_LABBRMCOW2213 = rubicon.fetchUnisFromDevice(device);
				uniListAllDevices.add(uniList_LABBRMCOW2213);				
			} else if (device.contains("LABBRMCOW2310")) {
				System.out.println("Fetching UNIs on " + device);
				uniList_LABBRMCOW2310 = rubicon.fetchUnisFromDevice(device);
				uniListAllDevices.add(uniList_LABBRMCOW2310);
			} else if (device.contains("LABBRMCOW2311")) {
				System.out.println("Fetching UNIs on " + device);
				uniList_LABBRMCOW2311 = rubicon.fetchUnisFromDevice(device);
				uniListAllDevices.add(uniList_LABBRMCOW2311);
			} else if (device.contains("LABBRMCOYP301")) {
				System.out.println("Fetching UNIs on " + device);
				uniList_LABBRMCOYP301 = rubicon.fetchUnisFromDevice(device);
				uniListAllDevices.add(uniList_LABBRMCOYP301);
			} else if (device.contains("LABBRMCOYP305")) {
				System.out.println("Fetching UNIs on " + device);
				uniList_LABBRMCOYP305 = rubicon.fetchUnisFromDevice(device);
				uniListAllDevices.add(uniList_LABBRMCOYP305);
			} else if (device.contains("LABBRMCOYJ302")) {
				System.out.println("Fetching UNIs on " + device);
				uniList_LABBRMCOYJ302 = rubicon.fetchUnisFromDevice(device);
				uniListAllDevices.add(uniList_LABBRMCOYJ302);
			} else if (device.contains("LABBRMCOYJ304")) {
				System.out.println("Fetching UNIs on " + device);
				uniList_LABBRMCOYJ304 = rubicon.fetchUnisFromDevice(device);
				uniListAllDevices.add(uniList_LABBRMCOYJ304);
			} else if (device.contains("LABBRMCOZN301")) {
				System.out.println("Fetching UNIs on " + device);
				uniList_LABBRMCOZN301 = rubicon.fetchUnisFromDevice(device);
				uniListAllDevices.add(uniList_LABBRMCOZN301);
			}else if (device.contains("LABBRMCOW2302")) {
				System.out.println("Fetching UNIs on " + device);
				uniList_LABBRMCOW2302 = rubicon.fetchUnisFromDevice(device);
				uniListAllDevices.add(uniList_LABBRMCOW2302);
			}
			else {
				System.out.println("No UNI's found on " + device);
			}
		}
		
		return uniListAllDevices;
		
	}

	public static void main(String[] args) {
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
					System.out.println(uniList.get(i)+"\t"+asri.getServiceEnvironment(uniList.get(i)));
				}
				System.out.println("#-------------------------------------------------------------------#\n\n");
			} else {
				System.out.println("No UNI's found on " + device);
			}
		}
		
	}

}
