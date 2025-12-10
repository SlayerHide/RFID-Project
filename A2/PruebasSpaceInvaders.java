package A2;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PruebasSpaceInvaders extends JPanel implements ActionListener, KeyListener {

	private Timer timer;
	private Jugador jugador;
	private int Porcentaje = 0;
	private ArrayList<Enemigo> enemigos = new ArrayList();
	private ArrayList<Proyectil> jugadorProyectiles;
	private ArrayList<Proyectil> enemigoProyectiles;
	private boolean GameOver = false;
	private String nomJugador;
	private List<String> puntajes;
	private static final int MAX_PUNTAJES = 5400;
	private final String NOMBRE_ARCHIVO = "puntajes1.txt";
	private int velocidadObjetos = 1;
	private boolean disparoPendiente = false;
	private BufferedImage jugadorImage;
	private BufferedImage enemigoImage;
	private BufferedImage jefeFinalImage;
	private Clip clip;
	private boolean movingRight = true;
	private static final int NUM_ENEMIGOS_INICIAL = 6;
	private static final int PUNTOS_POR_JEFE_FINAL = 1000;
	private int velocidadJefeFinal = 5;
	private int rondaActual = 1;
	private boolean jefeFinalActivo = false;
	private JefeFinal jefeFinal;
	private boolean vidsExtraJefe = true;//Estaba en true
	private int dañoJugador_Jefe = 500;

	public PruebasSpaceInvaders() {
		nomJugador = JOptionPane.showInputDialog("Ingrese su nickName");
		setPreferredSize(new Dimension(800, 600));
		addKeyListener(this);
		setFocusable(true);
		puntajes = cargarPuntajesDesdeArchivo();
		inicialiVGame();
		timer = new Timer(10, this);
		timer.start();

		try {
			enemigoImage = ImageIO.read(new File("Imagen_Alien2.png"));
			jugadorImage = ImageIO.read(new File("Imagen_Nave3.png"));
			jefeFinalImage = ImageIO.read(new File("Imagen_Jefe.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("Music_DOOM.wav"));
			clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	private void inicialiVGame() {
		jugador = new Jugador(400, 550);
		// enemigos = new ArrayList<>();
		jugadorProyectiles = new ArrayList<>();
		enemigoProyectiles = new ArrayList<>();
		generarEnemigos();

		// esto fue agrgado
		if (rondaActual == 2 && !jefeFinalActivo) {
			jefeFinal = new JefeFinal(400, 50);
			enemigos.clear(); // Limpiar enemigos actuales
			jefeFinalActivo = true;
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!GameOver) {
			jugador.movimiento();
			moveEnemies();
			gestionarRonda();
			gestionarJefeFinal();
			procesarProyectilesJugador();
			procesarColisionesEnemigos();
			procesarColisionesProyectilesEnemigos();
			generarDisparosEnemigos();
			procesarProyectilesJefeFinal();
			repaint();
		}

	}
     //metodo de busqueda
	private void procesarProyectilesJefeFinal() {
		for (int i = jugadorProyectiles.size() - 1; i >= 0; i--) {
			Proyectil projectile = jugadorProyectiles.get(i);
			projectile.mover();

			if (jefeFinal != null && projectile.intersects(jefeFinal.getBounds())) {
				jefeFinal.recibirDaño(this.dañoJugador_Jefe);
				jugadorProyectiles.remove(i);
				if (jefeFinal.muere()) {
					jefeFinal.destruir();
					Porcentaje += 1000; 
					GameOver = true; 
				}
				break; 
			}
		}
	}
     //metedo de busqueda
	private void procesarProyectilesJugador() {
		for (int i = jugadorProyectiles.size() - 1; i >= 0; i--) {
			Proyectil projectile = jugadorProyectiles.get(i);
			projectile.mover();
			for (int j = enemigos.size() - 1; j >= 0; j--) {
				Enemigo enemy = enemigos.get(j);
				if (projectile.intersects(enemy.getBounds())) {
					enemy.recibeDa(100);
					if (enemy.muere()) {
						Porcentaje += 100;
						enemigos.remove(j);
					}
					jugadorProyectiles.remove(i);
					break;
				}
			}
		}
	}
     //metodo de busqueda
	private void procesarColisionesEnemigos() {
		for (int i = enemigoProyectiles.size() - 1; i >= 0; i--) {
			Proyectil proyectil = enemigoProyectiles.get(i);
			proyectil.mover();
			if (proyectil.intersects(jugador.getBounds())) {
				jugador.recibirDaño(100);
				enemigoProyectiles.remove(i);
				if (jugador.muere()) {
					GameOver = true;
				}
			}
		}
	}

	private void procesarColisionesProyectilesEnemigos() {
		for (Enemigo enemigo : enemigos) {
			enemigo.mover();
			if (Math.random() < 0.01) {
				enemigoProyectiles.add(new Proyectil(enemigo.getX() + enemigo.getAnchura() / 2,
						enemigo.getY() + enemigo.getAltura(), false));
			}
		}
	}

	private void generarDisparosEnemigos() {
		for (Enemigo enemigo : enemigos) {
			if (Math.random() < 0.01) {
				enemigoProyectiles.add(new Proyectil(enemigo.getX() + enemigo.getAnchura() / 2,
						enemigo.getY() + enemigo.getAltura(), false));
			}
		}
	}
	

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());

		g.setColor(Color.WHITE);
		g.drawString("Jugador: [ " + nomJugador + " ]  Vida: [ " + jugador.getVida() + " ]", 0, 50);
		

		g.setColor(Color.WHITE);
		for (int i = 0; i < 10; i++) {
			int x = (int) (Math.random() * getWidth());
			int y = (int) (Math.random() * getHeight());
			g.fillRect(x, y, 2, 2);
			try {
				Thread.sleep(this.velocidadObjetos); // Pausa en milisegundos
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}

		g.setColor(Color.BLUE);
		for (int i = 0; i < 5; i++) {
			int x = (int) (Math.random() * getWidth());
			int y = (int) (Math.random() * getHeight());
			g.fillRect(x, y, 2, 2);

			try {
				Thread.sleep(this.velocidadObjetos);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		g.setColor(Color.YELLOW);
		for (int i = 0; i < 2; i++) {
			int x = (int) (Math.random() * getWidth());
			int y = (int) (Math.random() * getHeight());
			g.fillRect(x, y, 6, 6);

			try {
				Thread.sleep(this.velocidadObjetos);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		g.setColor(Color.GRAY);
		for (int i = 0; i < 5; i++) {
			int x = (int) (Math.random() * getWidth());
			int y = (int) (Math.random() * getHeight());
			g.fillRect(x, y, 4, 4);

			try {
				Thread.sleep(this.velocidadObjetos);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}

		// jugador.draw(g);//por si no jala la linea de abajo
		g.drawImage(jugadorImage, jugador.getX(), jugador.getY(), null);

		for (Enemigo enemy : enemigos) {
			// enemy.draw(g);
			g.drawImage(enemigoImage, enemy.getX(), enemy.getY(), null);

		}

		for (Proyectil projectile : jugadorProyectiles) {
			projectile.draw(g);
		}

		for (Proyectil projectile : enemigoProyectiles) {
			projectile.draw(g);
		}

		if (jefeFinal != null && !jefeFinal.muere()) {
			g.drawImage(jefeFinalImage, jefeFinal.getX(), jefeFinal.getY(), null);
		}

		g.drawString("Porcentaje: " + Porcentaje + "", 10, 20);
		
		//Apartado para ver la vida del jefe
		if(this.jefeFinalActivo == true) {
			g.setColor(Color.red);
			g.drawString("Vida del jefeFinal: [ "+jefeFinal.getVida()+" ]",150,20);
		}
		

		if (GameOver) {
			this.Porcentaje += jugador.getVida();
			
			//terminar al rato
			if(jugador.muere() == true) {
			}
			g.drawString("Game Over", 250, 300);
			g.drawString("Porcentaje: " + Porcentaje + "", 350, 300);
			String registroPuntaje = nomJugador + " - " + Porcentaje;
			puntajes.add(registroPuntaje);
			// Se encarga de aser las comporativas de la informacion del txt
			//metodo de ordenamiento                                             -----------------
			Collections.sort(puntajes, new Comparator<String>() {
				public int compare(String puntaje1, String puntaje2) {

					int puntajeInt1 = Integer.parseInt(puntaje1.split(" - ")[1]);
					int puntajeInt2 = Integer.parseInt(puntaje2.split(" - ")[1]);

					return Integer.compare(puntajeInt2, puntajeInt1);
				}
			});
			puntajes = puntajes.subList(0, Math.min(MAX_PUNTAJES, puntajes.size()));

			try {
				guardarPuntajesEnArchivo(puntajes);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			mostrarVentanaPuntajes();
			clip.stop();
		}

	}

	private void moveEnemies() {
		int velocidad = 2; 

		for (Enemigo enemigo : enemigos) {
			if (movingRight) {
				enemigo.setX(enemigo.getX() + velocidad);
			} else {
				enemigo.setX(enemigo.getX() - velocidad);
			}

			if (enemigo.getX() <= 0 || enemigo.getX() + enemigo.getAnchura() >= getWidth()) {
				movingRight = !movingRight;
			}
		}
	}

	private void generarEnemigos() {
		for (int i = 0; i < NUM_ENEMIGOS_INICIAL; i++) {
			for (int j = 0; j < 2; j++) {
				Enemigo enemy = new Enemigo(100 + i * 100, 50 + j * 50);
				enemy.mover();
				enemigos.add(enemy);
			}
		}
	}

	private void generarJefeFinal() {
		jefeFinal = new JefeFinal(400, 50);
		jefeFinalActivo = true;
		jefeFinal.setGraphics(getGraphics());// agregado
	}

	private void gestionarRonda() {
		if (enemigos.isEmpty()) {
			if (rondaActual == 1) {
				rondaActual++;
				generarEnemigos();
			} else if (rondaActual == 2 && !jefeFinalActivo) {
				generarJefeFinal();
			}
		}
	}
	
	private void gestionarJefeFinal() {
		if (jefeFinalActivo) {
			jefeFinal.mover();

			if (jefeFinal.getVida() == 300) {

				if (vidsExtraJefe == true) {

					if (Math.random() < 1) {
						enemigoProyectiles.add(new Proyectil(jefeFinal.getX() + jefeFinal.getAnchura() / 2,
								jefeFinal.getY() + jefeFinal.getAltura(), false));
						
					}
				} else {

					if (Math.random() < 0.1) {
						enemigoProyectiles.add(new Proyectil(jefeFinal.getX() + jefeFinal.getAnchura() / 2,
								jefeFinal.getY() + jefeFinal.getAltura(), false));
					}
				}
			} else {

				if (Math.random() < 0.1) {
					enemigoProyectiles.add(new Proyectil(jefeFinal.getX() + jefeFinal.getAnchura() / 2,
							jefeFinal.getY() + jefeFinal.getAltura(), false));
				}
			}

			if (jefeFinal.muere()) {
				Porcentaje += PUNTOS_POR_JEFE_FINAL;
				jefeFinalActivo = false;
				rondaActual++;
			}
		}
	}
	/*private void gestionarJefeFinal() {
		boolean seCobroVidaExtra = false;
		if (jefeFinalActivo) {
			jefeFinal.move();
			
			if(seCobroVidaExtra == false) {
				if (Math.random() < 0.1) {
					enemigoProyectiles.add(new Proyectil(jefeFinal.getX() + jefeFinal.getWidth() / 2,
							jefeFinal.getY() + jefeFinal.getHeight(), false));
				
				}
			}

			if (jefeFinal.getVida() == 300 && vidsExtraJefe == true) {
				if (Math.random() < 1) {
					enemigoProyectiles.add(new Proyectil(jefeFinal.getX() + jefeFinal.getWidth() / 2,
							jefeFinal.getY() + jefeFinal.getHeight(), false));
					vidsExtraJefe = false;//estaba en false
					seCobroVidaExtra = true;
					jefeFinal.setVida(700);
					// jugador.setVida(2000);
				}
				
			}

			if (jefeFinal.isDead()) {
				Porcentaje += PUNTOS_POR_JEFE_FINAL;
				jefeFinalActivo = false;
				rondaActual++;
			}
		}
	}*/
	

	// Apartado de gestion del Archivo txt
	private void mostrarVentanaPuntajes() {
		JFrame scoreFrame = new JFrame("Score||Game");
		scoreFrame.getContentPane().removeAll();

		List<String> puntajes = cargarPuntajesDesdeArchivo();
		JTextArea textArea = new JTextArea(20, 20);
		textArea.setEditable(false);
		//g.setColor(Color.BLACK);
		textArea.setFont(new Font("Arial", Font.PLAIN, 16));

		for (String puntaje : puntajes) {
			textArea.append(puntaje + "\n");
		}

		JScrollPane scrollPane = new JScrollPane(textArea);
		scoreFrame.add(scrollPane);

		scoreFrame.pack();
		scoreFrame.setLocationRelativeTo(null);
		scoreFrame.setVisible(true);
	}

	private List<String> cargarPuntajesDesdeArchivo() {
		List<String> puntajes = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(NOMBRE_ARCHIVO))) {
			String linea;
			while ((linea = reader.readLine()) != null) {
				puntajes.add(linea);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return puntajes;
	}

	private void guardarPuntajesEnArchivo(List<String> puntajes) throws IOException {

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(NOMBRE_ARCHIVO))) {
			for (String puntaje : puntajes) {
				writer.write(puntaje + "\n");
			}
		}
	}

	/// ------------------------------------------
	@Override
	public void keyPressed(KeyEvent e) {
		jugador.keyPressed(e);
		//boolean disparo = false;

		if (e.getKeyCode() == KeyEvent.VK_SPACE && disparoPendiente == false ) {
			Proyectil projectile = new Proyectil(jugador.getX() + jugador.getAnchura() / 2, jugador.getY(), true);
			jugadorProyectiles.add(projectile);
			 disparoPendiente = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		jugador.keyReleased(e);
		
		  if (e.getKeyCode() == KeyEvent.VK_SPACE) { disparoPendiente = false; }
		 
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

}

