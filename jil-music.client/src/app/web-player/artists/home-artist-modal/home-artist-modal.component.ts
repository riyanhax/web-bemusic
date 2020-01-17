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
import {AlbumContextMenuComponent} from "../../albums/album-context-menu/album-context-menu.component";
import {ArtistContextMenuComponent} from "../artist-context-menu/artist-context-menu.component";
import {WpUtils} from "../../web-player-utils";
import {ContextMenu} from 'vebto-client/core/ui/context-menu/context-menu.service';

import {WebPlayerState} from "../../web-player-state.service";

import {MAT_DIALOG_DATA, MatDialogRef, MatDialog} from "@angular/material";

import { NavigationStart, NavigationEnd, Router } from "@angular/router";
import { Subscription, Observable } from "rxjs";
import { filter } from 'rxjs/operators';

import {ArtistService} from "../artist.service";
import {Artists} from "../artists.service";
import {WebPlayerImagesService} from "../../web-player-images.service";
import {Track} from "../../../models/Track";

import {OverlayContainer} from '@angular/cdk/overlay';
import {BrowserEvents} from "vebto-client/core/services/browser-events.service";

export interface HomeArtistModalData {
    artist?: Artist
}

@Component({
    selector: 'home-artist-modal',
    templateUrl: './home-artist-modal.component.html',
    styleUrls: ['./home-artist-modal.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class HomeArtistModalComponent implements OnInit, OnDestroy {

    @ViewChild('scrollContainer') private scrollContainer: ElementRef;
    @ViewChild('scrollImgSmall') private scrollImgSmall: ElementRef;
    // public scrollContainerScrolled:boolean = false;
    public scrollHeaderScrolled:boolean = false;
    @HostListener('scroll', ['$event'])
        onScroll(event) {
            const scrollPosition = this.scrollContainer.nativeElement.scrollTop;
            if(scrollPosition < 250){                                
                this.scrollImgSmall.nativeElement.style.height = (150-scrollPosition/1.8)+'px';
                this.scrollImgSmall.nativeElement.style.width = (150-scrollPosition/1.8) +'px';
            }


            if(scrollPosition > 220 && !this.scrollHeaderScrolled){
                this.scrollHeaderScrolled = true;
                this.scrollContainer.nativeElement.classList.add('header-static'); 
                this.scrollContainer.nativeElement.classList.add('btn-play-static');                               
            }else if(scrollPosition < 220 && this.scrollHeaderScrolled){
                this.scrollHeaderScrolled = false;
                this.scrollContainer.nativeElement.classList.remove('header-static');
                this.scrollContainer.nativeElement.classList.remove('btn-play-static');
            }

            // if(scrollPosition > 350 && !this.scrollContainerScrolled){
            //     this.scrollContainerScrolled = true;
            //     this.scrollContainer.nativeElement.classList.add('btn-play-static');                
            // }else if(scrollPosition < 350 && this.scrollContainerScrolled){
            //     this.scrollContainerScrolled = false;
            //     this.scrollContainer.nativeElement.classList.remove('btn-play-static');
            // }
        }
        
    /**
     * Active component subscriptions.
     */
    private subscriptions: Subscription[] = [];

    private subscriptionsModal: Subscription[] = [];

    /**
     * Current layout of artist albums in the view.
     */
    public albumsLayout = 'list';

    /**
     * Whether albums layout toggle button should be visible.
     */
    public albumsLayoutShouldBeToggleable = true;

    /**
     * Currently active tab.
     */
    public activeTab = 'overview';

    /**
     * Artist service instance.
     */
    public artist: ArtistService;
    public artistInput: Artist;

    /**
     * number of popular tracks that should be displayed
     */
    public popularTracksCount = 5;

    public isDataArtist = false;

    public panCheck:boolean = true;

    /**
     * HomeAlbumModalComponent Constructor.
     */
    constructor(

        private dialogRefAll: MatDialog,

        private dialogRef: MatDialogRef<HomeArtistModalComponent>,        
        @Optional() @Inject(MAT_DIALOG_DATA) public dataHome: HomeArtistModalData,
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
        public wpImages: WebPlayerImagesService,
        private artists: Artists,
        private overlayContainer: OverlayContainer,

        private ngZone: NgZone,  
        protected el: ElementRef,
        private browserEvents: BrowserEvents,
    ) {
        this.dialogRefAll.closeAll();

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
     * Destroy fullscreen overlay service.
     */
    public destroyModal() {
        this.subscriptionsModal.forEach(subscription => {
            subscription.unsubscribe();
        });

        this.subscriptions.forEach(subscription => {
            subscription.unsubscribe();
        });
        
        this.artist.destroy();
        this.subscriptions = [];
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

    private hydrate(dataHome: HomeArtistModalData) {

        this.isDataArtist = false;

        if (dataHome.artist) this.artistInput = dataHome.artist;
        if(!this.artistInput.id) this.close(this.artistInput);

        let params = {'with': ['similar', 'genres'], top_tracks: true, validate: true}, id;
        id = this.artistInput.id;        
        const artist = new ArtistService(this.artists, this.player);        

        artist.init(id, params).then(() => {
            this.state.loading = false; 
            this.artist = artist;
            this.isDataArtist = true;
            this.popularTracksCount = 5;  
            
            this.state.isModalOpen = true;
            this.state.typeModalPage = 'artist';  
        }).catch(() => {
            this.state.loading = false;
            this.close(this.artistInput);
        }) as any;
         
        this.setActiveTab('');
        this.bindToRouterEvents();
        this.setAlbumsLayout();
    }

    public close(artist?: Artist) {        
        this.state.isModalOpen = false;
        this.state.typeModalPage = '';

        const overlayEl = this.dialogRef['_overlayRef'].overlayElement;        
        overlayEl.parentElement.classList.remove('home-modal-showed');

        setTimeout(() => {                        
            this.dialogRef.close(artist);
        }, 301);        
    }

/**
     * Play all artist tracks from specified track.
     */
    public async playFrom(track: Track) {
        let tracks = this.artist.getTracks(),
            index  = tracks.findIndex(curr => curr === track);

        this.player.handleContinuousPlayback = false;

        await this.player.overrideQueue({tracks: tracks.slice(index), queuedItemId: this.artist.get().id});

        this.player.play();
    }

    /**
     * Toggle number of popular tracks that
     * should be displayed between 5 and 20.
     */
    public togglePopularTracksCount() {
        this.popularTracksCount = this.popularTracksCount === 5 ? 20 : 5;
    }

    /**
     * Show context menu for specified track.
     */
    public showAlbumContextMenu(album: Album, e: MouseEvent) {
        e.stopPropagation();
        this.contextMenu.open(
            AlbumContextMenuComponent,
            e.target,
            {overlayY: 'center', data: {item: album, type: 'album'}}
            );
    }

    /**
     * Open artist context menu.
     */
    public showArtistContextMenu(e: MouseEvent) {
        e.stopPropagation();

        this.contextMenu.open(
            ArtistContextMenuComponent,
            e.target,
            {data: {item: this.artist.get(), type: 'artist'}, originX: 'center', overlayX: 'center'}
        );
    }

    /**
     * Toggle albums layout between grid and list.
     */
    public toggleAlbumsLayout() {
        if (this.albumsLayout === 'grid') {
            this.albumsLayout = 'list'
        } else {
            this.albumsLayout = 'grid';
        }
    }

    /**
     * Bind to router state change events.
     */
    private bindToRouterEvents() {
        const sub = this.router.events
            .pipe(filter(event => event instanceof NavigationEnd))
            .subscribe((event: NavigationEnd) => {
                this.setActiveTab(event.url);
            });

        this.subscriptions.push(sub);
    }

    public changeTabContent(tab: string) {        
        this.setActiveTab(tab);
    }

    /**
     * Set currently active tab based on specified url.
     */
    private setActiveTab(tab: string) {
        
        switch (tab) {
            case 'about':
                this.activeTab = 'about';
                break;
            case 'similar':
                this.activeTab = 'similar';
                break;
            default:
                this.activeTab = 'overview';
        }
    }

    /**
     * Set albums layout based on current artist provider.
     */
    private setAlbumsLayout() {
        if (this.settings.get('artist_provider') === 'Discogs') {
            this.albumsLayout = 'grid';
            this.albumsLayoutShouldBeToggleable = false;
        } else {
            this.albumsLayout = this.state.isMobile ? 'grid' : 'list';
            this.albumsLayoutShouldBeToggleable = true;
        }
    }


}
