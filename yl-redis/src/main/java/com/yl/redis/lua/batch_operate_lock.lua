local checkSet = cjson.decode(ARGV[1]) -- 传入的集合 A
local diffSet = {} -- 差集
-- 获取当前时间戳
local timestamp = ARGV[2]

--移除时间戳score已经过期30s的元素,
redis.call('ZREMRANGEBYSCORE', KEYS[1], '-inf',timestamp - 30)

-- 计算差集 checkSet - lockSet
for k,v in pairs(checkSet) do
    local isMember = redis.call('ZSCORE', KEYS[1], v);
    if not isMember then
        table.insert(diffSet,v)
    end
end

unpack = unpack or table.unpack

-- 将差集追加到原有集合中
if #diffSet > 0 then
    for i, member in pairs(diffSet) do
        redis.call('ZADD', KEYS[1], timestamp, member)
    end
    redis.call('EXPIRE', KEYS[1], 30)
end

-- 返回没有被锁的数据
return cjson.encode(diffSet)