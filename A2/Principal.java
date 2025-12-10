package A2;

import javax.swing.JFrame;

public class Principal {
	
	/*"Made in Mexico"*/
	
	
	//Don't cry by guns n' roses
	public static void main(String...XxSlayerHidexX) {
		//System.out.println(ClassLoader.getSystemResource("com/fazecast/jSerialComm/SerialPort.class"));

		JFrame frame = new JFrame("Space Invaders");
		SpaceInvadersGame game = new SpaceInvadersGame();
		frame.add(game);
		frame.pack(); 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
	}
}
