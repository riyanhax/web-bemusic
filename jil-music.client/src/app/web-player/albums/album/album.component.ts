import {Component, OnInit, ViewEncapsulation, HostListener, ViewChild, ElementRef} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {Album} from "../../../models/Album";
import {WebPlayerUrls} from "../../web-player-urls.service";
import {FormattedDuration} from "../../player/formatted-duration.service";
import {Player} from "../../player/player.service";
import {AlbumContextMenuComponent} from "../album-context-menu/album-context-menu.component";
import {WpUtils} from "../../web-player-utils";
import {ContextMenu} from 'vebto-client/core/ui/context-menu/context-menu.service';

import {WebPlayerState} from "../../web-player-state.service";
import { Location } from '@angular/common';

@Component({
    selector: 'album',
    templateUrl: './album.component.html',
    styleUrls: ['./album.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class AlbumComponent implements OnInit {

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

    /**
     * Album to be displayed.
     */
    public album: Album;

    /**
     * Total duration of all album tracks.
     */
    public totalDuration: string;

    /**
     * AlbumComponent Constructor.
     */
    constructor(
        private route: ActivatedRoute,
        public urls: WebPlayerUrls,
        private duration: FormattedDuration,
        private player: Player,
        private contextMenu: ContextMenu,
        private location: Location,
        public state: WebPlayerState,

        protected el: ElementRef,
    ) {}

    ngOnInit() {
        this.route.data.subscribe((data: {album: Album}) => {
            this.setAlbum(data.album);
            const total = this.album.tracks.reduce((total, track2) => total + track2.duration, 0);
            // this.totalDuration = this.duration.fromMilliseconds(total);
            this.totalDuration = this.duration.toVerboseString(total);            
        });
    }

    goBack() {
        this.location.back(); // go back to previous location
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
