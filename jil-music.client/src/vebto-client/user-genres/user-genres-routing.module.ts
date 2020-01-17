import {RouterModule, Routes} from '@angular/router';
import {NgModule} from "@angular/core";
import {AuthGuard} from "../guards/auth-guard.service";
import {UserGenresSettingComponent} from "./user-genres.component";
import {UserGenresSettingResolve} from "./user-genres-resolve.service";

const routes: Routes = [
    // {
    //     path: 'account',
    //     pathMatch: 'prefix',
    //     redirectTo: 'account/settings'
    // },
    {
        path: 'account/my-genres-setting',
        component: UserGenresSettingComponent,
        resolve: {resolves: UserGenresSettingResolve},
        canActivate: [AuthGuard],
        data: {name: 'my-genres-setting'},
    },
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class UserGenresSettingRoutingModule {}