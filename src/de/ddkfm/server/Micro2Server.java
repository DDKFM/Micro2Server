package de.ddkfm.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

import de.ddkfm.db.DBAccess;

public class Micro2Server {
	/**
	 * Setting for the HTTP-Server 
	 */
	private static int httpPort = 80;
	private static int httpsPort = 443;
	private static String contentPath = "micro2";
	private static boolean withHttps = false;
	
	/*
	 * Settings for the Backend(DB or File-based)
	 * 
	 */
	public static final int SAVEFORMAT_FILEBASED = 0;
	public static final int SAVEFORMAT_MYSQL = 1;
	
	private static int saveFormat = SAVEFORMAT_FILEBASED;
	private static String rootPath = "";
	private static String dbHostname= "";
	private static int dbPort = 3306;
	private static String dbName = "";
	private static String dbUser = "";
	private static String dbPassword = "";
	
	private static HttpServer httpServer;
	private static HttpsServer httpsServer;
	public static void main(String[] args) {
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(new File("micro2Server.properties")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Micro2Server.startServer(prop);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void startServer(Properties properties) throws IOException {
		if(properties.containsKey("micro2.server.withHttps")) {
			withHttps = Boolean.parseBoolean(properties.getProperty("micro2.server.withHttps"));
		}
		if(properties.containsKey("micro2.server.port.http")) {
			httpPort = Integer.parseInt(properties.getProperty("micro2.server.port.http"));
		}
		if(properties.containsKey("micro2.server.port.https")) {
			httpsPort = Integer.parseInt(properties.getProperty("micro2.server.port.https"));
		}
		if(properties.containsKey("micro2.server.context.path")) {
			contentPath = properties.getProperty("micro2.server.context.path");
		}
		if(properties.containsKey("micro2.server.data.saveFormat")) {
			String saveFormat = properties.getProperty("micro2.server.data.saveFormat");
			switch(saveFormat.trim().toLowerCase()) {
			case "mysql":
				Micro2Server.saveFormat = Micro2Server.SAVEFORMAT_MYSQL;
				break;
			case "file":
				Micro2Server.saveFormat = Micro2Server.SAVEFORMAT_FILEBASED;
				break;
			}
		}
		if(properties.containsKey("micro2.server.file.rootPath")) {
			rootPath = properties.getProperty("micro2.server.file.rootPath");
		}
		if(properties.containsKey("micro2.server.database.hostname")) {
			dbHostname = properties.getProperty("micro2.server.database.hostname");
		}if(properties.containsKey("micro2.server.database.port")) {
			dbPort = Integer.parseInt(properties.getProperty("micro2.server.database.port"));
		}if(properties.containsKey("micro2.server.database.name")) {
			dbName = properties.getProperty("micro2.server.database.name");
		}if(properties.containsKey("micro2.server.database.username")) {
			dbUser = properties.getProperty("micro2.server.database.username");
		}if(properties.containsKey("micro2.server.database.password")) {
			dbPassword = properties.getProperty("micro2.server.database.password");
		}
		Micro2Server.startServer();
	}
	public static void startServer() throws IOException {
		httpServer = HttpServer.create(new InetSocketAddress(httpPort), 0);
		httpsServer = null;
		if(withHttps) { 
			httpsServer = HttpsServer.create(new InetSocketAddress(httpsPort), 0);
		}
		
		DBAccess db = new DBAccess(dbHostname, Integer.toString(dbPort), dbName, dbUser, dbPassword);
		
		httpServer.createContext("/" + contentPath, new Micro2ServerHandler(db));
		if(httpsServer != null)
			httpsServer.createContext("/" + contentPath, new Micro2ServerHandler(db));
		
		httpServer.start();
		System.out.println("Http-Server auf Port " + httpPort + " gestartet");
		if(httpsServer != null) {
			httpsServer.start();
			System.out.println("Https-Server auf Port " + httpsPort + " gestartet");
		}
	}
	public static void stopServer() {
		if(httpServer != null) {
			httpServer.stop(1);
			System.out.println("HTTP-Server gestoppt");
		}
		if(httpsServer != null) {
			httpsServer.stop(1);
			System.out.println("HTTPS-Server gestoppt");
		}
	}
	

}
