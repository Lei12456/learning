package com.yl.hash;

import java.util.ArrayList;
import java.util.List;

/**
 * @author admin
 * @version 1.0.0
 * @Description 链式地址法避免哈希冲突
 * @createTime 2023年11月29日 10:20:00
 */
public class HashMapChaining {
    int size; //键值对数据量
    int capacity; //哈希表容量
    double loadThres; //负载因子
    int extendRatio; // 扩容倍数
    List<List<Pair>> buckets; // 桶数组
    // ，每个桶是一个链表

    public HashMapChaining(){
        size = 0;
        capacity = 4;
        loadThres = 2.0 / 3.0;
        extendRatio = 2;
        buckets = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; i++) {
            buckets.add(new ArrayList<>());
        }
    }

    /* 哈希函数 */
    int hashFunc(int key) {
        return key % capacity;
    }

    /* 负载因子 */
    double loadFactor() {
        return (double) size / capacity;
    }

    String get(Integer key) {
        //哈希值获取索引位置
        int index = hashFunc(key);
        //找到当前
        List<Pair> bucket = buckets.get(index);
        //遍历桶。找到对应的value
        for (Pair pair : bucket) {
            if (pair.key == key){
                return pair.val;
            }
        }
        return null;
    }

    void put(Integer key,String val) {
        //当负载因子超过阈值后，扩容
        if(loadFactor() > loadThres){
            extend();
        }
        int index = hashFunc(key);
        //获取老位置值上的桶，存在key则覆盖，不存在则新增
        List<Pair> bucket = buckets.get(index);
        for (Pair pair : bucket) {
            if (pair.key == key) {
                pair.val = val;
                return;
            }
        }
        //不存在，则使用尾插法添加到链表尾部
        Pair newPair = new Pair(key, val);
        bucket.add(newPair);
        size++;
    }

    void remove(Integer key){
        int index = hashFunc(key);

        //找到当亲key所在的链表
        List<Pair> buket = buckets.get(index);
        //遍历链表把 存在的key的值删除
        for (Pair pair : buket) {
            if (pair.key == key) {
                buket.remove(pair);
                size--;
                break;
            }
        }

    }

    /* 打印哈希表 */
    void print() {
        for (List<Pair> bucket : buckets) {
            List<String> res = new ArrayList<>();
            for (Pair pair : bucket) {
                res.add(pair.key + " -> " + pair.val);
            }
            System.out.println(res);
        }
    }

    /*
        扩容操作
     */
    void extend(){
        //暂存桶数组
        List<List<Pair>> bucketTemp = buckets;
        capacity *= extendRatio;
        buckets = new ArrayList<>(capacity);
        //初始化空的桶数组
        for (int i = 0; i < capacity; i++) {
            buckets.add(new ArrayList<>());
        }
        size = 0;
        //将原数组的元素添加至新数组
        for (List<Pair> bucket : bucketTemp) {
            for (Pair pair : bucket) {
                put(pair.key, pair.val);
            }
        }
    }

    public static void main(String[] args) {
        /* 初始化哈希表 */
        HashMapChaining map = new HashMapChaining();

        /* 添加操作 */
        // 在哈希表中添加键值对 (key, value)
        map.put(12836, "小哈");
        map.put(15937, "小啰");
        map.put(16750, "小算");
        map.put(13276, "小法");
        map.put(10583, "小鸭");

        map.print();

        map.remove(13276);

        map.print();


    }
}
