package de.ddkfm.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class DBAccess {
	private Connection connection = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	public DBAccess(){
		String host = "localhost";
		String port = "3306";
		String dbName = "micro2";
		String user = "asdasd";
		String password = "root";
		if(System.getProperty("micro2.config.dir") != null){
			Properties dbPropertes = new Properties();
			String dbProp = System.getProperty("micro2.config.dir");
			File dbFile = new File(dbProp + File.separator + "db.properties");
			if(dbFile.exists()){
				try {
					dbPropertes.load(new FileInputStream(dbFile));
					host = dbPropertes.getProperty("micro2web.db.host");
					port = dbPropertes.getProperty("micro2web.db.port");
					dbName = dbPropertes.getProperty("micro2web.db.dbName");
					user = dbPropertes.getProperty("micro2web.db.user");
					password = dbPropertes.getProperty("micro2web.db.password");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					
				}
			}
		}
		this.connect(host,port,dbName,user,password);
	}
	public DBAccess(String host, String port, String dbName, String username, String password) {
		this.connect(host,port,dbName,username,password);
	}
	public void connect(String host, String port, String dbName, String user, String password) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + dbName,user,password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean checkUser(String username, String password){
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			ResultSet rs = statement.executeQuery("Select username, password from users where username like '" + username + "'");
			while(rs.next()){
				String dbUser = rs.getString("username");
				String dbPass = rs.getString("password");
				if(dbPass.equals(password))
					return true;
			}
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	public List<Map<String, String>> getAllData(){
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			ResultSet rs = statement.executeQuery("Select username, dataId, data, name, comment from data left join users using(uid)");
			while(rs.next()){
				Map<String, String> map = new HashMap<String, String>();
				map.put("username", rs.getString("username"));
				map.put("id", rs.getString("dataId"));
				map.put("name", rs.getString("name"));
				list.add(map);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	public InputStream getData(int id){
		InputStream result = null;
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			ResultSet rs = statement.executeQuery("Select username, dataId, data, name, comment from data left join users using(uid) where dataId = " + id);
			while(rs.next()){
				Blob blob = rs.getBlob("data");
				result = blob.getBinaryStream();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	public static String toSHA1(byte[] convertme) {
	    MessageDigest md = null;
	    try {
	        md = MessageDigest.getInstance("SHA-256");
	    }
	    catch(NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } 
	    return new String(md.digest(convertme));
	}
}
