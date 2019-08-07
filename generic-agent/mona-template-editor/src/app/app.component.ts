import { Component, OnInit } from '@angular/core';

import { faSitemap, faCog } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  public faSitemap = faSitemap;
  public faCog = faCog;
}
