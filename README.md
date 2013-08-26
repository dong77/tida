#Tida

Tida是一个极其简单的权重累加器。后台是redis数据库，支持灵活的半衰期设定。

##试用场景
假如一个电商网站要统计每个用户最可能该兴趣的10个大的分类，一个可行的方案是根据用户对这些品类的浏览，购买行为对每个用户对应这10个分类做不同的加权计算。

##怎样用Tida

###设定何时的半衰期
设定半衰期是为了把用户过去行为的权重做适当的降权处理。假设半衰期设定为一个星期，那么一个星期前一个行为的权重将变成当时设定时候的1/2。半衰期越短，时间效应月明显。

```
import com.dongw.tida._
val pool = new JedisPool(new JedisPoolConfig(), "localhost")
val weighter = new HalfLifeDecayWeighter(pool, 10 /* minutes */)
```

上面的代码把半衰期设为10分钟。

###慢慢积累权重

为了积累不同用户行为的权重，可以调用`addWeight`方法：

```
val key = userId + "@" + categoryId
var weight = 1000 // a weight caused by a single product click

weighter.addWeight(key, Weight(weight, 26157161516116 /*  time in milliseconds when this happened */)

weight = 10000 // a weight caused by a single product purchase

weighter.addWeight(key, Weight(weight /* omit the second parameter so defaults to current time */)
```

###读取某个时间点的权重

为了读取当前有个key的权重，可以：

```
val key = userId + "@" + categoryId
var weight = 1000 // a weight caused by a single product click

weighter.getWeight(key)
```

为了读取过去或者未来某个时间点`26157161516116`的权重，可以：

```
val key = userId + "@" + categoryId
var weight = 1000 // a weight caused by a single product click

weighter.getWeight(key， 26157161516116)
```

###Caveat
有一点值得注意：lua代码返回的数值会被转化成整形。所以尽量使用远大于1的权重。

##Tida和直接Redis访问方式的区别

Tida把半衰期权重计算放到服务端去做，这样客户端不必读取Redis数据库的值，返回到客户单，然后在存回去。Tida把3次网络数据交换变成1此。


##外部依赖
 - jedis 2.1.0

##下一步的工作

- 一次读取多个key的权重。
- 在上一条基础上做排序，并截取top N。

##完整的使用代码示例

```
import com.dongw.tida._
import redis.clients.jedis._

object Test {
  def main(args: Array[String]) = {
    val pool = new JedisPool(new JedisPoolConfig(), "localhost")
    val impl = new HalfLifeDecayWeighter(pool, 1 /* minute */)
    (1 to 10) foreach { i =>
      println("write " + impl.addWeight("id", Weight(1000, i * 60000))) // one minute later
      println("read " + impl.getWeight("id", i * 60000))
    }
    pool.destroy()
  }
}
```
