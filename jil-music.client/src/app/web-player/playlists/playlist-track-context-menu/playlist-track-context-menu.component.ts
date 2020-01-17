import {Component, Injector, ViewEncapsulation,  NgZone, ElementRef} from '@angular/core';
import {TrackContextMenuComponent} from "../../tracks/track-context-menu/track-context-menu.component";
import {Player} from "../../player/player.service";
import {UserLibrary} from "../../users/user-library/user-library.service";
import {PlaylistService} from "../playlist.service";
import {Playlists} from "../playlists.service";
import {Lyrics} from "../../lyrics/lyrics.service";

import {OverlayContainer} from '@angular/cdk/overlay';

import {Modal} from "vebto-client/core/ui/modal.service";
import {ConfirmModalComponent} from "vebto-client/core/ui/confirm-modal/confirm-modal.component";

import {VoucherModalComponent} from "vebto-client/subscription/voucher-modal/voucher-modal.component";

@Component({
    selector: 'playlist-track-context-menu',
    templateUrl: './playlist-track-context-menu.component.html',
    encapsulation: ViewEncapsulation.None,
    host: {'class': 'context-menu'},
})
export class PlaylistTrackContextMenuComponent extends TrackContextMenuComponent {

    /**
     * PlaylistTrackContextMenuComponent Constructor.
     */
    constructor(
        protected player: Player,
        protected userLibrary: UserLibrary,
        protected injector: Injector,
        protected playlist: PlaylistService,
        protected playlists: Playlists,
        protected lyrics: Lyrics,
        protected overlayContainer: OverlayContainer,  
        
        protected ngZone: NgZone,   
        protected el: ElementRef,

        protected modal: Modal,
    ) {
        super(player, userLibrary, injector, lyrics, overlayContainer, ngZone, el, modal);
    }

    /**
     * Remove track from currently active playlist.
     */
    public removeFromPlaylist() {
        this.playlists.removeTracks(this.data.playlistId, this.getTracks()).subscribe();
        this.contextMenu.close();
    }
}
