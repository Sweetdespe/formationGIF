import { Pipe, PipeTransform } from '@angular/core';
import { Product } from '../bean/product';

@Pipe({
  name: 'price'
})
export class PricePipe implements PipeTransform {

  transform(value: Array<Product>, p_price:number): Array<Product> {

    let i:number = 0;
    let max:number = value.length;
    let results:Array<Product> = new Array<Product>();

    for( i = 0; i < max; i++ ){
      if( value[i].price <= p_price ){
        results.push(value[i]);
      }
    }

    return results;
  }

}
