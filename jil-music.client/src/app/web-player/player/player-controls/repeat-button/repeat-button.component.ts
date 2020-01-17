import {Component, ViewEncapsulation} from '@angular/core';
import {Player} from "../../player.service";

import {CurrentUser} from "vebto-client/auth/current-user";
import {Router} from '@angular/router';

import {Modal} from "vebto-client/core/ui/modal.service";
import {ConfirmModalComponent} from "vebto-client/core/ui/confirm-modal/confirm-modal.component";

import {VoucherModalComponent} from "vebto-client/subscription/voucher-modal/voucher-modal.component";

declare let Android: any;

@Component({
    selector: 'repeat-button',
    templateUrl: './repeat-button.component.html',
    styleUrls: ['./repeat-button.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class RepeatButtonComponent {

    public dataTrackToAndroid = '';       

    /**
     * Whether mobile layout should be activated.
     */
    public isMobile: boolean = false;

    constructor(
        public player: Player,
        protected router: Router,
        protected currentUser: CurrentUser,
        private modal: Modal,
    ) {this.isMobile = window.matchMedia && window.matchMedia('(max-width: 768px)').matches;}
     
    public callbackMobile() {
        
        let track_to_mobile = this.player.getCuedTrack();     
        if ( ! track_to_mobile){
            this.dataTrackToAndroid = '';
            return;
        }        

        if (track_to_mobile.album.is_premium){
            if (!this.currentUser.isLoggedIn()) {            
                this.maybeConfirm('login','Login In','You must be logged in to download track "'+track_to_mobile.name+'" from premium content.');    
                return;            
            }
            if (!this.currentUser.jilSubscriptionIsActive()) {            
                this.maybeConfirm('subscription','Add Voucher','To download track "'+track_to_mobile.name+'" from premium content you need to activate the subscription.');            
                return;
            }            
        }else{
            this.callbackMobileSend(track_to_mobile);
        }
        return;                
    }

    public callbackMobileSend(track_to_mobile) {
        try {Android.getTrackObj(JSON.stringify(track_to_mobile));}catch(error) {}
        this.dataTrackToAndroid = JSON.stringify(track_to_mobile);
    }
    /**
     * Ask user to confirm deletion of selected albums
     * and delete selected artists if user confirms.
     */
    public maybeConfirm(type, btn, text) {        
        this.modal.show(ConfirmModalComponent, {
            title: 'Premium Content',
            body:  text,
            ok:    btn
        }).afterClosed().subscribe(confirmed => {
            if ( ! confirmed) return;
            if (type == 'login'){
                this.router.navigate(['/login']);
            }else{
                this.openVoucherModal();                
            }            
        });
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
            if(this.currentUser.jilSubscriptionIsActive()) this.callbackMobile();
        });
    }

}
