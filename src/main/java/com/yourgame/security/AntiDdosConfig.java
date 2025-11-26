package com.yourgame.security;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Cấu hình AntiDDoS
 * Đọc / ghi từ file antiddos.properties
 */
public class AntiDdosConfig {

    public static boolean ENABLED = true;

    // Các tham số AntiDDoS
    public static int MAX_CONCURRENT_PER_IP = 5;
    public static int CONN_RATE_PER_10S = 20;
    public static int GLOBAL_CONN_RATE_PER_10S = 100;
    public static int QPS_PER_IP = 15;
    public static int QPS_SENSITIVE = 7;
    public static int QPS_BURST = 3;

    public static int SCORE_BLOCK = 100;
    public static int SCORE_CHALLENGE = 50;
    public static long TEMP_BAN_TTL_MS = 60_000; // 1 phút

    // Danh sách cổng được bảo vệ
    public static List<Integer> PROTECTED_PORTS = new ArrayList<>(Arrays.asList(14445,80,443));

    private static final String CONFIG_FILE = "antiddos.properties";

    /** Load cấu hình từ file */
    public static void load() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);

            ENABLED = Boolean.parseBoolean(props.getProperty("enabled", "true"));
            MAX_CONCURRENT_PER_IP = Integer.parseInt(props.getProperty("max_concurrent_per_ip", "5"));
            CONN_RATE_PER_10S = Integer.parseInt(props.getProperty("conn_rate_per_10s", "20"));
            GLOBAL_CONN_RATE_PER_10S = Integer.parseInt(props.getProperty("global_conn_rate_per_10s", "100"));
            QPS_PER_IP = Integer.parseInt(props.getProperty("qps_per_ip", "15"));
            QPS_SENSITIVE = Integer.parseInt(props.getProperty("qps_sensitive", "7"));
            QPS_BURST = Integer.parseInt(props.getProperty("qps_burst", "3"));
            SCORE_BLOCK = Integer.parseInt(props.getProperty("score_block", "100"));
            SCORE_CHALLENGE = Integer.parseInt(props.getProperty("score_challenge", "50"));
            TEMP_BAN_TTL_MS = Long.parseLong(props.getProperty("temp_ban_ttl_ms", "60000"));

            String portsStr = props.getProperty("protected_ports", "14445");
            PROTECTED_PORTS = Arrays.stream(portsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            System.out.println("[AntiDDoS] Config loaded. ENABLED=" + ENABLED + ", Ports=" + PROTECTED_PORTS);

        } catch (IOException e) {
            System.err.println("[AntiDDoS] Không tìm thấy file cấu hình, dùng mặc định.");
            save(); // Tạo file mặc định
        }
    }

    /** Lưu cấu hình ra file */
    public static void save() {
        Properties props = new Properties();
        props.setProperty("enabled", String.valueOf(ENABLED));
        props.setProperty("max_concurrent_per_ip", String.valueOf(MAX_CONCURRENT_PER_IP));
        props.setProperty("conn_rate_per_10s", String.valueOf(CONN_RATE_PER_10S));
        props.setProperty("global_conn_rate_per_10s", String.valueOf(GLOBAL_CONN_RATE_PER_10S));
        props.setProperty("qps_per_ip", String.valueOf(QPS_PER_IP));
        props.setProperty("qps_sensitive", String.valueOf(QPS_SENSITIVE));
        props.setProperty("qps_burst", String.valueOf(QPS_BURST));
        props.setProperty("score_block", String.valueOf(SCORE_BLOCK));
        props.setProperty("score_challenge", String.valueOf(SCORE_CHALLENGE));
        props.setProperty("temp_ban_ttl_ms", String.valueOf(TEMP_BAN_TTL_MS));

        String portsStr = PROTECTED_PORTS.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        props.setProperty("protected_ports", portsStr);

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "AntiDDoS Configuration");
            System.out.println("[AntiDDoS] Config saved.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
