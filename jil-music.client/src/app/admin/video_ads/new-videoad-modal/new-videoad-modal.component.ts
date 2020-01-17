import {Component, Inject, Optional, ViewEncapsulation} from '@angular/core';
import {Settings} from "vebto-client/core/config/settings.service";
import {VideoAds} from "../../../web-player/video_ads/video_ads.service";
import {VideoAd} from "../../../models/VideoAd";
import {Modal} from "vebto-client/core/ui/modal.service";
import {Album} from "../../../models/Album";
import {Artist} from "../../../models/Artist";
import {UploadFileModalComponent} from "vebto-client/core/files/upload-file-modal/upload-file-modal.component";
import {MAT_DIALOG_DATA, MatAutocompleteSelectedEvent, MatDialogRef} from "@angular/material";
import {FormControl} from '@angular/forms';
import {debounceTime, distinctUntilChanged, map, startWith, switchMap} from 'rxjs/operators';
import {of as observableOf} from 'rxjs';
import {Search} from '../../../web-player/search/search.service';
import {WebPlayerImagesService} from '../../../web-player/web-player-images.service';

import {RolesToTrack} from "../../../models/RolesToTrack";

import {Toast} from "vebto-client/core/ui/toast.service";
import {WebPlayerUrls} from "../../../web-player/web-player-urls.service";

export interface CrupdateVideoadModalData {
    videoad?: VideoAd,
}

