package main.java.Shop;

/****************************************************************************/
/*																			*/
/*																			*/
/*																			*/
/***************************** Semble terminé *******************************/
/*																			*/
/*																			*/
/*																			*/
/*																			*/
/****************************************************************************/

import java.io.FileReader;
import java.io.File;
import java.io.IOException;

/* affichage JSON par readallbytes */
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;

import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Shop.model.bean.Product;
import Shop.model.CatalogModel;

@WebServlet( name="Catalog", urlPatterns = {"/catalog","/catalog/add","/catalog/rem"} )
public class Catalog extends HttpServlet {
	
//	private ArrayList<Product> catalog;

	public Catalog() {
		super();

	}
	
//	Product pyjama = new Product( "Pyjama", "..",10 );
	
	public void doGet(
	
			HttpServletRequest p_request,
			HttpServletResponse p_response

	) throws ServletException, IOException {
				
		p_response.setHeader("Access-Control-Allow-Origin", "*");
		p_response.setCharacterEncoding("utf-8");
		
		if (CatalogModel.getInstance().isConnected() == false) {
			this.getServletContext().getRequestDispatcher("/FailureSQL.jsp").forward(p_request, p_response);
		}
		else {
		
		p_request.setAttribute("catalog", CatalogModel.getInstance().getCatalog() );
		
		
		this.getServletContext().getRequestDispatcher("/Catalog.jsp").forward(p_request, p_response);
		}
	}
	
public void doPost(
			
			HttpServletRequest p_request,
			HttpServletResponse p_response
			
	) throws ServletException, IOException 
	{
		p_response.setHeader("Access-Control-Allow-Origin", "*");
		p_response.setCharacterEncoding("utf-8");
		
		int len = CatalogModel.getInstance().getCatalog().size();
		boolean success = false;
		
		if( 
				p_request.getParameter("price") != null &&
				p_request.getParameter("url") 	!= null &&
				p_request.getParameter("title") != null 
		) {
			
			float price 	= Float.parseFloat(p_request.getParameter("price"));
			String url 		= p_request.getParameter("url");
			String title 	= p_request.getParameter("title");
	
			CatalogModel.getInstance().add(title, url, price);
	
			success = ( CatalogModel.getInstance().getCatalog().size() > len);
		}		
		
		
		
		if( success == true ) {
			this.getServletContext().getRequestDispatcher("/Success.jsp")
			.forward(p_request, p_response);
		}else {
			this.getServletContext().getRequestDispatcher("/Failure.jsp")
			.forward(p_request, p_response);
		}
		
	}

	public void doOptions (
			HttpServletRequest p_request,
			HttpServletResponse p_response
	) throws ServletException, IOException  {
		
		p_response.setHeader("Access-Control-Allow-Origin", "*");
		p_response.setHeader("Access-Control-Allow-Headers", "Content-Type");
		p_response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
		p_response.setCharacterEncoding("utf-8");
		
		this.getServletContext().getRequestDispatcher("/Success.jsp")
		.forward(p_request, p_response);
		
	}

	public void doDelete(
			
			HttpServletRequest p_request,
			HttpServletResponse p_response
			
	) throws ServletException, IOException 
	{
		p_response.setHeader("Access-Control-Allow-Origin", "*");
		p_response.setCharacterEncoding("utf-8");
		
		boolean success = false;
		
		if( 
				p_request.getParameter("id") 	!= null &&
				p_request.getParameter("api") 	!= null 
		) {
			int id 			= Integer.parseInt( p_request.getParameter("id") );
			String apiKey 	= p_request.getParameter("api");
		
		
			if( apiKey.equals("azerty123")) {
			
//				success = CatalogModel.getInstance().removeProductById(id);
				
				for (int i = 0; i < CatalogModel.getInstance().getCatalog().size(); i++) {
					if (CatalogModel.getInstance().getCatalog().get(i).getId() == id) {
						CatalogModel.getInstance().removeProductById(id); 
						success = true;
						break;                     
						}                 
					}


			}
			
		}
		
		if( success == true ) {
			this.getServletContext().getRequestDispatcher("/Success.jsp")
			.forward(p_request, p_response);
		}else {
			this.getServletContext().getRequestDispatcher("/Failure.jsp")
			.forward(p_request, p_response);
		}
		
	}
/*	
	{
		p_response.setHeader("Access-Control-Allow-Origin", "*");
		p_response.setCharacterEncoding("UTF-8");
/*		
		String data = this.readFile(
			p_request.getServletContext().getRealPath("products.json")
			
		);
*/
//		PrintWriter out = p_response.getWriter();
		
//		out.println(data);
		
//	}

	
	/* affichage JSON par readallbytes */
//	protected String readFile(String path) throws IOException {
		
	/*
		File data = new File(path);
		FileReader reader = new FileReader(data);
	*/

	/* affichage JSON par readallbytes */
//		String output = "";
	
/*		
		int character = 0;
		do {
			character = reader.read();
			
			if( character == -1 )
				break;
			
			output += (char)character;
		}while( character != -1 );
		
		reader.close();
		 
		return output;

*/
	
	/* affichage JSON par readallbytes */
/*	
		Path newpath = Paths.get(this.getServletContext().getRealPath("products.json"));
		
	    try {
	    
	      byte[] contenufichier = Files.readAllBytes(newpath);

	      output = new String(contenufichier, "UTF-8");
	      return output;
	      
	    } catch (IOException e) {
	      System.out.println(e);
	    }
	    
      return output;
	
	}
*/	
}
