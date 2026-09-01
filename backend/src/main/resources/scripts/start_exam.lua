-- start_exam.lua
--
-- Atomic "has this user started this exam" gate. Runs as a single
-- operation inside Redis (Redis executes Lua scripts atomically), so
-- 10,000 simultaneous requests for the same user+exam can never both
-- see "not started" - exactly one wins.
--
-- KEYS[1] = "user_exam:{userId}:{examId}"   -- the start flag
-- KEYS[2] = "deadline:{userId}:{examId}"    -- the deadline value
-- ARGV[1] = deadline epoch milliseconds (computed by the caller as
--           now + exam.durationMinutes, BEFORE calling this script)
-- ARGV[2] = TTL in seconds for both keys (exam duration + 5 min buffer)
--
-- Returns: {1, deadline} if this is a genuinely new start
--          {0, existingDeadline} if the student already started

local created = redis.call('SETNX', KEYS[1], '1')

if created == 0 then
    -- Already exists - this is a repeat click, browser retry, or page
    -- refresh. Do NOT touch the deadline key. Return what's already
    -- there so the caller can resume with the same deadline.
    local existing = redis.call('GET', KEYS[2])
    return {0, existing}
end

-- Genuinely first time. Give both keys the same TTL so they expire
-- together - prevents an orphaned deadline key from outliving its flag.
redis.call('EXPIRE', KEYS[1], ARGV[2])
redis.call('SET', KEYS[2], ARGV[1], 'EX', ARGV[2])

return {1, ARGV[1]}