import {Component, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import {Artists} from "../../web-player/artists/artists.service";
import {UrlAwarePaginator} from "vebto-client/admin/pagination/url-aware-paginator.service";
import {CurrentUser} from "vebto-client/auth/current-user";
import {Modal} from "vebto-client/core/ui/modal.service";
import {MatSort} from "@angular/material";
import {Artist} from "../../models/Artist";
import {ConfirmModalComponent} from "vebto-client/core/ui/confirm-modal/confirm-modal.component";
import {PaginatedDataTableSource} from 'vebto-client/admin/data-table/data/paginated-data-table-source';

import {MAT_DIALOG_DATA, MatAutocompleteSelectedEvent, MatDialogRef} from "@angular/material";
import {FormControl} from '@angular/forms';
import {debounceTime, distinctUntilChanged, map, startWith, switchMap} from 'rxjs/operators';
import {of as observableOf} from 'rxjs';
import {Search} from '../../web-player/search/search.service';

import {Router} from "@angular/router";

@Component({
    selector: 'artists',
    templateUrl: './artists.component.html',
    styleUrls: ['./artists.component.scss'],
    providers: [UrlAwarePaginator],
    encapsulation: ViewEncapsulation.None,
})
export class ArtistsComponent implements OnInit {
    @ViewChild(MatSort) matSort: MatSort;

    public dataSource: PaginatedDataTableSource<Artist>;

    /**
     * Whether album is being created or updated currently.
     */
    public loading = false;

    public emptyFetch = true;    

    /**
     * Model for fetch input and tags.
     */
    public fetchInput = {
        formControl: new FormControl(),
        searchResults: null,
    };

    public fethAuto = Artist;
    
    /**
     * ArtistsComponent Constructor.
     */
    constructor(
        public paginator: UrlAwarePaginator,
        private artists: Artists,
        private modal: Modal,
        public currentUser: CurrentUser,
        private search: Search,
        private router: Router,
    ) {
        this.hydrate();        
        this.bindArtistInput();
    }

    ngOnInit() {
        this.dataSource = new PaginatedDataTableSource<Artist>({
            uri: 'artists',
            dataPaginator: this.paginator,
            matSort: this.matSort,
            staticParams: {order_by: 'spotify_popularity'},
        });
    }

    private hydrate() {        
        this.emptyFetch = true;
    }

    private bindArtistInput() {
        this.fetchInput.searchResults = this.fetchInput.formControl.valueChanges
            .pipe(
                distinctUntilChanged(),
                debounceTime(350),
                startWith(''),
                switchMap(query => {
                    const results = this.search.everythingFetch(this.fetchDisplayFn(query), {limit: 20, type: 'artist'})
                        .pipe(map(results => results.artists));
                    return query ? results : observableOf([]);
                })
            );            
    }

    /**
     * Function for fetch autocomplete input.
     */
    public fetchDisplayFn(fetch?: any): string {
        if ( ! fetch) return '';
        if (typeof fetch === 'string') {            
            return fetch;
        } else {            
            return fetch.name
        }
    }

    public attachFetch(event: MatAutocompleteSelectedEvent) {         
        if(event.option.value && typeof event.option.value !== 'string') this.emptyFetch = false;     
    }
    
    public fethArtist() {  

        this.loading = true;
        this.dataSource.deselectAllItems();                  
        
        this.artists.fethNew(this.fetchInput.formControl.value.name).toPromise().then( artist => {                       
            this.loading = false;    
            if (artist){
                 this.emptyFetch = true;
                 this.fetchInput.formControl.setValue('');
                 this.paginator.refresh(); 
                 return this.router.navigate(['/admin/artists/'+artist.id+'/edit']);                  
            }             
        }).catch(() => {
            this.loading = false;
            console.log('catch');
        }) as any;        
    }

    /**
     * Ask user to confirm deletion of selected artists
     * and delete selected artists if user confirms.
     */
    public maybeDeleteSelectedArtists() {
        this.modal.show(ConfirmModalComponent, {
            title: 'Delete Artists',
            body:  'Are you sure you want to delete selected artists?',
            ok:    'Delete'
        }).beforeClose().subscribe(confirmed => {
            if (confirmed) {
                this.deleteSelectedArtists();
            } else {
                this.dataSource.deselectAllItems();
            }
        });
    }

    /**
     * Delete currently selected artists.
     */
    public deleteSelectedArtists() {
        const ids = this.dataSource.getSelectedItems();

        this.artists.delete(ids).subscribe(() => {
            this.paginator.refresh();
            this.dataSource.deselectAllItems();
        });
    }
}
