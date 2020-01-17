import {Component, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import {SubscriptionsTypes} from "../../../web-player/subscriptions_types/subscriptions_types.service";
import {ActivatedRoute, Router} from "@angular/router";
import {UrlAwarePaginator} from "vebto-client/admin/pagination/url-aware-paginator.service";
import {Modal} from "vebto-client/core/ui/modal.service";
import {CurrentUser} from "vebto-client/auth/current-user";
import {SubscriptionsType} from '../../../models/SubscriptionsType';
import {MatSort} from '@angular/material';
import {NewSubscriptionsTypeModalComponent} from '../new-subscriptions_type-modal/new-subscriptions_type-modal.component';
import {VouchersPricesModalComponent} from '../vouchers-prices-modal/vouchers-prices-modal.component';
import {ConfirmModalComponent} from 'vebto-client/core/ui/confirm-modal/confirm-modal.component';
import {PaginatedDataTableSource} from 'vebto-client/admin/data-table/data/paginated-data-table-source';


@Component({
    selector: 'susbcriptions_types',
    templateUrl: './subscriptions_types.component.html',
    styleUrls: ['./subscriptions_types.component.scss'],
    providers: [UrlAwarePaginator],
    encapsulation: ViewEncapsulation.None
})
export class SubscriptionsTypesComponent implements OnInit {
    @ViewChild(MatSort) matSort: MatSort;

    public dataSource: PaginatedDataTableSource<SubscriptionsType>;

    /**
     * TracksPageComponent Constructor.
     */
    constructor(
        private modal: Modal,
        private subscriptionsTypes: SubscriptionsTypes,
        private route: ActivatedRoute,
        private router: Router,
        public currentUser: CurrentUser,
        private paginator: UrlAwarePaginator,
    ) {}

    ngOnInit() {
        this.crupdateSubscriptionsTypeBasedOnQueryParams();
                
        this.dataSource = new PaginatedDataTableSource<SubscriptionsType>({
            uri: 'subscriptionstypes',
            dataPaginator: this.paginator,
            matSort: this.matSort
        });
    }

    /**
     * Open modal for editing existing track or creating a new one.
    */ 
    public openCrupdateSubscriptionsTypeModal(subscriptionsType?: SubscriptionsType) {
        this.dataSource.deselectAllItems();

        this.modal.open(NewSubscriptionsTypeModalComponent, {subscriptionsType}, 'new-subscriptionsType-modal-container')
            .afterClosed().subscribe(subscriptionsType => subscriptionsType && this.dataSource.refresh());
    }

    /**
     * Ask user to confirm deletion of selected tracks
     * and delete selected artists if user confirms.
    */
    public maybeDeleteSelectedSubscriptionsTypes() {
        this.modal.show(ConfirmModalComponent, {
            title: 'Delete Subscriptions Types',
            body:  'Are you sure you want to delete selected Subscriptions Types?',
            ok:    'Delete'
        }).beforeClose().subscribe(confirmed => {
            if (confirmed) {
                this.deleteSelectedSubscriptionsTypes();
            } else {
                this.dataSource.deselectAllItems();
            }
        });
    } 

    /**
     * Delete currently selected artists.
     */
    private deleteSelectedSubscriptionsTypes() {
        const ids = this.dataSource.selectedRows.selected.map(subscriptionsType => subscriptionsType.id);

        this.subscriptionsTypes.delete(ids).subscribe(() => {
            this.dataSource.refresh();
        });
    }

    /**
     * Ask user to confirm activate of selected ads
     * and activate selected ads if user confirms.
    */
    public maybeActivateSelectedSubscriptionsTypes() {
        this.modal.show(ConfirmModalComponent, {
            title: 'Activate Subscriptions Types',
            body:  'Are you sure you want to activate selected Subscriptions Types?',
            ok:    'Activate'
        }).afterClosed().subscribe(confirmed => {
            if ( ! confirmed) return;
            this.activateSelectedSubscriptionsTypes();
        });
    } 

    /**
     * Ask user to disable of selected ads
     * and disable selected ads if user confirms.
    */
    public maybeDisableSelectedSubscriptionsTypes() {
        this.modal.show(ConfirmModalComponent, {
            title: 'Disable Subscriptions Types',
            body:  'Are you sure you want to disable selected Subscriptions Types?',
            ok:    'Disable'
        }).afterClosed().subscribe(confirmed => {
            if ( ! confirmed) return;
            this.disableSelectedSubscriptionsTypes();
        });
    }

    /**
     * Delete currently selected artists.
     */
    private activateSelectedSubscriptionsTypes() {
        const ids = this.dataSource.selectedRows.selected.map(subscriptionsType => subscriptionsType.id);

        this.subscriptionsTypes.activate(ids).subscribe(() => {
            this.dataSource.refresh();
        });
    }

    /**
     * Delete currently selected artists.
     */
    private disableSelectedSubscriptionsTypes() {
        const ids = this.dataSource.selectedRows.selected.map(subscriptionsType => subscriptionsType.id);

        this.subscriptionsTypes.disable(ids).subscribe(() => {
            this.dataSource.refresh();
        });
    }

    /**
     * Open crupdate Videoad modal if Videoad id is specified in query params.
    */
    private crupdateSubscriptionsTypeBasedOnQueryParams() {
        let subscriptionsTypeId = +this.route.snapshot.queryParamMap.get('id'),
            newSubscriptionsType = this.route.snapshot.queryParamMap.get('newVideoad');

        if ( ! subscriptionsTypeId && ! newSubscriptionsType) return;

        this.router.navigate([], {replaceUrl: true}).then(async () => {
            let subscriptionsType = subscriptionsTypeId ? await this.subscriptionsTypes.get(subscriptionsTypeId).toPromise() : null;

            this.openCrupdateSubscriptionsTypeModal(subscriptionsType);
        });
    } 

    /**
     * Show modal for vouchers prices.
    */
    public showVouchersPricesModal() {
        this.modal.open(
            VouchersPricesModalComponent,
            {},
            'vouchers-prices-modal-container'
        ).afterClosed().subscribe(data => {
            //if ( ! data) return;
            //this.paginator.refresh();
        });
    }
}
