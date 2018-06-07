package shop.model.bean;


public class Product {

	private int id; 
	private String url;
	private String title;
	private float price;
	private float tva;
	
	static public int COUNTER = 0;
	
	public Product( 
			int p_id,
			String p_title, 
			String p_url, 
			float p_price, 
			float p_tva
	) {
		super();
		
		
		this.id 	= p_id;
		this.tva 	= p_tva;
		this.url 	= p_url;
		this.title 	= p_title;
		this.price 	= p_price;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public float getTva() {
		return tva;
	}

	public void setTva(float tva) {
		this.tva = tva;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public String toString() {		
		String data = "{";
	
		data += " \"id\": \""	+ this.id		+ "\",";
		data += " \"title\": \""+ this.title	+ "\",";
		data += " \"url\": \""	+ this.url		+ "\",";
		data += " \"tva\": \""	+ this.tva		+ "\",";
		data += " \"price\": \""+ this.price	+ "\"";
		
		data += "}";
		
		return data;
	}
	
	
	
	
	

}
