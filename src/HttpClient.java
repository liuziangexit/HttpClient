package com.game.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

import com.game.http.BodyDecoder.ChunkedBodyDecoder;
import com.game.http.BodyDecoder.ContentLengthBodyDecoder;
import com.game.http.Util.KMP;
import com.game.http.Util.Utility;
import com.game.server.impl.WServer;

/** 
 * @author  liuziang
 * @contact liuziang@liuziangexit.com
 * @date    10/27/2018
 * 
 * HTTP请求实用工具
 * 
 * 暂不支持压缩和长连接
 * 
 */

public final class HttpClient {

    // 对外接口↓

    /**
     * 异步HTTP GET
     * @param ipAddress     IP地址
     * @param port          端口 
     * @param host          HTTP Host头，可为null
     * @param path          Request-URI，可为null
     * @param urlParameter  URL参数，可为null
     * @param callback      回调，不可为null
     * @return 异步操作投递是否成功
     */
    static public boolean getAsync(String ipAddress, int port, String host, String path, QueryStringBuilder queryString,
	    IHttpRequestCallback callback) {
	return sendAsync(ipAddress, port, toHttpRequestString(host, path, "GET", queryString, null, null), callback);
    }

    /**
     * 异步HTTP POST
     * @param ipAddress     IP地址
     * @param port          端口 
     * @param host          HTTP Host头，可为null
     * @param path          Request-URI，可为null
     * @param urlParameter  URL参数，可为null
     * @param callback      回调，不可为null
     * @return 异步操作投递是否成功
     */
    static public boolean postAsync(String ipAddress, int port, String host, String path,
	    QueryStringBuilder queryString, IHttpRequestCallback callback) {
	String body = null;
	if (queryString != null)
	    body = queryString.toString();
	return sendAsync(ipAddress, port,
		toHttpRequestString(host, path, "POST", null, "application/x-www-form-urlencoded", body), callback);
    }

    // 内部实现↓

    // 生成HTTP请求报文
    static private String toHttpRequestString(String host, String path, String method, QueryStringBuilder queryString,
	    String contentType, String body) {
	StringBuilder builder = new StringBuilder();
	// Request-Line
	builder.append(method);
	builder.append(Utility.WhiteSpace);
	if (path != null)
	    builder.append(path);
	else
	    builder.append('/');
	if (queryString != null) {
	    builder.append('?');
	    builder.append(queryString.toString());
	}
	builder.append(Utility.WhiteSpace);
	builder.append("HTTP/1.1");
	builder.append(Utility.CRLF);
	// Headers
	if (host != null) {
	    builder.append("Host: ");
	    builder.append(host);
	    builder.append(Utility.CRLF);
	}
	builder.append("Connection: close");
	builder.append(Utility.CRLF);
	builder.append("Accept-Encoding: identity");
	builder.append(Utility.CRLF);
	if (contentType != null) {
	    builder.append("Content-Type: ");
	    builder.append(contentType);
	    builder.append("; charset=utf-8");
	    builder.append(Utility.CRLF);
	}
	if (body != null) {
	    builder.append("Content-Length: ");
	    builder.append(String.valueOf(Utility.UTF8EncodedLength(body)));
	} else {
	    builder.append("Content-Length: 0");
	}
	builder.append(Utility.CRLFx2);
	if (body != null)
	    builder.append(body);
	return builder.toString();
    }

    // 从HTTP响应报文中解析状态码
    static private int parseStatusCode(String responseString) {
	return Integer.parseInt(
		responseString.substring(0, responseString.indexOf(Utility.CRLF)).split(Utility.WhiteSpace)[1]);
    }

    // 从HTTP响应报文中解析Headers
    static private Map<String, String> parseHeaders(String responseString) {
	String headerString = responseString.substring(responseString.indexOf(Utility.CRLF) + Utility.CRLF.length(),
		responseString.indexOf(Utility.CRLFx2));
	String[] headers = headerString.split(Utility.CRLF);
	TreeMap<String, String> result = new TreeMap<String, String>();
	for (String header : headers) {
	    String[] keyValuePair = header.split(":");
	    result.put(keyValuePair[0], keyValuePair[1].trim());
	}
	return result;
    }

