import {Component, Inject, Optional, OnDestroy, OnInit, ViewEncapsulation, NgZone, AfterViewInit, ElementRef, HostListener, ViewChild} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {WebPlayerUrls} from "../../web-player-urls.service";
import {FormattedDuration} from "../../player/formatted-duration.service";
import {PlaylistService} from "../playlist.service";
import {PlaylistContextMenuComponent} from "../playlist-context-menu/playlist-context-menu.component";
import {Playlists} from "../playlists.service";
import {Track} from "../../../models/Track";
import {ContextMenu} from 'vebto-client/core/ui/context-menu/context-menu.service';
import {Playlist} from "../../../models/Playlist";
import {Modal} from "vebto-client/core/ui/modal.service";
import {WebPlayerState} from "../../web-player-state.service";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import { NavigationStart, Router } from "@angular/router";
import { Subscription, Observable } from "rxjs";
import { filter } from 'rxjs/operators';

import {Player} from "../../player/player.service";
import {Settings} from "vebto-client/core/config/settings.service";
import {WebPlayerImagesService} from '../../web-player-images.service';

import {OverlayContainer} from '@angular/cdk/overlay';
import {BrowserEvents} from "vebto-client/core/services/browser-events.service";

export interface HomePlaylistModalData {
    playlist?: Playlist
}

@Component({
    selector: 'home-playlist-modal',
    templateUrl: './home-playlist-modal.component.html',
    styleUrls: ['./home-playlist-modal.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class HomePlaylistModalComponent implements OnInit, OnDestroy {
    @ViewChild('scrollContainer') private scrollContainer: ElementRef;
    @ViewChild('scrollImgSmall') private scrollImgSmall: ElementRef;
    public scrollContainerScrolled:boolean = false;
    public scrollHeaderScrolled:boolean = false;
    @HostListener('scroll', ['$event'])
        onScroll(event) {            
            const scrollPosition = this.scrollContainer.nativeElement.scrollTop;
            if(scrollPosition < 250){                                
                this.scrollImgSmall.nativeElement.style.height = (150-scrollPosition/1.8)+'px';
                this.scrollImgSmall.nativeElement.style.width = (150-scrollPosition/1.8) +'px';
            }

            if(scrollPosition > 130 && !this.scrollHeaderScrolled){
                this.scrollHeaderScrolled = true;
                this.scrollContainer.nativeElement.classList.add('header-static');                
                // this.scrollContainer.nativeElement.classList.add('btn-play-static');                
            }else if(scrollPosition < 130 && this.scrollHeaderScrolled){
                this.scrollHeaderScrolled = false;
                this.scrollContainer.nativeElement.classList.remove('header-static');
                // this.scrollContainer.nativeElement.classList.remove('btn-play-static');
            }

            if(scrollPosition > 220 && !this.scrollContainerScrolled){
                this.scrollContainerScrolled = true;
                this.scrollContainer.nativeElement.classList.add('btn-play-static');                
            }else if(scrollPosition < 220 && this.scrollContainerScrolled){
                this.scrollContainerScrolled = false;
                this.scrollContainer.nativeElement.classList.remove('btn-play-static');
            }
        }
        
    /**
     * Active service subscriptions.
     */
    private subscriptionsModal: Subscription[] = [];

    /*
     * Playlist model.
     */
    public playlist: PlaylistService;
    public playlistInput: Playlist;    

    /**
     * Formatted duration of playlist.
     */
    public totalDuration: string;

    public isDataPlaylist = false;

    public panCheck:boolean = true;

    /**
     * HomeAlbumModalComponent Constructor.
     */
    constructor(
        private dialogRef: MatDialogRef<HomePlaylistModalComponent>,        
        @Optional() @Inject(MAT_DIALOG_DATA) public dataHome: HomePlaylistModalData,

        private route: ActivatedRoute,
        public urls: WebPlayerUrls,
        private duration: FormattedDuration,
        private contextMenu: ContextMenu,
        private playlists: Playlists,
        public state: WebPlayerState,
        private modal: Modal,
        private router: Router,

        private player: Player,
        private settings: Settings,        
        public images: WebPlayerImagesService,

        private overlayContainer: OverlayContainer,

        private ngZone: NgZone,  
        protected el: ElementRef,
        private browserEvents: BrowserEvents,
    ) {
        this.hydrate(dataHome);
    }

    ngOnInit() {
        this.panCheck = true;

        const overlayEl = this.dialogRef['_overlayRef'].overlayElement;
        overlayEl.parentElement.classList.add('dialog-wrapper-Home');

        this.overlayContainer.getContainerElement().classList.add('context-menu-show-inModal');

        setTimeout(() => {                        
            overlayEl.parentElement.classList.add('home-modal-showed')
        }, 201);
        
        this.bindToRouterEventsModal();
    }

    ngOnDestroy() {
        this.overlayContainer.getContainerElement().classList.remove('context-menu-show-inModal');

        this.destroyModal();
        this.subscriptionsModal = null;
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
    /**
     * Destroy fullscreen overlay service.
     */
    public destroyModal() {
        this.subscriptionsModal.forEach(subscription => {
            subscription.unsubscribe();
        });

        this.playlist.destroy();
    }

    /**
     * Minimize fullscreen overlay when navigation occurs.
     */
    private bindToRouterEventsModal() {
        const sub = this.router.events.pipe(
            filter(e => e instanceof NavigationStart))
            .subscribe(() => this.close());

        this.subscriptionsModal.push(sub);

        const subModal = this.browserEvents.globalKeyDown$.subscribe(e => {
            //close on ESC key press.
            if (e.keyCode === this.browserEvents.keyCodes.escape) {                
                this.close();
            }
        });

        this.subscriptionsModal.push(subModal);
    }

    private hydrate(dataHome: HomePlaylistModalData) {
        this.isDataPlaylist = false;

        if (dataHome.playlist) this.playlistInput = dataHome.playlist;
        if(!this.playlistInput.id) this.close(this.playlistInput);

        const id = this.playlistInput.id;
        const playlist = new PlaylistService(this.playlists, this.player, this.settings, this.images);

        playlist.init(id).then(() => {
            this.state.loading = false;
            this.playlist = playlist;
            this.playlist.bindToPlaylistEvents();
            this.totalDuration = this.duration.toVerboseString(this.playlist.totalDuration);

            this.isDataPlaylist = true;
            this.state.isModalOpen = true;
            this.state.typeModalPage = 'playlist';  
        }).catch(() => {
            this.state.loading = false;
            this.close(this.playlistInput);
        }) as any;

    }

    public close(playlist?: Playlist) {        
        this.state.isModalOpen = false;
        this.state.typeModalPage = '';

        const overlayEl = this.dialogRef['_overlayRef'].overlayElement;        
        overlayEl.parentElement.classList.remove('home-modal-showed');

        setTimeout(() => {                        
            this.dialogRef.close(playlist);
        }, 301);  
        
    }

    /**
     * Remove track from currently active playlist.
     */
    public removeTracksFromPlaylist(tracks: Track[]) {
        this.playlists.removeTracks(this.playlist.get().id, tracks).subscribe();
    }

    /**
     * Open playlist context menu.
     */
    public openContextMenu(e: MouseEvent) {
        e.stopPropagation();

        this.contextMenu.open(
            PlaylistContextMenuComponent,
            e.target,
            {originX: 'center', overlayX: 'center', data: {item: this.playlist.get(), extra: {image: this.playlist.getImage()}, type: 'playlist'}}
        );
    }


}
