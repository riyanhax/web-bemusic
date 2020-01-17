import {Component, Inject, Optional, ViewEncapsulation, OnDestroy, OnInit, NgZone, AfterViewInit, ElementRef, HostListener, ViewChild} from '@angular/core';
import {Settings} from "vebto-client/core/config/settings.service";
import {Albums} from "../../../web-player/albums/albums.service";
import {Album} from "../../../models/Album";
import {Artist} from "../../../models/Artist";
import {Modal} from "vebto-client/core/ui/modal.service";

import {ActivatedRoute} from "@angular/router";
import {WebPlayerUrls} from "../../web-player-urls.service";
import {FormattedDuration} from "../../player/formatted-duration.service";
import {Player} from "../../player/player.service";
import {AlbumContextMenuComponent} from "../album-context-menu/album-context-menu.component";
import {WpUtils} from "../../web-player-utils";
import {ContextMenu} from 'vebto-client/core/ui/context-menu/context-menu.service';

import {WebPlayerState} from "../../web-player-state.service";

import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";

import { NavigationStart, Router } from "@angular/router";
import { Subscription, Observable } from "rxjs";
import { filter } from 'rxjs/operators';

import {OverlayContainer} from '@angular/cdk/overlay';
import {BrowserEvents} from "vebto-client/core/services/browser-events.service";

export interface HomeAlbumModalData {
    album?: Album
}

@Component({
    selector: 'home-album-modal',
    templateUrl: './home-album-modal.component.html',
    styleUrls: ['./home-album-modal.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class HomeAlbumModalComponent  implements OnInit, OnDestroy {

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
     * Album model.
     */
    public album = new Album({tracks: []});

    /**
     * Artist new album should be attached to.
     */
    public artist: Artist;

     /**
     * Total duration of all album tracks.
     */
    public totalDuration: string;

    /**
     * Active service subscriptions.
     */
    protected subscriptions: Subscription[] = [];

    public panCheck:boolean = true;


    /**
     * HomeAlbumModalComponent Constructor.
     */
    constructor(
        private dialogRef: MatDialogRef<HomeAlbumModalComponent>,        
        @Optional() @Inject(MAT_DIALOG_DATA) public dataHome: HomeAlbumModalData,
        private settings: Settings,
        private modal: Modal,
        private albums: Albums,

        private route: ActivatedRoute,
        public urls: WebPlayerUrls,
        private duration: FormattedDuration,
        private player: Player,
        private contextMenu: ContextMenu,
        private state: WebPlayerState,

        private router: Router,
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

        this.totalDuration = '';
        this.bindToRouterEvents();
    }

    ngOnDestroy() {

        this.overlayContainer.getContainerElement().classList.remove('context-menu-show-inModal');

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

    private hydrate(dataHome: HomeAlbumModalData) {
        if (dataHome.album) this.album = Object.assign({}, dataHome.album);        
        if(!this.album.id) this.close(this.album);
        
        this.state.loading = true;
        const id = this.album.id;

        this.albums.get(id).toPromise().then(album => {
            this.state.loading = false;
            if (album) {                
                this.setAlbum(album);
                
                const total = this.album.tracks.reduce((total, track2) => total + track2.duration, 0);        
                this.totalDuration = this.duration.toVerboseString(total); 
                
                this.state.isModalOpen = true;
                this.state.typeModalPage = 'album';
                
            } else {
                this.close(this.album);
            }
        }).catch(() => {
            this.state.loading = false;
            this.close(this.album);
        }) as any;

    }

    public close(album?: Album) {        
        this.state.isModalOpen = false;
        this.state.typeModalPage = '';

        const overlayEl = this.dialogRef['_overlayRef'].overlayElement;        
        overlayEl.parentElement.classList.remove('home-modal-showed')

        setTimeout(() => {                        
            this.dialogRef.close(album);
        }, 301);  
    }

    /**
     * Check if album is playing currently.
     */
    public playing(): boolean {
        return this.player.state.playing && this.player.queue.getQueuedItem() === this.album.id;
    }

    /**
     * Play all album tracks.
     */
    public async play() {
        this.player.stop();
        this.player.state.buffering = true;

        this.player.overrideQueue({
            tracks: this.album.tracks,
            queuedItemId: this.album.id
        }).then(() => {
            this.player.play();
        });
    }

    /**
     * Pause the album.
     */
    public pause() {
        this.player.pause();
    }

    /**
     * Open album context menu.
     */
    public openContextMenu(e: MouseEvent) {
        e.stopPropagation();

        this.contextMenu.open(
            AlbumContextMenuComponent,
            e.target,
            {data: {item: this.album, type: 'album'}},
        );
    }

    /**
     * Set album object on each album track.
     */
    private setAlbum(album: Album) {
        const simplifiedAlbum = Object.assign({}, album, {tracks: []});
        album.tracks = WpUtils.assignAlbumToTracks(album.tracks, simplifiedAlbum);
        this.album = album;
    }

}
