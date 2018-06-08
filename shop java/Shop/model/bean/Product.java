package main.java.Shop.model.bean;

public class Product {
	
	private int id; 
	private float price;
    private String url;
    private String title;

	static public int COUNTER = 0;

	public Product( String p_title, String p_url, float p_price) {
		super();
		
		this.id 	= 0;
		this.url = p_url;
		this.title = p_title;
		this.price = p_price;
	}
	
    
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String toString() {		
		String data = "{";
		
		data += " \"id\": \""	+ this.id		+ "\",";	
		data += " \"title\": \""+this.title		+"\", ";
		data += " \"url\": \""	+this.url		+"\", ";
		data += " \"price\": \""+this.price		+"\"";
		
		data += "}";
		
		return data;
	}
}
