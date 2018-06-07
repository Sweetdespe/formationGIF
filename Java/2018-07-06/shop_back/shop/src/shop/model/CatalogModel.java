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
		
		// on crée un tableau de produit
		this.catalog 	= new ArrayList<Product>();
		
		// on n'est pas connecté par défaut
		this.connected 	= false;
		
		// on crée une variable contenant les infos de connexion
		String infos 	= "jdbc:mysql://127.0.0.1:3306/shop";
		String user 	= "root";
		String pwd 		= "root";
		
		this.bdd		= null;

		
		try {
			// on essaie de récupérer la classe qui correspond
			// au Driver nous permettant de nous connecter à MySQL
			Class.forName("com.mysql.cj.jdbc.Driver");
			this.bdd = DriverManager.getConnection(infos, user, pwd);
			this.connected = true;
			
		} catch( ClassNotFoundException | SQLException error ) {
			
			System.out.println(error.getMessage());
			// si on n'arrive pas à récupérer le driver alors on 
			// ne sera jamais connecté
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
		
		
		//on crée un tableau de produits vide
		ArrayList<Product> results = new ArrayList<Product>();
		
		try {
			
			// on crée une requête et on va chercher un jeu de résultats
			Statement query = this.bdd.createStatement();
			ResultSet data 	= query.executeQuery("SELECT * FROM products");
			
			// tant que l'on peut passer au résultat suivant ...
			while( data.next() ) {
				// on ajoute au tableau de produits ...
				results.add(
						// un nouveau produit crée à partir des données issues 
						// du jeu de résultats
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
