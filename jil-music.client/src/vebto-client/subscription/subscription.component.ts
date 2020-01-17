import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {Users} from "../auth/users.service";
import {CurrentUser} from "vebto-client/auth/current-user";
import {AuthService} from "../auth/auth.service";
import {Toast} from "../core/ui/toast.service";
import {ErrorsModel} from "./subscription-types";
import {Settings} from "../core/config/settings.service";
import {User} from "../core/types/models/User";
import {Translations} from '../core/translations/translations.service';
import {Localizations} from '../core/translations/localizations.service';

import {Modal} from "vebto-client/core/ui/modal.service";
import {VoucherModalComponent} from "./voucher-modal/voucher-modal.component";

declare let Android: any;

@Component({
    selector: 'account-subscription',
    templateUrl: './subscription.component.html',
    styleUrls: ['./subscription.component.scss'],
    encapsulation: ViewEncapsulation.None,
})
export class SubscriptionComponent implements OnInit {

    public voucher = '';

    public isMobile;

    public isSubscribe = true;

    /**
     * User model.
     */
    public user = new User();

    /**
     * Subscription
     */
    public subscription = {
        status: '',
        validity: ''
    };

    /**
     * Errors returned from backend.
     */
    public errors: ErrorsModel = {account: {}, subsribe: {}};

    /**
     * AccountSettingsComponent Constructor.
     */
    constructor(
        public settings: Settings,
        private route: ActivatedRoute,
        private users: Users,
        private currentUser: CurrentUser,
        private toast: Toast,
        private i18n: Translations,
        private localizations: Localizations,
        public auth: AuthService,

        private modal: Modal,
    ) {
        this.isMobile = window.matchMedia && window.matchMedia('(max-width: 768px)').matches;
    }

    ngOnInit() {
        this.checkSubscription();         
    }

    public checkSubscription() {        
        this.user = this.currentUser.getModel();
        if(this.user.subscription == null){
            this.subscription.status = 'Active';
            this.subscription.validity = 'Never';
            this.isSubscribe = false;
        }else{
            if(this.user.subscription_s){
                this.subscription.status = 'Active';
                this.subscription.validity = this.user.subscription;    
            }else{                
                if(this.user.subscription == '0000-00-00 00:00:00'){
                    this.subscription.status = 'No Subscription';
                    this.subscription.validity = '';
                }else{
                    this.subscription.status = 'Subscription expired';
                    this.subscription.validity = this.user.subscription;
                }                                
            }
        }
    }

    /**
     * Open modal for single artist page.
     */
    public openVoucherModal() {        
        const userInfo = this.currentUser.getModel();
        this.modal.open(
            VoucherModalComponent,
            {userInfo},
            'home-modal-container',
            'voucher-modal'
        )
        .afterClosed().subscribe(() => {
            this.checkSubscription(); 
        });
    }
    
    /**
     * Subscrib user.
     */
    public sendVoucherCode() {
        this.users.subscribAdd(this.currentUser.getModel().id, this.getVoucherPayload()).subscribe(response => {           
            this.errors.subsribe = {};
            this.voucher = '';
            
            this.currentUser.set('subscription', response['user']['subscription']);            
            this.currentUser.set('subscription_s', response['user']['subscription_s']);

            this.checkSubscription();

            try {Android.userLogin(JSON.stringify(this.currentUser.getModel()));}catch(error) {console.log(this.currentUser.getModel())}                    

        }, response => this.errors.subsribe = response.messages);
        
    }
    
    /**
     * Return payload to send to backend when updating account settings.
     */
    private getVoucherPayload() {
        return {
            voucher: this.voucher,            
        };
    }
}
