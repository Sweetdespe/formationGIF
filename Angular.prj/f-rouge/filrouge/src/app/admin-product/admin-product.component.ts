import { Component, OnInit } from '@angular/core';
import { Product } from '../bean/product';
import { ProductService } from '../service/product.service';

@Component({
  selector: 'app-admin-product',
  templateUrl: './admin-product.component.html',
  styleUrls: ['./admin-product.component.css']
})
export class AdminProductComponent implements OnInit {

  public product:Product;
  public catalog:Product[];
  private service:ProductService;

  constructor( p_service:ProductService ) { 
    this.product = new Product();
    this.service = p_service;
    this.catalog = new Array<Product>();
  }

  public delProductHandler(p_product:Product):void{
    this.service.removeProduct(p_product).then(
      () => {
        this.ngOnInit();
      }
    );
  }

  public postProductHandler():void{
    this.service.postProduct(this.product).then(
      () => {
        this.ngOnInit();
      }
    );
  }

  ngOnInit() {
    this.service.getProducts().then(
      ( tab:Product[]) => {
        this.catalog = tab;
      }
    )
  }

}
