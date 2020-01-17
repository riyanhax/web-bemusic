import {Component, OnDestroy, OnInit, Renderer2, ViewEncapsulation, NgZone} from '@angular/core';
import {SearchSlideoutPanel} from "./search/search-slideout-panel/search-slideout-panel.service";
import {Player} from "./player/player.service";
import {PlayerState} from "./player/player-state.service";
import {WebPlayerState} from "./web-player-state.service";
import {FullscreenOverlay} from "./fullscreen-overlay/fullscreen-overlay.service";
import {Track} from "../models/Track";
import {Settings} from "vebto-client/core/config/settings.service";
import {WebPlayerImagesService} from "./web-player-images.service";
import {OverlayContainer} from '@angular/cdk/overlay';

import {AdsPlayer} from "./ads-player/ads-player.service";
import {AdsPlayerState} from "./ads-player/ads-player-state.service";
import {FullscreenAds} from "./fullscreen-ads/fullscreen-ads.service";

@Component({
    selector: 'web-player',
    templateUrl: './web-player.component.html',
    styleUrls: ['./web-player.component.scss'],
    encapsulation: ViewEncapsulation.None,
    host: {'id': 'web-player'}
})
export class WebPlayerComponent implements OnInit, OnDestroy {

    /**
     * Whether small video should be hidden.
     */
    public shouldHideVideo = false;

    /**
     * WebPlayerComponent Constructor.
     */
    constructor(
        public searchPanel: SearchSlideoutPanel,
        public player: Player,
        public playerState: PlayerState,
        private renderer: Renderer2,
        public state: WebPlayerState,
        public overlay: FullscreenOverlay,
        private settings: Settings,
        private wpImages: WebPlayerImagesService,
        private overlayContainer: OverlayContainer,
        private ngZone: NgZone,
        
        public ads: FullscreenAds,
        public adsPlayer: AdsPlayer,
        public adsPlayerState: AdsPlayerState,
    ) {}

    ngOnInit() {        
        this.player.init();
        this.overlay.init();

        this.adsPlayer.init();
        this.ads.init();
        
        this.shouldHideVideo = this.settings.get('player.hide_video');
        this.overlayContainer.getContainerElement().classList.add('web-player-theme');

        window.myAndroid = window.myAndroid || {};
        window.myAndroid.appToAngular = window.myAndroid.appToAngular || {};

        window.myAndroid.appToAngular.stopFromApp = this.stopFromApp.bind(this);
        window.myAndroid.appToAngular.playFromApp = this.playFromApp.bind(this);
        window.myAndroid.appToAngular.nextFromApp = this.nextFromApp.bind(this);
        window.myAndroid.appToAngular.prevFromApp = this.prevFromApp.bind(this);
        window.myAndroid.appToAngular.pauseFromApp = this.pauseFromApp.bind(this);
    }

    ngOnDestroy() {
        this.player.destroy();
        this.overlay.destroy();

        this.adsPlayer.destroy();
        this.ads.destroy();

        this.overlayContainer.getContainerElement().classList.remove('web-player-theme');

        window.myAndroid.appToAngular.stopFromApp = null;
        window.myAndroid.appToAngular.playFromApp = null;
        window.myAndroid.appToAngular.nextFromApp = null;
        window.myAndroid.appToAngular.prevFromApp = null;
        window.myAndroid.appToAngular.pauseFromApp = null;
    }

    public stopFromApp() {  
        this.ngZone.run(() => {
            this.player.stop();
        });
    }

    public playFromApp() {
        this.ngZone.run(() => {
            this.player.play();
        });
    }

    public nextFromApp() {                
        this.ngZone.run(() => {
            this.player.playNext();
        });
    }

    public prevFromApp() {
        this.ngZone.run(() => {
            this.player.playPrevious();
        });
    }

    public pauseFromApp() {        
        this.ngZone.run(() => {
            this.player.pause();
        });
    }

}
