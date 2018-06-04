import { Component, OnInit } from '@angular/core';
import { CartService } from '../service/cart.service';
import { Product } from '../bean/product';

@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css']
})
export class CartComponent implements OnInit {

  private service:CartService;
  public cart:Array<Product>;

  constructor( p_service:CartService ) { 
    this.service = p_service;
    this.cart = p_service.getFullCart();
  }

  public remove( p_product:Product ):void{
    this.service.removeFromCart(p_product);
    this.cart = this.service.getFullCart();
  }

  ngOnInit() {
  }

}
