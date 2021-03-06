import { Injectable } from '@angular/core';
import { Router, Resolve, ActivatedRouteSnapshot } from '@angular/router';
import {AppHttpClient} from "vebto-client/core/http/app-http-client.service";

@Injectable()
export class TrendsStatisticsResolve implements Resolve<{client: Object, server: Object}> {

    constructor(private http: AppHttpClient, private router: Router) {}

    resolve(route: ActivatedRouteSnapshot): Promise<{client: Object, server: Object}> {
        return this.http.get('settings').toPromise().then(response => {
            return response;
        }, () => {
            this.router.navigate(['/admin']);
            return false;
        }) as any;
    }
}