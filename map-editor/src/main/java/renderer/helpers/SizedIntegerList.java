package renderer.helpers;

import java.util.ArrayList;

public class SizedIntegerList extends ArrayList<Integer>{
    private int max;

    public SizedIntegerList(int max) {
        this.max = max;
    }

    @Override
    public boolean add(Integer i) {
        if (this.size() >= max) {
            return false;
        }
        return super.add(i);
    }

    public int[] toIntArray() {
        int[] out = new int[max];
        int idx = 0;
        while (idx < size()) {
            out[idx] = get(idx);
            idx++;
        }
        return out;
    }
}
