package com.yl.redis.listener;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

@Component
public class CanalEventListener {

    @Autowired
    private CanalConnector canalConnector;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void start() {
        // 启动 Canal 客户端
        canalConnector.connect();
        canalConnector.subscribe(".*\\\\..*"); // 监听所有库和表
        new Thread(this::process).start(); // 启动事件处理线程
    }

    @PreDestroy
    public void stop() {
        // 关闭 Canal 客户端
        canalConnector.disconnect();
    }

    private void process() {
        while (true) {
            try {
                // 获取消息
                Message message = canalConnector.getWithoutAck(100);
                long batchId = message.getId();
                if (batchId == -1 || message.getEntries().isEmpty()) {
                    Thread.sleep(1000); // 没有数据，等待 1 秒
                    continue;
                }

                // 处理消息
                List<CanalEntry.Entry> entries = message.getEntries();
                for (CanalEntry.Entry entry : entries) {
                    if (entry.getEntryType() == CanalEntry.EntryType.ROWDATA) {
                        handleRowChange(entry);
                    }
                }

                // 确认消息
                canalConnector.ack(batchId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRowChange(CanalEntry.Entry entry) throws Exception {
        // 解析 Binlog 事件
        CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
        for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
            String tableName = entry.getHeader().getTableName();
            String key = tableName + ":" + rowData.getAfterColumns(0).getValue(); // 使用表名和主键作为 Redis 键

            if (rowChange.getEventType() == CanalEntry.EventType.DELETE) {
                // 删除操作
                redisTemplate.delete(key);
            } else {
                // 插入或更新操作
                String value = objectMapper.writeValueAsString(rowData.getAfterColumnsList());
                redisTemplate.opsForValue().set(key, value);
            }
        }
    }
}
