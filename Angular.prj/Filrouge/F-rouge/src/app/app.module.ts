import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { HeaderComponent } from './header/header.component';
import { MenuComponent } from './menu/menu.component';
import { CatalogComponent } from './catalog/catalog.component';
import { FooterComponent } from './footer/footer.component';
import { ProductThumbComponent } from './product-thumb/product-thumb.component';
import { FormsModule } from '@angular/forms';
import { AlphaPipe } from './pipe/alpha.pipe'

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    MenuComponent,
    CatalogComponent,
    FooterComponent,
    ProductThumbComponent,
    AlphaPipe,
  ],
  imports: [
    BrowserModule,
    FormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
