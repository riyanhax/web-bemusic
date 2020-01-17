import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {UiModule} from "../core/ui/ui.module";
import {UserGenresSettingResolve} from "./user-genres-resolve.service";
import {UserGenresSettingRoutingModule} from "./user-genres-routing.module";
import {UserGenresSettingComponent} from "./user-genres.component";


import {MatButtonModule, MatCheckboxModule, MatSlideToggleModule, MatStepperModule, MatFormFieldModule, MatInputModule, MatProgressBarModule} from '@angular/material';

@NgModule({
    imports:      [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        UiModule,
        UserGenresSettingRoutingModule,
        MatButtonModule,
        MatCheckboxModule,
        MatSlideToggleModule,
        MatStepperModule,
        MatFormFieldModule,
        MatInputModule,
        MatProgressBarModule        
    ],
    declarations: [
        UserGenresSettingComponent      
    ],
    entryComponents: [        
    ],
    exports:      [
        UserGenresSettingRoutingModule,
        MatButtonModule,
        MatCheckboxModule,
        MatSlideToggleModule,
        MatStepperModule,
        MatFormFieldModule,
        MatInputModule,
        MatProgressBarModule
    ],
    providers:    [
        UserGenresSettingResolve
    ]
})
export class UserGenresSettingModule { }