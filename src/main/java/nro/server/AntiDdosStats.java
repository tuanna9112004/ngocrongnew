package nro.server;

import java.util.ArrayList;
import java.util.List;

public class AntiDdosStats {

    private static boolean enabled = true;

    public static class IPInfo {
        public String address;
        public int suspicion;

        public IPInfo(String address, int suspicion) {
            this.address = address;
            this.suspicion = suspicion;
        }
    }

    private static final List<IPInfo> ipList = new ArrayList<>();
    private static final List<String> blockedIps = new ArrayList<>();
    private static int activeConnections = 0;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static int getActiveConnections() {
        return activeConnections;
    }

    public static List<String> getBlockedIps() {
        return blockedIps;
    }

    public static void clearBlockedIps() {
        blockedIps.clear();
    }

    public static List<IPInfo> getIpList() {
        return ipList;
    }

    // ví dụ gọi khi có IP mới
    public static void checkIp(String ip) {
        if (!enabled) return; // nếu AntiDDoS đang tắt thì bỏ qua
        // TODO: logic kiểm tra suspicion, block...
    }
}
