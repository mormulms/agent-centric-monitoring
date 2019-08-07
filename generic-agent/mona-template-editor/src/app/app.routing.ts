import { ModuleWithProviders } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const APP_ROUTES: Routes = [
  { path: 'template', loadChildren: './template/template.module#TemplateModule'},
  { path: 'settings', loadChildren: './settings/settings.module#SettingsModule'},
  { path: '**', redirectTo: '/template', pathMatch: 'full'}
];

export const routing: ModuleWithProviders = RouterModule.forRoot(APP_ROUTES, { useHash: true});
