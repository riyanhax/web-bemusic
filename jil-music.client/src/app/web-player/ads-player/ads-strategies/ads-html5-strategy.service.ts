import {AdsPlaybackStrategy} from "./ads-playback-strategy.interface";
import {AdsPlayerState} from "../ads-player-state.service";
import {Track} from "../../../models/Track";
import {Injectable, NgZone} from "@angular/core";
import {AdsPlayerQueue} from "../ads-player-queue.service";
import {WebPlayerImagesService} from '../../web-player-images.service';


@Injectable()
export class AdsHtml5Strategy implements AdsPlaybackStrategy {

    /**
     * Whether player is already bootstrapped.
     */
    private bootstrapped = false;

    /**
     * Volume that should be set after player is bootstrapped.
     * Number between 1 and 100.
     */
    private pendingVolume: number = null;

    /**
     * Html5 video element instance.
     */
    private html5: HTMLVideoElement;

    /**
     * Img element instance.
     */
    // private imgBackground: HTMLImageElement;

    private imgBackgroundParent: HTMLDivElement = null;
    private imgBackground: HTMLImageElement;

    /**
     * Track currently cued for playback.
     */
    private cuedTrack: Track;

    /**
     * Track that is currently being cued.
     */
    private cueing: Track;

    /**
     * Html5Strategy Constructor.
     */
    constructor(
        private state: AdsPlayerState,
        private zone: NgZone,
        private queue: AdsPlayerQueue,
        private wpImages: WebPlayerImagesService,
    ) {}

    /**
     * Start playback.
     */
    public async play() {
        await this.cueTrack(this.queue.getCurrent());
        this.html5.play();
        this.state.playing = true;
    }

    /**
     * Pause playback.
     */
    public pause() {
        this.html5.pause();
        this.state.playing = false;
    }

    /**
     * Stop playback.
     */
    public stop() {
        this.pause();
        this.seekTo(0);
        this.state.playing = false;
    }

    /**
     * Seek to specified time in track.
     */
    public seekTo(time: number) {
        this.html5.currentTime = time;
    }

    /**
     * Get loaded track duration in seconds.
     */
    public getDuration() {
        if ( ! this.html5.seekable.length) return 0;
        return this.html5.seekable.end(0);
    }

    /**
     * Get elapsed time in seconds since the track started playing
     */
    public getCurrentTime() {
        return this.html5.currentTime;
    }

    /**
     * Set html5 player volume to float between 0 and 1.
     */
    public setVolume(number: number) {
        if ( ! this.html5) {
            this.pendingVolume = number;
        } else {
            this.html5.volume = number / 100;
        }
    }

    /**
     * Mute the player.
     */
    public mute() {
        this.html5.muted = true;
    }

    /**
     * Unmute the player.
     */
    public unMute() {
        this.html5.muted = false;
    }

    /**
     * Get track that is currently cued for playback.
     */
    public getCuedTrack(): Track {
        return this.cuedTrack;
    }

    /**
     * Check if youtube player is ready.
     */
    public ready() {
        return this.bootstrapped;
    }

    /**
     * Fetch youtube ID for specified track if needed and cue it in youtube player.
     */
    public async cueTrack(track?: Track): Promise<any> {
        // if (this.cueing === track || this.cuedTrack === track) return;

        // this.cueing = track;

        // this.state.buffering = true;

        this.bootstrap();
        if (! track) return;

        if (this.cueing === track || this.cuedTrack === track) return;

        this.cueing = track;

        this.state.buffering = true;
        
        this.html5.src = track.url;
        this.html5.poster = this.wpImages.getTrackImage(track);
        this.cuedTrack = track;
        this.cueing = null;

        // if(this.state.isStubEnable()){
        //     this.bootstrapStub(track);
        // }
        // if(this.state.isStubEnable()){
        //     this.imgBackground.src = this.wpImages.getTrackImage(track);
        // }
        
        return new Promise(resolve => resolve());
    }

    /**
     * Destroy html5 playback strategy.
     */
    public destroy() {
        if(this.state.isStubEnable()){
            this.bootstrapStubDestroy();        
        }
        // if(this.state.isStubEnable()){
        //     this.imgBackground && this.imgBackground.remove();
        //     this.imgBackground = null;
        // }

        this.html5 && this.html5.remove();
        this.html5 = null;
        this.bootstrapped = false;
        this.cuedTrack = null;
    }

    public bootstrapStub(track){        
        
        if(this.imgBackgroundParent){
            this.imgBackground && this.imgBackground.remove();
            this.imgBackground = null;
            this.imgBackgroundParent && this.imgBackgroundParent.remove();
            this.imgBackgroundParent = null;
        }

        this.imgBackgroundParent = document.createElement('div');
        this.imgBackgroundParent.classList.add('player-container');
        this.imgBackgroundParent.classList.add('container-album-img-stub');

        this.imgBackground = document.createElement('img');
        this.imgBackground.classList.add('album-img-stub');
        this.imgBackground.src = this.wpImages.getTrackImage(track);

        let fixOverlay = document.createElement('div');
        fixOverlay.classList.add('album-img-stub-fix');
        this.imgBackgroundParent.appendChild(fixOverlay);

        this.imgBackgroundParent.appendChild(this.imgBackground);

        let sp2 = document.querySelector('.ads-html5-player');            
        let parentDiv = sp2.parentNode;
        parentDiv.insertBefore(this.imgBackgroundParent, sp2);       
        
        document.querySelector('.ads-html5-player').classList.add('hide-video-enable');
    }

    public bootstrapStubDestroy(){
        
            document.querySelector('.ads-html5-player').classList.remove('hide-video-enable');
            
            this.imgBackground && this.imgBackground.remove();
            this.imgBackground = null;
            this.imgBackgroundParent && this.imgBackgroundParent.remove();
            this.imgBackgroundParent = null;
    }

    /**
     * Bootstrap html5 player.
     */
    private bootstrap() {
        if (this.bootstrapped) return;

        this.html5 = document.createElement('video');
        this.html5.setAttribute('playsinline', 'true');
        this.html5.id = 'ads-html5-player';
        document.querySelector('.ads-html5-player').appendChild(this.html5);
        
        // if(this.state.isStubEnable()){
        //     this.imgBackground = document.createElement('img');
        //     this.imgBackground.classList.add('album-img-stub');
        //     document.querySelector('.ads-html5-player').appendChild(this.imgBackground);
        // }

        this.handlePlayerReadyEvent();
        this.handlePlayerStateChangeEvents();

        this.bootstrapped = true;
    }

    /**
     * Handle html5 playback state change events.
     */
    private handlePlayerStateChangeEvents() {
        this.html5.addEventListener('ended', () => {
            this.state.firePlaybackEnded();
            this.setState('playing', false);
        });

        this.html5.addEventListener('playing', (e) => {
            this.setState('playing', true);
        });

        this.html5.addEventListener('pause', () => {
            this.setState('playing', false);
        });

        this.html5.addEventListener('error', () => {
            this.cuedTrack = null;
            this.setState('playing', false);
            this.state.firePlaybackEnded();
        });
    }

    /**
     * Set specified player state.
     */
    private setState(name: string, value: boolean) {
        this.zone.run(() => this.state[name] = value);
    }

    /**
     * Handle html5 player ready event.
     */
    private handlePlayerReadyEvent(resolve?) {
        if (this.state.muted) this.mute();
        this.bootstrapped = true;
        resolve && resolve();
        this.state.fireReadyEvent();

        if (this.pendingVolume) {
            this.setVolume(this.pendingVolume);
            this.pendingVolume = null;
        }
    }
}