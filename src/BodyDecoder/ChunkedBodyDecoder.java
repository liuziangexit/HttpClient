package com.game.http.BodyDecoder;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

import com.game.http.Util.KMP;
import com.game.http.Util.Utility;

/** 
 * @author  liuziang
 * @contact liuziang@liuziangexit.com
 * @date    11/07/2018
 * @see https://tools.ietf.org/html/rfc2616#section-3.6.1
 * @see https://en.wikipedia.org/wiki/Chunked_transfer_encoding
 * 
 * 根据HTTP分块编码规则读取完整HTTP Body
 * TODO:取消body的额外读取，减少内存拷贝
 * 
 */

public class ChunkedBodyDecoder implements IBodyDecoder {

    /**
     * @see IBodyDecoder
     */
    @Override
    public byte[] decode(AsynchronousSocketChannel channel, byte[] rawBody) throws Exception {
	ByteBuffer readBuffer = null;
	byte[] result = null;

	// chunk长度起始下标
	int chunkSizeBegin = 0;
	// chunk长度结束下标
	int chunkSizeEnd = KMP.indexOf(rawBody, chunkSizeBegin, rawBody.length, Utility.CRLFBytes);
	byte[] chunkSizeBytes = new byte[12];
	Utility.memcpy(chunkSizeBytes, rawBody, chunkSizeBegin, chunkSizeEnd - chunkSizeBegin);
	// chunk长度
	int chunkSize = Integer.parseInt(new String(chunkSizeBytes, 0, chunkSizeEnd - chunkSizeBegin, "US-ASCII"), 16);
	// chunk内容起始下标
	int chunkBodyBegin = chunkSizeEnd + Utility.CRLFBytes.length;
	// chunk内容结束下标
	int chunkBodyEnd = chunkBodyBegin + chunkSize;
	// chunk内容实际结束下标(已读取的chunk内容)
	int chunkBodyRealEnd = rawBody.length;
	while (true) {
	    if (chunkBodyEnd > chunkBodyRealEnd) {
		// 如果chunk内容不完整...
		if (readBuffer == null || readBuffer.capacity() < chunkBodyEnd - chunkBodyRealEnd)
		    readBuffer = ByteBuffer.allocate(chunkBodyEnd - chunkBodyRealEnd);
		else
		    readBuffer.rewind();

		// 读取，直到chunkBody完整
		while (readBuffer.position() < chunkBodyEnd - chunkBodyRealEnd)
		    if (channel.read(readBuffer).get() == -1)
			continue;
		rawBody = Utility.realloc(rawBody, chunkBodyRealEnd + readBuffer.position());
		Utility.memcpy(rawBody, readBuffer.array(), 0, readBuffer.position(), chunkBodyRealEnd);
		chunkBodyRealEnd = chunkBodyEnd;
	    }
	    // 拷贝本次读取的chunk内容到输出buffer
	    if (result == null) {
		result = new byte[chunkBodyEnd - chunkBodyBegin];
		Utility.memcpy(result, rawBody, chunkBodyBegin, chunkBodyEnd - chunkBodyBegin);
	    } else {
		int oldLength = result.length;
		result = Utility.realloc(result, result.length + chunkBodyEnd - chunkBodyBegin);
		Utility.memcpy(result, rawBody, chunkBodyBegin, chunkBodyEnd - chunkBodyBegin, oldLength);
	    }
	    // 读取下一chunk的size
	    chunkSizeBegin = chunkBodyEnd + Utility.CRLFBytes.length;
	    if (chunkSizeBegin < rawBody.length)
		chunkSizeEnd = KMP.indexOf(rawBody, chunkSizeBegin, rawBody.length, Utility.CRLFBytes);
	    else
		chunkSizeEnd = -1;
	    if (chunkSizeEnd == -1) {
		// 如果下一个chunk的size不完整...
		if (readBuffer == null || readBuffer.capacity() < 8)
		    readBuffer = ByteBuffer.allocate(8);
		else
		    readBuffer.rewind();
		// 尝试读取下一个chunk的size
		// 若读取8个字节后仍未完整读取chunk size，则以异常中止操作
		while (true) {
		    if (channel.read(readBuffer).get() == -1)
			continue;
		    if (chunkSizeBegin >= rawBody.length)
			// body当前未达到chunk size的第一个字符
			chunkSizeEnd = KMP.indexOf(readBuffer.array(), chunkSizeBegin - rawBody.length,
				readBuffer.position(), Utility.CRLFBytes);
		    else
			// body当前已经达到chunk size的第一个字符
			chunkSizeEnd = KMP.indexOf(readBuffer.array(), 0, readBuffer.position(), Utility.CRLFBytes);
		    if (chunkSizeEnd != -1) {
			chunkSizeEnd += rawBody.length;
			rawBody = Utility.append(rawBody, rawBody.length, readBuffer.array(), readBuffer.position());
			break;
		    }
		    if (readBuffer.position() >= 8) {
			// 处理body位置在CRLF符号中间的情况
			rawBody = Utility.append(rawBody, rawBody.length, readBuffer.array(), readBuffer.position());
			chunkSizeEnd = KMP.indexOf(rawBody, chunkSizeBegin, rawBody.length, Utility.CRLFBytes);
			if (chunkSizeEnd == -1)
			    throw new Exception("next chunk size not found");
			break;
		    }
		}
	    }
	    // 计算下一个chunk的size
	    Utility.memcpy(chunkSizeBytes, rawBody, chunkSizeBegin, chunkSizeEnd - chunkSizeBegin);
	    chunkSize = Integer.parseInt(new String(chunkSizeBytes, 0, chunkSizeEnd - chunkSizeBegin, "US-ASCII"), 16);
	    chunkBodyBegin = chunkSizeEnd + Utility.CRLFBytes.length;
	    chunkBodyEnd = chunkBodyBegin + chunkSize;
	    chunkBodyRealEnd = rawBody.length;

	    // 长度为0的块是消息体的结束
	    if (chunkSize == 0)
		break;
	}
	return result;
    }

}
