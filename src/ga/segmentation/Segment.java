package ga.segmentation;

import java.util.ArrayList;
import java.util.List;

public class Segment {
    private List<Integer> pixels;

    public Segment () {
        this.pixels = new ArrayList<Integer>();
    }
    
    public void addPixel(int i) {
    	this.pixels.add(i);
    }

    public List<Integer> getPixels () {
        return this.pixels;
    }
}
