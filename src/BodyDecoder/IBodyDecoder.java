package com.game.http.BodyDecoder;

import java.nio.channels.AsynchronousSocketChannel;

/** 
 * @author  liuziang
 * @contact liuziang@liuziangexit.com
 * @date    11/07/2018
 * 
 * HTTP Body解码器
 * 
 * 由于TCP流式传输的性质，某些时候HTTP消息不会被一次完整读取
 * IBodyDecoder负责将HTTP消息读取完整
 * 
 */

public interface IBodyDecoder {

    /**
     * 解码
     * @param channel
     * @param rawBody 首次读取的原始Body内容
     * @param bodyLength Body实际长度
     * @return 解码后的完整HTTP Body
     */
    public byte[] decode(AsynchronousSocketChannel channel, byte[] rawBody) throws Exception;

}
