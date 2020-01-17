import {NgModule} from '@angular/core';
import {NavSidebarComponent} from './nav-sidebar/nav-sidebar.component';
import {WebPlayerComponent} from "./web-player.component";
import {SearchSlideoutPanelComponent} from './search/search-slideout-panel/search-slideout-panel.component';
import {SearchSlideoutPanel} from "./search/search-slideout-panel/search-slideout-panel.service";
import {Search} from "./search/search.service";
import { PopularAlbumsComponent } from './albums/popular-albums/popular-albums.component';
import {WebPlayerRoutingModule} from "./web-player-routing.module";
import {Albums} from "./albums/albums.service";
import {WebPlayerUrls} from "./web-player-urls.service";
import {PopularGenresComponent} from "./genres/popular-genres/popular-genres.component";
import {UserGenresComponent} from "./genres/user-genres/user-genres.component";
import {Genres} from "./genres/genres.service";
import { FilterablePageHeaderComponent } from './filterable-page/filterable-page-header/filterable-page-header.component';
import {Player} from "./player/player.service";
import {AdsPlayer} from "./ads-player/ads-player.service";
import { PlayerControlsComponent } from './player/player-controls/player-controls.component';
import { AdsPlayerControlsComponent } from './ads-player/ads-player-controls/ads-player-controls.component';
import {PlayerQueue} from "./player/player-queue.service";
import {AdsPlayerQueue} from "./ads-player/ads-player-queue.service";
import { AlbumComponent } from './albums/album/album.component';
import {AlbumResolver} from "./albums/album/album-resolver.service";
import { TrackListComponent } from './tracks/track-list/track-list.component';
import { PlayingIndicatorComponent } from './tracks/track-list/playing-indicator/playing-indicator.component';
import {YoutubeStrategy} from "./player/strategies/youtube-strategy.service";
import {PopularAlbumsResolver} from "./albums/popular-albums/popular-albums-resolver.service";
import {PopularGenresResolver} from "./genres/popular-genres/popular-genres-resolver.service";
import {UserGenresResolver} from "./genres/user-genres/user-genres-resolver.service";
import {PlayerState} from "./player/player-state.service";
import {AdsPlayerState} from "./ads-player/ads-player-state.service";
import {FormattedDuration} from "./player/formatted-duration.service";
import { QueueSidebarComponent } from './queue-sidebar/queue-sidebar.component';
import { VolumeControlsComponent } from './player/player-controls/volume-controls/volume-controls.component';
import { PlayerSeekbarComponent } from './player/player-controls/player-seekbar/player-seekbar.component';
import { AdsSeekbarComponent } from './ads-player/ads-player-controls/ads-seekbar/ads-seekbar.component';
import { PlaybackControlButtonComponent } from './player/playback-control-button/playback-control-button.component';
import { FullscreenOverlayComponent } from './fullscreen-overlay/fullscreen-overlay.component';
import { FullscreenAdsComponent } from './fullscreen-ads/fullscreen-ads.component';
import {MainPlaybackButtonsComponent} from './player/player-controls/main-playback-buttons/main-playback-buttons.component';
import {FullscreenOverlay} from "./fullscreen-overlay/fullscreen-overlay.service";
import {FullscreenAds} from "./fullscreen-ads/fullscreen-ads.service";
import {RepeatButtonComponent} from './player/player-controls/repeat-button/repeat-button.component';
import {QueueSidebar} from "./queue-sidebar/queue-sidebar.service";
import {UserLibrary} from "./users/user-library/user-library.service";
import { LibraryTrackToggleButtonComponent } from './users/user-library/library-track-toggle-button/library-track-toggle-button.component';
import { LibraryTracksComponent } from './users/user-library/library-tracks/library-tracks.component';
import {LibraryTracksResolver} from "./users/user-library/library-tracks/library-tracks-resolver.service";
import {LibraryTracks} from "./users/user-library/library-tracks.service";
import { LibraryAlbumsComponent } from './users/user-library/library-albums/library-albums.component';
import {LibraryAlbumsResolver} from "./users/user-library/library-albums/library-albums-resolver.service";
import { SortingHeaderComponent } from './filterable-page/sorting-header/sorting-header.component';
import { LibraryArtistsComponent } from './users/user-library/library-artists/library-artists.component';
import {LibraryArtistsResolver} from "./users/user-library/library-artists/library-artists-resolver.service";
import { AlbumItemComponent } from './albums/album-item/album-item.component';
import { MediaGridComponent } from './media-grid/media-grid.component';
import {Artists} from "./artists/artists.service";
import {ArtistItemComponent} from "./artists/artist-item/artist-item.component";
import {ArtistService} from "./artists/artist.service";
import { ArtistComponent } from './artists/artist/artist.component';
import {ArtistResolver} from "./artists/artist/artist-resolver.service";
import {AlbumContextMenuComponent} from './albums/album-context-menu/album-context-menu.component';
import { CrupdatePlaylistModalComponent } from './playlists/crupdate-playlist-modal/crupdate-playlist-modal.component';
import { TrackContextMenuComponent } from './tracks/track-context-menu/track-context-menu.component';
import { ArtistsLinksListComponent } from './artists/artists-links-list/artists-links-list.component';
import { ArtistContextMenuComponent } from './artists/artist-context-menu/artist-context-menu.component';
import { GenreComponent } from './genres/genre/genre.component';
import {GenreArtistsResolver} from "./genres/genre/genre-artists-resolver.service";
import {UserPlaylists} from "./playlists/user-playlists.service";
import {Playlists} from "./playlists/playlists.service";
import { ContextMenuPlaylistPanelComponent } from './context-menu/context-menu-playlist-panel/context-menu-playlist-panel.component';
import { PlaylistComponent } from './playlists/playlist/playlist.component';
import {PlaylistResolver} from "./playlists/playlist/playlist-resolver.service";
import { SearchComponent } from './search/search/search.component';
import {SearchResolver} from "./search/search/search-resolver.service";
import {SearchTabValidGuard} from "./search/search/search-tab-valid.guard";
import {PlaylistItemComponent} from "./playlists/playlist-item/playlist-item.component";
import {PlaylistItemAsRowComponent} from "./playlists/playlist-item-as-row/playlist-item-as-row.component";
import {PlaylistItemAsSliderComponent} from "./playlists/playlist-item-as-slider/playlist-item-as-slider.component";
import {PlaylistService} from "./playlists/playlist.service";
import { UserItemComponent } from './users/user-item/user-item.component';
import { PlaylistTrackContextMenuComponent } from './playlists/playlist-track-context-menu/playlist-track-context-menu.component';
import { PlaylistContextMenuComponent } from './playlists/playlist-context-menu/playlist-context-menu.component';
import {WebPlayerState} from "./web-player-state.service";
import { TrackPageComponent } from './tracks/track-page/track-page.component';
import {TrackPageResolver} from "./tracks/track-page/track-page-resolver.service";
import {Tracks} from "./tracks/tracks.service";
import {VideoAds} from "./video_ads/video_ads.service";
import {SubscriptionsTypes} from './subscriptions_types/subscriptions_types.service';
import {VouchersPrices} from './vouchers_prices/vouchers_prices.service';
import {UserSubscriptions} from './user_subscriptions/user_subscriptions.service';
import { UserProfilePageComponent } from './users/user-profile-page/user-profile-page.component';
import {UserProfilePageResolver} from "./users/user-profile-page/user-profile-page-resolver.service";
import {NewReleasesPageResolver} from "./albums/new-releases-page/new-releases-page.resolver.service";
import {NewReleasesPageComponent} from "./albums/new-releases-page/new-releases-page.component";
import { TopTracksPageComponent } from './tracks/top-tracks-page/top-tracks-page.component';
import {TopTracksPageResolver} from "./tracks/top-tracks-page/top-tracks-page-resolver.service";
import {RadioPageComponent} from './radio-page/radio-page.component';
import {RadioPageResolver} from "./radio-page/radio-page-resolver.service";
import {ShareMediaItemModalComponent} from "./context-menu/share-media-item-modal/share-media-item-modal.component";
import {LyricsModalComponent} from "./lyrics/lyrics-modal/lyrics-modal.component";
import {Lyrics} from "./lyrics/lyrics.service";
import { MobilePlayerControlsComponent } from './player/mobile-player-controls/mobile-player-controls.component';
import { UserLibraryComponent } from './users/user-library/user-library.component';
import { UserAccountComponent } from './users/user-account/user-account.component';
import { LibraryPlaylistsComponent } from './users/user-library/library-playlists/library-playlists.component';
import {LibraryPlaylistsResolver} from "./users/user-library/library-playlists/library-playlists-resolver.service";
import {Html5Strategy} from "./player/strategies/html5-strategy.service";
import {AdsHtml5Strategy} from "./ads-player/ads-strategies/ads-html5-strategy.service";
import {SoundcloudStrategy} from "./player/strategies/soundcloud-strategy.service";
import {TrackPlays} from "./player/track-plays.service";
import {AdsTrackPlays} from "./ads-player/ads-track-plays.service";
import {AVAILABLE_CONTEXT_MENUS, UiModule} from "vebto-client/core/ui/ui.module";
import {LazyLoadDirective} from "./lazy-load.directive";
import {MatDialogModule, MatSidenavModule} from "@angular/material";
import {WEB_PLAYER_CONTEXT_MENUS} from './available-context-menus';
import {ReorderPlaylistTracksDirective} from './playlists/reorder-playlist-tracks.directive';

