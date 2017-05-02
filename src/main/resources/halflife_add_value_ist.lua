-- all time/time-span are in seconds
local key  = KEYS[1]
local half_life  = tonumber(ARGV[1])
local expire = tonumber(ARGV[2])
local timestamp = tonumber(ARGV[3])
local value  = tonumber(ARGV[4])
local threshold  = tonumber(ARGV[5])

-- check parameters
if not key then return 0 end
if not half_life or half_life <= 0 then return 0 end
if not expire or expire < 0 then return 0 end 
if not timestamp or timestamp < 0 then return 0 end
if not value or value < 0 then return 0 end
if not threshold then return 0 end

-- if the key is new
if redis.call("EXISTS", key) == 0 then
  if not value < threshold then return 0 end
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
sum = sum * math.pow(0.5, (timestamp - modified) * 1.0 / half_life)

if not sum < threshold then return 0 end

sum = math.max(0, sum + value)
payload = cmsgpack.pack({timestamp, sum})
redis.call("SET", key, payload)
redis.call("EXPIRE", key, expire)
return sum -- result will be auto-convert to Integer or Long
