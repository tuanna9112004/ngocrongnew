package nro.services;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

public class BerryGiftService {
    // Map: key = tên đã chuẩn hóa, value = số lượt
    private static final Map<String, Integer> allowedCounts = new HashMap<>();

    // Chuẩn hóa tên: cắt khoảng trắng, về thường, bỏ dấu
    public static String normalize(String name) {
        if (name == null) return "";
        String s = name.trim().toLowerCase();
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return s;
    }

    public static void addAllowance(String playerName) {
        String key = normalize(playerName);
        int after = allowedCounts.getOrDefault(key, 0) + 1;
        allowedCounts.put(key, after);
      //  System.out.println("[BerryGift] addAllowance for '" + playerName + "' -> key='" + key + "', now=" + after);
    }

    public static boolean canReceive(String playerName) {
        String key = normalize(playerName);
        return allowedCounts.getOrDefault(key, 0) > 0;
    }

    public static void consume(String playerName) {
        String key = normalize(playerName);
        int cur = allowedCounts.getOrDefault(key, 0);
        if (cur > 0) {
            allowedCounts.put(key, cur - 1);
          //  System.out.println("[BerryGift] consume for '" + playerName + "' -> key='" + key + "', left=" + (cur - 1));
        } else {
         //   System.out.println("[BerryGift] consume called but no allowance for '" + playerName + "' (key='" + key + "')");
        }
    }

    public static int getAllowance(String playerName) {
        String key = normalize(playerName);
        int v = allowedCounts.getOrDefault(key, 0);
       // System.out.println("[BerryGift] getAllowance '" + playerName + "' -> key='" + key + "', now=" + v);
        return v;
    }
}
