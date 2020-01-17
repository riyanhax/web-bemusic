import {Injectable} from '@angular/core';
import {AdsPlayerQueue} from "./ads-player-queue.service";
import {Track} from "../../models/Track";
import {AdsPlayerState} from "./ads-player-state.service";
import {Settings} from "vebto-client/core/config/settings.service";
import {WebPlayerState} from "../web-player-state.service";
import {AdsPlaybackStrategy} from "./ads-strategies/ads-playback-strategy.interface";
import {AdsHtml5Strategy} from "./ads-strategies/ads-html5-strategy.service";
import {Subscription} from "rxjs";
import {AdsTrackPlays} from "./ads-track-plays.service";
import {LocalStorage} from "vebto-client/core/services/local-storage.service";
import {BrowserEvents} from "vebto-client/core/services/browser-events.service";

import {CurrentUser} from "vebto-client/auth/current-user";
import {Router} from '@angular/router';

import {Album} from "../../models/Album";
import {Albums} from "../albums/albums.service";

import {VideoAd} from "../../models/VideoAd";
import {VideoAds} from "../video_ads/video_ads.service";

import {Modal} from "vebto-client/core/ui/modal.service";

import {FullscreenAds} from "../fullscreen-ads/fullscreen-ads.service";

declare let google: any;
declare let Android: any;

@Injectable()
export class AdsPlayer {

    /**
     * Active service subscriptions.
     */
    private subscriptions: Subscription[] = [];

    /**
     * Currently active playback strategy.
     */
    private playbackStrategy: AdsPlaybackStrategy;

    /**
     * Currently active playback strategy.
     */
    private activePlaybackStrategy: 'html5';

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
    
    public adsVideo = {};

    public videoAds : VideoAd[];

    public curVideoAds : VideoAd;

    public androidNeedTrack:string;


    // public checkPremiumStatus:boolean = false;
   
    /****************************GOOGLE ADS*************************/
    public videoContent;
    public adDisplayContainer;
    public adsLoader;
    public adsManager;
    public intervalTimer;

    private playbackGoogle = false;
    /****************************GOOGLE ADS*************************/

    /**
     * Player Constructor.
     */
    constructor(
        public queue: AdsPlayerQueue,        
        private html5: AdsHtml5Strategy,
        private storage: LocalStorage,
        private settings: Settings,
        public state: AdsPlayerState,
        private globalState: WebPlayerState,
        private browserEvents: BrowserEvents,
        private trackPlays: AdsTrackPlays,
        protected router: Router,
        protected currentUser: CurrentUser,
        protected albums: Albums, 
        protected videoAdsServise: VideoAds,

        private ads: FullscreenAds,

        private modal: Modal,
    ) {
        this.androidNeedTrack = '';

        // if(this.globalState.adsType() == 'google') this.initGooleAds();
    }
    
    /**
     * Return active strategy (forever html5 :) )
     */
    public getActivePlaybackStrategy(){
        return this.activePlaybackStrategy;
    }

    /**
     * Android send callback
    */
    public getCuedTrackForAndroid(action: string) {      
        setTimeout(() => {                        
                let track_to_mobile = this.getCuedTrack();        
            if(track_to_mobile){
                this.androidNeedTrack = '';
                try {Android.adsToPlay(JSON.stringify({'action':action, 'track':track_to_mobile}));}
                catch(error) {console.log({'action':action, 'track':track_to_mobile})}
            }else{
                this.androidNeedTrack = action;        
            } 
        }, 301);               
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
        this.playbackStrategy.cueTrack();

        this.getAds();
    }

    /**
     * Get ADS from server
     */
    private getAds(){
        if(this.globalState.adsType().length == 0) return;
        const adsType = this.globalState.adsType();        
        this.videoAdsServise.getActive().subscribe(data => {                
            this.videoAds = data;
            adsType.map(index => {                
                if(!this.videoAds[index]) this.globalState.adsTypeDelete(index);                                
            });            
        });
    }

    /**
     * Add spesifed ADS to ads-player
     */
    private cueAdsTrack(adsItem: VideoAd){
        this.adsVideo = {
            id: '99999999',
            name: adsItem.name,
            album_name: adsItem.type,
            number: 0,
            duration: adsItem.duration,
            youtube_id: '',
            spotify_popularity: 0,
            album_id: 0,
            url: adsItem.type == 'video' ? adsItem.url : '',
            plays: 0,
        };                

        this.queue.prependAds(this.adsVideo);
    }

    /**
     * Check is ads enable and loaded
     */
    public readyAds(){
        
        if(this.globalState.adsType().length == 0) return false;

        if(!(this.globalState.adsType().indexOf('adsima') > -1) && this.globalState.adsType().length == 1 && this.videoAds.length == 0) return false;

        return true;
    }

    /**
     * Get random index for show ads type and ads obj     
     */
    private getRandomInt(min, max) {
        return Math.floor(Math.random() * (max - min + 1)) + min;
    }

