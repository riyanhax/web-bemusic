import {Injectable} from '@angular/core';
import {AppHttpClient} from "vebto-client/core/http/app-http-client.service";
import {Observable} from "rxjs";
import {SubscriptionsType} from "../../models/SubscriptionsType";

@Injectable()
export class SubscriptionsTypes {

    constructor(private http: AppHttpClient) {}

    /**
     * Get videoad by specified id.
     */
    public get(id: number): Observable<SubscriptionsType> {
        return this.http.get(`subscriptionstypes/${id}`);
    }

    /**
     * Get top 50 VideoAds.
     */
    public getTop(): Observable<SubscriptionsType[]> {
        return this.http.get('subscriptionstypes/top');
    }

    /**
     * Get top Active VideoAds.
     */
    public getActive(): Observable<SubscriptionsType[]> {
        return this.http.get('subscriptionstypes/active');
    }

    /**
     * Create a new videoad.
     */
    public create(payload: object): Observable<SubscriptionsType> {
        return this.http.post('subscriptionstypes', payload);
    }

    /**
     * Update existing videoad.
     */
    public update(id: number, payload: object): Observable<SubscriptionsType> {
        return this.http.put('subscriptionstypes/'+id, payload);
    }

    /**
     * Delete specified videoads.
     */
    public delete(ids: number[]) {
        return this.http.delete('subscriptionstypes', {ids});
    }

     /**
     * Activate specified videoads.
     */
    public activate(ids: number[]) {
        return this.http.post('subscriptionstypes/activate', {ids});
    }

     /**
     * Disable specified videoads.
     */
    public disable(ids: number[]) {
        return this.http.post('subscriptionstypes/disable', {ids});
    }

    /**
     * Get voucher prices.
     
    public getVouchersPrices() {
        return this.http.get('vouchersprices');
    }*/

}
