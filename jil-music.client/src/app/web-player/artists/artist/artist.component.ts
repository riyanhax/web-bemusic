import {Component, OnDestroy, OnInit, ViewEncapsulation, HostListener, ViewChild, ElementRef} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router} from "@angular/router";
import {ArtistService} from "../artist.service";
import {WebPlayerUrls} from "../../web-player-urls.service";
import {Subscription} from "rxjs";
import {Player} from "../../player/player.service";
import {Album} from "../../../models/Album";
import {AlbumContextMenuComponent} from "../../albums/album-context-menu/album-context-menu.component";
import {ArtistContextMenuComponent} from "../artist-context-menu/artist-context-menu.component";
import {WebPlayerState} from "../../web-player-state.service";
import {Settings} from "vebto-client/core/config/settings.service";
import {Track} from "../../../models/Track";
import {filter} from "rxjs/operators";
import {WebPlayerImagesService} from "../../web-player-images.service";
import {ContextMenu} from 'vebto-client/core/ui/context-menu/context-menu.service';

import { Location } from '@angular/common';

@Component({
    selector: 'artist',
    templateUrl: './artist.component.html',
    styleUrls: ['./artist.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class ArtistComponent implements OnInit, OnDestroy {
    
    @ViewChild('scrollContainer') private scrollContainer: ElementRef;
    @ViewChild('scrollImgSmall') private scrollImgSmall: ElementRef;
    // public scrollContainerScrolled:boolean = false;
    public scrollHeaderScrolled:boolean = false;
    @HostListener('scroll', ['$event'])
        onScroll(event) {
            if(this.state.isMobile){
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
        }
    /**
     * Active component subscriptions.
     */
    private subscriptions: Subscription[] = [];

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

    /**
     * number of popular tracks that should be displayed
     */
    public popularTracksCount = 5;

    /**
     * ArtistComponent Constructor.
     */
    constructor(
        private route: ActivatedRoute,
        private router: Router,
        public urls: WebPlayerUrls,
        public player: Player,
        private contextMenu: ContextMenu,
        public state: WebPlayerState,
        public settings: Settings,
        public wpImages: WebPlayerImagesService,
        private location: Location,

        protected el: ElementRef,
    ) {}

    ngOnInit() {
        this.route.data.subscribe(data => {
            this.popularTracksCount = 5;
            this.artist = data.artist;
        });
        this.setActiveTab(this.router.url);
        this.bindToRouterEvents();
        this.setAlbumsLayout();
    }

    ngOnDestroy() {
        this.subscriptions.forEach(subscription => {
            subscription.unsubscribe();
        });
        this.artist.destroy();
        this.subscriptions = [];
    }

    goBack() {
        this.location.back(); // go back to previous location
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

    /**
     * Set currently active tab based on specified url.
     */
    private setActiveTab(url: string) {
        const tab = url.split('/').pop();

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
