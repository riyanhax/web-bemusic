import {Component, ElementRef, OnDestroy, OnInit, ViewEncapsulation, NgZone, AfterViewInit} from '@angular/core';
import {PlayerQueue} from "../player/player-queue.service";
import {Track} from "../../models/Track";
import {FullscreenOverlay} from "./fullscreen-overlay.service";
import {Player} from "../player/player.service";
import {PlayerState} from "../player/player-state.service";
import {YoutubeStrategy} from "../player/strategies/youtube-strategy.service";
import {Html5Strategy} from "../player/strategies/html5-strategy.service";
import {TrackContextMenuComponent} from "../tracks/track-context-menu/track-context-menu.component";
import {Subscription} from "rxjs";
import {WebPlayerState} from "../web-player-state.service";
import {BrowserEvents} from "vebto-client/core/services/browser-events.service";
import {WebPlayerImagesService} from "../web-player-images.service";
import {ContextMenu} from 'vebto-client/core/ui/context-menu/context-menu.service';

import {CurrentUser} from "vebto-client/auth/current-user";
import {Router} from '@angular/router';

import {Modal} from "vebto-client/core/ui/modal.service";
import {ConfirmModalComponent} from "vebto-client/core/ui/confirm-modal/confirm-modal.component";

import {VoucherModalComponent} from "vebto-client/subscription/voucher-modal/voucher-modal.component";

declare let Android: any;

@Component({
    selector: 'fullscreen-overlay',
    templateUrl: './fullscreen-overlay.component.html',
    styleUrls: ['./fullscreen-overlay.component.scss'],
    encapsulation: ViewEncapsulation.None,
    host: {
        '[class.maximized]': 'overlay.isMaximized()',
        'class': 'fullscreen-overlay',
        '[class.mobile-first]': 'state.isMobile',        
    }
})
export class FullscreenOverlayComponent implements OnInit, OnDestroy {

    /**
     * Active component subscription.
     */
    public subscription: Subscription;

    public currentTrackPointer;

    public dataTrackToAndroid = '';       

    public isStabAvalible : boolean;

    /**
     * FullscreenOverlayComponent Constructor.
     */
    constructor(
        public player: Player,
        public playerState: PlayerState,
        private youtube: YoutubeStrategy,
        private html5: Html5Strategy,
        private el: ElementRef,
        public queue: PlayerQueue,
        private contextMenu: ContextMenu,
        public overlay: FullscreenOverlay,
        private browserEvents: BrowserEvents,
        public state: WebPlayerState,
        public wpImages: WebPlayerImagesService,
        protected router: Router,
        protected currentUser: CurrentUser,
        private modal: Modal,

        private ngZone: NgZone,
    ) {}

    ngOnInit () {
        
        /*try {            
            let android = Android;
            android = null;
            this.isStabAvalible = false;
        }
        catch(error) {
            this.isStabAvalible = true;
        }*/
        
        this.isStabAvalible = true;

        this.currentTrackPointer = 1;

        this.subscription = this.browserEvents.globalKeyDown$.subscribe(e => {
            //minimize overlay on ESC key press.
            if (e.keyCode === this.browserEvents.keyCodes.escape) {
                this.overlay.minimize();
            }
        });

        setTimeout(() => {            
            this.bindHammerEvents();            
        }, 201);
    }

    /**
     * Called after component's view has been fully initialized.
     */
    ngAfterViewInit() {
        //wait for animations to complete
        //TODO: refactor this to use events instead
        setTimeout(() => {            
            document.querySelector('.fullscreen-overlay').classList.remove('mobile-first');
        }, 301);
        
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
                
        hammer.on("panup pandown", e => {
            this.ngZone.run(() => {
                if(e.type == 'panup' && !this.overlay.isMaximized()) this.overlay.maximize();
                if(e.type == 'pandown' && this.overlay.isMaximized()) this.overlay.minimize();
            });
        });
    }

    ngOnDestroy() {        
        this.subscription.unsubscribe();
        this.subscription = null;
    }

    public toggleStub(){
        let stubChenge = this.playerState.toggleStub();

        if(stubChenge){
            switch (this.player.getActivePlaybackStrategy()) {
                case 'youtube':
                    this.html5.bootstrapStubDestroy();
                    this.youtube.bootstrapStub(this.player.getCuedTrack());
                    break;
                case 'html5':
                    this.youtube.bootstrapStubDestroy();
                    this.html5.bootstrapStub(this.player.getCuedTrack());
                    break;                
            }             
        }else if(!stubChenge){
            switch (this.player.getActivePlaybackStrategy()) {
                case 'youtube':
                    this.youtube.bootstrapStubDestroy();
                    break;
                case 'html5':
                    this.html5.bootstrapStubDestroy();
                    break;                
            }            
        }

    }
    

    /**
     * Get current track in player queue.
     */
    public getCurrent() {        
        this.currentTrackPointer = this.queue.getPointer() + 1 || 1;
        return this.queue.getCurrent() || new Track();
    }

    /**
     * Get previous track in player queue.
     */
    public getPrevious() {
        return this.queue.getPrevious() || this.getCurrent();
    }

    /**
     * Get next track in player queue.
     */
    public getNext() {
        return this.queue.getNext() || this.getCurrent();
    }

    /**
     * Get image for specified track.
     */
    public getTrackImage(track: Track) {
        if ( ! track || ! track.album) return this.wpImages.getDefault('album');
        return track.album.image;
    }

    /**
     * Open track context menu.
     */
    public openTrackContextMenu(track: Track, e: MouseEvent) {
        e.stopPropagation();

        this.contextMenu.open(
            TrackContextMenuComponent,
            e.target,
            {data: {item: track, type: 'track'}}
        );
    }

    /**
     * Exit browser fullscreen mode or minimize the overlay.
     */
    public minimize() {
        if (this.isBrowserFullscreen()) {
            this.exitBrowserFullscreen();
        } else {
            this.overlay.minimize();
        }
    }

    /**
     * Toggle browser fullscreen mode.
     */
    public toggleBrowserFullscreen() {
        let el = this.el.nativeElement;

        if (this.isBrowserFullscreen()) {
            return this.exitBrowserFullscreen();
        }

        if (el.requestFullscreen) {
            el.requestFullscreen();
        } else if (el.msRequestFullscreen) {
            el.msRequestFullscreen();
        } else if (el.mozRequestFullScreen) {
            el.mozRequestFullScreen();
        } else if (el.webkitRequestFullScreen) {
            el.webkitRequestFullScreen();
        }
    }

    /**
     * Exit browser fullscreen mode.
     */
    public exitBrowserFullscreen() {
        if (document.exitFullscreen) {
            document.exitFullscreen();
        } else if (document.webkitExitFullscreen) {
            document.webkitExitFullscreen();
        } else if (document['mozCancelFullScreen']) {
            document['mozCancelFullScreen']();
        } else if (document['msExitFullscreen']) {
            document['msExitFullscreen']();
        }
    }

    /**
     * Check if browser fullscreen mode is active.
     */
    public isBrowserFullscreen() {
        return document.fullscreenElement ||
        document.webkitFullscreenElement ||
        document['mozFullscreenElement'] ||
        document['msFullScreenElement'];
    }

    public callbackMobile() {
        
        // let track_to_mobile = this.player.getCuedTrack();     
        let track_to_mobile = this.getCurrent();   
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
