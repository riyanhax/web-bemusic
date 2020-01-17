import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import {WebPlayerUrls} from "../../web-player-urls.service";
import {Genre} from "../../../models/Genre";
import {Album} from "../../../models/Album";
import {Track} from "../../../models/Track";
import {Genres} from "../../../web-player/genres/genres.service";
import {Artists} from "../../../web-player/artists/artists.service";
import {Albums} from "../../../web-player/albums/albums.service";
import {Tracks} from "../../../web-player/tracks/tracks.service";
import {FilterablePage} from "../../filterable-page/filterable-page";
import {ActivatedRoute, Router} from "@angular/router";
import {Settings} from "vebto-client/core/config/settings.service";
import {WebPlayerImagesService} from "../../web-player-images.service";
import {WebPlayerState} from "../../web-player-state.service";

import {Playlist} from "../../../models/Playlist";
import {PlaylistContextMenuComponent} from "../../playlists/playlist-context-menu/playlist-context-menu.component";
import {ContextMenu} from 'vebto-client/core/ui/context-menu/context-menu.service';

import { SlickModule } from 'ngx-slick';



@Component({
    selector: 'user-genres',
    templateUrl: './user-genres.component.html',
    styleUrls: ['./user-genres.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class UserGenresComponent extends FilterablePage<Genre> implements OnInit {

    ngxHmCarousel = {
        index : 0,
        autoplay : false,
        infinite : true,
        direction : 'right',
        directionToggle : true,  
        albums : <Album[]>[],
    };

    ngxHmCarouselTrack = {
        index : 1,
        autoplay : false,
        infinite : true,
        direction : 'right',
        directionToggle : true,  
        tracks : <Track[]>[],
        showNum: 3,
    };

    /**
     * Whether mobile layout should be activated.
     */
    // public isMobile: boolean = true;

    public selectedGenreGroup: string;
    public selectedArtistGroup: string;
    public selectedAlbumGroup: string;  
    public selectedTrackGroup: number;  
    
    public artistList = [];
    public albumList = [];
    public tracksList = [];
    
    /**
     * All current user playlists.
     */
    public playlists: Playlist[] = [];

    public slideConfig = {"slidesToShow": 3, "slidesToScroll": 3};
    
    /**
     * PopularGenresComponent Constructor.
     */
    constructor(
        private route: ActivatedRoute,
        private router: Router,
        public urls: WebPlayerUrls,
        public settings: Settings,
        public wpImages: WebPlayerImagesService,
        private genres: Genres,
        private artists: Artists,
        private albums: Albums,
        public state: WebPlayerState,
        private contextMenu: ContextMenu,
        private tracks: Tracks, 
    ) {
        super(['name']);
        // this.isMobile = window.matchMedia && window.matchMedia('(max-width: 768px)').matches;
    }

    ngOnInit() {

        this.selectedGenreGroup = 'favorite';
        this.selectedArtistGroup = 'favorite';
        this.selectedAlbumGroup = 'favorite'; 
        this.selectedTrackGroup = 1;                
        
        const homeAlbums = this.settings.get('home.albums');  
        const homeTracks = this.settings.get('home.tracks');  

        this.hydrate(homeAlbums, homeTracks);

        this.route.data.subscribe((data: {userStats: any}) => {
            if(!data || !data.userStats ){
                this.router.navigate(['/account/my-genres-setting']);
            }            

            this.selectedGenreGroup = data.userStats.genresGroup;
            this.setItems(data.userStats.genres);

            this.selectedArtistGroup = data.userStats.artistsGroup;
            this.artistList = data.userStats.artists;

            this.selectedAlbumGroup = data.userStats.albumsGroup;
            this.albumList = data.userStats.albums;            
            
            this.playlists = data.userStats.playlists;

            // this.tracksList = data.userStats.tracks;

            //this.state.loading = false;
        });
                
    }
    public indexChanged(index) {
        console.log(index);
      }

    /**
     * Hydrate track and album models.
     */
    private hydrate(homeAlbumsSetting: string, homeTracksSetting: string,) {
        
        this.albums.getAlbums(homeAlbumsSetting ? JSON.parse(homeAlbumsSetting) : []).subscribe(albums => {
            this.ngxHmCarousel.albums = albums as any;              
        });     

        this.tracks.getTracks(homeTracksSetting ? JSON.parse(homeTracksSetting) : []).subscribe(tracks => {
            this.ngxHmCarouselTrack.tracks = tracks as any;  
        });     
    }

    public onTrackChange(index: number) {        
        
        console.log(index);        
    }
    
    /**
     * Get playlist image.
     */
    public getImage(playlist: Playlist) {
        if (playlist.image) return playlist.image;
        if (playlist.tracks && playlist.tracks.length) return playlist.tracks[0].album.image;
        return this.wpImages.getDefault('artist');
    }

    /**
     * Open playlist context menu.
     */
    public showContextMenu(playlist: Playlist, e: MouseEvent) {
        e.stopPropagation();

        this.contextMenu.open(
            PlaylistContextMenuComponent,
            e.target,
            {data: {item: playlist, extra: {image: this.getImage(playlist)}, type: 'playlist'}}
        );
    }

    /*
    * Genres
    */
    public onGroupGenreChange(groupGenre: string) {        
        this.selectedGenreGroup = groupGenre;
        if(this.selectedGenreGroup == 'popular'){
            this.getPopularList();
        }else if(this.selectedGenreGroup == 'favorite'){
            this.getFavoriteList();
        }else if(this.selectedGenreGroup == 'resent'){
            this.getResentList();
        }
        
    }

    private getPopularList() {        
        //this.state.loading = true;
        this.genres.getPopular().toPromise().then(genres => {                        
            this.setItems(genres);      
            this.state.loading = false;      
            if ( ! genres) this.claearItems();
        }).catch(() => {
            this.claearItems();            
            //this.state.loading = false;      
        }) as any;
    }

    private getFavoriteList() {        
        //this.state.loading = true;        
        this.genres.getUserGenres().toPromise().then(genres => {                        
            this.setItems(genres);                       
            //this.state.loading = false;       
            if ( ! genres) this.claearItems();
        }).catch(() => {
            this.claearItems();            
            //this.state.loading = false;      
        }) as any;
    }

    private getResentList() {     
        //this.state.loading = true;   
        this.genres.getUserResentGenres().toPromise().then(genres => {                        
            this.setItems(genres);    
            //this.state.loading = false;              
            if ( ! genres) this.claearItems();
        }).catch(() => {                        
            this.claearItems();            
            //this.state.loading = false;      
        }) as any;
    }

    /*
    * Artists
    */
    public onGroupArtistChange(groupArtist: string) {        
        this.selectedArtistGroup = groupArtist;
        if(this.selectedArtistGroup == 'popular'){
            this.getPopularArtistsList();
        }else if(this.selectedArtistGroup == 'favorite'){
            this.getFavoriteArtistsList();
        }else if(this.selectedArtistGroup == 'resent'){
            this.getResentArtistsList();
        }        
    }

    private getPopularArtistsList() {        
        //this.state.loading = true;
        this.artists.getPopular().toPromise().then(artists => {                        
            this.artistList = artists;      
            //this.state.loading = false;      
            if ( ! artists) this.artistList = [];
        }).catch(() => {
            this.artistList = [];
            //this.state.loading = false;      
        }) as any;
    }

    private getFavoriteArtistsList() {        
        //this.state.loading = true;        
        this.artists.getUserArtists().toPromise().then(artists => {                        
            this.artistList = artists;      
            //this.state.loading = false;      
            if ( ! artists) this.artistList = [];
        }).catch(() => {
            this.artistList = [];
           // this.state.loading = false;      
        }) as any;
    }

    private getResentArtistsList() {     
        //this.state.loading = true;   
        this.artists.getUserResentArtists().toPromise().then(artists => {                        
            this.artistList = artists;      
           // this.state.loading = false;      
            if ( ! artists) this.artistList = [];
        }).catch(() => {
            this.artistList = [];
          //  this.state.loading = false;      
        }) as any;
    }

    /*
    * Albums
    */

    public onGroupAlbumChange(groupAlbum: string) {        
        this.selectedAlbumGroup = groupAlbum;
        if(this.selectedAlbumGroup == 'popular'){
            this.getPopularAlbumsList();
        }else if(this.selectedAlbumGroup == 'favorite'){
            this.getFavoriteAlbumsList();
        }else if(this.selectedAlbumGroup == 'resent'){
            this.getResentAlbumsList();
        }        
    }

    private getPopularAlbumsList() {        
        //this.state.loading = true;
        this.albums.getPopular().toPromise().then(albums => {                        
            this.albumList = albums;      
         //   this.state.loading = false;      
            if ( ! albums) this.albumList = [];
        }).catch(() => {
            this.albumList = [];
        //    this.state.loading = false;      
        }) as any;
    }

    private getFavoriteAlbumsList() {        
       // this.state.loading = true;        
        this.albums.getUserAlbums().toPromise().then(albums => {                        
            this.albumList = albums;      
      //      this.state.loading = false;      
            if ( ! albums) this.albumList = [];
        }).catch(() => {
            this.albumList = [];
        //    this.state.loading = false;      
        }) as any;
    }

    private getResentAlbumsList() {     
    //    this.state.loading = true;   
        this.albums.getUserResentAlbums().toPromise().then(albums => {                        
            this.albumList = albums;      
          //  this.state.loading = false;      
            if ( ! albums) this.albumList = [];
        }).catch(() => {
            this.albumList = [];
       //     this.state.loading = false;      
        }) as any;
    }

}
