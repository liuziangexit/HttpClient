package com.game.http.Util;

public class Utility {

    static public void memcpy(byte[] dst, byte[] src, int srcBegin, int copyLength) {
	for (int i = 0; i < copyLength; i++, srcBegin++)
	    dst[i] = src[srcBegin];
    }

    static public void memcpy(byte[] dst, byte[] src, int srcBegin, int copyLength, int dstBegin) {
	int i = dstBegin;
	while (i - dstBegin < copyLength)
	    dst[i++] = src[srcBegin++];
    }

    static public byte[] realloc(byte[] src, int newCapacity) {
	if (src.length == newCapacity)
	    return src;

	byte[] returnMe = new byte[newCapacity];
	int copyLength = src.length;
	if (newCapacity < src.length)
	    copyLength = newCapacity;

	for (int i = 0; i < copyLength; i++)
	    returnMe[i] = src[i];
	return returnMe;
    }

    static public byte[] append(byte[] dst, int dstLength, byte[] src, int srcLength) {
	if (dst.length >= dstLength + srcLength) {
	    memcpy(dst, src, 0, srcLength, dstLength);
	    return dst;
	}
	byte[] newBuff = new byte[dstLength + srcLength];
	memcpy(newBuff, dst, 0, dstLength);
	memcpy(newBuff, src, 0, srcLength, dstLength);
	return newBuff;
    }

    // 计算字符串进行UTF-8编码之后的长度
    static public int UTF8EncodedLength(CharSequence sequence) {
	int length = 0;
	for (int i = 0, len = sequence.length(); i < len; i++) {
	    char ch = sequence.charAt(i);
	    if (ch <= 0x7F) {
		length++;
	    } else if (ch <= 0x7FF) {
		length += 2;
	    } else if (Character.isHighSurrogate(ch)) {
		length += 4;
		++i;
	    } else {
		length += 3;
	    }
	}
	return length;
    }

    static public String CRLF = "\r\n";
    static public String CRLFx2 = "\r\n\r\n";
    static public byte[] CRLFBytes = new byte[] { 13, 10 };
    static public byte[] CRLFx2Bytes = new byte[] { 13, 10, 13, 10 };
    static public String WhiteSpace = " ";

}
