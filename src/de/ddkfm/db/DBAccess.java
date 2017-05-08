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
			checkTableStructure();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void checkTableStructure() {
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String query =
				"CREATE TABLE IF NOT EXISTS users (" +
				"uid int AUTO_INCREMENT," +
				"username varchar(50)," +
				"password varchar(128)," +
				"PRIMARY KEY(uid)" +
				");";
		boolean success = executeQuery(query);
		query =
				"CREATE TABLE IF NOT EXISTS data(" +
				"dataId int AUTO_INCREMENT," +
				"name varchar(50)," +
				"data BLOB," +
				"comment varchar(100)," +
				"uid int," +
				"PRIMARY KEY(dataId)" +
				");";
		success = executeQuery(query);
	}
	private List<Map<String, String>> getQuery(String query) {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		try {
			ResultSet rs = statement.executeQuery(query);
			while(rs.next()){
				Map<String, String> map = new HashMap<String, String>();
				result.add(map);
            }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	private boolean executeQuery(String query) {
		try {
			boolean success = statement.execute(query);
			return success;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
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