import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {MatDividerModule} from '@angular/material/divider';
import {MatListModule} from '@angular/material/list';

import { SlickModule } from 'ngx-slick';
import { NgxHmCarouselModule } from 'ngx-hm-carousel';

import { SingleTrackItemComponent } from './tracks/track-item/track-item.component';
import { NgMultiSelectDropDownModule } from 'ng-multiselect-dropdown';

import {HomeAlbumModalComponent} from './albums/home-album-modal/home-album-modal.component';
import {HomeArtistModalComponent} from './artists/home-artist-modal/home-artist-modal.component';
import {HomePlaylistModalComponent} from './playlists/home-playlist-modal/home-playlist-modal.component';

@NgModule({
    imports: [
        UiModule,
        WebPlayerRoutingModule,

        //material
        MatSidenavModule,
        MatDialogModule,
        SlickModule.forRoot(),
        MatButtonToggleModule,
        MatDividerModule,
        MatListModule,
        NgxHmCarouselModule,
        NgMultiSelectDropDownModule.forRoot()
    ],
    declarations: [
        WebPlayerComponent,
        NavSidebarComponent,
        PopularAlbumsComponent,
        NewReleasesPageComponent,
        PopularGenresComponent,
        UserGenresComponent,
        SearchSlideoutPanelComponent,
        FilterablePageHeaderComponent,
        PlayerControlsComponent,
        AdsPlayerControlsComponent,
        AlbumComponent,
        TrackListComponent,
        PlayingIndicatorComponent,
        QueueSidebarComponent,
        VolumeControlsComponent,
        PlayerSeekbarComponent,
        AdsSeekbarComponent,
        PlaybackControlButtonComponent,
        FullscreenOverlayComponent,
        FullscreenAdsComponent,
        MainPlaybackButtonsComponent,
        RepeatButtonComponent,
        LibraryTrackToggleButtonComponent,
        LibraryTracksComponent,
        LibraryAlbumsComponent,
        SortingHeaderComponent,
        LibraryArtistsComponent,
        AlbumItemComponent,
        SingleTrackItemComponent,
        ArtistItemComponent,
        UserItemComponent,
        PlaylistItemComponent,
        PlaylistItemAsRowComponent,
        PlaylistItemAsSliderComponent,
        MediaGridComponent,
        ArtistComponent,
        AlbumContextMenuComponent,
        CrupdatePlaylistModalComponent,
        TrackContextMenuComponent,
        ArtistsLinksListComponent,
        ArtistContextMenuComponent,
        GenreComponent,
        ContextMenuPlaylistPanelComponent,
        PlaylistComponent,
        SearchComponent,
        UserItemComponent,
        PlaylistTrackContextMenuComponent,
        PlaylistContextMenuComponent,
        TrackPageComponent,
        UserProfilePageComponent,
        TopTracksPageComponent,
        RadioPageComponent,
        ShareMediaItemModalComponent,
        LyricsModalComponent,
        MobilePlayerControlsComponent,
        UserLibraryComponent,
        UserAccountComponent,
        LibraryPlaylistsComponent,
        LazyLoadDirective,
        ReorderPlaylistTracksDirective,
        HomeAlbumModalComponent,
        HomeArtistModalComponent,
        HomePlaylistModalComponent,
    ],
    entryComponents: [
        AlbumContextMenuComponent,
        TrackContextMenuComponent,
        ArtistContextMenuComponent,
        PlaylistContextMenuComponent,
        PlaylistTrackContextMenuComponent,
        CrupdatePlaylistModalComponent,
        ShareMediaItemModalComponent,
        LyricsModalComponent,
        HomeAlbumModalComponent,
        HomeArtistModalComponent,
        HomePlaylistModalComponent,
    ],
    providers: [
        Albums,
        Artists,
        Genres,
        Search,
        Player,
        AdsPlayer,
        ArtistService,
        PlaylistService,
        YoutubeStrategy,
        Html5Strategy,
        AdsHtml5Strategy,
        SoundcloudStrategy,
        WebPlayerUrls,
        PlayerQueue,
        AdsPlayerQueue,
        PlayerState,
        AdsPlayerState,
        FormattedDuration,
        QueueSidebar,
        SearchSlideoutPanel,
        FullscreenOverlay,
        FullscreenAds,
        AlbumResolver,
        PopularAlbumsResolver,
        PopularGenresResolver,
        UserGenresResolver,
        GenreArtistsResolver,
        PlaylistResolver,
        ArtistResolver,
        NewReleasesPageResolver,
        UserLibrary,
        SearchResolver,
        LibraryTracksResolver,
        LibraryAlbumsResolver,
        LibraryArtistsResolver,
        LibraryPlaylistsResolver,
        LibraryTracks,
        UserPlaylists,
        Playlists,
        SearchTabValidGuard,
        WebPlayerState,
        TrackPageResolver,
        Tracks,
        VideoAds,
        SubscriptionsTypes,
        VouchersPrices,
        UserSubscriptions,
        UserProfilePageResolver,
        TopTracksPageResolver,
        RadioPageResolver,
        Lyrics,
        TrackPlays,
        AdsTrackPlays,
        {
            provide: AVAILABLE_CONTEXT_MENUS,
            useValue: WEB_PLAYER_CONTEXT_MENUS,
            multi: true,
        },
    ]
})
export class WebPlayerModule {}
