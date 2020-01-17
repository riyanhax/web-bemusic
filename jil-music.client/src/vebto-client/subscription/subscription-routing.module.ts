import {RouterModule, Routes} from '@angular/router';
import {NgModule} from "@angular/core";
import {AuthGuard} from "../guards/auth-guard.service";
import {SubscriptionComponent} from "./subscription.component";
import {SubscriptionResolve} from "./subscription-resolve.service";

const routes: Routes = [
    // {
    //     path: 'account',
    //     pathMatch: 'prefix',
    //     redirectTo: 'account/settings'
    // },
    {
        path: 'account/subscription',
        component: SubscriptionComponent,
        resolve: {resolves: SubscriptionResolve},
        canActivate: [AuthGuard],
        data: {name: 'subscription'},
    },
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class SubscriptionRoutingModule {}