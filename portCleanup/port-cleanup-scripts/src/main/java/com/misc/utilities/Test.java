package com.misc.utilities;
import java.util.ArrayList;

import com.act.utilities.Act;
import com.act.utilities.Rubicon;
import com.asri.utilities.Asri;

public class Test {

	public static void main(String[] args) {
		Act act = new Act();
		ArrayList<String> requestIDs = act.getRequestIDs("CO/KXFN/049565/LUMN", "Test4");
        System.out.println(requestIDs);		
	}

}
