import {Injectable} from '@angular/core';
import {Router, Resolve, ActivatedRouteSnapshot} from '@angular/router';
import {Users} from "../auth/users.service";
import {CurrentUser} from "vebto-client/auth/current-user";
import {ValueLists} from "../core/services/value-lists.service";
import {User} from "../core/types/models/User";
import {map} from "rxjs/operators";
import {forkJoin} from "rxjs";
import {UserGenresSettingModule} from "./user-genres.module";
import {AuthService} from "../auth/auth.service";

@Injectable({
    providedIn: UserGenresSettingModule,
})
export class UserGenresSettingResolve implements Resolve<{user: User, selects: Object}> {

    constructor(
        private users: Users,
        private router: Router,
        private currentUser: CurrentUser,
        private values: ValueLists,
        private auth: AuthService,
    ) {}

    resolve(route: ActivatedRouteSnapshot): Promise<{user: User, selects: object}> {
        
        return null;
    }
}