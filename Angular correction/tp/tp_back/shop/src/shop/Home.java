package shop;

import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Home extends HttpServlet {

	public Home() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public void doGet(
			
			HttpServletRequest p_request,
			HttpServletResponse p_response
			
	) throws ServletException, IOException 
	{
		p_response.setContentType("application/json");
		p_response.setHeader("Access-Control-Allow-Origin", "*");
		p_response.setCharacterEncoding("utf-8");
		
		
		String data = this.readFile(
			p_request.getServletContext().getRealPath("products.json")
		);
		
		PrintWriter out = p_response.getWriter();
		
		out.println(data);
		
	}
	
	protected String readFile(String path) throws IOException {
		
		File data = new File(path);
		FileReader reader = new FileReader(data);
		String output = "";
		
		int character = 0;
		do {
			character = reader.read();
			
			if( character == -1 )
				break;
			
			output += (char)character;
		}while( character != -1 );
		
		reader.close();
		
		return output;
	}

}
