package de.ddkfm.server;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import de.ddkfm.db.DBAccess;

public class Micro2ServerHandler implements HttpHandler {
	private DBAccess db;
	public Micro2ServerHandler(DBAccess db) {
		this.db = db;
	}
	private Map<String, String> queryToMap(String query){
	    Map<String, String> result = new HashMap<String, String>();
	    if(query == null || query.equals(""))
	    	return result;
	    for (String param : query.split("&")) {
	        String pair[] = param.split("=");
	        if (pair.length>1) {
	            result.put(pair[0], pair[1]);
	        }else{
	            result.put(pair[0], "");
	        }
	    }
	    return result;
	}
	
	@Override
	public void handle(HttpExchange httpObject) throws IOException {
		httpObject.getResponseHeaders().add("Content-type", "application/xml; charset=\"UTF-8\"");
		String query = httpObject.getRequestURI().getQuery();
		Map<String, String> request = queryToMap(httpObject.getRequestURI().getQuery());
		String id = request.get("id") != null ? request.get("id") : "all";
		if(id.equals("all")){
			List<Map<String, String>> result = db.getAllData();
			Element response = new Element("Response");
			Document doc = new Document(response);
			for(Map<String, String> map : result){
				Element data = new Element("data");
				data.setAttribute("username", map.get("username"));
				data.setAttribute("dataID", map.get("id"));
				data.setAttribute("name", map.get("name"));
				response.addContent(data);
			}
			String outString = new XMLOutputter().outputString(doc);
			httpObject.sendResponseHeaders(200, outString.getBytes().length);
			this.writeToResponseBody(httpObject, outString);
			
		}else{
			int dataID = -1;
			try {
				dataID = Integer.parseInt(id);
			} catch (NumberFormatException e) {}
			if(dataID == -1){
				httpObject.sendResponseHeaders(404, 0);
			}else{
				InputStream data = db.getData(dataID);
				if(data != null) {
					StringWriter writer = new StringWriter();
					IOUtils.copy(data, writer, Charset.forName("UTF-8"));
					String outString = writer.toString();
					System.out.println(outString);
					httpObject.sendResponseHeaders(200, outString.getBytes("UTF-8").length);
					this.writeToResponseBody(httpObject, outString);
				} else
					httpObject.sendResponseHeaders(404, 0);
			}
		}
	}
	private void writeToResponseBody(HttpExchange httpExchange, String data) {
		int BUFFER_SIZE = 8192;
		try (BufferedOutputStream out = new BufferedOutputStream(httpExchange.getResponseBody())) {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(data.getBytes("UTF-8"))) {
                byte [] buffer = new byte [BUFFER_SIZE];
                int count ;
                while ((count = bis.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
            } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}


}
