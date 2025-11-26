package nro.models.player;

import nro.models.item.Item;
import nro.models.item.ItemOption;

/**
 *
 * @author Văn Tuấn - 0337766460
 * @copyright 💖 GirlkuN 💖
 *
 */
public class SetClothes {

    private Player player;

    public SetClothes(Player player) {
        this.player = player;
    }

    public byte songoku;
    public byte thienXinHang;
    public byte kirin;

    public byte ocTieu;
    public byte pikkoroDaimao;
    public byte picolo;

    public byte kakarot;
    public byte cadic;
    public byte nappa;
    
     public byte songokulv1;
    public byte thienXinHanglv1;
    public byte kirinlv1;

    public byte ocTieulv1;
    public byte pikkoroDaimaolv1;
    public byte picololv1;

    public byte kakarotlv1;
    public byte cadiclv1;
    public byte nappalv1;
    
    
    public byte songokulv2;
    public byte thienXinHanglv2;
    public byte kirinlv2;

    public byte ocTieulv2;
    public byte pikkoroDaimaolv2;
    public byte picololv2;

    public byte kakarotlv2;
    public byte cadiclv2;
    public byte nappalv2;

    public byte tinhan;
    public byte nguyetan;   
    public byte nhatan;
     
   
    public byte setDHD;
    public byte setDTS;
    public byte setDTL;

    public int ctHaiTac = -1;

    public void setup() {
        setDefault();
        setupSKT();
        setupAN();
        setupDTS();
        setupDHD();
        setupDTL();
        Item ct = this.player.inventory.itemsBody.get(5);
        if (ct.isNotNullItem()) {
            switch (ct.template.id) {
                case 618:
                case 619:
                case 620:
                case 621:
                case 622:
                case 623:
                case 624:
                case 626:
                case 627:
                    this.ctHaiTac = ct.template.id;
                    break;
            }
        }
    }

    private void setupSKT() {
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                boolean isActSet = false;
                for (ItemOption io : item.itemOptions) {
                    switch (io.optionTemplate.id) {
                        case 129:
                        case 141:
                            isActSet = true;
                            songoku++;
                            break;
                        case 127:
                        case 139:
                            isActSet = true;
                            thienXinHang++;
                            break;
                        case 128:
                        case 140:
                            isActSet = true;
                            kirin++;
                            break;
                        case 131:
                        case 143:
                            isActSet = true;
                            ocTieu++;
                            break;
                        case 132:
                        case 144:
                            isActSet = true;
                            pikkoroDaimao++;
                            break;                           
                        case 130:
                        case 142:
                            isActSet = true;
                            picolo++;
                            break;
                        case 135:
                        case 138:
                            isActSet = true;
                            nappa++;
                            break;
                        case 133:
                        case 136:
                            isActSet = true;
                            kakarot++;
                            break;
                        case 134:
                        case 137:
                            isActSet = true;
                            cadic++;
                            break;
                       
                      case 211:
                          isActSet = true;
                          cadiclv1++;
                            break;
                        case 212:
                            isActSet = true;
                            kakarotlv1++;
                            break;
                        case 213:
                            isActSet = true;
                            nappalv1++;
                            break;
                         case 214:
                            isActSet = true;
                            thienXinHanglv1++;
                            break;
                        case 215:
                            isActSet = true;
                            kirinlv1++;
                            break;
                         case 216:
                            isActSet = true;
                            songokulv1++;
                            break;
                         case 217:
                            isActSet = true;
                            picololv1++;
                            break;
                            case 218:
                            isActSet = true;
                            ocTieulv1++;
                            break;
                            case 219:
                            isActSet = true;
                            pikkoroDaimaolv1++;
                            break;
                            case 220:
                          isActSet = true;
                          cadiclv2++;
                            break;
                        case 221:
                            isActSet = true;
                            kakarotlv2++;
                            break;
                        case 222:
                            isActSet = true;
                            nappalv2++;
                            break;
                         case 223:
                            isActSet = true;
                            thienXinHanglv2++;
                            break;
                        case 224:
                            isActSet = true;
                            kirinlv2++;
                            break;
                         case 225:
                            isActSet = true;
                            songokulv2++;
                            break;
                         case 226:
                            isActSet = true;
                            picololv2++;
                            break;
                            case 227:
                            isActSet = true;
                            ocTieulv2++;
                            break;
                            case 228:
                            isActSet = true;
                            pikkoroDaimaolv2++;
                            break;                           
                    }
                    if (isActSet) {
                        break;
                    }
                }
            } else {
                break;
            }
        }
    }

    private void setupAN() {
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                boolean isActSett = false;
                for (ItemOption io : item.itemOptions) {
                    switch (io.optionTemplate.id) {
                        case 34:
                            isActSett = true;
                            tinhan++;
                            break;
                        case 35:
                            isActSett = true;
                            nguyetan++;
                            break;
                        case 36:
                            isActSett = true;
                            nhatan++;
                            break;
                    }
                    if (isActSett) {
                        break;
                    }

                }
            } else {
                break;
            }
        }
    }

    private void setupDTS() {
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                boolean isActSet = false;
                for (ItemOption io : item.itemOptions) {
                    switch (io.optionTemplate.id) {
                        case 21:
                            if (io.param == 120) {
                                setDTS++;
                            }
                            break;
                    }
                    if (isActSet) {
                        break;
                    }

                }
            } else {
                break;
            }
        }
    }

    private void setupDHD() {
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                boolean isActSet = false;
                for (ItemOption io : item.itemOptions) {
                    switch (io.optionTemplate.id) {
                        case 21:
                            if (io.param == 80) {
                                setDHD++;
                            }
                            break;
                    }
                    if (isActSet) {
                        break;
                    }

                }
            } else {
                break;
            }
        }
    }

    private void setupDTL() {
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                boolean isActSet = false;
                    switch (item.template.id) {
                        case 555:
                        case 556:
                        case 557:
                        case 558:
                        case 559:
                        case 560:
                        case 561:
                        case 562:
                        case 563:
                        case 564:
                        case 565:
                        case 566:
                        case 567:
                                setDTL++;
                            break;
                    }
                    if (isActSet) {
                        break;
                    }
            } else {
                break;
            }
        }
    }

    private void setDefault() {
        this.songoku = 0;
        this.thienXinHang = 0;
        this.kirin = 0;
        this.ocTieu = 0;
        this.pikkoroDaimao = 0;
        this.picolo = 0;
        this.kakarot = 0;
        this.cadic = 0;
        this.nappa = 0;
        this.ctHaiTac = -1;
        
        this.songokulv1 = 0;
        this.thienXinHanglv1 = 0;
        this.kirinlv1 = 0;
        this.ocTieulv1 = 0;
        this.pikkoroDaimaolv1 = 0;
        this.picololv1 = 0;
        this.kakarotlv1 = 0;
        this.cadiclv1 = 0;
        this.nappalv1 = 0;
        
        this.songokulv2 = 0;
        this.thienXinHanglv2 = 0;
        this.kirinlv2 = 0;
        this.ocTieulv2 = 0;
        this.pikkoroDaimaolv2 = 0;
        this.picololv2 = 0;
        this.kakarotlv2 = 0;
        this.cadiclv2 = 0;
        this.nappalv2 = 0;

        this.tinhan = 0;
        this.nhatan = 0;
        this.nguyetan = 0;
        
        

        this.setDHD = 0;
        this.setDTS = 0;
        this.setDTL = 0;
    }

    public void dispose() {
        this.player = null;
    }
}
