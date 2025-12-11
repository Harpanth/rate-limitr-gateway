local key = KEYS[1]

local capacity = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

-- Load bucket values from Redis
local tokens = tonumber(redis.call("HGET", key, "tokens_left"))
local last_refill = tonumber(redis.call("HGET", key, "last_refill"))

-- If no previous bucket exists → create one
if tokens == nil or last_refill == nil then
    tokens = capacity
    last_refill = now
end

-- Refill tokens
local elapsed = now - last_refill
if elapsed > 0 then
    local refill = elapsed * refill_rate
    tokens = math.min(capacity, tokens + refill)
    last_refill = now
end

-- Try consume 1 token
local allowed = 0
if tokens > 0 then
    tokens = tokens - 1
    allowed = 1
end

-- Save updated values
redis.call("HSET", key, "tokens_left", tokens)
redis.call("HSET", key, "last_refill", last_refill)

-- Return allowed, tokens_left, last_refill
return { allowed, tokens, last_refill }
