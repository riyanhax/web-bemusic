import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {AdminModule} from 'vebto-client/admin/admin.module';
import {AppAdminRoutingModule} from './app-admin-routing.module';
import {ArtistsComponent} from './artists/artists.component';
import {NewArtistPageComponent} from './artists/new-artist-page/new-artist-page.component';
import {ArtistAlbumsTableComponent} from './artists/new-artist-page/artist-albums-table/artist-albums-table.component';
import {CrupdateAlbumModalComponent} from './albums/crupdate-album-modal/crupdate-album-modal.component';
import {CrupdateLyricModalComponent} from './lyrics-page/crupdate-lyric-modal/crupdate-lyric-modal.component';
import {NewTrackModalComponent} from './tracks/new-track-modal/new-track-modal.component';
import {TracksPageComponent} from './tracks/tracks-page/tracks-page.component';
import {NewVideoadModalComponent} from './video_ads/new-videoad-modal/new-videoad-modal.component';
import {VideoAdsPageComponent} from './video_ads/video-ads-page/video-ads-page.component';
import {SubscriptionsTypesComponent} from './subscriptions_types/subscription_types-page/subscriptions_types.component';
import {NewSubscriptionsTypeModalComponent} from './subscriptions_types/new-subscriptions_type-modal/new-subscriptions_type-modal.component';
import {VouchersPricesModalComponent} from './subscriptions_types/vouchers-prices-modal/vouchers-prices-modal.component';
import {AlbumsPageComponent} from './albums/albums-page/albums-page.component';
import {LyricsPageComponent} from './lyrics-page/lyrics-page.component';
import {AlbumTracksTableComponent} from './albums/crupdate-album-modal/album-tracks-table/album-tracks-table.component';
import {MatAutocompleteModule, MatChipsModule} from '@angular/material';
import {PlaylistsPageComponent} from './playlists-page/playlists-page.component';
import {ProvidersSettingsComponent} from './settings/providers/providers-settings.component';
import {GenresSettingsComponent} from './settings/genres/genres-settings.component';
import {PlayerSettingsComponent} from './settings/player/player-settings.component';
import {BlockedArtistsSettingsComponent} from './settings/blocked-artists/blocked-artists-settings.component';

import {RolesArtistsSettingsComponent} from './settings/roles-artists/roles-artists-settings.component';
import {HomeAlbumsSettingsComponent} from './settings/home-albums/home-albums-settings.component';
import {HomeTracksSettingsComponent} from './settings/home-tracks/home-tracks-settings.component';

import { GenresComponent } from './genres/genres.component';
import { CrupdateGenreModalComponent } from './genres/crupdate-genre-modal/crupdate-genre-modal.component';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        AppAdminRoutingModule,
        AdminModule,

        // material
        MatChipsModule,
        MatAutocompleteModule,
    ],
    declarations: [
        ArtistsComponent,
        NewArtistPageComponent,
        ArtistAlbumsTableComponent,
        CrupdateAlbumModalComponent,
        CrupdateLyricModalComponent,
        NewTrackModalComponent,
        TracksPageComponent,
        NewVideoadModalComponent,
        VideoAdsPageComponent,
        NewSubscriptionsTypeModalComponent,
        VouchersPricesModalComponent,
        AlbumsPageComponent,
        LyricsPageComponent,
        AlbumTracksTableComponent,
        PlaylistsPageComponent,
        SubscriptionsTypesComponent,

        //settings
        ProvidersSettingsComponent,
        GenresSettingsComponent,
        PlayerSettingsComponent,
        BlockedArtistsSettingsComponent,
        RolesArtistsSettingsComponent,
        HomeAlbumsSettingsComponent,
        HomeTracksSettingsComponent,
        GenresComponent,
        CrupdateGenreModalComponent,
    ],
    entryComponents: [
        CrupdateAlbumModalComponent,
        CrupdateLyricModalComponent,
        NewTrackModalComponent,
        NewVideoadModalComponent,
        NewSubscriptionsTypeModalComponent,
        VouchersPricesModalComponent,
        CrupdateGenreModalComponent
    ]
})
export class AppAdminModule {
}
