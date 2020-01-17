import {Component, OnInit, ViewEncapsulation} from "@angular/core";
import {SettingsPanelComponent} from 'vebto-client/admin/settings/settings-panel.component';

import {Track} from "../../../models/Track";

import {MAT_DIALOG_DATA, MatAutocompleteSelectedEvent, MatDialogRef} from "@angular/material";
import {FormControl} from '@angular/forms';
import {debounceTime, distinctUntilChanged, map, startWith, switchMap} from 'rxjs/operators';
import {of as observableOf} from 'rxjs';
//import {Search} from '../../../web-player/search/search.service';

@Component({
    selector: 'home-tracks-settings',
    templateUrl: './home-tracks-settings.component.html',
    styleUrls: ['./home-tracks-settings.component.scss'],
    encapsulation: ViewEncapsulation.None,
})
export class HomeTracksSettingsComponent extends SettingsPanelComponent implements OnInit {

    
    /**
     * Artist new album should be attached to.
     */
    public artist: Track;

    /**
     * Model for artists input and tags.
     */
    public artistsInput = {
        formControl: new FormControl(),
        attachedArtists: <Track[]>[],
        searchResults: null,
    };

    /**
     * List of home albums names.
     */
    public homeAlbums = [];

    ngOnInit() {        
        const homeAlbums = this.state.client['home.tracks'];        
        this.hydrate(homeAlbums);
        this.bindArtistsInput();        
    }

     /**
     * Hydrate track and album models.
     */
    private hydrate(homeAlbumsSetting: string) {
        this.homeAlbums = homeAlbumsSetting ? JSON.parse(homeAlbumsSetting) : [];

        this.tracks.getTracks(this.homeAlbums).subscribe(albums => {
            this.artistsInput.attachedArtists = albums as any;    
        });
        
    }

    private bindArtistsInput() {
        this.artistsInput.searchResults = this.artistsInput.formControl.valueChanges
            .pipe(
                distinctUntilChanged(),
                debounceTime(350),
                startWith(''),
                switchMap(query => {
                    const results = this.search.everything(this.albumDisplayFn(query), {limit: 15})
                        .pipe(map(results => results.tracks));

                    return query ? results : observableOf([]);
                })
            );            
    }

    /**
     * Function for album autocomplete input.
     */
    
    public albumDisplayFn(album?: Track|string): any {
        if ( ! album) return '';

        if (typeof album === 'string') {
            return album;
        } else {
            // return album.name
            return album;
        }
        
    }

     /**
     * Attach a new artist to track.
     */
    public attachArtist(event: MatAutocompleteSelectedEvent) {
        const artistName = event.option.value;
        this.artistsInput.formControl.setValue('');

        if ( ! artistName) return;

        //make sure artist is not already attached to track
        if (this.artistsInput.attachedArtists.findIndex(curr => curr.id === artistName.id) > -1) return;

        this.artistsInput.attachedArtists.push(artistName);
        this.homeAlbums.push(artistName.id);
    }

    /**
     * Detach specified artist from track.
     */
    public detachArtist(artist: Track) {
        let i = this.artistsInput.attachedArtists.findIndex(curr => curr.id === artist.id);
        this.artistsInput.attachedArtists.splice(i, 1);
        this.homeAlbums.splice(i, 1);
    }
    
    /**
     * Save current settings to the server.
     */
    public saveSettings() {
        
        let payload = {'home.tracks': JSON.stringify(this.homeAlbums)};
        this.loading = true;

        this.settings.save({client: payload}).subscribe(() => {
            this.toast.open('Saved settings');
            this.loading = false;
        });
    }
}
