import {Injectable} from '@angular/core';
import {AppHttpClient} from "vebto-client/core/http/app-http-client.service";
import {Observable} from "rxjs";
import {UserSubscription} from "../../models/UserSubscription";

@Injectable()
export class UserSubscriptions {

    constructor(private http: AppHttpClient) {}

    /**
     * Get top Active VideoAds.
     */
    public getAll(id: number): Observable<UserSubscription[]> {
        return this.http.get('usersubscriptions/'+id);
    }

}
