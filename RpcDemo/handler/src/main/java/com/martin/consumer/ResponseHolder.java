package com.martin.consumer;

import com.martin.entity.Response;
import com.martin.entity.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * RPC 异步发送请求必备组件，用于保存回复结果的 map，并使用单一线程池定期删除过期 key
 **/
public class ResponseHolder {

	private static final Logger logger = LoggerFactory.getLogger(ResponseHolder.class);

	private static final Map<String, ResponseWrapper> futureMap = new ConcurrentHashMap<>();

	/**
	 * 单一线程池，检查是否过期
	 **/
	private static final ExecutorService removeExpireKeyExecutor = Executors.newSingleThreadExecutor();

	/**
	 * 启动线程，不断循环检查，删除超时未获取到结果的 key，防止内存泄漏
	 **/
	static {
		// 删除超时未获取到结果的 key，防止内存泄漏
		removeExpireKeyExecutor.execute(new Runnable() {
			@Override
			public void run() {
				for (;;) {
					try {
						for (Map.Entry<String, ResponseWrapper> entry : futureMap.entrySet()) {
							if (entry.getValue().isExpire()) {
								futureMap.remove(entry.getKey());
							}
							Thread.sleep(10);
						}
					} catch (InterruptedException e) {
						logger.error("移除过期回复线程中断: {}", e);
					}
				}
			}
		});
	}

	public static void initResponse(String requestId) {
		futureMap.put(requestId, new ResponseWrapper());
	}

	public static void putResponse(Response response) {
		// 获取数据时，将结果存入 future 中
		ResponseWrapper responseWrapper = futureMap.get(response.getResponseId());
		responseWrapper.setResponseTime(System.currentTimeMillis());
		// 将回复存入阻塞队列中
		responseWrapper.getResponseBlockingQueue().add(response);
		futureMap.put(response.getResponseId(), responseWrapper);
	}

	/**
	 * 从 future 中获取真正的结果
	 **/
	public static Response getResponseValue(String requestId, long timeout) {
		ResponseWrapper responseWrapper = futureMap.get(requestId);
		try {
			return responseWrapper.getResponseBlockingQueue().poll(timeout, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			logger.error("获取结果失败: " +  e);
		} finally {
			// 超时或未完成则移除
			futureMap.remove(requestId);
		}
		return null;
	}
}
