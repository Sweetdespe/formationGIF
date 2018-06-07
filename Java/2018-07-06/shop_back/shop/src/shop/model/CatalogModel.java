package shop.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import shop.model.bean.Product;

public class CatalogModel {
	
	private static CatalogModel instance = null;
	
	public static CatalogModel getInstance() {
		if( CatalogModel.instance == null ) {
			CatalogModel.instance = new CatalogModel();
		}
		
		return CatalogModel.instance;
	}

	
	private ArrayList<Product> catalog;
	/***********************************************/
	/***********************************************/
	/***********************************************/
	private boolean connected;
	private Connection bdd;
	
	private CatalogModel(){
		
		super();
		
		// on cr�e un tableau de produit
		this.catalog 	= new ArrayList<Product>();
		
		// on n'est pas connect� par d�faut
		this.connected 	= false;
		
		// on cr�e une variable contenant les infos de connexion
		String infos 	= "jdbc:mysql://127.0.0.1:3306/shop";
		String user 	= "root";
		String pwd 		= "root";
		
		this.bdd		= null;

		
		try {
			// on essaie de r�cup�rer la classe qui correspond
			// au Driver nous permettant de nous connecter � MySQL
			Class.forName("com.mysql.cj.jdbc.Driver");
			this.bdd = DriverManager.getConnection(infos, user, pwd);
			this.connected = true;
			
		} catch( ClassNotFoundException | SQLException error ) {
			
			System.out.println(error.getMessage());
			// si on n'arrive pas � r�cup�rer le driver alors on 
			// ne sera jamais connect�
			this.connected = false;
			
		}
	}
	
	public boolean isConnected() {
		return this.connected;
	}
	/***********************************************/
	/***********************************************/
	/**
	 * @throws SQLException *********************************************/
	
	public ArrayList<Product> getCatalog(){
		
		
		//on cr�e un tableau de produits vide
		ArrayList<Product> results = new ArrayList<Product>();
		
		try {
			
			// on cr�e une requ�te et on va chercher un jeu de r�sultats
			Statement query = this.bdd.createStatement();
			ResultSet data 	= query.executeQuery("SELECT * FROM products");
			
			// tant que l'on peut passer au r�sultat suivant ...
			while( data.next() ) {
				// on ajoute au tableau de produits ...
				results.add(
						// un nouveau produit cr�e � partir des donn�es issues 
						// du jeu de r�sultats
						new Product(
								data.getInt("id"),
								data.getString("title")	,
								data.getString("url")	,
								data.getFloat("price")	,
								data.getFloat("tva")
						)
				);
			}
			
			query.close();
		
		}
		catch( SQLException error ) {}
		
		return results;
	}
	
	public void add(String p_title, String p_url, float p_price, float p_tva) {
		try {
			PreparedStatement query = this.bdd.prepareStatement(
				"INSERT INTO products (title,url,price,tva) VALUES(?,?,?,?)"
			);
			
			query.setString(1, p_title);
			query.setString(2, p_url);
			query.setFloat(3, p_price);
			query.setFloat(4, p_tva);
			
			query.executeUpdate();
			query.close();
			
		} catch (SQLException e) {}
		
	}
	
	public boolean removeProductById( int p_id ) {
		
		try {
			PreparedStatement query = this.bdd.prepareStatement("DELETE FROM `products` WHERE id=?");
			query.setInt(1, p_id);
			query.executeUpdate();
			query.close();
		} 
		catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
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
	
	
	public void removeProduct( Product p_product ) {
		if( this.catalog.contains(p_product) ) {
			this.catalog.remove(p_product);
		}
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
