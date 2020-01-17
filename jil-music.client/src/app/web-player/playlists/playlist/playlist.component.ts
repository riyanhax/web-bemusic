import {Component, OnDestroy, OnInit, ViewEncapsulation, ElementRef, HostListener, ViewChild} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {WebPlayerUrls} from "../../web-player-urls.service";
import {FormattedDuration} from "../../player/formatted-duration.service";
import {PlaylistService} from "../playlist.service";
import {PlaylistContextMenuComponent} from "../playlist-context-menu/playlist-context-menu.component";
import {Playlists} from "../playlists.service";
import {Track} from "../../../models/Track";
import {ContextMenu} from 'vebto-client/core/ui/context-menu/context-menu.service';

import {WebPlayerState} from "../../web-player-state.service";
import { Location } from '@angular/common';

@Component({
    selector: 'playlist',
    templateUrl: './playlist.component.html',
    styleUrls: ['./playlist.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class PlaylistComponent implements OnInit, OnDestroy {

    @ViewChild('scrollContainer') private scrollContainer: ElementRef;
    @ViewChild('scrollImgSmall') private scrollImgSmall: ElementRef;
    public scrollContainerScrolled:boolean = false;
    public scrollHeaderScrolled:boolean = false;
    @HostListener('scroll', ['$event'])
        onScroll(event) {    
            if(this.state.isMobile){        
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
        }
        
    /*
     * Playlist model.
     */
    public playlist: PlaylistService;

    /**
     * Formatted duration of playlist.
     */
    public totalDuration: string;

    /**
     * PlaylistComponent Constructor.
     */
    constructor(
        private route: ActivatedRoute,
        public urls: WebPlayerUrls,
        private duration: FormattedDuration,
        private contextMenu: ContextMenu,
        private playlists: Playlists,
        private location: Location,
        public state: WebPlayerState,

        protected el: ElementRef,
    ) {}

    ngOnInit() {
        this.bindToRouterData();
    }

    ngOnDestroy() {
        this.playlist.destroy();
    }

    goBack() {
        this.location.back(); // go back to previous location
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

    /**
     * Set component playlist from resolver.
     */
    private bindToRouterData() {
        this.route.data.subscribe(data => {
            this.playlist = data.playlist;
            this.playlist.bindToPlaylistEvents();
            this.totalDuration = this.duration.toVerboseString(this.playlist.totalDuration);
        });
    }
}
