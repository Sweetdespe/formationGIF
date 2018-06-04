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

  public getProductByTitle(p_title: String): Promise<Product> {

    return this.getProducts().then(
      (products: Product[]): Product => {
        const max: number = products.length;
        let i: number = 0;

        for (i = 0; i < max; i++) {
          if (products[i].title == p_title)
            return products[i];
        }

        return null;
      }
    ).catch(
      (error) => {
        console.log(error);
        return null;
      }
    );
  }

  public getProducts(): Promise<Array<Product>> {


    const promise: Promise<Array<Product>> = this.service.get(
      "http://localhost:8080/shop/"
    ).toPromise()
      .then(
        (rep: Response): Array<Product> => {
          return rep.json() as Array<Product>;
        }
      ).catch(
        (error: any): Promise<any> => {
          return Promise.reject(error);
        }
      );

    return promise;

  }

}