@Component({
    selector: 'new-videoad-modal',
    templateUrl: './new-videoad-modal.component.html',
    styleUrls: ['./new-videoad-modal.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class NewVideoadModalComponent {

    /**
     * Model for fetch input.
     */
    public emptyFetch = true;
    public fetchInput = {
        formControl: new FormControl(),
        searchResults: null,
    };
    public fethAuto = Artist;

    /**
     * Ads Type
     */

    public acceptedTypes: string[] = [];

    public privType: string;

    public avalibleTypes = {
        video : false,
        promo : false,
        custom : false,
        adscene : false,
        adsima : false,
    };

    // public acceptedPromo = ['artist','album','track','playlist'];
    public acceptedPromo = ['artist','album','playlist'];
    
    /**
     * Backend validation errors from last create or update request.
     */
    public errors: any = {};

    /**
     * Album this videoad should be attached to.
     */
    public rolestovideoad: RolesToTrack;
    public mykey;

    /**
     * Whether we are updating or creating a videoad.
     */
    public updating = false;

    public loading = false;

    /**
     * New videoad model.
     */
    public videoad = new VideoAd();

    /**
     * NewTrackModalComponent Constructor.
     */
    constructor(
        public settings: Settings,
        protected videoads: VideoAds,
        protected modal: Modal,
        private search: Search,
        private dialogRef: MatDialogRef<NewVideoadModalComponent>,
        public images: WebPlayerImagesService,
        @Optional() @Inject(MAT_DIALOG_DATA) public data: CrupdateVideoadModalData,

        private toast: Toast,
        public urls: WebPlayerUrls,
    ) {
        this.hydrate(this.data);
        this.acceptedTypes =  this.settings.has('videoads_source') ? JSON.parse(this.settings.get('videoads_source')) : []; 
        
        this.hydratePromo();        
        this.bindArtistInput();
    }

    private hydratePromo() {        
        this.emptyFetch = true;

        if(this.videoad.id && this.videoad.type == 'promo'){
            this.fetchInput.formControl.setValue(this.videoad.name);
        }        
    }

    private bindArtistInput() {
        this.fetchInput.searchResults = this.fetchInput.formControl.valueChanges
            .pipe(
                distinctUntilChanged(),
                debounceTime(350),
                startWith(''),
                switchMap(query => {
                    const results = this.search.everythingAdmin(this.fetchDisplayFn(query), {limit: 20, type: this.videoad.link_text})
                        .pipe(map(results => results[this.videoad.link_text+'s']));
                    return query ? results : observableOf([]);
                })
            );            
    }
    
    public fetchDisplayFn(fetch?: any): string {
        if ( ! fetch) return '';
        if (typeof fetch === 'string') {            
            return fetch;
        } else {            
            return fetch.name
        }
    }
    
    public attachFetch(event: MatAutocompleteSelectedEvent) {         
        if(event.option.value && typeof event.option.value !== 'string'){
            this.emptyFetch = false;     
            this.videoad.name = event.option.value.name;
            this.videoad.url = this.getFetchImage(event.option.value);
            this.videoad.link = this.getFetchUrl(event.option.value);
        } else{
            this.emptyFetch = true;     
            delete this.videoad.name;
            delete this.videoad.url;
            delete this.videoad.link;
        }
    }

    public getFetchUrl(fetches: any){        
        let url = [];
        switch (this.videoad.link_text) {
            case 'artist': url = this.urls.artist(fetches); break;
            case 'album': url = this.urls.album(fetches); break;
            case 'track': url = this.urls.track(fetches); break;
            case 'playlist': url = this.urls.playlist(fetches); break;
        }
        return JSON.stringify(url);
    }

    public getFetchImage(fetches: any){        
        let img = '';
        switch (this.videoad.link_text) {
            case 'artist': img = fetches.image_small; break;
            case 'album': img = fetches.image; break;
            case 'track': img = fetches.album.image; break;
            case 'playlist': img = fetches.image; break;
        }
        return img;
    }

    public getFetchSub(fetches: any){                
        let sub = '';
        switch (this.videoad.link_text) {
            case 'artist': 
                fetches.genres.map(key => {                                                
                    sub += key.name + ' | ';
                });
                if (sub.length > 0) sub.slice(0, -2);
                break;
            case 'album': sub = fetches.artist.name; break;
            case 'track': sub = fetches.album.artist.name + ' | ' + fetches.album.name; break;
            case 'playlist': sub = fetches.editors[0].display_name; break;
        }
        return sub;
    }

    /**
     * Check ADS type
     */
    public CheckStateTypes() {        
        this.acceptedTypes.map(key => {                                                
            this.avalibleTypes[key] = true;
        });        
    }

    /**
     * Confirm videoad creation.
     */
    public confirm() {
        if(this.checkPromo()){
            if (this.videoad.id) {
                this.update();
            } else {
                this.create();                
            }
        }        
    }

    private checkPromo(){
        if(this.videoad.type != 'promo') return true;

        if(this.videoad.type == 'promo' && this.fetchInput.formControl.value && this.videoad.name == this.fetchInput.formControl.value) return true;

        if ( ! this.fetchInput.formControl.value || typeof this.fetchInput.formControl.value === 'string'){
            this.toast.open('Please check Search Content. Nothing found.');
            return false;
        } 
        
        return true;
    }

    public close(data?: any) {
        this.dialogRef.close(data);
    }

    /**
     * Update existing videoad.
     */
    public update() {
        this.videoads.update(this.videoad.id, this.getPayload()).subscribe(videoad => {
            this.loading = false;
            this.dialogRef.close(videoad);
        }, errors => {
            this.loading = false;
            this.errors = errors.messages;
        });
    }

    /**
     * Create a new videoad.
     */
    public create() {
        this.videoads.create(this.getPayload()).subscribe(videoad => {
            this.loading = false;
            this.dialogRef.close(videoad);
        }, errors => {
            this.loading = false;
            this.errors = errors.messages;
        });
    }

    /**
     * Open modal for uploading videoad streaming file.
     */
    public openUploadMusicModal() {
        const params = {uri: 'uploads/videos', httpParams: {type: 'videoad'}};
        this.modal.show(UploadFileModalComponent, params).afterClosed().subscribe(uploadedFile => {
            if ( ! uploadedFile) return;
            this.videoad.url = uploadedFile.url;
            this.autoFillDuration(this.videoad.url);
        });
    }

    /**
     * Open modal for uploading img file.
     */
    public openUploadImageModal() {
        const params = {uri: 'uploads/images', httpParams: {type: 'videoad'}};
        this.modal.show(UploadFileModalComponent, params).afterClosed().subscribe(uploadedFile => {
            if ( ! uploadedFile) return;
            this.videoad.url = uploadedFile.url;
        });
    }
    
    public changeType() {

        if( !this.privType ){
            this.privType = this.videoad.type;
            if(this.videoad.type == 'promo') this.videoad.link_text = this.acceptedPromo[0];
            return;
        }

        delete this.videoad.url;
        delete this.videoad.link;
        delete this.videoad.link_text;

        if(this.videoad.type == 'video' || this.privType == 'video') delete this.videoad.duration;

        if((this.videoad.type == 'promo' && this.privType != 'custom') || (this.videoad.type == 'custom' && this.privType != 'promo')) delete this.videoad.text;

        this.emptyFetch = true;
        this.fetchInput.formControl.setValue('');

        this.privType = this.videoad.type;
    }

    public changeContentType(){

        this.emptyFetch = true;
        this.fetchInput.formControl.setValue('');

        delete this.videoad.url;        
        delete this.videoad.link;
        delete this.videoad.name;
    }

    /**
     * Auto fill duration field using specified media file url.
     */
    public autoFillDuration(url: string) {
        if ( ! url || this.videoad.type != 'video') return;        
        const audio = document.createElement('audio');

        audio.addEventListener('canplaythrough', (e) => {
            const target = (e.currentTarget || e.target) as HTMLMediaElement;

            if (target.duration) {
                this.videoad.duration = Math.ceil(target.duration * 1000);
            }

            audio.remove();
        });

        audio.src = url;
    }

        /**
     * Get videoad payload for backend.
     */
    private getPayload() {
        let payload = Object.assign({}, this.videoad);
        return payload;
    }

    /**
     * Hydrate videoad and album models.
     */
    private hydrate(params: CrupdateVideoadModalData) {
        
        if (params.videoad) this.videoad = Object.assign({}, params.videoad);
        this.updating = !!params.videoad;
    }
  
}
