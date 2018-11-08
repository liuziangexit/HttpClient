package com.game.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;

/** 
 * @author  liuziang
 * @contact liuziang@liuziangexit.com
 * @date    11/06/2018
 * 
 * QueryString构造器
 * 
 */

public class QueryStringBuilder {

    // 对外接口↓

    /**
     * 从Map<String,Object>创建一个QueryString，以UTF-8编码
     * @exception IllegalArgumentException 当map不合法时
     * @exception UnsupportedCharsetException 当字符编码不受支持时
     */
    static public QueryStringBuilder createQueryStringFromMap(Map<String, Object> map) {
	return createQueryStringFromMap(map, "UTF-8");
    }

    /**
     * 从Map<String,Object>创建一个QueryString，以指定的字符集编码
     * @exception IllegalArgumentException 当map不合法时
     * @exception UnsupportedCharsetException 当字符编码不受支持时
     */
    static public QueryStringBuilder createQueryStringFromMap(Map<String, Object> map, String charset) {
	if (map == null || map.size() == 0)
	    throw new IllegalArgumentException();

	boolean isFirst = true;
	QueryStringBuilder builder = new QueryStringBuilder();
	for (Map.Entry<String, Object> kv : map.entrySet()) {
	    builder.addParam(kv.getKey(), kv.getValue().toString(), charset, isFirst);
	    isFirst = false;
	}
	return builder;
    }

    /**
     * 创建一个QueryString，并设定首个名值对，以UTF-8编码
     * @exception IllegalArgumentException 当name为null或空时
     * @exception UnsupportedCharsetException 当字符编码不受支持时
     */
    static public QueryStringBuilder createQueryString(String name, String value) {
	return createQueryString(name, value, "UTF-8");
    }

    /**
     * 创建一个QueryString，并设定首个名值对，以指定的字符集编码
     * @exception IllegalArgumentException 当name为null或空时
     * @exception UnsupportedCharsetException 当字符编码不受支持时
     */
    static public QueryStringBuilder createQueryString(String name, String value, String charset) {
	QueryStringBuilder builder = new QueryStringBuilder();
	return builder.addParam(name, value, charset, true);
    }

    /**
     * 添加名值对，以UTF-8编码
     * @exception IllegalArgumentException 当name为null或空时
     * @exception UnsupportedCharsetException 当字符编码不受支持时
     */
    public QueryStringBuilder addParam(String name, String value) {
	return addParam(name, value, "UTF-8");
    }

    /**
     * 添加名值对，以指定的字符集编码
     * @exception IllegalArgumentException 当name为null或空时
     * @exception UnsupportedCharsetException 当字符编码不受支持时
     */
    public QueryStringBuilder addParam(String name, String value, String charset) {
	return addParam(name, value, charset, false);
    }

    /**
     * 返回经过URL编码的QueryString
     */
    @Override
    public String toString() {
	return sb.toString();
    }

    // 实现↓

    private QueryStringBuilder() {
    }

    private QueryStringBuilder addParam(String name, String value, String charset, boolean isFirst) {
	if (name == null || name.equals(""))
	    throw new IllegalArgumentException("name can not be null or empty");
	String encodedName, encodedValue;
	try {
	    encodedName = URLEncoder.encode(name, charset);
	    encodedValue = URLEncoder.encode(value, charset);
	} catch (UnsupportedEncodingException e) {
	    throw new UnsupportedCharsetException(charset);
	}
	if (!isFirst)
	    sb.append('&');
	sb.append(encodedName);
	sb.append('=');
	sb.append(encodedValue);
	return this;
    }

    private StringBuilder sb = new StringBuilder();

}