    /**
     * Choice random ads type and ads obj to show
     */
    public start(){
        const typeIndex = this.globalState.adsType()[this.getRandomInt(0, this.globalState.adsType().length - 1)]; 
        const adsIndex = typeIndex == 'adsima' ? 0 : this.getRandomInt(0, this.videoAds[typeIndex].length - 1); 
        
        this[typeIndex+'Set'](this.videoAds[typeIndex][adsIndex]);
    }
    
    private promoSet(adsItem: VideoAd){          
        this.curVideoAds = adsItem;            
        this.ads.setType(adsItem.type);  
        this.cueAdsTrack(adsItem);
        this.play();
    }

    private videoSet(adsItem: VideoAd){
        this.curVideoAds = adsItem;            
        this.ads.setType(adsItem.type);  
        this.cueAdsTrack(adsItem);
        this.play();        
    }

    private customSet(adsItem: VideoAd){
        this.curVideoAds = adsItem;            
        this.ads.setType(adsItem.type);  
        this.cueAdsTrack(adsItem);
        this.play();
    }

    private adsceneSet(adsItem: VideoAd){
        
        console.log(adsItem);
    }

    private adsimaSet(adsItem: any){
        
        console.log(adsItem);
    }

    /**
     * Start the playback.
     */
    public play(typeToAndroid?: string) {        
        if ( ! this.ready()) return;
        
        let track = this.queue.getCurrent();            
        if ( ! track) return this.stop();        
        
        this.setStrategy(track);        
        //console.log(track);
        
        if(track.album_name == 'video'){
            this.playbackStrategy.play();
        }else{
            this.state.playing = true;
            this.state.firePlaybackCustomStart();
        }
        
        
         let action = typeToAndroid ? typeToAndroid : 'play ads';
         this.getCuedTrackForAndroid(action);
     
    }

    /**
     * Get total custom duration of track in seconds.
     */
    public getCustomDuration() {

        if(this.androidNeedTrack != '') this.getCuedTrackForAndroid(this.androidNeedTrack);

        let track = this.queue.getCurrent();
        return track.duration;
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
    public getState(): AdsPlayerState {
        return this.state;
    }

    /**
     * Get player queue service.
     */
    public getQueue(): AdsPlayerQueue {
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
        this.storage.set('ads_player.volume', volume);
    }

    /**
     * Stop playback and seek to start of track.
     */
    public stop() {
        if ( ! this.state.playing) return;     
        
        if ( this.playbackGoogle ) return;        

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
        if ( this.playbackGoogle ) return;      

        if (this.state.playing && this.getCuedTrack().album_name == 'video_ads'){            
            this.playbackStrategy.seekTo(this.getCurrentTime());  
        }else{
            this.playbackStrategy.seekTo(time);
        }         
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
        if ( this.playbackGoogle ) return;

        this.playbackPause = false;

        if (this.state.playing && this.getCuedTrack().album_name == 'video_ads'){            
            return;
        }
        
        this.stop(); 
        let track = this.queue.getCurrent();
                
        if (track && track.album_name && track.album_name == 'video_ads'){            
            this.globalState.adsShowed();
            track = this.queue.getPrevious();            
        }else if (this.state.repeating && this.queue.isLast()) {
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
        if ( this.playbackGoogle ) return;
        
        this.playbackPause = false;

        if (this.state.playing && this.getCuedTrack().album_name == 'video_ads'){            
            return;
        }
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

        // this.putQueueIntoLocalStorage(params.tracks);
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
        this.storage.set('ads_player.queue', {tracks: tracks.slice(0, 15)});
    }

    /**
     * Set playback strategy based on specified track.
     */
    private setStrategy(track: Track): AdsPlaybackStrategy {                
        this.playbackStrategy = this.html5;
        this.activePlaybackStrategy = 'html5';
        
        return this.playbackStrategy;
    }

    private loadStateFromLocalStorage() {
        this.state.muted = this.storage.get('ads_player.muted', false);                
        this.queue.override({tracks: []} , 0);
    }

    /**
     * Set initial player volume.
     */
    private setInitialVolume() {
        let defaultVolume = this.settings.get('ads_player.default_volume', 30);
            defaultVolume = this.storage.get('ads_player.volume', defaultVolume);

        this.setVolume(defaultVolume);
        this.html5.setVolume(defaultVolume);
    }

    /**
     * Play next track when current track ends.
     */
    private bindToPlaybackStateEvents() {        
        this.state.onChange$.subscribe(type => {               
            if (type === 'ADS_PLAYBACK_STARTED') {
                this.trackPlays.increment(this.getCuedTrack());
            } else if (type === 'ADS_PLAYBACK_ENDED') {
                this.trackPlays.clearPlayedTrack(this.getCuedTrack());
            } else if(type === 'ADS_CUSTOM_PLAYBACK_ENDED') {                
                this.trackPlays.clearPlayedTrack(this.getCuedTrack());            
            } else if (type === 'ADS_PLAYBACK_STOPPED') {
                this.stop();
            }            
        });
    }

}
