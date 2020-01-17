import {NgModule} from '@angular/core';
import {AppComponent} from './app.component';
import {AppRoutingModule} from "./app-routing.module";
import {WebPlayerModule} from "./web-player/web-player.module";
import {BrowserModule} from "@angular/platform-browser";
import {UrlSerializer} from "@angular/router";
import {CoreModule} from "vebto-client/core/core.module";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {CustomUrlSerializer} from "./custom-url-serializer";
import {Bootstrapper} from "vebto-client/core/bootstrapper.service";
import {BeMusicBootstrapper} from "./bootstrapper.service";
import {AuthModule} from "vebto-client/auth/auth.module";
import {WildcardRoutingModule} from "vebto-client/core/wildcard-routing.module";
import {AccountSettingsModule} from "vebto-client/account-settings/account-settings.module";
import {MatMenuModule, MatButtonModule, MatTableModule, MatPaginatorModule, MatSortModule } from '@angular/material';
import {APP_CONFIG} from 'vebto-client/core/config/vebto-config';
import {BEMUSIC_CONFIG} from './bemusic-config';

import { Ng2DeviceDetectorModule } from 'ng2-device-detector';
import {SubscriptionModule} from "vebto-client/subscription/subscription.module";
import {UserGenresSettingModule} from "vebto-client/user-genres/user-genres.module";

import { SliderModule } from 'angular-image-slider';
import { NgMultiSelectDropDownModule } from 'ng-multiselect-dropdown';

@NgModule({
    declarations: [
        AppComponent,
    ],
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        CoreModule.forRoot(),
        AppRoutingModule,
        AuthModule,
        WebPlayerModule,
        AccountSettingsModule,
        WildcardRoutingModule,
        MatMenuModule,
        MatButtonModule,
        MatTableModule,
        MatPaginatorModule,
        MatSortModule,
        Ng2DeviceDetectorModule.forRoot(),
        SubscriptionModule,
        UserGenresSettingModule,
        SliderModule,
        NgMultiSelectDropDownModule.forRoot()
    ],
    providers: [
        {provide: APP_CONFIG, useValue: BEMUSIC_CONFIG, multi: true},
        {provide: Bootstrapper, useClass: BeMusicBootstrapper},
        {provide: UrlSerializer, useClass: CustomUrlSerializer},
    ],
    bootstrap: [AppComponent]
})
export class AppModule {
}
