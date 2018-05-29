import { Component, OnInit } from '@angular/core';
import {Product} from '../bean/product';

@Component({
  selector: 'app-catalog',
  templateUrl: './catalog.component.html',
  styleUrls: ['./catalog.component.css']
})
export class CatalogComponent implements OnInit {

  public catalog: Array<Product>;
  constructor() {
    this.catalog = [
      { 
        title: "Chaussettes", 
        url: "https://goo.gl/VY2H3h", 
        price: 3
      },
      { 
        title: "Slip", 
        url: "https://goo.gl/Lz7H1Q", 
        price: 6
      },
      { 
        title: "Soutien Gorge", 
        url: "https://goo.gl/Ur5iSa", 
        price: 20
      },
      { 
        title: "Caleçon", 
        url: "https://goo.gl/DGkt6b", 
        price: 20
      }
    ]
  }

  ngOnInit() {
  }

}
