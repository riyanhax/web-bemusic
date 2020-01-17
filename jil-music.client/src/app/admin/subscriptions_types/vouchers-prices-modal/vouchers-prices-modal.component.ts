import {Component, OnDestroy, Inject, ViewEncapsulation, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {MatSort} from "@angular/material";
import { VoucherPrice } from '../../../models/VoucherPrice';
import {VouchersPrices} from "../../../web-player/vouchers_prices/vouchers_prices.service";

@Component({
    selector: 'vouchers-prices-modal',
    templateUrl: './vouchers-prices-modal.component.html',
    styleUrls: ['./vouchers-prices-modal.component.scss'],
})
export class VouchersPricesModalComponent implements OnInit {

    /**
     * All existing user prices.
     */
    voucherPrices: VoucherPrice[] = [];

    /**
     * Errors returned from backend.
     */
    public errors: any = {};

    /**
     * SelectGroupModalComponent Constructor.
     */
    constructor(
        private dialogRef: MatDialogRef<VouchersPricesModalComponent>,
        public vouchersPricesService: VouchersPrices,
    ) {}

    public ngOnInit() {
        this.resetState();
        this.fetchVouchersPrices();
    }

    private fetchVouchersPrices() {
        this.vouchersPricesService.getVouchersPrices()
            .subscribe(response => {this.voucherPrices = response; Object.assign(this.voucherPrices, response);});
    }

    /**
     * Close the modal and pass specified data.
     */
    public close(data?) {
        this.dialogRef.close();
    }

     /**
     * Populate user model with given data.
     */
    private hydrateModel(user) {
        
        //Object.assign(this.model, user);
        
    }

    /**
     * Reset all modal state to default.
     */
    private resetState() {
        //this.model = new User();
        this.errors = {};
    }

    /**
     * Format errors received from backend.
     */
    public handleErrors(response: {messages: object} = {messages: {}}) {
        this.errors = response.messages;
    }
}