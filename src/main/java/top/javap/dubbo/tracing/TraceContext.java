package top.javap.dubbo.tracing;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: pch
 * @description:
 * @date: 2024/11/5
 **/
public class TraceContext {
    private static final ThreadLocal<Map<String, Object>> TRACE_THREAD_LOCAL = new ThreadLocal() {
        @Override
        protected Object initialValue() {
            return new HashMap<>();
        }
    };

    public static boolean isStarted() {
        return !get().isEmpty();
    }

    public static void start(String traceId) {
        start(traceId, "0");
    }

    public static void start(String traceId, String spanId) {
        get().put(TracingConstant.TRACE_ID, traceId);
        get().put(TracingConstant.SPAN_ID, spanId);
        get().put(TracingConstant.LOGIC_ID, new AtomicInteger(0));
    }

    public static String getTraceId() {
        return (String) get().get(TracingConstant.TRACE_ID);
    }

    public static String getSpanId() {
        String s = (String) get().get(TracingConstant.SPAN_ID);
        return s;
    }

    public static int nextLogicId() {
        return ((AtomicInteger) get().get(TracingConstant.LOGIC_ID)).incrementAndGet();
    }

    private static Map<String, Object> get() {
        return TRACE_THREAD_LOCAL.get();
    }

    public static void clear() {
        TRACE_THREAD_LOCAL.remove();
    }
}