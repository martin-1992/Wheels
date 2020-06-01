package com.martin.config;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import java.io.IOException;import java.io.InputStream;import java.util.Properties;/** * 解析 ZooKeeper.properties 配置文件 **/public class ZooKeeperProperty {    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperProperty.class);    /**     * zookeeper 服务地址     **/    private static final String zkService;    /**     * zookeeper session 超时时间     **/    private static final int zkSessionTimeout;    /**     * zookeeper connection 超时时间     **/    private static int zkConnectionTimeout;    /**     * 序列化算法类型     **/    private static final String serializeType;    /**     * 每个服务端提供者的 Netty 连接数，Netty Channel 阻塞队列的长度     **/    private static final int nettyChannelConnectCount;    /**     * 最大重试次数     **/    private static final int maxRetries;    private static final int baseSleepTimeMs;    /**     * zk 节点根路径     **/    private static String ROOT_PATH;    private static String PROVIDER_PATH;    private static String CONSUMER_PATH;    /**     * 配置文件的地址     **/    private static final String PROPERTY_CLASSPATH = "/zookeeper.properties";    private static final Properties properties = new Properties();    static {        InputStream inputStream = null;        try {            inputStream = ZooKeeperProperty.class.getResourceAsStream(PROPERTY_CLASSPATH);            if (inputStream == null) {                logger.error("配置文件 zookeeper.properties 没找到");                throw new IllegalArgumentException("配置文件没找到");            }            // 载入配置文件            properties.load(inputStream);            // 解析配置文件            zkService = properties.getProperty("zk_service");            zkSessionTimeout = Integer.parseInt(properties.getProperty("zk_session_timeout", "500"));            zkConnectionTimeout = Integer.parseInt(properties.getProperty("zk_connection_timeout", "500"));            nettyChannelConnectCount = Integer.parseInt(properties.getProperty("netty_channel_connect_count", "10"));            baseSleepTimeMs = Integer.parseInt(properties.getProperty("base_sleep_time_ms", "1000"));            maxRetries = Integer.parseInt(properties.getProperty("max_retries", "3"));            serializeType = properties.getProperty("serialize_type", "FastJsonSeralizer");            ROOT_PATH = properties.getProperty("root_path");            PROVIDER_PATH = properties.getProperty("provider_path");            CONSUMER_PATH = properties.getProperty("consumer_path");        } catch (IOException e) {            logger.error("载入 zookeeper.properties 失败: " + e);            throw new RuntimeException("载入 zookeeper.properties 失败");        } finally {            if (inputStream != null) {                try {                    inputStream.close();                } catch (IOException e) {                    logger.error("配置文件，输入流关闭失败: " + e);                }            }        }    }    public static String getZkService() {        return zkService;    }    public static int getZkSessionTimeout() {        return zkSessionTimeout;    }    public static int getZkConnectionTimeout() {        return zkConnectionTimeout;    }    public static String getSerializeType() {        return serializeType;    }    public static int getNettyChannelConnectCount() {        return nettyChannelConnectCount;    }    public static int getMaxRetries() {        return maxRetries;    }    public static int getBaseSleepTimeMs() {        return baseSleepTimeMs;    }    public static String getRootPath() {        return ROOT_PATH;    }    public static String getProviderPath() {        return PROVIDER_PATH;    }    public static String getConsumerPath() {        return CONSUMER_PATH;    }}