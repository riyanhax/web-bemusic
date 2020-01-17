import {NgModule} from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {AuthGuard} from "vebto-client/guards/auth-guard.service";
import {CheckPermissionsGuard} from "vebto-client/guards/check-permissions-guard.service";
import {AdminComponent} from "vebto-client/admin/admin.component";

import {SettingsComponent} from "vebto-client/admin/settings/settings.component";
import {StatisticsComponent} from "vebto-client/admin/statistics/statistics.component";

import {SettingsResolve} from "vebto-client/admin/settings/settings-resolve.service";
import {StatisticsResolve} from "vebto-client/admin/statistics/statistics-resolve.service";

import {ArtistsComponent} from "./artists/artists.component";
import {NewArtistPageComponent} from "./artists/new-artist-page/new-artist-page.component";
import {EditArtistPageResolver} from "./artists/new-artist-page/edit-artist-page-resolver.service";
import {AlbumsPageComponent} from "./albums/albums-page/albums-page.component";
import {TracksPageComponent} from "./tracks/tracks-page/tracks-page.component";
import {VideoAdsPageComponent} from "./video_ads/video-ads-page/video-ads-page.component";
import {LyricsPageComponent} from "./lyrics-page/lyrics-page.component";
import {PlaylistsPageComponent} from './playlists-page/playlists-page.component';
import {ProvidersSettingsComponent} from './settings/providers/providers-settings.component';
import {PlayerSettingsComponent} from './settings/player/player-settings.component';
import {GenresSettingsComponent} from './settings/genres/genres-settings.component';
import {BlockedArtistsSettingsComponent} from './settings/blocked-artists/blocked-artists-settings.component';

import {vebtoSettingsRoutes} from 'vebto-client/admin/settings/settings-routing.module';
import {vebtoStatisticsRoutes} from 'vebto-client/admin/statistics/statistics-routing.module';

import {vebtoAdminRoutes} from 'vebto-client/admin/admin-routing.module';

import {RolesArtistsSettingsComponent} from './settings/roles-artists/roles-artists-settings.component';
import {HomeAlbumsSettingsComponent} from './settings/home-albums/home-albums-settings.component';
import {HomeTracksSettingsComponent} from './settings/home-tracks/home-tracks-settings.component';
import {SubscriptionsTypesComponent} from './subscriptions_types/subscription_types-page/subscriptions_types.component';

import {GenresComponent} from './genres/genres.component';

const routes: Routes = [
    {
        path: '',
        component: AdminComponent,
        canActivate: [AuthGuard, CheckPermissionsGuard],
        canActivateChild: [AuthGuard, CheckPermissionsGuard],
        data: {permissions: ['admin.access']},
        children: [
            {
                path: 'artists',
                children: [
                    {path: '', component: ArtistsComponent, data: {permissions: ['artists.update']}},
                    {path: 'new', component: NewArtistPageComponent, data: {permissions: ['artists.create']}},
                    {path: ':id/edit', component: NewArtistPageComponent, resolve: {artist: EditArtistPageResolver}, data: {permissions: ['artists.update']}},
                ]
            },
            {
                path: 'albums',
                component: AlbumsPageComponent,
                data: {permissions: ['albums.update']}
            },
            {
                path: 'tracks',
                component: TracksPageComponent,
                data: {permissions: ['tracks.update']}
            },
            {
                path: 'lyrics',
                component: LyricsPageComponent,
                data: {permissions: ['lyrics.update']}
            },
            {
                path: 'genres',
                component: GenresComponent,
                data: {permissions: ['genres.update']}
            },
            {
                path: 'playlists',
                component: PlaylistsPageComponent,
                data: {permissions: ['playlists.update']}
            },
            {
                path: 'settings',
                component: SettingsComponent,
                resolve: {settings: SettingsResolve},
                data: {permissions: ['settings.view']},
                children: [
                    {path: 'providers', component: ProvidersSettingsComponent},
                    {path: 'player', component: PlayerSettingsComponent},
                    {path: 'genres', component: GenresSettingsComponent},
                    {path: 'blocked-artists', component: BlockedArtistsSettingsComponent},
                    {path: 'roles-artists', component: RolesArtistsSettingsComponent},                    
                    {path: 'home-albums', component: HomeAlbumsSettingsComponent}, 
                    {path: 'home-tracks', component: HomeTracksSettingsComponent},                                                            
                    ...vebtoSettingsRoutes,
                ],
            },
            {
                path: 'statistics',
                component: StatisticsComponent,
                resolve: {settings: StatisticsResolve},
                data: {permissions: ['settings.view']},
                children: [                    
                    ...vebtoStatisticsRoutes,
                ],
            },
            {
                path: 'video_ads',
                component: VideoAdsPageComponent,
                data: {permissions: ['ads.update']}
            },
            {
                path: 'subscriptions_types',
                component: SubscriptionsTypesComponent,
                data: {permissions: ['ads.update']}
            },
            ...vebtoAdminRoutes,
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class AppAdminRoutingModule {
}
