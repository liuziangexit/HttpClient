package com.game.http.Test;

import java.util.HashMap;

import com.game.http.QueryStringBuilder;

public class HttpTest {

    public static void main(String[] args) {
	boolean isAllPassed = true;
	// ---------------------------------
	try {
	    QueryStringBuilder.createQueryString(null, "zzz");
	    System.out.println("QueryStringBuilder测试1-FAILED");
	    isAllPassed = false;
	} catch (IllegalArgumentException ex) {
	    System.out.println("QueryStringBuilder测试1-PASS");
	}
	// ---------------------------------
	try {
	    if (QueryStringBuilder.createQueryString("name1", "zzz").toString().equals("?name1=zzz")) {
		System.out.println("QueryStringBuilder测试2-PASS");
	    } else {
		System.out.println("QueryStringBuilder测试2-FAILED");
		isAllPassed = false;
	    }
	} catch (IllegalArgumentException ex) {
	    System.out.println("QueryStringBuilder测试2-FAILED");
	    isAllPassed = false;
	}
	// ---------------------------------
	try {
	    if (QueryStringBuilder.createQueryString("name1", "zzz").addParam("name2", "zzz2").toString()
		    .equals("?name1=zzz&name2=zzz2")) {
		System.out.println("QueryStringBuilder测试3-PASS");
	    } else {
		System.out.println("QueryStringBuilder测试3-FAILED");
		isAllPassed = false;
	    }
	} catch (IllegalArgumentException ex) {
	    System.out.println("QueryStringBuilder测试3-FAILED");
	    isAllPassed = false;
	}
	// ---------------------------------
	try {
	    HashMap<String, Object> map = new HashMap<String, Object>();
	    map.put("2018", "RDR2");
	    map.put("2016", "BF1");
	    map.put("2015", "GTAV");
	    map.put("NumberTest", 9710);
	    if (QueryStringBuilder.createQueryStringFromMap(map).toString()
		    .equals("?2018=RDR2&2016=BF1&2015=GTAV&NumberTest=9710")) {
		System.out.println("QueryStringBuilder测试4-PASS");
	    } else {
		System.out.println("QueryStringBuilder测试4-FAILED");
		isAllPassed = false;
	    }
	} catch (IllegalArgumentException ex) {
	    System.out.println("QueryStringBuilder测试4-FAILED");
	    isAllPassed = false;
	}
	// ---------------------------------
	try {
	    HashMap<String, Object> map = new HashMap<String, Object>();
	    map.put("2018", "RDR2");
	    map.put("2016", "BF1");
	    map.put("", "GTAV");
	    map.put("NumberTest", 9710);
	    QueryStringBuilder.createQueryStringFromMap(map);
	    System.out.println("QueryStringBuilder测试5-FAILED");
	    isAllPassed = false;
	} catch (IllegalArgumentException ex) {
	    System.out.println("QueryStringBuilder测试5-PASS");
	}
	// ---------------------------------
	try {
	    HashMap<String, Object> map = new HashMap<String, Object>();
	    QueryStringBuilder.createQueryStringFromMap(map);
	    System.out.println("QueryStringBuilder测试6-FAILED");
	    isAllPassed = false;
	} catch (IllegalArgumentException ex) {
	    System.out.println("QueryStringBuilder测试6-PASS");
	}
	// ---------------------------------
	try {
	    QueryStringBuilder.createQueryStringFromMap(null);
	    System.out.println("QueryStringBuilder测试7-FAILED");
	    isAllPassed = false;
	} catch (IllegalArgumentException ex) {
	    System.out.println("QueryStringBuilder测试7-PASS");
	}
	// ---------------------------------
	if (isAllPassed)
	    System.out.println("全部通过");
	else
	    System.out.println("未通过");
    }

}
