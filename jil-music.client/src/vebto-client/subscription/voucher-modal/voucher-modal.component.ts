import {Component, OnInit, OnDestroy, ViewEncapsulation, NgZone, AfterViewInit, Inject, Optional, ElementRef } from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {Users} from "../../auth/users.service";
import {CurrentUser} from "vebto-client/auth/current-user";
import {AuthService} from "../../auth/auth.service";
import {Toast} from "../../core/ui/toast.service";
import {ErrorsModel} from "../subscription-types";
import {Settings} from "../../core/config/settings.service";
import {User} from "../../core/types/models/User";
import {Translations} from '../../core/translations/translations.service';
import {Localizations} from '../../core/translations/localizations.service';

import {MAT_DIALOG_DATA, MatDialogRef, MatDialog} from "@angular/material";
import {OverlayContainer} from '@angular/cdk/overlay';
import {WebPlayerState} from "../../../app/web-player/web-player-state.service";

import {Modal} from "vebto-client/core/ui/modal.service";
import { Subscription, Observable } from "rxjs";
import { NavigationStart, NavigationEnd, Router } from "@angular/router";
import { filter } from 'rxjs/operators';
import {BrowserEvents} from "vebto-client/core/services/browser-events.service";

declare let Android: any;

export interface VoucherModalData {
    userInfo?: User
}

@Component({
    selector: 'voucher-modal',
    templateUrl: './voucher-modal.component.html',
    styleUrls: ['./voucher-modal.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class VoucherModalComponent  implements OnInit, OnDestroy {
    
    /**
     * Active service subscriptions.
     */
    protected subscriptions: Subscription[] = [];

    public panCheck:boolean = true;
    
    public voucher = '';

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
     * VoucherModalComponent Constructor.
     */
    constructor(
        private dialogRefAll: MatDialog,
        @Optional() @Inject(MAT_DIALOG_DATA) public dataUser: VoucherModalData,
        private dialogRef: MatDialogRef<VoucherModalComponent>,     
        private overlayContainer: OverlayContainer,
        private settings: Settings,
        private modal: Modal,
        private router: Router,
        private browserEvents: BrowserEvents,
        private state: WebPlayerState,
        private users: Users,    
        private currentUser: CurrentUser,    
        private toast: Toast,

        private ngZone: NgZone, 
        protected el: ElementRef,
    ) {
        this.dialogRefAll.closeAll();
        this.hydrate(dataUser);
    }

    ngOnInit() {
        this.panCheck = true;
        
        const overlayEl = this.dialogRef['_overlayRef'].overlayElement;
        //overlayEl.parentElement.classList.add('dialog-wrapper-Home');
        overlayEl.parentElement.classList.add('dialog-wrapper-Voucher');

        this.overlayContainer.getContainerElement().classList.add('context-menu-show-inModal');

        setTimeout(() => {                        
            overlayEl.parentElement.classList.add('home-modal-showed')
        }, 201);
        
        this.bindToRouterEvents();
    }

    ngOnDestroy() {
        //const overlayEl = this.dialogRef['_overlayRef'].overlayElement;
        //overlayEl.parentElement.classList.remove('dialog-wrapper-Voucher');

        this.overlayContainer.getContainerElement().classList.remove('context-menu-show-inModal');

        this.destroy();
        this.subscriptions = null;
    }
    
    /**
     * Called after component's view has been fully initialized.
     */
    ngAfterViewInit() {
        //wait for animations to complete
        //TODO: refactor this to use events instead
        setTimeout(() => {            
            this.bindHammerEvents();            
        }, 201);
    }
    /**
     * Bind handlers to needed hammer.js events.
     */
    private bindHammerEvents() {
        let hammer, tap, pan;
        
        this.ngZone.runOutsideAngular(() => {
            hammer = new Hammer.Manager(this.el.nativeElement);
            tap = new Hammer.Tap();
            pan = new Hammer.Pan();
            hammer.add([tap, pan]);
        });
                
        hammer.on("panleft", e => {
            this.ngZone.run(() => {
                if(e.type == 'panleft' && this.panCheck){
                    this.panCheck = false;
                    this.close();         
                } 
            });                        
        });
    }

    /**
     * Destroy fullscreen overlay service.
     */
    public destroy() {
        this.subscriptions.forEach(subscription => {
            subscription.unsubscribe();
        });
    }

    /**
     * Minimize fullscreen overlay when navigation occurs.
     */
    private bindToRouterEvents() {
        const sub = this.router.events.pipe(
            filter(e => e instanceof NavigationStart))
            .subscribe(() => this.close());

        this.subscriptions.push(sub);

        const subModal = this.browserEvents.globalKeyDown$.subscribe(e => {
            //close on ESC key press.
            if (e.keyCode === this.browserEvents.keyCodes.escape) {                
                this.close();
            }
        });

        this.subscriptions.push(subModal);
    }

    private hydrate(dataUser: VoucherModalData) {

        if (dataUser.userInfo) this.user = dataUser.userInfo;        
        if(!this.user.id || this.user.subscription == null) this.close();

        this.state.isModalOpen = true;
        this.state.typeModalPage = 'voucher';
    }

    public close() {        
        this.state.isModalOpen = false;
        this.state.typeModalPage = '';

        const overlayEl = this.dialogRef['_overlayRef'].overlayElement;        
        overlayEl.parentElement.classList.remove('home-modal-showed')

        setTimeout(() => {                        
            this.dialogRef.close();
        }, 301);  
    }

    /**
     * Subscrib user.
     */
    public sendVoucherCode() {
        
        this.users.subscribAdd(this.user.id, this.getVoucherPayload()).subscribe(response => {           
            this.errors.subsribe = {};
            this.voucher = '';
            
            this.user.subscription = response['user']['subscription'];
            this.currentUser.set('subscription', response['user']['subscription']);
            this.user.subscription_s = response['user']['subscription_s'];
            this.currentUser.set('subscription_s', response['user']['subscription_s']);

            this.toast.open('Voucher successfully activated!');
            try {Android.userLogin(JSON.stringify(this.user));}catch(error) {}                    

            setTimeout(() => {                        
                this.close();
            }, 500);              

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
