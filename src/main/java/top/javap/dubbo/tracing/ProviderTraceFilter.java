package top.javap.dubbo.tracing;

import com.alibaba.fastjson2.JSON;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.rpc.*;
import org.apache.logging.log4j.ThreadContext;

/**
 * @author: pch
 * @description:
 * @date: 2024/11/5
 **/
@Activate(group = {"provider"})
public class ProviderTraceFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        final String traceId = RpcContext.getServerAttachment().getAttachment(TracingConstant.DUBBO_TRACE_ID);
        final String spanId = RpcContext.getServerAttachment().getAttachment(TracingConstant.DUBBO_SPAN_ID);
        if (StringUtils.isAnyEmpty(traceId, spanId)) {
            return invoker.invoke(invocation);
        }
        TraceContext.start(traceId, spanId);
        ThreadContext.put(TracingConstant.TRACE_ID, TraceContext.getTraceId());
        ThreadContext.put(TracingConstant.SPAN_ID, TraceContext.getSpanId());
        try {
            return invoker.invoke(invocation);
        } catch (Throwable e) {
            throw e;
        } finally {
            TraceContext.clear();
            ThreadContext.remove(TracingConstant.TRACE_ID);
            ThreadContext.remove(TracingConstant.SPAN_ID);
        }
    }
}