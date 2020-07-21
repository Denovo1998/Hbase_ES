package com.denovo.hbaseObserver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author liusinan
 * @version 1.0.0
 * @ClassName ElasticsearchPoolUtil.java
 * @Description
 * @createTime 2020年07月20日 20:21:09
 */
public class ElasticsearchPoolUtil {
    private static final Log LOGGER = LogFactory.getLog(ElasticsearchPoolUtil.class);

    //默认最小连接数
    public static Integer MAX_CONNECT_SIZE = 1;

    //默认重试次数
    private static Integer MAX_RETRY_SIZE = 600;

    //静态的Connection队列
    private static LinkedList<TransportClient> clientQueue = null;

    public static String CLUSTER_NAME = "elasticsearch";

    public static List<String> esHostTcpList = new ArrayList<>();

    public synchronized static TransportClient getClient() throws Exception {
        if (clientQueue == null) {
            clientQueue = new LinkedList<>();
            for (int i = 0; i < MAX_CONNECT_SIZE; i++) {
                clientQueue.push(clientPush());
            }
        } else if (clientQueue.size() == 0) {
            clientQueue.push(clientPush());
        }
        return clientQueue.poll();
    }

    public static TransportAddress[] initTranSportAddress() {
        TransportAddress[] transportAddresses = new TransportAddress[esHostTcpList.size()];
        int offset = 0;
        for (int i = 0; i < esHostTcpList.size(); i++) {
            String[] ipHost = esHostTcpList.get(i).split(":");
            try {
                transportAddresses[offset] = new TransportAddress(InetAddress.getByName(ipHost[0].trim()), Integer.valueOf(ipHost[1].trim()));
                offset++;
            } catch (Exception e) {
                LOGGER.error("exec init transport address error:", e);
            }
        }
        return transportAddresses;
    }

    public static void pilotConnection() {
        synchronized (clientQueue) {
            long startTime = System.currentTimeMillis();
            LOGGER.warn("正在销毁连接，目前连接数为：" + clientQueue.size());
            if (clientQueue.size() > MAX_CONNECT_SIZE) {
                clientQueue.getLast().close();
                clientQueue.removeLast();
                LOGGER.warn("关闭连接耗时：" + (System.currentTimeMillis() - startTime));
                pilotConnection();
            } else {
                return;
            }
        }
    }

    public static void destoryAllConnection() {
        LOGGER.warn("正在销毁连接，目前连接数为：" + clientQueue.size());
        synchronized (clientQueue) {
            long startTime = System.currentTimeMillis();
            if (clientQueue.size() > 0) {
                clientQueue.getLast().close();
                clientQueue.removeLast();
                LOGGER.warn("关闭连接耗时：" + (System.currentTimeMillis() - startTime));
                pilotConnection();
            } else {
                return;
            }
        }
    }

    private synchronized static TransportClient clientPush() throws Exception {
        TransportClient client = null;
        int upCount = 0;
        while (clientQueue.size() < MAX_CONNECT_SIZE && client == null && upCount < MAX_RETRY_SIZE) {
            client = init();
            Thread.sleep(100);
            upCount++;
        }
        if (client == null) {
            throw new Exception("Es client init failed wait for 60s");
        }
        return client;
    }

    public static TransportClient init() throws Exception {
        System.setProperty("es.set.netty.runtime.available.processors", "true");
        Settings esSettings = Settings.builder().put("cluster.name", CLUSTER_NAME).put("client.transport.sniff", false).build();
        TransportClient client = new PreBuiltTransportClient(esSettings);
        client.addTransportAddresses(initTranSportAddress());
        return client;
    }

    public synchronized static void returnClient(TransportClient client) {
        if (clientQueue == null) {
            clientQueue = new LinkedList<>();
        }
        clientQueue.push(client);
    }
}
