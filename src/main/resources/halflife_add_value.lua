-- all time/time-span are in seconds
local key  = KEYS[1]
local half_life  = tonumber(ARGV[1])
local expire = tonumber(ARGV[2])
local timestamp = tonumber(ARGV[3])
local value  = tonumber(ARGV[4])

-- check parameters
if not key then return -1 end
if not half_life or half_life <= 0 then return -1 end
if not expire or expire < 0 then return -1 end 
if not timestamp or timestamp < 0 then return -1 end
if not value then return -1 end

-- if the key is new
if redis.call("EXISTS", key) == 0 then
  local payload =  cmsgpack.pack({timestamp, value})
  redis.call("SET", key, payload)
  redis.call("EXPIRE", key, expire)
  return value
end

-- if the key exists
local payload = cmsgpack.unpack(redis.call("GET",key))
local modified  = payload[1]
local sum = payload[2]

-- update sum & modified
sum =  math.max(0, value + sum * math.pow(0.5, (timestamp - modified) * 1.0 / half_life))

-- the new sum is very close to 0 or less, we remove this key
if sum == 0 then
  redis.call("DEL", key)
  return 0
end

payload = cmsgpack.pack({timestamp, sum})
redis.call("SET", key, payload)
redis.call("EXPIRE", key, expire)
return sum -- result will be auto-convert to Integer or Long
