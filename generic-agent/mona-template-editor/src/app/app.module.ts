import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

import { CoreModule } from '@mona/core/core.module';
import { routing } from '@mona/app/app.routing';
import { AppComponent } from '@mona/app/app.component';

@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    FontAwesomeModule,
    CoreModule,
    routing,
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
