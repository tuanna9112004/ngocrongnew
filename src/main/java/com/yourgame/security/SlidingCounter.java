package com.yourgame.security;

import java.util.ArrayDeque;

/**
 * Bộ đếm số sự kiện theo "cửa sổ trượt" (sliding window).
 * Ví dụ: SlidingCounter(10000) -> đếm số lần gọi trong 10 giây gần nhất.
 */
public final class SlidingCounter {

    private final long windowMs;
    private final ArrayDeque<Long> times = new ArrayDeque<>();

    public SlidingCounter(long windowMs) {
        this.windowMs = windowMs;
    }

    /** Ghi nhận 1 sự kiện xảy ra ngay bây giờ */
    public synchronized void incr() {
        long now = System.currentTimeMillis();
        times.addLast(now);
        purgeOld(now);
    }

    /** Lấy số lượng sự kiện trong cửa sổ thời gian */
    public synchronized int count() {
        long now = System.currentTimeMillis();
        purgeOld(now);
        return times.size();
    }

    /** Xóa các sự kiện đã quá hạn */
    private void purgeOld(long now) {
        while (!times.isEmpty() && (now - times.peekFirst() > windowMs)) {
            times.removeFirst();
        }
    }
}
