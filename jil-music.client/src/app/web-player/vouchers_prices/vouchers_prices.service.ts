import {Injectable} from '@angular/core';
import {AppHttpClient} from "vebto-client/core/http/app-http-client.service";
import {Observable} from "rxjs";

@Injectable()
export class VouchersPrices {

    constructor(private http: AppHttpClient) {}

    /**
     * Get voucher prices.
     */
    public getVouchersPrices() {
        return this.http.get('vouchersprices');
    }

}
