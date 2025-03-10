package com.yl.shardCluster;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;

public class RedisClusterExample {

    public static void main(String[] args) {
        // 创建一个HashSet来存储集群节点的信息
        Set<HostAndPort> jedisClusterNodes = new HashSet<>();
        // 添加集群节点信息（这里仅作为示例，请替换为你的实际节点IP和端口）
        jedisClusterNodes.add(new HostAndPort("127.0.0.1", 7001));
        jedisClusterNodes.add(new HostAndPort("127.0.0.1", 7002));
        jedisClusterNodes.add(new HostAndPort("127.0.0.1", 7003));

        // 创建JedisCluster对象
        try (JedisCluster jedisCluster = new JedisCluster(jedisClusterNodes)) {
            // 设置一个键值对
            String key = "greeting";
            String value = "Hello, Redis!";
            jedisCluster.set(key, value);
            System.out.println("Set result: " + value);

            // 获取刚才设置的值
            String retrievedValue = jedisCluster.get(key);
            System.out.println("Get result: " + retrievedValue);

            // 删除键值对
            Long delResult = jedisCluster.del(key);
            System.out.println("Delete result: " + (delResult > 0 ? "Deleted" : "Not found"));

            // 尝试获取已经被删除的键
            String deletedValue = jedisCluster.get(key);
            System.out.println("After deletion get result: " + (deletedValue == null ? "Not found" : deletedValue));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}