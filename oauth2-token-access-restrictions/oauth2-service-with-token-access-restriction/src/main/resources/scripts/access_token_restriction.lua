redis.replicate_commands()

local key = KEYS[1]

local windowSize = tonumber(ARGV[1])
local rate = tonumber(ARGV[2])
local now = tonumber(redis.call("TIME")[1])

redis.call("zadd", key, now, now)
local start = math.max(0, now - windowSize)

local requestRate = tonumber(redis.call("zcount", key, start, now))

local result = true
if requestRate > rate then
  result = false
end

redis.call("zremrangebyscore", key, "-inf", "("..start)

return result
