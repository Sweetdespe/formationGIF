import { Component, OnInit, Input } from '@angular/core';
import { Product } from '../bean/product';

@Component({
  selector: 'app-product-thumb',
  templateUrl: './product-thumb.component.html',
  styleUrls: ['./product-thumb.component.css']
})
export class ProductThumbComponent implements OnInit {

  @Input()
  public data:Product;
  
  constructor() { }

  ngOnInit() {
  }

}
