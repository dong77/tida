
class TimeDecayAttributionModel {

val jedis = new JedisPool(new JedisPoolConfig(), "localhost");
jedis.set("foo", "bar");
String value = jedis.get("foo");
}