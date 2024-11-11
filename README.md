# dubbo-tracing

## 介绍
dubbo-tracing 是一个用于跟踪 Dubbo 分布式RPC调用日志的 Filter 扩展包，通过在attachments中传递 traceId 和 spanId 将整个调用链串起来，便于开发者排查线上问题。

## 使用说明
直接在项目中引入依赖即可
```xml
<dependency>
    <groupId>top.javap</groupId>
    <artifactId>dubbo-tracing</artifactId>
    <version>${lastVersion}</version>
</dependency>
```
Tips：日志系统默认使用 Apache log4j

## 系统设计

### TraceId&SpanId生成规则
TraceId一般由接受请求的第一个服务器产生，具有唯一性，且在整个调用链路中保持不变。
TraceId的生成规则是：服务器IP + 时间戳 + 自增序列 + 进程号，比如：
```
c0a861711731309291125100068524
```
前8位`c0a86171`是生成TraceId的服务器IP，它被编码为十六进制，每2位代表IP地址中的一段，转换成十进制结果就是`192.168.97.113`，可以根据该号段快速定位到生成TraceId的服务器。
后面的13位`1731309291125`是生成TraceId的毫秒级时间戳；之后的4位`1000`是一个自增的序列，从 1000 开始，涨到 9999 后又会回到 1000；最后的部分`68524`是当前进程的ID，主要是为了防止单机多进程间产生的TraceId发生冲突。

SpanId 代表本次调用在整个调用链路树中的位置。
假设一个 Web 系统 A 接收了一次用户请求，那么在这个系统的 MVC 日志中，记录下的 SpanId 是 0，代表是整个调用的根节点，如果 A 系统处理这次请求，需要通过 RPC 依次调用 B、C、D 三个系统，那么在 A 系统的 RPC 客户端日志中，SpanId 分别是 0.1，0.2 和 0.3，在 B、C、D 三个系统的 RPC 服务端日志中，SpanId 也分别是 0.1，0.2 和 0.3；如果 C 系统在处理请求的时候又调用了 E，F 两个系统，那么 C 系统中对应的 RPC 客户端日志是 0.2.1 和 0.2.2，E、F 两个系统对应的 RPC 服务端日志也是 0.2.1 和 0.2.2。

根据上面的描述可以知道，如果把一次调用中所有的 SpanId 收集起来，可以组成一棵完整的链路树。

假设一次分布式调用中产生的 TraceId 是 0a1234（实际不会这么短），那么根据上文 SpanId 的产生过程，如下图所示：
![img.png](https://help-static-aliyun-doc.aliyuncs.com/assets/img/zh-CN/8703070161/p225164.png)

> 声明：TraceId&SpanId的生成规则借鉴了阿里的方案。

