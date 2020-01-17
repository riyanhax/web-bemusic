import {Injectable} from '@angular/core';
import {Resolve, RouterStateSnapshot, ActivatedRouteSnapshot} from '@angular/router';
import {WebPlayerState} from "../../web-player-state.service";
import {AppHttpClient} from "vebto-client/core/http/app-http-client.service";

@Injectable()
export class UserGenresResolver implements Resolve<any> {
    
    constructor(
        private http: AppHttpClient,
        private state: WebPlayerState
    ) {}

    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<any> {
        //this.state.loading = true;
                
        return this.http.get('genres/my_stats').toPromise().then(userStats => {
            //this.state.loading = false;
            return userStats;
        }).catch(() => {
            //this.state.loading = false;
            return null;
        }) as any;
    }

}