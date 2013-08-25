local key = KEYS[1]
local half_life = tonumber(ARGV[1])
local expire = tonumber(ARGV[2])
local timestamp = tonumber(ARGV[3])
local weight = tonumber(ARGV[4])

-- check parameters
if not key then
	-- redis.log(redis.LOG_VERBOSE, "bad parameter: key nil")
	return 0
end

if not half_life or half_life <= 0 then
	-- redis.log(redis.LOG_VERBOSE, "bad parameter: half_life " .. half_life)
	return 0
end
if not expire or expire < 0 then
	-- redis.log(redis.LOG_VERBOSE, "bad parameter: expire " .. expire)
	return 0		
end 
if not timestamp or timestamp < 0 then
	-- redis.log(redis.LOG_VERBOSE, "bad parameter: timestamp " .. timestamp)
	return 0			
end
if not weight or weight < 0 then
	-- redis.log(redis.LOG_VERBOSE, "bad parameter: weight " .. weight)
	return 0			
end

-- if the key is new
if redis.call("EXISTS", key) == 0 then
	local payload =  cmsgpack.pack({timestamp, weight})
	redis.call("SET", key, payload)
	redis.call("EXPIRE", key, expire)
	-- redis.log(redis.LOG_VERBOSE, "write to new key " .. key .. " with sum " .. weight)
	return weight
end

-- if the key exists
local payload = cmsgpack.unpack(redis.call("GET",key))
local modified = payload[1]
local sum = payload[2]

-- update sum
if timestamp >= modified then
	local decay = math.exp((timestamp - modified)  * math.log(0.5) / half_life)
	-- redis.log(redis.LOG_VERBOSE, "normal decay " .. decay)
	sum = weight + sum * decay
	modified = timestamp
else
	local decay = math.exp((modified - timestamp)  * math.log(0.5) / half_life)
	-- redis.log(redis.LOG_VERBOSE, "reverse decay " .. decay)
	sum = weight * decay + sum
end

-- redis.log(redis.LOG_VERBOSE, "new sum " .. sum .. " and modified " .. modified)

-- the new sum is 0 or less, we remove this key completely
if sum <=0 then
	redis.call("DEL", key)
	-- redis.log(redis.LOG_VERBOSE, "sum " .. sum .. " to small, removed key " .. key)
	return 0
end

payload = cmsgpack.pack({modified, sum})
redis.call("SET", key, payload)
redis.call("EXPIRE", key, expire)
-- redis.log(redis.LOG_VERBOSE, "write to existing key " .. key .. " with sum " .. weight)
return sum

