package shop;


import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import shop.model.bean.Product;

public class Catalog extends HttpServlet {

	private ArrayList<Product> catalog;
	
	public Catalog() {
		super();
		
		this.catalog = new ArrayList<Product>();
		this.catalog.add( new Product("pyjama", "..", (float)10.5, (float)0.2) );
		this.catalog.add( new Product("pyjama", "..", (float)10.5, (float)0.2) );
		this.catalog.add( new Product("pyjama", "..", (float)10.5, (float)0.2) );
		// TODO Auto-generated constructor stub
	}
	
	public void doGet(
			
			HttpServletRequest p_request,
			HttpServletResponse p_response
			
	) throws ServletException, IOException 
	{
		
		p_response.setHeader("Access-Control-Allow-Origin", "*");
		p_response.setCharacterEncoding("utf-8");
		
		
		p_request.setAttribute("catalog", this.catalog );
		
		
		this.getServletContext().getRequestDispatcher("/Catalog.jsp")
								.forward(p_request, p_response);
	}

public void doPost(
			
			HttpServletRequest p_request,
			HttpServletResponse p_response
			
	) throws ServletException, IOException 
	{
		p_response.setHeader("Access-Control-Allow-Origin", "*");
		p_response.setCharacterEncoding("utf-8");
		
		int len = this.catalog.size();
		
		
		float price 	= Float.parseFloat( p_request.getParameter("price") );
		float tva 		= Float.parseFloat( p_request.getParameter("tva") );
		String url 		= p_request.getParameter("url");
		String title 	= p_request.getParameter("title");
		
		this.catalog.add(new Product(title, url, price, tva));
		
		boolean success = ( this.catalog.size() > len );
		
		
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
		int id 			= Integer.parseInt( p_request.getParameter("id") );
		String apiKey 	= p_request.getParameter("api");
		
		
		if( apiKey.equals("azerty123")) {
			
			for( int i = 0; i < this.catalog.size(); i++ ) {
				if( this.catalog.get(i).getId() == id ) {
					this.catalog.remove(i);
					success = true;
					break;
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

}
