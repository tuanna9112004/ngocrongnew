package nro.models.player;

/**
 * Quản lý thông tin gia đình (kết hôn, ly hôn, goá phụ, nuôi con, thai kỳ)
 */
public class Family {

    // ====== HẰNG SỐ TRẠNG THÁI HÔN NHÂN ======
    public static final byte STATUS_SINGLE   = 0; // Độc thân
    public static final byte STATUS_MARRIED  = 1; // Đã kết hôn
    public static final byte STATUS_DIVORCED = 2; // Đã ly hôn
    public static final byte STATUS_WIDOW    = 3; // Goá phụ (nuôi con 1 mình)

    // ====== THÔNG TIN HÔN NHÂN ======
    public long spouseId;        // ID người chơi kết hôn (0 nếu không có)
    public long marriedAt;       // Thời điểm kết hôn (timestamp millis)
    public byte status;          // Trạng thái hôn nhân

    // ====== THÔNG TIN CON CÁI ======
    public byte childLevel;      // Cấp độ con (0 = chưa có, max = 18)
    public int childExp;         // Kinh nghiệm hiện tại của con
    public byte custody;         // Quyền nuôi con (0: chung, 1: vợ, 2: chồng...)

    // ====== THAI KỲ ======
    public boolean isPregnant;   // Có đang mang thai không
    public long pregnancyStart;  // Thời điểm bắt đầu mang thai (timestamp millis)

    // ====== HÀM KIỂM TRA ======
    /** Có đang kết hôn không */
    public boolean isMarried() {
        return status == STATUS_MARRIED && spouseId > 0;
    }

    /** Có phải goá phụ không */
    public boolean isWidow() {
        return status == STATUS_WIDOW;
    }

    /** Đã có con chưa */
    public boolean hasChild() {
        return childLevel > 0;
    }

    /** Đang mang thai không */
    public boolean isPregnantNow() {
        return isPregnant;
    }

    // ====== RESET ======
    /** Reset thông tin con */
    public void resetChild() {
        this.childLevel = 0;
        this.childExp = 0;
    }

    /** Reset thông tin thai kỳ */
    public void resetPregnancy() {
        this.isPregnant = false;
        this.pregnancyStart = 0;
    }
}
