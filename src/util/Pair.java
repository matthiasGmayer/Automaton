package util;

import java.io.Serializable;

public class Pair<X, Y> implements Serializable{
	private static final long serialVersionUID = -5139336514174332317L;
	public final X x;
	public final Y y;

	public Pair(X x, Y y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}

		if (!(other instanceof Pair)) {
			return false;
		}

		@SuppressWarnings("unchecked")
		Pair<X, Y> o = (Pair<X, Y>) other;
		boolean b = false;
		if (x == null) {
			if (o.x == null) {
				b = true;
			}
		} else {
			b = x.equals(o.x);
		}
		if (!b) {
			return false;
		}
		if (y == null) {
			return o.y == null;
		} else {
			return y.equals(o.y);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		return result;
	}
}
