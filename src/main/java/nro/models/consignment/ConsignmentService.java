package nro.models.consignment;

import nro.models.player.Player;

/**
 * @author Văn Tuấn - 0337766460
 * Service mỏng cho Consignment (để mở rộng rule/check nếu cần)
 */
public class ConsignmentService {

    private static final ConsignmentService INSTANCE = new ConsignmentService();
    public static ConsignmentService gI() { return INSTANCE; }

    /** Có cho phép player này dùng ký gửi không (ví dụ yêu cầu actived, power...) */
    public boolean canUseConsign(Player p) {
        return p != null && p.getSession() != null && p.getSession().actived;
    }

    /** Phí giao dịch (%) khi người bán nhận tiền sau khi bán */
    public int feePercent() {
        return 10;
    }

    /** Giá hợp lệ? (10..1_000_000_000) */
    public boolean isValidPrice(int price) {
        return price >= 10 && price <= 1_000_000_000;
    }

    /** Số lượng hợp lệ? (1..99) */
    public boolean isValidQuantity(int q) {
        return q >= 1 && q <= 99;
    }
}
