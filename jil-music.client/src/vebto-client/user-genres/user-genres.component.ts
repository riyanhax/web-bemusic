import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {Users} from "../auth/users.service";
import {CurrentUser} from "vebto-client/auth/current-user";
import {AuthService} from "../auth/auth.service";
import {Toast} from "../core/ui/toast.service";
import {ErrorsModel} from "./user-genres-types";
import {Settings} from "../core/config/settings.service";
import {User} from "../core/types/models/User";
import {Translations} from '../core/translations/translations.service';
import {Localizations} from '../core/translations/localizations.service';

import {Genre} from "../../app/models/Genre";
import {Genres} from "../../app/web-player/genres/genres.service";

import {Artist} from "../../app/models/Artist";
import {Artists} from "../../app/web-player/artists/artists.service";

import {WebPlayerImagesService} from "../../app/web-player/web-player-images.service";

import {FormBuilder, FormGroup, Validators} from '@angular/forms';

@Component({
    selector: 'user-genres-setting',
    templateUrl: './user-genres.component.html',
    styleUrls: ['./user-genres.component.scss'],
    encapsulation: ViewEncapsulation.None,
})
export class UserGenresSettingComponent implements OnInit {

    /**
    * Whether genres actions currently.
    */
    loading = false;

    /**
     * Whether mobile layout should be activated.
     */
    public isMobile: boolean = false;

    /*
    *Progress bar
    */
    color = 'primary';
    mode = 'determinate';
    value = 33;

    /*
    *Stepper
    */
    isLinear = true;
    firstFormGroup: FormGroup;
    secondFormGroup: FormGroup;

    public mygenres: Genre[];
    public avaliblegenres: Genre[];

    public myartists: Artist[];
    public avaliblartists: Artist[];
    
    /**
     * User model.
     */
    public user = new User();

    /**
     * AccountSettingsComponent Constructor.
     */
    constructor(
        public settings: Settings,
        private route: ActivatedRoute,
        private users: Users,
        private currentUser: CurrentUser,
        private toast: Toast,
        private i18n: Translations,
        private localizations: Localizations,
        public auth: AuthService,
        private genres: Genres,
        private artists: Artists,
        private _formBuilder: FormBuilder,
        public wpImages: WebPlayerImagesService,
    ) {
        this.isMobile = window.matchMedia && window.matchMedia('(max-width: 768px)').matches;
    }

    ngOnInit() {

        this.avaliblartists = [];

        this.firstFormGroup = this._formBuilder.group({
            firstCtrl: ['', Validators.required]
        });
        this.secondFormGroup = this._formBuilder.group({
            secondCtrl: ['']
        });

        this.loading = true;

        this.genres.getUserGenres().toPromise().then(genres => {                        
            this.mygenres = genres;    
            if(!genres) this.mygenres = [];
            this.validateGvenre();                    
            this.loading = false;
        }).catch(() => {            
            this.mygenres = [];
            this.loading = false;
        }) as any;

        this.artists.getUserArtists().toPromise().then(artists => {                        
            this.myartists = artists;    
            if(!artists) this.myartists = [];
            this.loading = false;
        }).catch(() => {            
            this.myartists = [];
            this.loading = false;
        }) as any;

        this.getPopularList();           
        this.getGenreArtist();                   
    }

    public validateGvenre(){        
        let validGvenre = '';        
        if(this.mygenres && this.mygenres.length >= 3) validGvenre = 'ok';

        this.firstFormGroup = this._formBuilder.group({
            firstCtrl: [validGvenre, Validators.required]
        });        

    }

    public isSelectedGenre(genreName){           
        if( this.mygenres.findIndex(myGenres => myGenres.name == genreName) < 0 ) return false;
        
        return true;
    }

    public isSelectedArtist(artistID){           
        if( this.myartists.findIndex(myArtist => myArtist.id == artistID) < 0 ) return false;
        
        return true;
    }

    public isSelectedArtistIcon(artistID){
        if(!this.isSelectedArtist(artistID)) return 'add';

        return 'check';
    }    

    public selectGenre(genre){
        let index = this.mygenres.findIndex(myGenres => myGenres.name == genre.name);

        if(index < 0){                        
            this.addSelectedGenre(genre);            
        }else{
            this.deleteSelectedGenre(this.mygenres[index]);
        }
    }

    public selectArtist(artist){
        let index = this.myartists.findIndex(myArtists => myArtists.id == artist.id);

        if(index < 0){                        
            this.addSelectedArtist(artist);            
        }else{
            this.deleteSelectedArtist(this.myartists[index]);
        }
    }
    
    private getPopularList() {
        this.loading = true;
        this.genres.getPopular().toPromise().then(genres => {                        
            this.avaliblegenres = genres;            
            this.loading = false;
        }).catch(() => {
            this.loading = false;
        }) as any;
    }

    private getGenreArtist() {
        this.artists.getArtistsUserGenres().toPromise().then(artists => {                        
            this.avaliblartists = artists;      
            this.loading = false;      
            if ( ! artists) this.avaliblartists = [];
        }).catch(() => {
            this.avaliblartists = [];
            this.loading = false;      
        }) as any;
    }

    /**
     * Delete Genre from Favorite.
     */
    public deleteSelectedGenre(remGenre:Genre) {
        this.loading = true;
        this.genres.deleteUserGenreByID(remGenre.id).toPromise().then(genres => {                        
            this.mygenres = genres;  
            this.validateGvenre();          
            this.loading = false;
            this.getGenreArtist();                   
        }).catch(() => {
            this.loading = false;
        }) as any;        
    }

    /**
     * Add Genre to Favorite.
     */
    public addSelectedGenre(addGenre:Genre) {
        this.loading = true;        
        this.genres.addUserGenreByID(addGenre.name).toPromise().then(genres => {                        
            this.mygenres = genres; 
            this.validateGvenre();           
            this.loading = false;
            this.getGenreArtist();                   
        }).catch(() => {
            this.loading = false;
        }) as any;                        
    }

    /**
     * Delete Genre from Favorite.
     */
    public deleteSelectedArtist(remArtist:Artist) {
        this.loading = true;
        this.artists.deleteUserArtistByID(remArtist.id).toPromise().then(artist => {                        
            this.myartists = artist;        
            this.loading = false;
        }).catch(() => {
            this.loading = false;
        }) as any;        
    }

    /**
     * Add Genre to Favorite.
     */
    public addSelectedArtist(addArtist:Artist) {
        this.loading = true;        
        this.artists.addUserArtistByID(addArtist.id).toPromise().then(artist => {                        
            this.myartists = artist;         
            this.loading = false;
        }).catch(() => {
            this.loading = false;
        }) as any;                        
    }
    
}
