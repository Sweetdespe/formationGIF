package main.java.Shop.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import main.java.Shop.model.bean.Product;

public class CatalogModel {

	private static CatalogModel instance = null;
		
	public static CatalogModel getInstance() {
		if( CatalogModel.instance == null ) {
			CatalogModel.instance = new CatalogModel();
		}
			
		return CatalogModel.instance;
	}
		
	private ArrayList<Product> catalog;
	
	// on n'est pas connecté par défaut
	private boolean connected = false;
	private Connection bdd;
	
	public CatalogModel() {
		super();

		// on crée un tableau de produit
		this.catalog = new ArrayList<Product>();
		
						//jdbc:mysql://nomhote:port/nombdd				
		String infos 	= "jdbc:mysql://127.0.0.1:3306/shop";
		String user 	= "root";
		String pwd 		= "root";
		
		this.bdd = null;
				
		try {
			
			Class.forName( "com.mysql.cj.jdbc.Driver" );
			this.bdd = DriverManager.getConnection(infos, user, pwd);
			this.connected = true;
			
		} catch ( ClassNotFoundException | SQLException error) {
			
				this.connected = false;
						
		}
		
	}
	
	public boolean isConnected() {
		return this.connected;
	}
	
	
	public ArrayList<Product> getCatalog(){
		
		ArrayList<Product> results = new ArrayList<Product>();
		
		try {
		
			Statement query = this.bdd.createStatement();
			ResultSet data = query.executeQuery("SELECT * FROM products");
			while ( data.next() ) {
				results.add(
						new Product(
								data.getString("title"),
								data.getString("url"),
								data.getFloat("price")
								)
						);
				
			}
			query.close();
		}
		catch (SQLException error) {}
			
		return results;
	}
	
	public void add(String p_title, String p_url, float p_price) {
		try {
			PreparedStatement query = this.bdd.prepareStatement(
				"INSERT INTO products (title,url,price,tva) VALUES(?,?,?)"
			);
			
			query.setString(1, p_title);
			query.setString(2, p_url);
			query.setFloat(3, p_price);
			
			query.executeUpdate();
			query.close();
			
		} catch (SQLException error) {}
	}
	
	public boolean removeProductById( int p_id ) {
		try {
			PreparedStatement query = this.bdd.prepareStatement("DELETE FROM `products` WHERE id=?");
			query.setInt(1, p_id);
			System.out.println(query.toString());
			query.execute();
			query.close();
		} 
		catch (SQLException e) {
			
			
		}
				
		return true;
	}
	
	public boolean removeProductByTitle( String p_title ) {
		
		try {
			PreparedStatement query = this.bdd.prepareStatement("DELETE FROM `products` WHERE title=?");
			query.setString(1, p_title);
			query.executeUpdate();
			query.close();
		} 
		catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}
			
		return true;
	}
	
	
	public Product getProductById(int p_id) {
		
		int i = this.catalog.size();
		while( --i > -1 ) {
			if( this.catalog.get(i).getId() == p_id )
				return this.catalog.get(i);
		}
		
		return null;
	}

}
