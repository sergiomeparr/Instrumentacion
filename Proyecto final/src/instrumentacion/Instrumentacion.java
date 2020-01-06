
package instrumentacion;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.fazecast.jSerialComm.SerialPort;
import org.jfree.chart.plot.PlotOrientation;

public class Instrumentacion {

	static SerialPort puertoSeleccionado;
	static int x = 0;

	public static void main(String[] args) {
            // Creamos y configuramos la ventana
            JFrame ventana = new JFrame();
            ventana.setTitle("Grafica de Sensor");
            ventana.setSize(800, 600);
            ventana.setLayout(new BorderLayout());
            ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Creamos un select para elegir la conexion, lo ubicamos
            JComboBox<String> listaPuertos = new JComboBox<String>();
            JButton conexionBoton = new JButton("Conectar");
            JPanel topPanel = new JPanel();
            topPanel.add(listaPuertos);
            topPanel.add(conexionBoton);
            ventana.add(topPanel, BorderLayout.NORTH);

            // -------USO DE JSERIALCOMM
            // Poblamos la el select con los seriales disponibles
            SerialPort[] nombrePuertos = SerialPort.getCommPorts();
            for (int i = 0; i < nombrePuertos.length; i++) {
                    listaPuertos.addItem(nombrePuertos[i].getSystemPortName());
            }

            // -------USO DE JFREECHART
            // creamos la grafica
            XYSeries series = new XYSeries("Lectura del sensor");
            XYSeriesCollection conjuntoDatos = new XYSeriesCollection(series);
            JFreeChart grafico = ChartFactory.createXYLineChart("Lectura del Sensor de Luz", "Tiempo ( mSegundos)", "Lectura del ADC", conjuntoDatos, PlotOrientation.VERTICAL, true, true , false);
            ventana.add(new ChartPanel(grafico), BorderLayout.CENTER);

            // configuramos el boton de conectar y creamos hilo para recibir la
            // informacion
            conexionBoton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    if (conexionBoton.getText().equals("Conectar")) {
                        // intentamos conectar el serial
                        puertoSeleccionado = SerialPort.getCommPort(listaPuertos.getSelectedItem().toString());
                        puertoSeleccionado.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
                        if (puertoSeleccionado.openPort()) {
                            conexionBoton.setText("Desconectar");
                            listaPuertos.setEnabled(false);
                        }
                        // creamos el hilo donde se recibe la informacion para el
                        // grafico
                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                Scanner scanner = new Scanner(puertoSeleccionado.getInputStream());
                                while (scanner.hasNextLine()) {
                                    //try {
                                        String linea = scanner.nextLine();
                                        int numero = Integer.parseInt(linea);
                                        series.add(x++, numero);
                                        //series.add(x++, 1023 - numero);
                                        ventana.repaint();
                                    //} catch (Exception e) {}
                                }
                            scanner.close();
                            }
                        };
                        thread.start();
                    } else {
                        // desconectamos el serial
                        puertoSeleccionado.closePort();
                        listaPuertos.setEnabled(true);
                        conexionBoton.setText("Conectar");
                        series.clear();
                        x = 0;
                    }
                }
            });
		// Mostramos la ventana
		ventana.setVisible(true);
	}
}

