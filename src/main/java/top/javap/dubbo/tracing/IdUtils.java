package top.javap.dubbo.tracing;

import org.apache.dubbo.common.utils.NetUtils;
import org.apache.logging.log4j.util.ProcessIdUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: pch
 * @description:
 * @date: 2024/11/5
 **/
public class IdUtils {
    private static final String PROCESS_ID;
    private static final String IP_HEX_CODE;
    private static final AtomicInteger COUNTER;
    private static final int COUNT_INIT_VALUE = 1000;
    private static final int COUNT_MAX_VALUE = 9999;
    private static long lastTimestamp = 0L;

    static {
        PROCESS_ID = ProcessIdUtil.getProcessId();
        IP_HEX_CODE = getIpHexCode();
        COUNTER = new AtomicInteger(COUNT_INIT_VALUE);
    }

    /**
     * 8位         13位            4位
     * 服务器 IP + ID 产生的时间 + 自增序列 + 当前进程号
     *
     * @return
     */
    public static synchronized String newTraceId() {
        final long timestamp = System.currentTimeMillis();
        long count;
        if (timestamp > lastTimestamp) {
            COUNTER.set(COUNT_INIT_VALUE);
            count = COUNT_INIT_VALUE;
            lastTimestamp = timestamp;
        } else {
            count = COUNTER.incrementAndGet();
            if (count == COUNT_MAX_VALUE) {
                COUNTER.set(COUNT_INIT_VALUE - 1);
            }
        }
        return IP_HEX_CODE + timestamp + count + PROCESS_ID;
    }

    private static String getIpHexCode() {
        final StringBuilder builder = new StringBuilder();
        String host = NetUtils.getLocalHost();
        String[] split = host.split("\\.");
        for (String s : split) {
            String hex = Integer.toHexString(Integer.valueOf(s));
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            builder.append(hex);
        }
        return builder.toString();
    }
}