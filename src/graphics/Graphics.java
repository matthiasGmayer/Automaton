package graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import math.Circle;
import math.Line;
import math.Matrix;
import math.Rectangle;
import math.Vector;
import run.Main;
import util.AutomatonState;

public class Graphics {
	private Graphics2D g;
	private static int pixelGradientSize = 15;
	private static Color standardColor = Color.black;

	public void set(Graphics2D g) {
		this.g = g;
	}

	public void drawOval(int x, int y, int w, int h, Color color) {
		g.setColor(color);
		g.drawOval(x - w / 2, y - h / 2, w, h);
	}

	public void drawOval(int x, int y, int w, int h) {
		drawOval(x, y, w, h, standardColor);
	}

	public void drawString(String s, int x, int y, Color color) {
		g.setColor(color);
		Rectangle2D r = g.getFontMetrics().getStringBounds(s, g);
		int w = (int) r.getWidth();
		int h = (int) r.getHeight();
		g.drawString(s, x - w / 2, y + 2 * h / 6);
	}

	public void drawString(String s, int x, int y) {
		drawString(s, x, y, standardColor);
	}

	public void drawString(String s, Vector v, Color color) {
		drawString(s, v.x.intValue(), v.y.intValue(), color);
	}

	public void drawString(String s, Vector v) {
		drawString(s, v.x.intValue(), v.y.intValue());
	}

	public void drawState(int x, int y, int r, String name, boolean isFinal, Color color) {
		int offset = isFinal ? 10 : 0;
		for (int i = -pixelGradientSize; i <= 0; i++) {
			float c = Math.min(i * i + 50, 255) / 255f;
			drawOval(x, y, r + i - offset, r + i - offset, new Color(c * (color.getRed() / 255f),
					c * (color.getGreen() / 255f), c * (color.getBlue() / 255f)));
		}
		drawOval(x, y, r - offset, r - offset);
		drawString(name, x, y);
		if (isFinal)
			drawOval(x, y, r, r, color);
//		drawOval(x, y, r - 1, r - 1, color);
	}

	public void drawState(AutomatonState s, Color color) {
		drawState(s.position.x.intValue(), s.position.y.intValue(), Main.stateRadius * 2, s.name, s.isFinal, color);
	}

	public void drawCircle(Circle c, Color color) {
		drawOval(c.position.x.intValue(), c.position.y.intValue(), (int) c.radius * 2, (int) c.radius * 2, color);
	}

	public void drawCircle(Circle c) {
		drawCircle(c, standardColor);
	}

	public void drawLine(Vector v, Vector v2, Color color) {
		g.setColor(color);
		g.drawLine(v.x.intValue(), v.y.intValue(), v2.x.intValue(), v2.y.intValue());
	}

	public void drawLine(Vector v, Vector v2) {
		drawLine(v, v2, standardColor);
	}

	public void drawLine(Line l, Color color) {
		if (l.segmented)
			drawLine(l.anchor, l.anchor.add(l.direction), color);
		else {
			Vector add = l.direction.norm(5000f);
			drawLine(l.anchor.sub(add), l.anchor.add(add), color);
		}
	}

	public void drawLine(Line l) {
		drawLine(l, standardColor);
	}

	public void drawRectangle(Rectangle r, Color color) {
		g.setColor(color);
		Vector position = r.position.sub(r.dimension.scale(0.5f));
		g.drawRect(position.x.intValue(), position.y.intValue(), r.dimension.x.intValue(), r.dimension.y.intValue());
	}

	public void drawRectangle(Rectangle r) {
		drawRectangle(r, standardColor);
	}

	public void draw(Object... os) {
		for (Object o : os) {
			if (o instanceof Line)
				drawLine((Line) o);
			if (o instanceof Circle)
				drawCircle((Circle) o);
		}
	}

	public void drawEdge(Circle c, Circle c2, Vector v) {
		Vector v1, v2;
		v1 = c.position.add(v.sub(c.position).norm(c.radius));
		v2 = c2.position.add(v.sub(c2.position).norm(c2.radius));
		drawLine(v1, v);
		drawLine(v, v2);
		Vector a = v.sub(v2).norm(10);
		Vector b = a.add(new Vector(-a.y, a.x).scale(0.5f));
		Vector b2 = a.add(new Vector(a.y, -a.x).scale(0.5f));
		drawLine(v2, v2.add(b));
		drawLine(v2, v2.add(b2));
	}

	public void drawTransition(AutomatonState s, AutomatonState s2, String name, Color color) {
		name = name.replace("null", "\u03B5");
		if (s == s2) {
			Vector pos = s.position.sub(new Vector(Main.stateRadius * 2.4f, Main.stateRadius));
			int w = (int) (Main.stateRadius * 2);
			g.setColor(color);
			g.drawArc(pos.x.intValue(), pos.y.intValue(), w, w, 45, 270);
			drawString(name.replace(",", "\n"), pos.add(new Vector(-10, Main.stateRadius)));
			Vector v = Matrix.createRotation2D(-3.141f / 4).multiply(new Vector(Main.stateRadius, 0));
			drawArrow(s.position.sub(v).sub(new Vector(1, -1)), s.position.sub(v), color);
		} else {
			Vector offset = Matrix.createRotation2D(3.141f / 2)
					.multiply(s2.position.sub(s.position).norm(Main.transitionOffset));
			Vector pos1 = s.position.add(offset), pos2 = s2.position.add(offset);
			Vector v1, v2;
			v1 = pos1.add(pos2.sub(pos1).norm(Main.stateRadius));
			v2 = pos2.add(pos1.sub(pos2).norm(Main.stateRadius));
			drawString(name,v1.add(v2).scale(0.5f).add(offset));
			drawArrow(v1, v2, color);
		}
	}

	public void drawArrow(Vector v1, Vector v2, Color color) {
		drawLine(v1, v2, color);
		Vector a = v1.sub(v2).norm(10);
		Vector b = a.add(new Vector(-a.y, a.x).scale(0.5f));
		Vector b2 = a.add(new Vector(a.y, -a.x).scale(0.5f));
		drawLine(v2, v2.add(b), color);
		drawLine(v2, v2.add(b2), color);
	}

	public void fillRect(int x, int y, int width, int height, Color color) {
		g.setColor(color);
		g.fillRect(x, y, width, height);
	}
	
	public void translate(Vector v) {
		g.translate(v.x, v.y);
	}
	public void scale(float f) {
		g.scale(f, f);
	}
	private AffineTransform save;
	public void save() {
		save = g.getTransform();
	}
	public void reset() {
		g.setTransform(save);
	}
}
