package nro.services;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class muacode1 {
    // Map: key = code ngẫu nhiên, value = tên người chơi sở hữu
    private static final Map<String, String> codeMap = new HashMap<>();

    // Sinh code ngẫu nhiên 7 ký tự (5 số + 2 chữ)
    public static String generateCode(String playerName) {
        Random rnd = new Random();

        // 5 số
        StringBuilder numPart = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            numPart.append(rnd.nextInt(10)); // 0-9
        }
        String chars = "qwertyuiopasdfghjklzxcvbnm";
        StringBuilder charPart = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            charPart.append(chars.charAt(rnd.nextInt(chars.length())));
        }

        String code = numPart.toString() + charPart.toString();
        codeMap.put(code.toUpperCase(), normalize(playerName)); // gán người sở hữu
      //  System.out.println("[Muacode] generateCode for '" + playerName + "' -> " + code);
        return code;
    }

    // Chuẩn hóa tên
    public static String normalize(String name) {
        if (name == null) return "";
        String s = name.trim().toLowerCase();
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return s;
    }

    // Kiểm tra người chơi có được dùng code này không
    public static boolean canReceive(String code, String playerName) {
        if (code == null) return false;
        String owner = codeMap.get(code.toUpperCase());
        return owner != null && owner.equals(normalize(playerName));
    }

    // Dùng code → xoá khỏi map (chỉ 1 lần duy nhất)
    public static void consume(String code) {
        if (code != null) {
            codeMap.remove(code.toUpperCase());
           // System.out.println("[Muacode] consume code -> " + code);
        }
    }
}
