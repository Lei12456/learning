-- 获取 Redis 中的键名
local tokens_key = KEYS[1]  -- 令牌数量的键名
local timestamp_key = KEYS[2]  -- 上次刷新时间的键名

-- 打印日志（调试用）
redis.log(redis.LOG_WARNING, "tokens_key " .. tokens_key)

-- 获取传入的参数
local rate = tonumber(ARGV[1])  -- 令牌生成速率（每秒生成的令牌数量）
local capacity = tonumber(ARGV[2])  -- 令牌桶的容量（最大令牌数量）
local now = tonumber(ARGV[3])  -- 当前时间戳
local requested = tonumber(ARGV[4])  -- 本次请求需要的令牌数量

-- 计算令牌桶的填充时间和过期时间
local fill_time = capacity / rate  -- 令牌桶填满所需的时间
local ttl = math.floor(fill_time * 2)  -- 设置键的过期时间为填充时间的两倍

-- 打印日志（调试用）
redis.log(redis.LOG_WARNING, "rate " .. ARGV[1])
redis.log(redis.LOG_WARNING, "capacity " .. ARGV[2])
redis.log(redis.LOG_WARNING, "now " .. ARGV[3])
redis.log(redis.LOG_WARNING, "requested " .. ARGV[4])
redis.log(redis.LOG_WARNING, "filltime " .. fill_time)
redis.log(redis.LOG_WARNING, "ttl " .. ttl)

-- 获取当前令牌数量
local last_tokens = tonumber(redis.call("get", KEYS[1]))
if last_tokens == nil then
  last_tokens = capacity  -- 如果令牌数量为空，则初始化为桶的容量
end
redis.log(redis.LOG_WARNING, "last_tokens " .. last_tokens)

-- 获取上次刷新时间
local last_refreshed = tonumber(redis.call("get", KEYS[2]))
if last_refreshed == nil then
  last_refreshed = 0  -- 如果上次刷新时间为空，则初始化为 0
end
redis.log(redis.LOG_WARNING, "last_refreshed " .. last_refreshed)

-- 计算时间差和新增的令牌数量
local delta = math.max(0, now - last_refreshed)  -- 当前时间与上次刷新时间的时间差
local filled_tokens = math.min(capacity, last_tokens + (delta * rate))  -- 计算当前桶中的令牌数量
local allowed = filled_tokens >= requested  -- 判断桶中的令牌是否足够
local new_tokens = filled_tokens  -- 新的令牌数量
local allowed_num = 0  -- 是否允许请求的标志（0 表示拒绝，1 表示允许）

-- 如果允许请求，则减少令牌数量
if allowed then
  new_tokens = filled_tokens - requested  -- 减少令牌数量
  allowed_num = 1  -- 允许请求
end

-- 打印日志（调试用）
redis.log(redis.LOG_WARNING, "delta " .. delta)
redis.log(redis.LOG_WARNING, "filled_tokens " .. filled_tokens)
redis.log(redis.LOG_WARNING, "allowed_num " .. allowed_num)
redis.log(redis.LOG_WARNING, "new_tokens " .. new_tokens)

-- 更新令牌数量和刷新时间，并设置过期时间
if ttl > 0 then
  redis.call("setex", KEYS[1], ttl, new_tokens)  -- 更新令牌数量
  redis.call("setex", KEYS[2], ttl, now)  -- 更新刷新时间
end

-- 返回结果
return { allowed_num, new_tokens }