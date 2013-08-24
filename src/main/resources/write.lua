redis.log(redis.LOG_VERBOSE, {"write request:", KEYS, ARGV})
local key = KEYS[1]
local halflife = tonumber(ARGV[1]) -- in minutes
local time = tonumber(ARGV[2])
local contrib = tonumber(ARGV[3])

-- check parameters
if not halflife or halflife <= 0 or not key or not time or time <= 0 or not contrib or contrib <= 0 then
	return -1
end

-- if the key is new
if redis.call("EXISTS", key) == 0 then
	local payload =  cmsgpack.pack({time, contrib})
	redis.call("SET", key, payload)
	return contrib
end


-- if the key exists
local payload = cmsgpack.unpack(redis.call("GET",key))
local modified = payload[1]
local weight = payload[2]

-- update weight
if time >= modified then
	local decay = math.exp((time - modified)/60  * math.log(0.5) / halflife)
	weight = contrib + weight * decay
	modified = time
else
	local decay = math.exp((modified - time)/60  * math.log(0.5) / halflife)
	weight = contrib * decay + weight
end

-- the new weight is 0 or less, we remove this key completely
if weight <=0 then
	redis.call("DEL", key)
	return 0
end

payload = cmsgpack.pack({modified, weight})
redis.call("SET", key, payload)
redis.call("EXPIRE", key, 7776000) -- 3 month
return weight

