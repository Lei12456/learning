package com.yl.utils;

import com.baomidou.mybatisplus.core.config.GlobalConfig;
import org.springframework.batch.item.database.JdbcCursorItemReader;

import java.util.Map;

public class CursorSpringBatchUtils {


    public <A extends com.baomidou.mybatisplus.core.mapper.BaseMapper<com.yl.entity.User>, V> long cursorSelectCanStopNoSqlBuilder(Map<String, Object> param, Class<V> voClass, Class<A> daoClass,
                                                                                                                                   String queryId, boolean lastCallBack) {

        JdbcCursorItemReader reader = new JdbcCursorItemReader();
        long count = 0;
        try {

            // 设置参数

            // 创建游标
            reader.open(new org.springframework.batch.item.ExecutionContext());

            // 使用游标迭代获取每个记录
            V doc;
            while ((reader.read()) != null) {
                count++;
            }
        } catch (Exception e) {
        } finally {
            reader.close();
        }
        return count;
    }

    private GlobalConfig getSqlSessionTemplate() {
        return new GlobalConfig();
    }

}