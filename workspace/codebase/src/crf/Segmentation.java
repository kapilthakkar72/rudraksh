package crf;

public class Segmentation implements Comparable{
	public String seg;
	public double prob;

	public Segmentation(String _seg, double _prob) {
		seg = _seg;
		prob = _prob;
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return (prob<((Segmentation)o).prob)?1:-1;
	}
}
