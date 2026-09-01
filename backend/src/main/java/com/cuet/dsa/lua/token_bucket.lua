-- token_bucket.lua
-- Runs ENTIRELY inside Redis to preserve transaction isolation.
--
-- KEYS[1] = bucket key, e.g. "rate:user:42"
-- ARGV[1] = capacity      (max tokens the bucket can hold)
-- ARGV[2] = refill_rate   (tokens added per second)
-- ARGV[3] = now_ms        (current time in milliseconds from Java)

local key         = KEYS[1]
local capacity     = tonumber(ARGV[1])
local refill_rate  = tonumber(ARGV[2])
local now          = tonumber(ARGV[3])

-- Read bucket attributes natively
local bucket = redis.call("HMGET", key, "tokens", "last_refill")
local tokens       = tonumber(bucket[1])
local last_refill  = tonumber(bucket[2])

if tokens == nil then
    -- Cold start configuration
    tokens = capacity
    last_refill = now
else
    -- Time metric parsing
    local elapsed_ms = now - last_refill
    if elapsed_ms < 0 then
        elapsed_ms = 0
    end

    -- Compute lazy token addition based on floating point differences
    local tokens_to_add = (elapsed_ms / 1000) * refill_rate
    tokens = math.min(capacity, tokens + tokens_to_add)
end

local allowed
if tokens >= 1 then
    allowed = 1
    tokens = tokens - 1
else
    allowed = 0
end

-- Persist raw floating point state values to avoid losing precise ticks
redis.call("HSET", key, "tokens", tostring(tokens), "last_refill", tostring(now))
redis.call("EXPIRE", key, 3600)

return {allowed, math.floor(tokens)}