    // 读取到HTTP响应报文后
    static private void onRespond(Integer bytesTransferred, AsynchronousSocketChannel attachment, ByteBuffer readBuffer,
	    IHttpRequestCallback callback) {
	try {
	    if (bytesTransferred == -1) {
		userCallbackExecutorService.execute(new Runnable() {
		    public void run() {
			try {
			    callback.onFailed(new Exception("read socket failed"));
			} catch (Exception ex) {
			    ex.printStackTrace();
			}
		    }
		});
		return;
	    }

	    String rawResponse = new String(readBuffer.array(), 0, bytesTransferred, "US-ASCII");
	    int statusCode = parseStatusCode(rawResponse);
	    Map<String, String> headers = parseHeaders(rawResponse);

	    String charsetName = "UTF-8";
	    if (headers.containsKey("Content-Type")) {
		String contentType = headers.get("Content-Type");
		int charsetBegin = contentType.indexOf("charset");
		if (charsetBegin != -1)
		    charsetName = contentType.substring(charsetBegin).split("=")[1];
	    }

	    HttpResponse httpResponse = new HttpResponse();
	    httpResponse.setStatusCode(statusCode);
	    httpResponse.setHeaders(headers);

	    int beginOfBody = KMP.indexOf(readBuffer.array(), 0, bytesTransferred, Utility.CRLFx2Bytes)
		    + Utility.CRLFx2Bytes.length;
	    byte[] fullBody = null;
	    if (headers.containsKey("Content-Length")) {
		byte[] bodyBytes = new byte[Integer.parseInt(headers.get("Content-Length"))];
		Utility.memcpy(bodyBytes, readBuffer.array(), beginOfBody, bytesTransferred - beginOfBody);
		fullBody = new ContentLengthBodyDecoder(bytesTransferred - beginOfBody).decode(attachment, bodyBytes);
	    } else if (headers.containsKey("Transfer-Encoding") && headers.get("Transfer-Encoding").equals("chunked")) {
		byte[] bodyBytes = new byte[bytesTransferred - beginOfBody];
		Utility.memcpy(bodyBytes, readBuffer.array(), beginOfBody, bytesTransferred - beginOfBody);
		fullBody = new ChunkedBodyDecoder().decode(attachment, bodyBytes);
	    } else {
		throw new Exception("unsupported Transfer-Encoding");
	    }

	    httpResponse.setBody(new String(fullBody, charsetName));
	    final HttpResponse pretendCopy = httpResponse;
	    userCallbackExecutorService.execute(new Runnable() {
		@Override
		public void run() {
		    try {
			callback.onRespond(pretendCopy.getStatusCode(), pretendCopy.getHeaders(),
				pretendCopy.getBody());
		    } catch (Exception ex) {
			// 用户回调异常
			ex.printStackTrace();
		    }
		}
	    });
	} catch (Exception ex) {
	    ex.printStackTrace();
	    userCallbackExecutorService.execute(new Runnable() {
		@Override
		public void run() {
		    try {
			callback.onFailed(
				new Exception("an error occurred while parsing the response: " + ex.getMessage()));
		    } catch (Exception ex) {
			ex.printStackTrace();
		    }
		}
	    });
	} finally {
	    try {
		if (attachment.isOpen())
		    attachment.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    static private boolean sendAsync(String ipAddress, int port, String requestString, IHttpRequestCallback callback) {
	AsynchronousSocketChannel channel;
	try {
	    channel = AsynchronousSocketChannel.open();
	} catch (IOException e1) {
	    return false;
	}
	channel.connect(new InetSocketAddress(ipAddress, port), channel,
		new CompletionHandler<Void, AsynchronousSocketChannel>() {
		    @Override
		    public void completed(Void arg0, AsynchronousSocketChannel arg1) {
			// 连接成功后...
			byte[] requestBytes = null;
			try {
			    requestBytes = requestString.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e1) {
			    // never happen
			    e1.printStackTrace();
			}
			// 发送HTTP请求
			arg1.write(ByteBuffer.wrap(requestBytes), arg1,
				new CompletionHandler<Integer, AsynchronousSocketChannel>() {
				    @Override
				    public void completed(Integer bytesTransferred,
					    AsynchronousSocketChannel attachment) {
					// 请求发送成功后...
					// 投递一个异步读请求
					ByteBuffer readBuffer = ByteBuffer.allocate(4096);
					attachment.read(readBuffer, attachment,
						new CompletionHandler<Integer, AsynchronousSocketChannel>() {
						    @Override
						    public void completed(Integer bytesTransferred,
							    AsynchronousSocketChannel attachment) {
							// 读取到响应后...
							onRespond(bytesTransferred, attachment, readBuffer, callback);
						    }

						    @Override
						    public void failed(Throwable exc,
							    AsynchronousSocketChannel attachment) {
							// 读取HTTP响应失败
							try {
							    userCallbackExecutorService.execute(new Runnable() {
								@Override
								public void run() {
								    try {
									callback.onFailed(exc);
								    } catch (Exception ex) {
									ex.printStackTrace();
								    }
								}
							    });
							} finally {
							    try {
								if (attachment.isOpen())
								    attachment.close();
							    } catch (IOException e) {
								e.printStackTrace();
							    }
							}
						    }
						});
				    }

				    @Override
				    public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
					// 发送HTTP请求失败
					try {
					    userCallbackExecutorService.execute(new Runnable() {
						@Override
						public void run() {
						    try {
							callback.onFailed(exc);
						    } catch (Exception ex) {
							ex.printStackTrace();
						    }
						}
					    });
					} finally {
					    try {
						if (attachment.isOpen())
						    attachment.close();
					    } catch (IOException e) {
						e.printStackTrace();
					    }
					}
				    }

				});
		    }

		    @Override
		    public void failed(Throwable arg0, AsynchronousSocketChannel arg1) {
			// 连接到服务器失败
			userCallbackExecutorService.execute(new Runnable() {
			    @Override
			    public void run() {
				try {
				    callback.onFailed(arg0);
				} catch (Exception ex) {
				    ex.printStackTrace();
				}
			    }
			});
		    }
		});
	return true;
    }

    static private ExecutorService userCallbackExecutorService = WServer.getInstance().getExecutorService();

}
