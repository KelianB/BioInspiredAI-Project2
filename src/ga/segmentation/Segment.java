package ga.segmentation;

public class Segment {

    private LinkedList<int[]> pixels;

    public Segment () {
        this.pixels = new LinkedList<int[]>();
    }

    public LinkedList<int[]> getPixels () {
        return this.pixels;
    }

}
