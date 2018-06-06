package shop.model;

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
	
	private CatalogModel() {
		super();
		this.catalog = new ArrayList<Product>();
		this.catalog.add(new Product("pyjama", "..", (float) 10.5, (float) 0.2));
		this.catalog.add(new Product("pyjama", "..", (float) 10.5, (float) 0.2));
		this.catalog.add(new Product("pyjama", "..", (float) 10.5, (float) 0.2));
	}
	
	public ArrayList<Product> getCatalog(){
		return this.catalog;
	}
	
	public void add(String p_title, String p_url, float p_price, float p_tva) {
		this.catalog.add( new Product(p_title, p_url, p_price, p_tva) );
	}
	
	public boolean removeProductById( int p_id ) {
		Product current = this.getProductById(p_id);
		
		if( current == null )
			return false;
		
		this.catalog.remove(current);
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
