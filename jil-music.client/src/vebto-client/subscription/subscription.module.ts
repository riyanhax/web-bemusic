import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {SubscriptionResolve} from "./subscription-resolve.service";
import {UiModule} from "../core/ui/ui.module";
import {SubscriptionRoutingModule} from "./subscription-routing.module";
import {SubscriptionComponent} from "./subscription.component";

import {VoucherModalComponent} from "./voucher-modal/voucher-modal.component";

import { NgxPhoneMaskModule } from 'ngx-phone-mask';

@NgModule({
    imports:      [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        UiModule,
        SubscriptionRoutingModule,
        NgxPhoneMaskModule,
    ],
    declarations: [
        SubscriptionComponent,    
        VoucherModalComponent,    
    ],
    entryComponents: [        
        VoucherModalComponent,
    ],
    exports:      [
        SubscriptionRoutingModule,
        NgxPhoneMaskModule,
    ],
    providers:    [
        SubscriptionResolve
    ]
})
export class SubscriptionModule { }