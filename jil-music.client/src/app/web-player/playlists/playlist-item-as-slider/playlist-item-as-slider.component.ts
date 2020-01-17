import {Component, Input, OnDestroy, OnInit, ViewEncapsulation} from '@angular/core';
import {WebPlayerUrls} from "../../web-player-urls.service";
import {Player} from "../../player/player.service";
import {Playlist} from "../../../models/Playlist";
import {PlaylistService} from "../playlist.service";
import {User} from "vebto-client/core/types/models/User";
import {WebPlayerImagesService} from "../../web-player-images.service";

import {Modal} from "vebto-client/core/ui/modal.service";
import {HomePlaylistModalComponent} from "../home-playlist-modal/home-playlist-modal.component";
import {Settings} from "vebto-client/core/config/settings.service";

import {Track} from "../../../models/Track";
import { SlickModule } from 'ngx-slick';

@Component({
    selector: 'playlist-item-as-slider',
    templateUrl: './playlist-item-as-slider.component.html',
    styleUrls: ['./playlist-item-as-slider.component.scss'],
    encapsulation: ViewEncapsulation.None,
    host: {'class': 'media-grid-item', '[class.active]': 'playing()'},
})
export class PlaylistItemAsSliderComponent implements OnDestroy, OnInit {
    @Input() scrollContainer: HTMLElement;

    /**
     * Playlist model.
     */
    @Input() playlist: Playlist;

    /**
     * Creator of playlist.
     */
    @Input() creator: User;

    /**
     * Show tarack count or Creator of playlist.
     */
    @Input() showSongCount: boolean = false;

    @Input() inModal: boolean = false;

    public imgFol = this.settings.getAssetUrl() + 'icons/heart.svg';

    public slideConfig = {"slidesToShow": 3, "slidesToScroll": 3};
    ngxHmCarouselTrack = {
        index : 1,
        autoplay : false,
        infinite : true,
        direction : 'right',
        directionToggle : true,  
        tracks : <Track[]>[],
        showNum: 3,
    };

    public selectedTrackGroup: number;
    
    /**
     * PlaylistItemComponent Constructor
     */
    constructor(
        public urls: WebPlayerUrls,
        private player: Player,
        private playlistService: PlaylistService,
        public wpImages: WebPlayerImagesService,
        private modal: Modal,
        private settings: Settings,
    ) { }

    ngOnInit() {
        this.selectedTrackGroup = 1;        
        this.ngxHmCarouselTrack.tracks = this.playlist.tracks as any;  
    }
    /**
     * Get playlist image.
     */
    public getImage(playlist: Playlist) {
        if (playlist.image) return playlist.image;
        if (playlist.tracks && playlist.tracks.length) return playlist.tracks[0].album.image;
        return this.wpImages.getDefault('artist');
    }

    ngOnDestroy() {
        this.playlistService.destroy();
    }

    /**
     * Open modal for single playlist page.
     */
    public openHomePlaylistModal(playlist?: Playlist) {        
        
        this.modal.open(
            HomePlaylistModalComponent,
            {playlist},
            'home-modal-container',
            'home-modal'
        )
        .afterClosed();
    }

    /**
     * Check if playlist is currently playing.
     */
    public playing() {
        this.playlistService.playing(this.playlist);
    }

    /**
     * Play all playlist's tracks.
     */
    public async play() {
        this.player.stop();
        this.player.state.buffering = true;
        await this.playlistService.init(this.playlist.id);
        this.playlistService.play();
    }

    /**
     * Pause the playlist.
     */
    public pause() {
        this.player.pause();
    }

    /**
     * Get creator of playlist.
     */
    public getCreator(): User {
        return this.creator || this.playlist.editors[0];
    }
}
