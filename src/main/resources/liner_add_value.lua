-- all time/time-span are in seconds
local key       = KEYS[1]
local spread    = tonumber(ARGV[1])
local timestamp = tonumber(ARGV[2])
local value     = tonumber(ARGV[3])

-- check parameters
if not key then return -1 end
if not spread or spread <= 0 then return -1 end
if not timestamp or timestamp < 0 then return -1 end
if not value then return -1 end

-- if the key is new
if redis.call("EXISTS", key) == 0 then
  local payload =  cmsgpack.pack({timestamp, value})
  redis.call("SET", key, payload)
  redis.call("EXPIRE", key, spread)
  return value
end

-- if the key exists
-- if the key exists
local payload = cmsgpack.unpack(redis.call("GET",key))
local modified  = payload[1]
local sum = payload[2]

-- update sum & modified
sum =  math.max(0, value + sum * (spread - (timestamp - modified)) / spread)

-- the new sum is very close to 0 or less, we remove this key
if sum <= 0 then
  redis.call("DEL", key)
  return 0
end

payload = cmsgpack.pack({timestamp, sum})
redis.call("SET", key, payload)
redis.call("EXPIRE", key, spread)
return sum -- result will be auto-convert to Integer or Long
