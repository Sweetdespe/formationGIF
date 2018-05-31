import { Injectable } from '@angular/core';
import { Product, PRODUCT_MOCK } from '../bean/product';
import { Http, Response } from '@angular/http';
import 'rxjs/add/operator/toPromise';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private service: Http;

  constructor(p_service: Http) {
    this.service = p_service;
  }

  public getProducts(): Promise<Array<Product>> {


    const promise: Promise<Array<Product>> = this.service.get(
      "assets/products.json"
    ).toPromise()
     .then(
          (rep: Response): Array<Product> => {
            return rep.json() as Array<Product>;
          }
      );

      return promise;

  }

}
