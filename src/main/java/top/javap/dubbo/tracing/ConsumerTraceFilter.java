package top.javap.dubbo.tracing;

import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.rpc.*;
import org.apache.logging.log4j.ThreadContext;

/**
 * @author: pch
 * @description:
 * @date: 2024/11/5
 **/
@Activate(group = {"consumer"})
public class ConsumerTraceFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (!TraceContext.isStarted()) {
            TraceContext.start(getTraceId());
        }
        ThreadContext.put(TracingConstant.TRACE_ID, TraceContext.getTraceId());
        ThreadContext.put(TracingConstant.SPAN_ID, TraceContext.getSpanId());
        invocation.setAttachment(TracingConstant.DUBBO_TRACE_ID, TraceContext.getTraceId());
        invocation.setAttachment(TracingConstant.DUBBO_SPAN_ID, TraceContext.getSpanId() + "." + TraceContext.nextLogicId());
        return invoker.invoke(invocation);
    }

    private String getTraceId() {
        String traceId = ThreadContext.get(TracingConstant.TRACE_ID);
        if (StringUtils.isEmpty(traceId)) {
            traceId = IdUtils.newTraceId();
        }
        return traceId;
    }
}