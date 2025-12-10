package A2;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class Jugador {
	private int x;
	private int y;
	private int anchura;
	private int altura;
	private int velocidad;
	private boolean presionIzq;
	private boolean presionDer;
	private int vida;
	private String rfid;

	public Jugador() {
		super();
	}

	public Jugador(int x, int y) {
		this.x = x;
		this.y = y;
		this.anchura = 70;
		this.altura = 20;
		this.velocidad = 10;
		this.presionIzq = false;
		this.presionDer = false;
		this.vida = 2000;
	}

	public boolean collidesWith(Proyectil proyectil) {
		Rectangle playerRect = new Rectangle(x, y, anchura, altura);
		Rectangle projectileRect = new Rectangle(proyectil.getX(), proyectil.getY(), proyectil.getAnchura(),
				proyectil.getAltura());
		return playerRect.intersects(projectileRect);
	}

	public boolean intersects(Rectangle projectileBounds) {
		Rectangle playerRect = new Rectangle(x, y, anchura, altura);
		return playerRect.intersects(projectileBounds);
	}

	public void movimiento() {

		if (presionIzq && x > 0) {
			x -= velocidad;
		}
		if (presionDer && x < 750 - anchura) {
			x += velocidad;
		}
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			presionIzq = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			presionDer = true;
		}
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			presionIzq = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			presionDer = false;
		}
	}

	public void draw(Graphics g) {
		g.setColor(Color.DARK_GRAY);
		g.fillRect(x, y, anchura, altura);
	}

    public void recibirDaño(int daño) {
        vida -= daño;
    }

    public boolean muere() {
        return vida <= 0;
    }
    
    public Rectangle getBounds() {
		return new Rectangle(x, y, anchura, altura);
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

	public int getVida() {
		return vida;
	}

	public void setVida(int vida) {
		this.vida = vida;
	}
	
	public String getRFID() {
	    return rfid;
	}

	public void setRFID(String rfid) {
	    this.rfid = rfid;
	}


}
