import {ChangeDetectionStrategy, Component, Input, ViewEncapsulation} from '@angular/core';
import {WebPlayerUrls} from "../../web-player-urls.service";

import {Artist} from "../../../models/Artist";
import {Artists} from "../../../web-player/artists/artists.service";

import {Modal} from "vebto-client/core/ui/modal.service";
import {HomeArtistModalComponent} from "../home-artist-modal/home-artist-modal.component";

import {FullscreenOverlay} from "../../fullscreen-overlay/fullscreen-overlay.service";

@Component({
    selector: 'artists-links-list',
    templateUrl: './artists-links-list.component.html',
    styleUrls: ['./artists-links-list.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ArtistsLinksListComponent {

    public artistData: Artist;

    /**
     * ArtistsLinksListComponent Constructor.
     */
    constructor(
        public urls: WebPlayerUrls,
        private modal: Modal,
        private artistsServise: Artists,
        public overlay: FullscreenOverlay,
    ) {}

    /**
     * Open modal for single artist page.
     */
    public openHomeArtistModal(artistName?: string) {        
         
        this.artistData = null;
        if(!artistName) return;
        this.artistsServise.getByName(artistName).toPromise().then(artist => {                                    
            if(!artist || !artist.artist) return;
            if(this.overlay.isMaximized()) this.overlay.minimize();
            this.artistData = artist.artist;                        
        }).then(() => {
            const artist = this.artistData;            
            this.modal.open(
                HomeArtistModalComponent,
                {artist},
                'home-modal-container',
                'home-modal'
            ).afterClosed();          
        }).catch(() => {
            this.artistData = null;
            return;
        }) as any;

    }

    /**
     * List of artist names to render.
     */
    @Input() artists: string[];

    @Input() inModal: boolean = false;

}
