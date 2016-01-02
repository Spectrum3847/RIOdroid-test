package org.spectrum3847.RIOdroid;

import java.io.IOException;
import java.util.List;

import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

public class RIOadb {

	private static JadbConnection m_jadb = null;
    private static List<JadbDevice> m_devices = null;
    private static JadbDevice m_currentDevice = null;
    private static int m_nextLocalHostPort = 3800;
	
    private RIOadb() {
		//STATIC CLASS CAN'T BE CALLED
	}
	
	/**
	 * Attempt to make a connection to the ADB server running on the roboRIO
	 */
	public static void init(){
		System.out.println(RIOdroid.executeCommand("/etc/init.d/adb.sh start")); //Start the deamon
		//Might need to wait here for deamon to start
		try {
			m_jadb = new JadbConnection();
		} catch (IOException e) {
	        System.out.println("Failed at connection");
			e.printStackTrace();
		}
	}
	
	public static List<JadbDevice> getDevicesList(){
		if (m_jadb != null){
			try {
				m_devices = m_jadb.getDevices();
				return m_devices;
			} catch (IOException | JadbException e) {
		        System.out.println("Failed at device list");
				e.printStackTrace();
			}
		} else {
			System.out.println("Failed to get device list");
		}
		return null;
	}
	
	public static void setCurrentDevice(JadbDevice device){
		m_currentDevice = device;
	}
	
	public static void screencap(String filePath){
		if (m_currentDevice != null){
			try {
				m_currentDevice.executeShell("screencap", filePath);
			} catch (IOException | JadbException e) {
		        System.out.println("Failed to take screencap");
				e.printStackTrace();
			}
		} else {
			System.out.println("Current Device is null");
		}
	}
	
	/**
	 * Forward a localhost port from the roborio to the android device connected to the roborio
	 * @param roboRioPort
	 * @param devicePort
	 * @return
	 */
	public static String adbForward(int roboRioPort, int devicePort){
		if (m_jadb != null){
				return RIOdroid.executeCommand("adb forward tcp:" + roboRioPort + " tcp:" + devicePort);
		} else {
			System.out.println("Current adb connection is null");
		}
		return null;
	}
	
	/**
	 * Forward an external port of the roborio to roborio local port
	 * @param roboRioPort
	 * @param devicePort
	 * @return
	 */
	public static String forwardToLocal(int roboRioExternalPort, int localhostPort){
		String out = "socat TCP4-LISTEN:" + roboRioExternalPort + ",fork TCP4:127.0.0.1:" + localhostPort;
		RIOdroid.executeCommandThread(out);
		return out;
	}
	
	/**
	 * Combine the two other foward commands to allow you to get to a device port from the roborio external port
	 * This can be used to forward a port that is running a video stream, webserver, etc on the android device
	 * @param roboRioExternalPort
	 * @param devicePort
	 * @return
	 */
	public static String forward(int roboRioExternalPort, int devicePort){
		int localhostPort = m_nextLocalHostPort++;
		adbForward(localhostPort, devicePort);
		return forwardToLocal(roboRioExternalPort, localhostPort);
	}
	
	public static String clearNetworkPorts(){
		String out = RIOdroid.executeCommand("/etc/init.d/networking restart &") 
				+ "ADB CLEAR: " + RIOdroid.executeCommand("adb forward --remove-all");
		return out;
		
	}
}

