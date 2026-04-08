package com.yl.source.hashmap;

import java.util.HashMap;
import java.util.Map;

/**
 * HashMap 关键机制演示类。
 * <p>
 * 本类用于学习 HashMap 的底层行为：
 * <ul>
 *     <li>哈希扰动：{@code (h = key.hashCode()) ^ (h >>> 16)}</li>
 *     <li>寻址：{@code (n - 1) & hash}</li>
 *     <li>扩容：容量翻倍，阈值约为 {@code capacity * loadFactor}</li>
 *     <li>冲突处理：链表（高版本在特定条件树化为红黑树）</li>
 * </ul>
 * </p>
 */
public class TestHashMap {

	/**
	 * 演示 HashMap 基本操作，并打印结果。
	 *
	 * @param args 启动参数
	 */
	public static void main(String[] args) {
		// 初始容量 4，负载因子 0.75；达到阈值后会触发扩容。
		Map<Integer, String> map = new HashMap<>(4, 0.75f);

		map.put(1, "A");
		map.put(5, "B");
		map.put(9, "C");
		map.put(13, "D");

		// 读取
		System.out.println("key=9 -> " + map.get(9));

		// 覆盖更新
		map.put(9, "C-NEW");
		System.out.println("key=9(updated) -> " + map.get(9));

		// 删除
		map.remove(5);
		System.out.println("contains key=5 ? " + map.containsKey(5));

		// 遍历
		map.forEach((k, v) -> System.out.println(k + " => " + v));
	}
}
