package sampleclass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ClassForCreatingJUnit {

	private ArrayList<String> arraylist;
	private HashMap<String, String> hashMap;
	private HashSet<Integer> hashset;
	private Other1 other1 = new Other1();
	private Other2 other2 = new Other2();

	public ClassForCreatingJUnit() {
		arraylist = new ArrayList<>();
		hashMap = new HashMap<>();
		hashset = new HashSet<>();
	}

	public void method1(String s1, String s2) {
		arraylist.add(s1);
		arraylist.add(s2);
		hashset.add(s1.length());
		hashset.add(s2.length());
		hashMap.put(s1, s2);
	}

	public void method2() {
		other1.other1Method1("");
		other1.other1Method2();
		other1.other1Method3();
		other2.other2Method1("");
		other2.other2Method2();
		other1.other1Method4(null, null);
	}
	
	
	
	private void ev1() {
		
	}
}
