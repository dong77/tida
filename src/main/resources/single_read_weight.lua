-- all time/time-span are in seconds
local key = KEYS[1]
local half_life = tonumber(ARGV[1])
local timestamp = tonumber(ARGV[2])

-- check parameters
if not key then return 0 end
if not half_life or half_life <= 0 then return 0 end
if not timestamp or timestamp < 0 then return 0 end

-- if the key doesn't exist at all
if redis.call("EXISTS", key) == 0 then return 0 end

-- if the key exists
local payload = cmsgpack.unpack(redis.call("GET",key))
local modified = payload[1]
local sum = payload[2]
local decay = math.exp((timestamp - modified)  * math.log(0.5) / half_life)
return  sum * decay  -- result will be auto-convert to Integer or Long
