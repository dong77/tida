-- all time/time-span are in seconds
local ke	= KEYS[1]
local half_lif	= tonumber(ARGV[1])
local expir	= tonumber(ARGV[2])
local timestamp	= tonumber(ARGV[3])
local weight	= tonumber(ARGV[4])

-- check parameters
if not key then return 0 end
if not half_life or half_life <= 0 then return 0 end
if not expire or expire < 0 then return 0 end 
if not timestamp or timestamp < 0 then return 0 end
if not weight or weight < 0 then return 0 end

-- if the key is new
if redis.call("EXISTS", key) == 0 then
	local payload =  cmsgpack.pack({timestamp, weight})
	redis.call("SET", key, payload)
	redis.call("EXPIRE", key, expire)
	return weight
end

-- if the key exists
local payload	= cmsgpack.unpack(redis.call("GET",key))
local modified	= payload[1]
local sum	= payload[2]

-- update sum & modified
sum = weight + sum * math.pow(0.5, (timestamp - modified) * 1.0 / half_life)
modified = timestamp

-- the new sum is very close to 0 or less, we remove this key
if sum < 0.00001 then
	redis.call("DEL", key)
	return 0
end

payload = cmsgpack.pack({modified, sum})
redis.call("SET", key, payload)
redis.call("EXPIRE", key, expire)
return sum -- result will be auto-convert to Integer or Long

