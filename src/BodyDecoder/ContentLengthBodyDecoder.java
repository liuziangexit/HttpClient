package com.game.http.BodyDecoder;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

import com.game.http.Util.Utility;

/** 
 * @author  liuziang
 * @contact liuziang@liuziangexit.com
 * @date    11/07/2018
 * 
 * 根据Content-Length头读取完整HTTP Body
 * 
 */

public class ContentLengthBodyDecoder implements IBodyDecoder {

    public ContentLengthBodyDecoder(int inputBodyLength) {
	bodyLength = inputBodyLength;
    }

    /**
     * @see IBodyDecoder
     */
    @Override
    public byte[] decode(AsynchronousSocketChannel channel, byte[] rawBody) throws Exception {
	if (rawBody.length == bodyLength)
	    return rawBody;

	ByteBuffer readBuffer = ByteBuffer.allocate(rawBody.length - bodyLength);
	while (readBuffer.position() != readBuffer.capacity())
	    if (channel.read(readBuffer).get() == -1)
		continue;
	return Utility.append(rawBody, bodyLength, readBuffer.array(), readBuffer.capacity());
    }

    // 已读到的body长度
    private int bodyLength;

}
