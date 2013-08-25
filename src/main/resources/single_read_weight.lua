redis.log(redis.LOG_WARNING, "read.lua starts to run")

-- all time/time-span are in seconds
local key = KEYS[1]
local half_life = tonumber(ARGV[1])
local timestamp = tonumber(ARGV[2])

-- check parameters
if not key then
	redis.log(redis.LOG_WARNING, "bad parameter: key nil")
	return 0
end

if not half_life or half_life <= 0 then
	redis.log(redis.LOG_WARNING, "bad parameter: half_life " .. half_life)
	return 0
end

if not timestamp or timestamp < 0 then
	redis.log(redis.LOG_WARNING, "bad parameter: timestamp " .. timestamp)
	return 0			
end

-- if the key is new
if redis.call("EXISTS", key) == 0 then
	return 0
end

-- if the key exists
local payload = cmsgpack.unpack(redis.call("GET",key))
local modified = payload[1]
local sum = payload[2]

print("modified: " .. modified)
print("timestamp: " .. timestamp)
print("halflife: " .. half_life)

local decay = math.exp((timestamp - modified)  * math.log(0.5) / half_life)
redis.log(redis.LOG_WARNING, "read normal decay " .. decay)
return  sum * decay
