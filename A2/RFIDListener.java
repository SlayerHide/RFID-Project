package A2;

import com.fazecast.jSerialComm.SerialPort;
import java.io.InputStream;
import java.util.function.Consumer;

public class RFIDListener implements Runnable {
    private SerialPort comPort;
    private final Consumer<String> onUID;
    private volatile boolean running = true;

    public RFIDListener(String portName, Consumer<String> onUID) {
        this.onUID = onUID;
        // Usar el portName que pasas al constructor
        comPort = SerialPort.getCommPort("COM4");
        comPort.setBaudRate(9600);
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
    }

    public void stop() {
        running = false;
        if (comPort != null && comPort.isOpen()) comPort.closePort();
    }

    @Override
    public void run() {
        if (!comPort.openPort()) {
            System.err.println("No se pudo abrir puerto: " + comPort.getSystemPortName());
            return;
        }

        InputStream in = comPort.getInputStream();
        byte[] buffer = new byte[1024];

        try {
            while (running) {
                while (in.available() > 0) {
                    int len = in.read(buffer);
                    if (len > 0) {
                        // Lo que llega desde Arduino
                        String s = new String(buffer, 0, len).trim();

                        // Nos quedamos solo con caracteres hexadecimales (por si vienen textos tipo "UID ...")
                        String uidRaw = s.replaceAll("[^0-9A-Fa-f]", "");

                        if (!uidRaw.isEmpty()) {
                            try {
                                // Convertir el UID completo a un número (como en el unsigned long de Arduino)
                                long uidValue = Long.parseLong(uidRaw, 16);

                                // Formatear a 8 dígitos HEX, ceros a la izquierda, MAYÚSCULAS
                                String uid8 = String.format("%08X", uidValue & 0xFFFFFFFFL);

                                // Devolver al callback el mismo formato que imprime tu Arduino
                                onUID.accept(uid8);

                            } catch (NumberFormatException e) {
                                System.err.println("UID inválido recibido: " + uidRaw);
                            }
                        }
                    }
                }
                Thread.sleep(50);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { in.close(); } catch (Exception ex) {}
            comPort.closePort();
        }
    }
}
