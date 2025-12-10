package A2;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageObserver;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.Timer;

import java.awt.Color;

public class JefeFinal  {

	private int x, y;
	private int vida;
	private int velocidadX;
	private int anchura;
	private int altura;
	private Timer timerDisparos;
	private Timer timerMovimiento;
	private Color color;
	private ArrayList<Proyectil> jefeProyectiles;
	private Graphics g;

	public JefeFinal(int x, int y) {
		this.x = x;
		this.y = y;
		this.anchura = 50;
		this.altura = 50;
		this.vida = 2000;
		this.color = Color.GREEN;
		this.jefeProyectiles = new ArrayList<>();
		this.velocidadX = 10;

		timerMovimiento = new Timer(10, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mover();
			}
		});
		timerMovimiento.start();

		timerDisparos = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				realizarDisparo();
			}
		});
		timerDisparos.start();
	}

	private void realizarDisparo() {

		int disparoX = getX() + getAnchura() / 2;
		int disparoY = getY() + getAltura();

		Proyectil proyectil = new Proyectil(disparoX, disparoY, false);
		jefeProyectiles.add(new Proyectil(x + anchura / 2, y + altura, false));
	}

	public void mover() {
		if (x <= 0) {
			velocidadX = Math.abs(velocidadX); 
		} else if (x >= 750) {
			velocidadX = -Math.abs(velocidadX); 
		}
		x += velocidadX;
	}

	public Rectangle getBounds() {
		return new Rectangle(x, y, anchura, altura);
	}

	public void recibirDaño(int cantidad) {
		vida -= cantidad;
		if (vida <= 0) {
			destruir();
		}
	}

	public void destruir() {

		for (int i = 0; i < 100; i++) {

			int explosionX = x + (int) (Math.random() * anchura);
			int explosionY = y + (int) (Math.random() * altura);
			Color explosionColor = new Color((int) (Math.random() * 256), (int) (Math.random() * 256),
					(int) (Math.random() * 256));

			if (g != null) {
				g.setColor(explosionColor);
				g.fillRect(explosionX, explosionY, 5, 5);
			}

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void setGraphics(Graphics graphics) {
		this.g = graphics;
	}

	public void draw(Graphics g) {
		g.setColor(Color.red);
		g.fillRect(x, y, anchura, altura);
	}

	public ArrayList<Proyectil> getJefeProyectiles() {
		return jefeProyectiles;
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


	public int getVida() {
		return vida;
	}

	public void setVida(int vida) {
		this.vida = vida;
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

}
