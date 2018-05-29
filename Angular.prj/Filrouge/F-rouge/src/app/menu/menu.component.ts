import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.css']
})
export class MenuComponent implements OnInit {

  public menu: Array<Object>;

  constructor() { 
    this.menu = [
      {
        Menu: "Google",
        url: "http://www.google.fr",
      },
      {
        Menu: "Yahoo",
        url: "http://www.yahoo.com",
      },
      {
        Menu: "webmail",
        url: "http://zimbra.free.fr",
      },
      {
        Menu: "Tinyurl",
        url: "https://tinyurl.com",
      },
      {
        Menu: "netflix",
        url: "https://www.netflix.com/",
      }
    ]

  }

  ngOnInit() {
  }

}
