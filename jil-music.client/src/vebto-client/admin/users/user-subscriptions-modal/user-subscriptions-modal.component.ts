import {Component, OnDestroy, Inject, ViewEncapsulation, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {MatSort} from "@angular/material";
import {User} from "../../../core/types/models/User";
import {Users} from "../../../auth/users.service";
import {Toast} from "../../../core/ui/toast.service";
import {UserSubscription} from "../../../../app/models/UserSubscription";
import {UserSubscriptions} from "../../../../app/web-player/user_subscriptions/user_subscriptions.service";
import {PaginatedDataTableSource} from '../../data-table/data/paginated-data-table-source';
import {UrlAwarePaginator} from "../../pagination/url-aware-paginator.service";

export interface CrupdateUserModalData {
    user?: User
}

@Component({
    selector: 'user-subscriptions-modal',
    templateUrl: './user-subscriptions-modal.component.html',
    styleUrls: ['./user-subscriptions-modal.component.scss'],
    encapsulation: ViewEncapsulation.None,
    providers: [UrlAwarePaginator],
})
export class UserSubscriptionsModalComponent implements OnInit, OnDestroy {
    @ViewChild(MatSort) matSort: MatSort;

    /**
     * User model.
     */
    public model: User;

    /**
     * All existing user Subscriptions.
     */
    public userSubscriptions: UserSubscription[] = [];

    /**
     * Errors returned from backend.
     */
    public errors: any = {};

    public dataSource: PaginatedDataTableSource<UserSubscription>;

    /**
     * SelectGroupModalComponent Constructor.
     */
    constructor(
        public paginator: UrlAwarePaginator,
        private userSubscriptionsService: UserSubscriptions,
        @Inject(MAT_DIALOG_DATA) public data: CrupdateUserModalData,
        private dialogRef: MatDialogRef<UserSubscriptionsModalComponent>,
    ) {}

    public ngOnInit() {
        this.resetState();
        this.hydrateModel(this.data.user);
        //this.fetchAllSubscriptions(this.data.user.id);
        
        this.dataSource = new PaginatedDataTableSource<UserSubscription>({
            uri: 'usersubscriptions',
            dataPaginator: this.paginator,
            matSort: this.matSort,
        });
        this.dataSource.setParams({user_id: this.data.user.id});
        
    }

    ngOnDestroy() {
        this.paginator.destroy();
    }

    /**
     * Close the modal and pass specified data.
     */
    public close(data?) {
        this.dialogRef.close(data);
    }

    /**
     * Set all available groups on component,
     * if not provided fetch from the server.
     */
    private fetchAllSubscriptions(id) {
        this.userSubscriptionsService.getAll(id)
            .subscribe(response => {this.userSubscriptions = response; Object.assign(this.userSubscriptions, response);});
    }

     /**
     * Populate user model with given data.
     */
    private hydrateModel(user) {
        
        Object.assign(this.model, user);
        
    }

    /**
     * Reset all modal state to default.
     */
    private resetState() {
        this.model = new User();
        this.errors = {};
    }

    /**
     * Format errors received from backend.
     */
    public handleErrors(response: {messages: object} = {messages: {}}) {
        this.errors = response.messages;
    }
}