import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-catalog',
  templateUrl: './catalog.component.html',
  styleUrls: ['./catalog.component.css']
})
export class CatalogComponent implements OnInit {

  public catalog: Array<Object>;
  constructor() {
    this.catalog = [
      { 
        titre: "Chaussettes", 
        url: "https://goo.gl/VY2H3h", 
        price: 3
      },
      { 
        titre: "Slip", 
        url: "https://goo.gl/Lz7H1Q", 
        price: 6
      },
      { 
        titre: "Soutien Gorge", 
        url: "https://goo.gl/Ur5iSa", 
        price: 20
      },
      { 
        titre: "Caleçon", 
        url: "https://goo.gl/DGkt6b", 
        price: 20
      }
    ]
  }

  ngOnInit() {
  }

}
