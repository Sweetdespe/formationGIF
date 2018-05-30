import { Component, OnInit } from '@angular/core';
import {Product, PRODUCT_MOCK} from '../bean/product';
import { ProductService } from '../service/product.service';

@Component({
  selector: 'app-catalog',
  templateUrl: './catalog.component.html',
  styleUrls: ['./catalog.component.css']
})
export class CatalogComponent implements OnInit {

  public catalog: Array<Product>;
  public nom:string;
  public prix:number;
  private service:ProductService;

  constructor( p_service:ProductService ) {
    
    this.nom      = "";
    this.prix     = 1000;
    this.service  = p_service;
    this.catalog  = p_service.getProducts();
  }

  public hideProduct( p_product:Product ):void{

    const tmp:Array<Product> = new Array<Product>();
    let i:number = 0;
    let max:number = this.catalog.length;

    for( i = 0; i < max; i++ )
    {
      if( this.catalog[i] == p_product )
        continue;
      
      tmp.push(this.catalog[i]);
    }
    
    this.catalog = tmp;
  }

  public addToCart(p_product:Product):void{
    alert(p_product.title+" a été ajouté au panier !");
  }

  ngOnInit() {
  }

}
