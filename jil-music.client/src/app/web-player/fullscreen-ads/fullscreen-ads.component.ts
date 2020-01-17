import {Component, ElementRef, OnDestroy, OnInit, ViewEncapsulation, NgZone, AfterViewInit} from '@angular/core';
import {AdsPlayerQueue} from "../ads-player/ads-player-queue.service";
import {Track} from "../../models/Track";
import {FullscreenAds} from "./fullscreen-ads.service";
import {AdsPlayer} from "../ads-player/ads-player.service";
import {AdsPlayerState} from "../ads-player/ads-player-state.service";
import {AdsHtml5Strategy} from "../ads-player/ads-strategies/ads-html5-strategy.service";
import {Subscription} from "rxjs";
import {WebPlayerState} from "../web-player-state.service";
import {BrowserEvents} from "vebto-client/core/services/browser-events.service";
import {WebPlayerImagesService} from "../web-player-images.service";

import {CurrentUser} from "vebto-client/auth/current-user";
import {Router} from '@angular/router';

import {VideoAd} from "../../models/VideoAd";

@Component({
    selector: 'fullscreen-ads',
    templateUrl: './fullscreen-ads.component.html',
    styleUrls: ['./fullscreen-ads.component.scss'],
    encapsulation: ViewEncapsulation.None,
    host: {
        '[class.maximized]': 'ads.isMaximized()',
        'class': 'fullscreen-ads',
        '[class.mobile-first]': 'ads.isMobile',
        '[class.promo-ads]': 'ads.isType("promo")',
        '[class.custom-ads]': 'ads.isType("custom")',
        '[class.video-ads]': 'ads.isType("adsima") || ads.isType("video")',
        '[class.adscene-ads]': 'ads.isType("adscene")',
    }
})
export class FullscreenAdsComponent implements OnInit, OnDestroy {

    /**
     * Active component subscription.
     */
    // public subscription: Subscription;
    public subscriptions: Subscription[] = [];

    public currentTrackPointer;
    /**
     * FullscreenAdsComponent Constructor.
     */
    constructor(
        public player: AdsPlayer,
        public playerState: AdsPlayerState,
        private html5: AdsHtml5Strategy,
        private el: ElementRef,
        public queue: AdsPlayerQueue,
        public ads: FullscreenAds,
        private browserEvents: BrowserEvents,
        public state: WebPlayerState,
        public wpImages: WebPlayerImagesService,
        protected router: Router,
        protected currentUser: CurrentUser,   
        
        private ngZone: NgZone,
    ) {}

    ngOnInit () {
                        
        this.currentTrackPointer = 1;

        const sub = this.browserEvents.globalKeyDown$.subscribe(e => {
            //minimize overlay on ESC key press.
            if (e.keyCode === this.browserEvents.keyCodes.escape) {
                this.ads.minimize();
                this.playerState.firePlaybackCustomStop();
                this.playerState.firePlaybackStopped();
            }
        });

        this.subscriptions.push(sub);

        const adsModal = this.playerState.onChange$.subscribe(type => {                           
            if(type === 'ADS_CUSTOM_PLAYBACK_ENDED' || type === 'ADS_PLAYBACK_ENDED') {                
                this.minimizeFired();
            }
        });

        this.subscriptions.push(adsModal);
    }

    /**
     * Called after component's view has been fully initialized.
     */
    ngAfterViewInit() {
        //wait for animations to complete
        //TODO: refactor this to use events instead
        setTimeout(() => {            
            document.querySelector('.fullscreen-ads').classList.remove('mobile-first');
        }, 301);        
    }

    ngOnDestroy() {                
        this.destroy();
        this.subscriptions = null;
    }
    
    /**
     * Destroy fullscreen overlay service.
     */
    public destroy() {
        this.subscriptions.forEach(subscription => {
            subscription.unsubscribe();
        });
    }

    getPromoLink(curAds: VideoAd){
        if(curAds.type != 'promo') return;
        return JSON.parse(curAds.link);
    }

    /**
     * Exit browser fullscreen mode or minimize the overlay.
     */
    public minimizeFired() {
        this.ads.minimize();
        document.querySelector('.fullscreen-ads').classList.remove('maximized-height');        

        new Promise(resolve => {
            setTimeout(() => {                 
                document.querySelector('.fullscreen-ads').classList.remove('maximized');        
             }, 301);
        });
    }
    /**
     * Exit browser fullscreen mode or minimize the overlay.
     */
    public minimize() {
        this.ads.minimize();
    }

}
