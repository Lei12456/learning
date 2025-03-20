local setA = cjson.decode(ARGV[1]) -- 传入的集合 A

-- 从集合 B 中移除集合 A 的元素
for k,v in pairs(setA) do
    redis.call("ZREM", KEYS[1], v)
end

return true;