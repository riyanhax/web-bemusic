import {Injectable} from '@angular/core';
import {PlayerQueue} from "./player-queue.service";
import {Track} from "../../models/Track";
import {YoutubeStrategy} from "./strategies/youtube-strategy.service";
import {PlayerState} from "./player-state.service";
import {Settings} from "vebto-client/core/config/settings.service";
import {FullscreenOverlay} from "../fullscreen-overlay/fullscreen-overlay.service";
import {WebPlayerState} from "../web-player-state.service";
import {PlaybackStrategy} from "./strategies/playback-strategy.interface";
import {Html5Strategy} from "./strategies/html5-strategy.service";
import {SoundcloudStrategy} from "./strategies/soundcloud-strategy.service";
import {Subscription, from} from "rxjs";
import {TrackPlays} from "./track-plays.service";
import {LocalStorage} from "vebto-client/core/services/local-storage.service";
import {BrowserEvents} from "vebto-client/core/services/browser-events.service";

import {CurrentUser} from "vebto-client/auth/current-user";
import {Router} from '@angular/router';

import {Album} from "../../models/Album";
import {Albums} from "../albums/albums.service";

import {AdsPlayer} from "../ads-player/ads-player.service";
import {AdsPlayerState} from "../ads-player/ads-player-state.service";


import {Modal} from "vebto-client/core/ui/modal.service";
import {ConfirmModalComponent} from "vebto-client/core/ui/confirm-modal/confirm-modal.component";

import {VoucherModalComponent} from "vebto-client/subscription/voucher-modal/voucher-modal.component";

import {FullscreenAds} from "../fullscreen-ads/fullscreen-ads.service";

declare let Android: any;

@Injectable()
export class Player {

    /**
     * Active service subscriptions.
     */
    private subscriptions: Subscription[] = [];

    /**
     * Currently active playback strategy.
     */
    private playbackStrategy: PlaybackStrategy;

    /**
     * Currently active playback strategy.
     */
    private activePlaybackStrategy: 'youtube'|'html5'|'soundcloud';

    /**
     * Current player volume.
     */
    private volume: number;

    
    /**
     * Whether continuous playback should be
     * handled by the player after song ends.
     */
    public handleContinuousPlayback = true;

    /**
     * Whether playback has been started via user gesture.
     *
     * If true, there's no need to maximize player overlay
     * anymore, because external controls will work properly.
     */
    private playbackStartedViaGesture = false;
    
    private playbackPause = false;

    private playbackEmpty = 0;

    public album : Album;
    
    // public adsVideo = {};

    // public videoAds : VideoAd[];

    public androidNeedTrack:string;

    public androidAction:string;

    public checkPremiumStatus:boolean = false;
       
    /**
     * Player Constructor.
     */
    constructor(
        public queue: PlayerQueue,
        private youtube: YoutubeStrategy,
        private html5: Html5Strategy,
        private soundcloud: SoundcloudStrategy,
        private storage: LocalStorage,
        private settings: Settings,
        public state: PlayerState,
        private globalState: WebPlayerState,
        private overlay: FullscreenOverlay,
        private browserEvents: BrowserEvents,
        private trackPlays: TrackPlays,
        protected router: Router,
        protected currentUser: CurrentUser,
        protected albums: Albums, 
        
        private ads: FullscreenAds,
        public adsState: AdsPlayerState,
        private modal: Modal,

        public asdPlayer: AdsPlayer,
    ) {
        this.androidNeedTrack = '';
    }

