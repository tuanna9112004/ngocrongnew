package com.yourgame.security;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Quản lý AntiDDoS cho server.
 * Kiểm soát kết nối, request, và block theo IP.
 */
public class AntiDdosManager {

    private static final ConcurrentHashMap<String, IpState> IP = new ConcurrentHashMap<>();
    private static final SlidingCounter globalConn10s = new SlidingCounter(10_000);

    /** Kiểm tra xem port có nằm trong danh sách bảo vệ không */
    private static boolean isProtectedPort(int port) {
        for (int p : AntiDdosConfig.PROTECTED_PORTS) {
            if (p == port) return true;
        }
        return false;
    }

    /** Khi có kết nối mới */
    public static Decision onConnect(String ip, int port) {
        if (!AntiDdosConfig.ENABLED) return Decision.ALLOW;
        if (!isProtectedPort(port)) return Decision.ALLOW;

        IpState s = IP.computeIfAbsent(ip, k -> new IpState());
        s.concurrent.incrementAndGet();
        s.conn10s.incr();
        globalConn10s.incr();

        if (s.bannedUntil > System.currentTimeMillis()) {
            return Decision.BLOCK;
        }

        if (s.concurrent.get() > AntiDdosConfig.MAX_CONCURRENT_PER_IP
                || s.conn10s.count() > AntiDdosConfig.CONN_RATE_PER_10S
                || globalConn10s.count() > AntiDdosConfig.GLOBAL_CONN_RATE_PER_10S) {
            bumpScore(s, +40);
        }
        return decide(s);
    }

    public static void onHandshakeOK(String ip) {
        IpState s = IP.get(ip);
        if (s != null) bumpScore(s, -10);
    }

    public static void onDisconnect(String ip) {
        IpState s = IP.get(ip);
        if (s != null) s.concurrent.updateAndGet(v -> Math.max(0, v - 1));
    }

    public static Decision onMessage(String ip, int port, boolean heavy) {
        if (!AntiDdosConfig.ENABLED) return Decision.ALLOW;
        if (!isProtectedPort(port)) return Decision.ALLOW;

        IpState s = IP.computeIfAbsent(ip, k -> new IpState());
        s.req1s.incr();
        s.lastSeen = System.currentTimeMillis();

        int qps = s.req1s.count();
        int limit = heavy ? AntiDdosConfig.QPS_SENSITIVE : AntiDdosConfig.QPS_PER_IP;

        if (qps > limit + AntiDdosConfig.QPS_BURST) {
            bumpScore(s, +20);
        } else {
            bumpScore(s, -1);
        }
        return decide(s);
    }

    /** Tăng/giảm điểm nghi ngờ */
    private static void bumpScore(IpState s, int delta) {
        s.score = Math.max(0, s.score + delta);
    }

    /** Quyết định xử lý IP */
    private static Decision decide(IpState s) {
        if (s.score >= AntiDdosConfig.SCORE_BLOCK) {
            s.bannedUntil = System.currentTimeMillis() + AntiDdosConfig.TEMP_BAN_TTL_MS;
            return Decision.BLOCK;
        }
        if (s.score >= AntiDdosConfig.SCORE_CHALLENGE) {
            return Decision.CHALLENGE;
        }
        return Decision.ALLOW;
    }

    /** Trạng thái của một IP */
    public static class IpState {
        public AtomicInteger concurrent = new AtomicInteger(0);
        public SlidingCounter conn10s = new SlidingCounter(10_000);
        public SlidingCounter req1s = new SlidingCounter(1_000);
        public long lastSeen = System.currentTimeMillis();
        public int score = 0;
        public long bannedUntil = 0;
    }

    /** Các quyết định xử lý */
    public enum Decision {
        ALLOW, BLOCK, CHALLENGE
    }
}
