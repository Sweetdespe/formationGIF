import { Injectable } from '@angular/core';
import { Product, PRODUCT_MOCK } from '../bean/product';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  constructor() { }

  public getProducts():Array<Product>{
    return PRODUCT_MOCK;
  }

}
