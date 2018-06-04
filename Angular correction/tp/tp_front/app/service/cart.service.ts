import { Injectable } from '@angular/core';
import { Product } from '../bean/product';

@Injectable({
  providedIn: 'root'
})
export class CartService {

  private cart:Array<Product>;

  constructor() { 
    this.cart = new Array<Product>();
  }

  public getFullCart():Array<Product>{
    return this.cart;
  }

  public addToCart(p_product:Product):void{
    this.cart.push(p_product);
  }

  public removeFromCart(p_product:Product):void{

    const index:number = this.cart.indexOf(p_product );
    if( index > -1 ){
      this.cart.splice(index, 1);
    }
  }

}
