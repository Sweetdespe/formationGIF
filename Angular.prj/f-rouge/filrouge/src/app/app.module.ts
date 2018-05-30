import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { HeaderComponent } from './header/header.component';
import { MenuComponent } from './menu/menu.component';
import { CatalogComponent } from './catalog/catalog.component';
import { FooterComponent } from './footer/footer.component';
import { ProductThumbComponent } from './product-thumb/product-thumb.component';
import {FormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import { AlphaPipe } from './pipe/alpha.pipe';
import { PricePipe } from './pipe/price.pipe';
import { HomeComponent } from './home/home.component';
import { ProductService } from './service/product.service';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    MenuComponent,
    CatalogComponent,
    FooterComponent,
    ProductThumbComponent,
    AlphaPipe,
    PricePipe,
    HomeComponent
  ],
  imports: [
    BrowserModule, 
    FormsModule, 
    RouterModule.forRoot(
      [
        {
          path: "home", 
          component: HomeComponent, 
          pathMatch: 'full'
        }, 
        {
          path: "catalog", 
          component: CatalogComponent, 
          pathMatch: 'full'
        }
      ], 
      {
        useHash: false
      }
    )
  ],
  providers: [ProductService],
  bootstrap: [
    AppComponent
  ]
})
export class AppModule { }
