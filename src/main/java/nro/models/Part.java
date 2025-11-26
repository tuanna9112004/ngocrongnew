
package nro.models;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Kitak
 */
@Getter
@Setter
public class Part {

    public short id;

    byte type;

    private PartImage[] pi;

    public short getIcon(int index) {
        return pi[index].getIcon();
    }
    public static class ArrHead2Frames {

        public List<Integer> frames = new ArrayList();

    }
}
