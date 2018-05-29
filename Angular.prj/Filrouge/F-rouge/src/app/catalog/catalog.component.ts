import { Component, OnInit } from '@angular/core';
import { Product, PRODUCT_MOCK } from '../bean/product';


@Component({
  selector: 'app-catalog',
  templateUrl: './catalog.component.html',
  styleUrls: ['./catalog.component.css']
})
export class CatalogComponent implements OnInit {

  public catalog: Array<Object>;
  public tartampion: string;

  constructor() {
    this.catalog = PRODUCT_MOCK;
    this.tartampion = "";
  }

  ngOnInit() {
  }

}
