package A2;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Proyectil {
	private int x;
	private int y;
	private int anchura;
	private int altura;
	private int velocidad;
	private boolean esProyectilDelJugador;
	private int daño;
	
	public Proyectil(int x, int y, boolean esProyectilDelJugador) {
		this.x = x;
		this.y = y;
		this.anchura = 5;
		this.altura = 10;
		this.velocidad = 10;
		this.esProyectilDelJugador = esProyectilDelJugador;
		this.daño = 100;
	}

	public void mover() {
		if (esProyectilDelJugador) {
			y -= velocidad;
		} else {
			y += velocidad;
		}
	}

	public void draw(Graphics g) {
		g.setColor(esProyectilDelJugador ? Color.MAGENTA : Color.CYAN);
		g.fillRect(x, y, anchura, altura);
	}

	public boolean intersects(Rectangle other) {
		Rectangle projectileRect = new Rectangle(x, y, anchura, altura);
		return projectileRect.intersects(other);
	}
	
	

	public int getDaño() {
		return daño;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getAnchura() {
		return anchura;
	}

	public int getAltura() {
		return altura;
	}

}
