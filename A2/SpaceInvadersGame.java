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
import java.util.Stack;
import java.sql.SQLException;

public class SpaceInvadersGame extends JPanel implements ActionListener, KeyListener  {

	/**
	 * Proyecto_VideoJuego_SpaceInvaders
	 *  SLAYER_HIDE_GAMES® company  
	 *  Made in Mexico
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Timer timer;
	private Jugador jugador;
	private int Porcentaje = 0;
	private Stack<Enemigo> enemigos;
	private Stack<Proyectil> jugadorProyectiles; 
    private Stack<Proyectil> enemigoProyectiles;
	private boolean GameOver = false;
	private String nomJugador;
	private List<String> puntajes;
	private static final int MAX_PUNTAJES = 5400;
	private final String REGISTRO_PUNTAJES = "puntajes1.txt";
	private int velocidadObjetos = 1;
	private boolean disparoPendiente = false;
	private BufferedImage jugadorImage;
	private BufferedImage enemigoImage;
	private BufferedImage enemigoImage2;
	private BufferedImage jefeFinalImage;
	private BufferedImage jefeFinalImage2;
	private Clip clip;
	private boolean movimDerch = true;
	private static final int NUM_ENEMIGOS_INICIAL = 6;
	private static final int PUNTOS_POR_JEFE_FINAL = 1000;
	private int rondaActual = 1;
	private boolean jefeFinalActivo = false;
	private JefeFinal jefeFinal;
	private boolean vidsExtraJefe = true;//Estaba en true
	private int dañoJugador_Jefe = 100;
	private boolean pausa = false;
	private Timer timerMovimiento;
	private boolean moviendoHaciaAbajo = true;
	private int dañoJugador_Enemigos = 100;
	private final String IMAGEN_JUGADOR = "Imagen_Nave3.png";
	private final String IMAGEN_ALIEN_NORMAL = "Imagen_Alien2.png";


	// Campos añadidos para RFID/BD

	private DBManager dbManager = null;
	private RFIDListener rfidListener = null;
	private Thread rfidThread = null;
	private volatile String activeRFID = null;
	private boolean dbSaved = false;
	private String nombreBD = null;
	private int scoreBD = 0; 


	public SpaceInvadersGame() {
	
		
	
           
		nomJugador = JOptionPane.showInputDialog("Ingrese su nickName");	
		setPreferredSize(new Dimension(800, 600));
		addKeyListener(this);
		setFocusable(true);
		puntajes = cargarPuntajesDesdeArchivo();
		inicializarValoresDelGame();
		timer = new Timer(10, this);
		timer.start();


		// Inicializar DB y RFID

		try {
			// Instancia DBManager usa la configuración que tengas en DBManager (clase)
			dbManager = new DBManager();
		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "No se pudo conectar a la base de datos (RFID/BD deshabilitado).");
			dbManager = null;
		}

		// Iniciar listener serial solo si DBManager existe
		if (dbManager != null) {
			try {
				// Cambia "COM3" por tu puerto real si es necesario
				rfidListener = new RFIDListener("COM4", uid -> onRFIDDetected(uid));
				rfidThread = new Thread(rfidListener);
				rfidThread.start();
			} catch (Exception ex) {
				ex.printStackTrace();
				System.err.println("No se pudo iniciar RFIDListener. Verifica librería/jSerialComm y puerto.");
			}
		}

		try {
			if(this.nomJugador.equalsIgnoreCase("admin")) {
				jugador.setVida(10000);
				setDañoJugador_Enemigos(1000);
				this.dañoJugador_Jefe = 200;
				jugadorImage = ImageIO.read(new File("Imagen_Admin.png"));
				enemigoImage = ImageIO.read(new File("Imagen_troll2.png"));
				
			}else {
				jugadorImage = ImageIO.read(new File(this.IMAGEN_JUGADOR));	
				enemigoImage = ImageIO.read(new File(this.IMAGEN_ALIEN_NORMAL));
			}
			jefeFinalImage = ImageIO.read(new File("Imagen_Jefe.png"));
			enemigoImage2 = ImageIO.read(new File("Imagen_Alien4.png"));
			jefeFinalImage2 = ImageIO.read(new File("Imagen_Jefe2.png"));
	
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			if(!GameOver) {
				
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("Music_DOOM.wav"));
			clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
				
			}	
			
			
		} catch ( NullPointerException |UnsupportedAudioFileException | IOException | LineUnavailableException e  ) {
			e.printStackTrace();
		}
		
		timerMovimiento = new Timer(3000, new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            cambiarDireccion();
	        }
	    });
	    timerMovimiento.start();
	
	}

	private void inicializarValoresDelGame() {
		jugador = new Jugador(400, 550);
		enemigos = new Stack<>() ;
		jugadorProyectiles = new Stack<>();
        enemigoProyectiles = new Stack<>();
		generarEnemigos();

		// esto fue agrgado
		if (rondaActual == 2 && !jefeFinalActivo) {
			jefeFinal = new JefeFinal(400, 50);
			enemigos.clear(); // Limpiar enemigos actuales
			jefeFinalActivo = true;
		}

		// Reset de bandera de guardado DB al iniciar/ reiniciar valores del juego
		dbSaved = false;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!GameOver && !pausa) {
			jugador.movimiento();
			movimientoEnemigos();
			gestionarRonda();
			gestionarJefeFinal();
			procesarProyectilesJugador();
			procesarProyectilesEnemigos();
			probabiliProyectilesEnemigos();
			procesarProyectilesJefeFinal();
			repaint();
			 if (moviendoHaciaAbajo) {
			        moverAbajo();
			    } else {
			        moverArriba();
			    }
			    repaint();
		}

	}
	
	// Apartado de enemigos
	
	private void generarEnemigos() {
		for (int i = 0; i < NUM_ENEMIGOS_INICIAL; i++) {
			for (int j = 0; j < 2; j++) {
				Enemigo enemigo = new Enemigo(100 + i * 100, 50 + j * 50);
				enemigo.mover();
				enemigos.add(enemigo);
				
			}
		}
	}
	
	private void cambiarDireccion() {
	    moviendoHaciaAbajo = !moviendoHaciaAbajo;
	}
	
	public void moverAbajo() {
	    for (Enemigo enemigo : enemigos) {
	        enemigo.setY(enemigo.getY() + 1); 
	    }
	}

	public void moverArriba () {
	    for (Enemigo enemigo : enemigos) {
	        enemigo.setY(enemigo.getY() - 3); 
	    }
	}
	

	private void movimientoEnemigos() {
		int velocidad = 2; 

		for (Enemigo enemigo : enemigos) {			
			if (movimDerch) {
				enemigo.setX(enemigo.getX() + velocidad);
			} else {
				enemigo.setX(enemigo.getX() - velocidad);
			}

			if (enemigo.getX() <= 0 || enemigo.getX() + enemigo.getAnchura() >= getWidth()) {
				movimDerch = !movimDerch;
			}
		}

	}

     //metodo de busqueda
	private void procesarProyectilesJefeFinal() {
		for (int i = jugadorProyectiles.size() - 1; i >= 0; i--) {
			Proyectil proyectil = jugadorProyectiles.get(i);
			proyectil.mover();

			if (jefeFinal != null && proyectil.intersects(jefeFinal.getBounds())) {
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
			Proyectil proyectil = jugadorProyectiles.get(i);
			proyectil.mover();
			for (int j = enemigos.size() - 1; j >= 0; j--) {
				Enemigo enemy = enemigos.get(j);
				if (proyectil.intersects(enemy.getBounds())) {
					enemy.recibeDa(this.dañoJugador_Enemigos);
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
	private void procesarProyectilesEnemigos() {
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

	private void probabiliProyectilesEnemigos() {
		for (Enemigo enemigo : enemigos) {
			enemigo.mover();
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

		
	    g.setColor(Color.MAGENTA);  
	    g.drawString("Puntaje: " + Porcentaje + "", 10, 20);

		g.setColor(Color.GREEN);
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
        // se usan pilas 
		for (Enemigo enemigo : enemigos) {
			// enemy.draw(g);
			//g.drawImage(enemigoImage, enemigo.getX(), enemigo.getY(), null);
			if(enemigo.getVida() > 200) {
				g.drawImage(enemigoImage, enemigo.getX(), enemigo.getY(), null);
			}
			else {
				g.drawImage(enemigoImage2, enemigo.getX(), enemigo.getY(), null);
			}


		}

		for (Proyectil proyectile : jugadorProyectiles) {
			proyectile.draw(g);
		}

		for (Proyectil proyectile : enemigoProyectiles) {
			proyectile.draw(g);
		}

		if (jefeFinal != null && !jefeFinal.muere()) {
			if(jefeFinal.getVida() == 1000 || jefeFinal.getVida() == 300) {
			g.setColor(Color.red);
			g.drawString("!  Ataque Furtivo Activo  !",350,20);	
			g.drawImage(jefeFinalImage2, jefeFinal.getX(), jefeFinal.getY(), null);	
			}
			else {
			g.drawImage(jefeFinalImage, jefeFinal.getX(), jefeFinal.getY(), null);	
			}
			
		}
		
		//Apartado para ver la vida del jefe
		if(this.jefeFinalActivo == true ) {
			g.setColor(Color.red);
			g.drawString("Vida del jefeFinal: [ "+jefeFinal.getVida()+" ]",150,20);
		}
		

		if (GameOver) {
			this.Porcentaje += jugador.getVida();
			

			// Guardar en BD una unica vez

			if (!dbSaved && dbManager != null && activeRFID != null) {
				dbSaved = true; 
				final String uidToSave = activeRFID;
				final int scoreToSave = this.Porcentaje + this.scoreBD;
				new Thread(() -> {
					try {
						dbManager.updateScoreAndSession(uidToSave, scoreToSave, "SpaceInvaders");
						System.out.println("Puntaje guardado en BD para UID: " + uidToSave + " -> " + scoreToSave);
					} catch (SQLException ex) {
						ex.printStackTrace();
					}
				}).start();
			}
			
			if(jugador.muere()) {
				g.setColor(Color.RED);
				g.drawString("LOL!  NOOB! ",450,300);
			}
			g.drawString("Game Over", 250, 300);
			g.drawString("Puntaje: " + Porcentaje + "", 350, 300);
			String registroPuntaje = nomJugador + " - " + Porcentaje;
			puntajes.add(registroPuntaje);
			// Se encarga de aser las comporativas de la informacion del txt
			//metodo de ordenamiento                                           
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

			// liberar activeRFID para la siguiente sesión
			activeRFID = null;
		}

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
	
// Apartado Jefe Final
	private void generarJefeFinal() {
		jefeFinal = new JefeFinal(400, 50);
		jefeFinalActivo = true;
		jefeFinal.setGraphics(getGraphics());// agregado
	}
	
	private void gestionarJefeFinal() {
		if (jefeFinalActivo) {
			jefeFinal.mover();

			if (jefeFinal.getVida() == 300 || jefeFinal.getVida() == 1000) {

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

	// aqui se va a modificar para los puntos para arduino 
	
	private List<String> cargarPuntajesDesdeArchivo() {
		List<String> puntajes = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(REGISTRO_PUNTAJES))) {
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

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(REGISTRO_PUNTAJES))) {
			for (String puntaje : puntajes) {
				writer.write(puntaje + "\n");
			}
		}
	}

	/// ------------------------------------------
	@Override
	public void keyPressed(KeyEvent e) {
		jugador.keyPressed(e);
		

		if (e.getKeyCode() == KeyEvent.VK_SPACE && disparoPendiente == false ) {
			Proyectil projectile = new Proyectil(jugador.getX() + jugador.getAnchura() / 2, jugador.getY(), true);
			jugadorProyectiles.add(projectile);
			 disparoPendiente = true;
		}
		
		 if (e.getKeyChar() == 'p' || e.getKeyChar() == 'P') {
             togglePausa();
         }
	}

	@Override
	public void keyReleased(KeyEvent e) {
		jugador.keyReleased(e);
		
		  if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			  disparoPendiente = false; 
			  }
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	public void setDañoJugador_Enemigos(int dañoJugador_Enemigos) {
		this.dañoJugador_Enemigos = dañoJugador_Enemigos;
	}
	
    private void togglePausa() {
	    pausa = !pausa;
	}


    // Manejo de RFID metodo que ejecuta cuando llega UID

    private void onRFIDDetected(String uid) {
    	
    	if (activeRFID != null && !GameOver) {
    		System.out.println("Partida en curso, Ignorando UID: " + uid);
    		return;
    	}
    	
    	activeRFID = uid;
    	
    	if (dbManager != null) {
    		new Thread(() -> {
    			try {
    				dbManager.ensureUserExists(uid,this.nomJugador);
    				DBManager.PlayerInfo pi = dbManager.loadPlayerInfo(uid);
    				if (pi != null) {
    					
    					this.nombreBD = pi.username;
    					this.scoreBD = pi.score; 
    				} else {

    				}
    				System.out.println("RFID cargado: " + uid + " -> usuario: " + nomJugador + " puntos: " + this.scoreBD);
 
    				SwingUtilities.invokeLater(() -> {
    					JOptionPane.showMessageDialog(null, "Bienvenido: " + nomJugador);
    				});
    			} catch (SQLException ex) {
    				ex.printStackTrace();
    			}
    		}).start();
    	}
    	
    	SwingUtilities.invokeLater(() -> {
    	    if (GameOver) {
 
    	        Window w = SwingUtilities.getWindowAncestor(this);
    	        if (w != null) w.dispose();
    	    }
    	});

    }

	
}
