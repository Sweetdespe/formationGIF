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
    let a:number = 0;
    let b:number = 0;

    for( i = 0; i < max; i++ ){
      a = parseFloat(value[i].price.toString());
      b = parseFloat(p_price.toString());

      if( a <= b ){
        results.push(value[i]);
      }
    }

    return results;
  }

}