    /**
     * Check Track in Premium Album.
     */
    public trackIsPremiumStop(track_to_listn) {
        if (!this.currentUser.isLoggedIn()) {
            this.stop();
            this.maybeConfirm('login','Login In','You must be logged in to listen track "'+track_to_listn.name+'" from premium content.');
            return true;
        }
        if (!this.currentUser.jilSubscriptionIsActive()) {
            this.stop();
            this.maybeConfirm('subscription','Add Voucher','To listen track "'+track_to_listn.name+'" from premium content you need to activate the subscription.');            
            return true;
        }
        return false;
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
            if(this.currentUser.jilSubscriptionIsActive()) this.play('play');
        });
    }

    public getActivePlaybackStrategy(){
        return this.activePlaybackStrategy;
    }

    /**
     * Check ads type to show.
     */
    public addAdsToPlay(typeToAndroid?: string) {      

        if ( (!this.currentUser.isLoggedIn() || !this.currentUser.jilSubscriptionIsActive()) && this.globalState.adsShow() ){
            if(this.asdPlayer.readyAds()){
                this.androidAction = typeToAndroid;

                this.globalState.ads = true;
                this.playbackStrategy.pause();
                this.ads.maximize();
                this.asdPlayer.start();
            }else{
                this.androidAction = null;
                this.playbackStrategy.play(); 

                let action = typeToAndroid ? typeToAndroid : 'play';
                this.getCuedTrackForAndroid(action);
            }
            
        }else{            
            this.playbackStrategy.play();

            let action = typeToAndroid ? typeToAndroid : 'play';
            this.getCuedTrackForAndroid(action);
        }
    }

    public playAfterAds() {
        
        this.globalState.adsShowed();
        this.playbackStrategy.play(); 

        let action = this.androidAction ? this.androidAction : 'play';
        this.getCuedTrackForAndroid(action);
    }
        
    /* Android */

    public getCuedTrackForAndroid(action: string) {        
        let track_to_mobile = this.getCuedTrack();        
        if(track_to_mobile){
            this.androidNeedTrack = '';
            try {Android.trackToPlay(JSON.stringify({'action':action, 'track':track_to_mobile}));}
            catch(error) {console.log({'action':action, 'track':track_to_mobile})}
        }else{
            this.androidNeedTrack = action;        
        }        
    }

    /**
     * Start the playback.
     */
    public play(typeToAndroid?: string) {
        if (!this.ready()) return;

        let track = this.queue.getCurrent();
        if (!track) return this.stop();

        if (track.album.is_premium) {
            if (this.trackIsPremiumStop(track)) return;
            this.playAfterCheck(track, typeToAndroid);
        } else {
            if (this.album && this.album.id == track.album.id && this.album.is_premium) {
                if (this.album.is_premium) {
                    if (this.trackIsPremiumStop(track)) return;
                    this.playAfterCheck(track, typeToAndroid);
                } else {
                    this.playAfterCheck(track, typeToAndroid);
                }
            } else {
                this.albums.get(track.album.id).subscribe(response => {
                    this.album = response;
                    if (this.album.is_premium) {
                        if (this.trackIsPremiumStop(track)) return;
                        this.playAfterCheck(track, typeToAndroid);
                    } else {
                        this.playAfterCheck(track, typeToAndroid);
                    }
                }, response => this.playAfterCheck(track, typeToAndroid));

            }
        }

    }

    public playAfterCheck(track, typeToAndroid?: string) {
        
        if ( !track.url && !track.youtube_id) {
            this.stop();            
            this.playbackEmpty++;
            if(this.playbackEmpty < 3 ) return this.playNext();
            return;
        }
        this.playbackEmpty = 0;
        
        this.setStrategy(track);        
        this.maybeMaximizeOverlay();

        this.addAdsToPlay(typeToAndroid);
    }

    /**
     * Pause the playback.
     */
    public pause() {
        this.playbackStrategy.pause();
        this.playbackPause = true;
        this.getCuedTrackForAndroid('pause');
    }

    /**
     * Play or pause player based on current playback state.
     */
    public togglePlayback() {        
        if (this.isPlaying()) {            
            this.pause();            
            this.getCuedTrackForAndroid('pause');
        } else {            
            this.play('togglePlayback');
        }
    }

    /**
     * Check if current playback strategy is ready.
     */
    public ready() {        
        return this.playbackStrategy.ready();
    }

    /**
     * Check if playback is in progress.
     */
    public isPlaying(): boolean {
        return this.state.playing;
    }

    /**
     * Check if player has any or specified track cued.
     */
    public cued(track?: Track) {
        const cued = this.getCuedTrack() && this.getCuedTrack().id;
        if ( ! track) return cued;
        return cued && this.getCuedTrack() === track;
    }

    /**
     * Get player state service.
     */
    public getState(): PlayerState {
        return this.state;
    }

    /**
     * Get player queue service.
     */
    public getQueue(): PlayerQueue {
        return this.queue;
    }

    /**
     * Check if player is buffering currently..
     */
    public isBuffering(): boolean {
        return this.state.buffering;
    }

    /**
     * Check if player is muted.
     */
    public isMuted(): boolean {
        return this.state.muted;
    }

    /**
     * Get track that is currently cued.
     */
    public getCuedTrack(): Track {
        if ( ! this.playbackStrategy) return null;
        return this.playbackStrategy.getCuedTrack();
    }

    /**
     * Mute player.
     */
    public mute() {
        this.playbackStrategy.mute();
        this.state.muted = true;
    }

    /**
     * Unmute player.
     */
    public unMute() {
        this.playbackStrategy.unMute();
        this.state.muted = false;
    }

    /**
     * Get current player volume.
     */
    public getVolume() {
        return this.volume;
    }

    /**
     * Set volume to a number between 0 and 100.
     */
    public setVolume(volume: number) {
        this.volume = volume;
        this.playbackStrategy.setVolume(volume);
        this.storage.set('player.volume', volume);
    }

    /**
     * Stop playback and seek to start of track.
     */
    public stop() {
        if ( ! this.state.playing) return;     

        this.playbackStrategy.pause();
        this.seekTo(0);
        this.state.playing = false;
        this.state.firePlaybackStopped();

        this.getCuedTrackForAndroid('stop');
    }

    /**
     * Get time that has elapsed since playback start.
     */
    public getCurrentTime() {
        return this.playbackStrategy.getCurrentTime();
    }

    /**
     * Get total duration of track in seconds.
     */
    public getDuration() {
        if(this.androidNeedTrack != '') this.getCuedTrackForAndroid(this.androidNeedTrack);

        return this.playbackStrategy.getDuration();
    }

    /**
     * Seek to specified time in track.
     */
    public seekTo(time: number): Promise<any> {        
        this.playbackStrategy.seekTo(time);

        return new Promise(resolve => setTimeout(() => resolve(), 50));
    }

    /**
     * Toggle between repeat, repeat one and no repeat modes.
     */
    public toggleRepeatMode() {
        if (this.state.repeating) {
            this.state.repeatingOne = true;
        } else if (this.state.repeatingOne) {
            this.state.repeatingOne = false;
            this.state.repeating = false;
        } else {
            this.state.repeating = true;
        }
    }

    /**
     * Play next track in queue based on current repeat setting.
     */
    public playNext() {                
        this.playbackPause = false;
        
        this.stop(); 
        let track = this.queue.getCurrent();

        if (this.state.repeating && this.queue.isLast()) {
            track = this.queue.getFirst();            
            if(!track) return;
        } else if ( ! this.state.repeatingOne) {
            track = this.queue.getNext();            
            if(!track) return;
        }

        this.queue.select(track);  
        
        this.play('playNext');
    }

    /**
     * Play previous track in queue based on current repeat setting.
     */
    public playPrevious() {  
        this.playbackPause = false;

        this.stop(); let track = this.queue.getCurrent();

        if (this.state.repeating && this.queue.isFirst()) {
            track = this.queue.getLast();
            if(!track) return;
        } else if (!this.state.repeatingOne) {
            track = this.queue.getPrevious();
            if(!track) return;
        }

        this.queue.select(track);

        this.play('playPrevious');
    }

    /**
     * Toggle player shuffle mode.
     */
    public toggleShuffle() {        
        if (this.state.shuffling) {
            this.queue.restoreOriginal();
        } else {
            this.queue.shuffle();
        }

        this.state.shuffling = !this.state.shuffling;
    }

    /**
     * Override player queue and cue first track.
     */
    public overrideQueue(params: {tracks: Track[], queuedItemId?: number|string}, queuePointer: number = 0): Promise<any> {
        this.playbackPause = false;

        this.putQueueIntoLocalStorage(params.tracks);
        this.queue.override(params, queuePointer);

        return this.cueTrack(this.queue.getCurrent());
    }

    /**
     * Cue specified track for playback.
     */
    public cueTrack(track: Track): Promise<any> {
        this.playbackPause = false;
        
        let promise: Promise<any>;
        this.setStrategy(track);

        if ( ! track || ! this.playbackStrategy) {
            promise = new Promise(resolve => resolve());
        } else {
            this.queue.select(track);
            promise = this.playbackStrategy.cueTrack(track);
        }

        return promise.then(() => {
            this.state.buffering = false;
        });
    }

    /**
     * Get currently active playback strategy.
     */
    public getPlaybackStrategy(): string {
        return this.activePlaybackStrategy;
    }

    /**
     * Init the player.
     */
    public init() {
        this.loadStateFromLocalStorage();
        this.setStrategy(this.queue.getCurrent());
        this.setInitialVolume();
        this.cueTrack(this.queue.getCurrent());
        this.bindToPlaybackStateEvents();
        this.bindToAdsPlaybackStateEvents();        
        this.initKeybinds();
    }

    /**
     * Destroy the player.
     */
    public destroy() {
        this.playbackStrategy && this.playbackStrategy.destroy();
        this.state.playing = false;

        this.subscriptions.forEach(subscription => {
            subscription.unsubscribe();
        });

        this.subscriptions = [];
    }

    /**
     * Put specified queue into local storage and limit tracks to 15.
     */
    private putQueueIntoLocalStorage(tracks: Track[]) {
        if ( ! tracks) return;
        this.storage.set('player.queue', {tracks: tracks.slice(0, 15)});
    }

    /**
     * Set playback strategy based on specified track.
     */
    private setStrategy(track: Track): PlaybackStrategy {        
        if (track && track.url) {
            this.playbackStrategy = this.html5;
            this.activePlaybackStrategy = 'html5';
        } else if (this.settings.get('audio_search_provider') === 'soundcloud') {
            this.playbackStrategy = this.soundcloud;
            this.activePlaybackStrategy = 'soundcloud';
        } else {
            this.playbackStrategy = this.youtube;
            this.activePlaybackStrategy = 'youtube';
        }

        //destroy all except current active playback strategy
        if (this.activePlaybackStrategy !== 'youtube') this.youtube.destroy();
        if (this.activePlaybackStrategy !== 'html5') this.html5.destroy();
        if (this.activePlaybackStrategy !== 'soundcloud') this.soundcloud.destroy();

        return this.playbackStrategy;
    }

    private loadStateFromLocalStorage() {
        this.state.muted = this.storage.get('player.muted', false);
        this.state.repeating = this.storage.get('player.repeating', true);
        this.state.repeatingOne = this.storage.get('player.repeatingOne', false);
        this.state.shuffling = this.storage.get('player.shuffling', false);
        const queuePointer = this.storage.get('player.queue.pointer', 0);
        this.queue.override(this.storage.get('player.queue', {tracks: []}), queuePointer);
    }

    /**
     * Set initial player volume.
     */
    private setInitialVolume() {
        let defaultVolume = this.settings.get('player.default_volume', 30);
            defaultVolume = this.storage.get('player.volume', defaultVolume);

        this.setVolume(defaultVolume);
        this.html5.setVolume(defaultVolume);
    }

    /**
     * Maximize fullscreen overlay if we're on mobile,
     * because youtube embed needs to be visible to start
     * playback with external youtube iframe api controls
     */
    private async maybeMaximizeOverlay(): Promise<boolean> {
        const shouldOpen = this.settings.get('player.mobile.auto_open_overlay');

        if (this.playbackStartedViaGesture || ! shouldOpen || ! this.globalState.isMobile) return;

        await this.overlay.maximize();
        this.playbackStartedViaGesture = true;
    }

    /**
     * Play next track when current track ends.
     */
    private bindToPlaybackStateEvents() {
        this.state.onChange$.subscribe(type => {
            if (type === 'PLAYBACK_STARTED') {
                this.trackPlays.increment(this.getCuedTrack());
            } else if (type === 'PLAYBACK_ENDED' && this.handleContinuousPlayback) {
                this.trackPlays.clearPlayedTrack(this.getCuedTrack());
                this.playNext();
            }
        });
    }

    /**
     * Play next track when current track ends.
     */
    private bindToAdsPlaybackStateEvents() {
        this.adsState.onChange$.subscribe(type => {
            if(type === 'ADS_CUSTOM_PLAYBACK_ENDED' || type === 'ADS_PLAYBACK_ENDED') {
                this.playAfterAds();
            }
        });
    }

    /**
     * Initiate player keyboard shortcuts.
     */
    public initKeybinds() {
        const sub = this.browserEvents.globalKeyDown$.subscribe((e: KeyboardEvent) => {
            //SPACE - toggle playback
            if (e.keyCode === this.browserEvents.keyCodes.space) {
                this.togglePlayback(); e.preventDefault();

                //ctrl+right - play next track
            } else if (e.ctrlKey && e.keyCode === this.browserEvents.keyCodes.arrowRight) {
                this.playNext(); e.preventDefault();
            }

            //ctrl+left - play previous track
            else if (e.ctrlKey && e.keyCode === this.browserEvents.keyCodes.arrowLeft) {
                this.playPrevious(); e.preventDefault();
            }
        });

        this.subscriptions.push(sub);
    }

}
