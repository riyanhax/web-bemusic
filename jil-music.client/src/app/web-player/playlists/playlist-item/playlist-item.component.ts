import {Component, Input, OnDestroy, ViewEncapsulation} from '@angular/core';
import {WebPlayerUrls} from "../../web-player-urls.service";
import {Player} from "../../player/player.service";
import {Playlist} from "../../../models/Playlist";
import {PlaylistService} from "../playlist.service";
import {User} from "vebto-client/core/types/models/User";
import {WebPlayerImagesService} from "../../web-player-images.service";

import {Modal} from "vebto-client/core/ui/modal.service";
import {HomePlaylistModalComponent} from "../home-playlist-modal/home-playlist-modal.component";

@Component({
    selector: 'playlist-item',
    templateUrl: './playlist-item.component.html',
    styleUrls: ['./playlist-item.component.scss'],
    encapsulation: ViewEncapsulation.None,
    host: {'class': 'media-grid-item', '[class.active]': 'playing()'},
})
export class PlaylistItemComponent implements OnDestroy {
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

    /**
     * PlaylistItemComponent Constructor
     */
    constructor(
        public urls: WebPlayerUrls,
        private player: Player,
        private playlistService: PlaylistService,
        public wpImages: WebPlayerImagesService,
        private modal: Modal,
    ) {}

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
