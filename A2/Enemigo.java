package A2;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Enemigo {
	private int x;
	private int y;
	private int anchura;
	private int altura;
	private int velocidad;
	private int vida;

	
	public Enemigo() {
		super();
	}

	public Enemigo(int x, int y) {
		this.x = x;
		this.y = y;
		this.anchura = 62;
		this.altura = 42;
		this.velocidad = 1;
		this.vida = 400;
		
	}


	public boolean collidesWith(Proyectil proyectil) {
		Rectangle enemyRect = new Rectangle(x, y, anchura, altura);
		Rectangle projectileRect = new Rectangle(proyectil.getX(), proyectil.getY(), proyectil.getAnchura(),
				proyectil.getAltura());
		return enemyRect.intersects(projectileRect);
	}

	public Rectangle getBounds() {
		return new Rectangle(x, y, anchura, altura);
	}

	public void mover() {
		y += velocidad;	
		
	}
	

	public void draw(Graphics g) {
		g.setColor(Color.BLUE);
		g.fillRect(x, y, anchura, altura);
	}
	
	public void recibeDa(int daño) {
        vida -= daño;
    }

    public boolean muere() {
        return vida <= 0;
    }

	public int getAnchura() {
		return anchura;
	}

	public int getAltura() {
		return altura;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getVida() {
		return vida;
	}

	public void setVida(int vida) {
		this.vida = vida;
	}
	

}
