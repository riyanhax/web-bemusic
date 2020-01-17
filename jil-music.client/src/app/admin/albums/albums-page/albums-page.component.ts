import {Component, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import {Settings} from "vebto-client/core/config/settings.service";
import {Albums} from "../../../web-player/albums/albums.service";
import {CrupdateAlbumModalComponent} from "../crupdate-album-modal/crupdate-album-modal.component";
import {ActivatedRoute, Router} from "@angular/router";
import {UrlAwarePaginator} from "vebto-client/admin/pagination/url-aware-paginator.service";
import {Modal} from "vebto-client/core/ui/modal.service";
import {MatSort} from "@angular/material";
import {Album} from "../../../models/Album";
import {ConfirmModalComponent} from "vebto-client/core/ui/confirm-modal/confirm-modal.component";
import {CurrentUser} from "vebto-client/auth/current-user";
import {WebPlayerUrls} from "../../../web-player/web-player-urls.service";
import {PaginatedDataTableSource} from 'vebto-client/admin/data-table/data/paginated-data-table-source';
import {WebPlayerImagesService} from '../../../web-player/web-player-images.service';

import {MAT_DIALOG_DATA, MatAutocompleteSelectedEvent, MatDialogRef} from "@angular/material";
import {FormControl} from '@angular/forms';
import {debounceTime, distinctUntilChanged, map, startWith, switchMap} from 'rxjs/operators';
import {of as observableOf} from 'rxjs';
import {Search} from '../../../web-player/search/search.service';

@Component({
    selector: 'albums-page',
    templateUrl: './albums-page.component.html',
    styleUrls: ['./albums-page.component.scss'],
    providers: [UrlAwarePaginator],
    encapsulation: ViewEncapsulation.None
})
export class AlbumsPageComponent implements OnInit {
    @ViewChild(MatSort) matSort: MatSort;

    public dataSource: PaginatedDataTableSource<Album>;

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

    public fethAuto = Album;

    /**
     * AlbumsPageComponent Constructor.
     */
    constructor(
        private paginator: UrlAwarePaginator,
        private settings: Settings,
        private modal: Modal,
        private albums: Albums,
        private route: ActivatedRoute,
        private router: Router,
        public currentUser: CurrentUser,
        public urls: WebPlayerUrls,
        public images: WebPlayerImagesService,
        private search: Search,
    ) {
        this.hydrate();        
        this.bindArtistInput();
    }

    ngOnInit() {
        this.crupdateAlbumBasedOnQueryParams();

        this.dataSource = new PaginatedDataTableSource<Album>({
            uri: 'albums',
            dataPaginator: this.paginator,
            matSort: this.matSort,
            staticParams: {order_by: 'spotify_popularity', 'with': 'tracks'}
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
                    const results = this.search.everythingFetch(this.fetchDisplayFn(query), {limit: 20, type: 'album'})
                        .pipe(map(results => results.albums));
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
    
    public fethAlbum() {  

        this.loading = true;
        this.dataSource.deselectAllItems();                  

        this.albums.fethNewInfo(this.fetchInput.formControl.value.name, {'type': 'new'}).toPromise().then( album => {                       
            this.loading = false;    
            if (album){
                 this.emptyFetch = true;
                 this.fetchInput.formControl.setValue('');
                 this.paginator.refresh();  
                 this.openCrupdateAlbumModal(album);
            }             
        }).catch(() => {
            this.loading = false;
            console.log('catch');
        }) as any;        
    }

    /**
     * Open modal for editing existing album or creating a new one.
     */
    public openCrupdateAlbumModal(album?: Album) {
        this.dataSource.deselectAllItems();
        const artist = album ? album.artist : null;

        this.modal.open(
            CrupdateAlbumModalComponent,
            {album, artist},
            'crupdate-album-modal-container'
        )
        .afterClosed()
        .subscribe(() => {
            this.dataSource.deselectAllItems();
            this.paginator.refresh();
        });
    }

    public fethNewReleases() {  

        this.loading = true;

        this.dataSource.deselectAllItems();                  
        this.albums.fethNew().toPromise().then( albums => {                       
            this.loading = false;    
            this.paginator.refresh();
        }).catch(() => {
            this.loading = false;    
            console.log('catch');
        }) as any;
    }
    
    /**
     * Ask user to confirm deletion of selected albums
     * and delete selected artists if user confirms.
     */
    public maybeDeleteSelectedAlbums() {
        this.modal.show(ConfirmModalComponent, {
            title: 'Delete Albums',
            body:  'Are you sure you want to delete selected albums?',
            ok:    'Delete'
        }).afterClosed().subscribe(confirmed => {
            if ( ! confirmed) return;
            this.deleteSelectedAlbums();
        });
    }

    /**
     * Ask user to confirm validate of selected albums
     * and validate selected albums if user confirms.
     */
    public maybeValidateSelectedAlbums() {
        this.modal.show(ConfirmModalComponent, {
            title: 'Validate Albums',
            body:  'Are you sure you want to validate selected albums?',
            ok:    'Validate'
        }).afterClosed().subscribe(confirmed => {
            if ( ! confirmed) return;
            this.validateSelectedAlbums();
        });
    }

    /**
     * Ask user to reject of selected albums
     * and delete selected albums if user confirms.
     */
    public maybeRejectSelectedAlbums() {
        this.modal.show(ConfirmModalComponent, {
            title: 'Reject Albums',
            body:  'Are you sure you want to reject selected albums?',
            ok:    'Reject'
        }).afterClosed().subscribe(confirmed => {
            if ( ! confirmed) return;
            this.rejectSelectedAlbums();
        });
    }

    /**
     * Get available album image url or default one.
     */
    public getAlbumImage(album: Album): string {
        if (album.image) return album.image;
        return this.images.getDefault('album');
    }

    /**
     * Delete currently selected artists.
     */
    private deleteSelectedAlbums() {
        const ids = this.dataSource.getSelectedItems();

        this.albums.delete(ids).subscribe(() => {
            this.dataSource.deselectAllItems();
            this.paginator.refresh();
        });
    }
    
    /**
     * Validate currently selected artists.
     */
    private validateSelectedAlbums() {
        const ids = this.dataSource.getSelectedItems();

        this.albums.validateAlbums(ids).subscribe(() => {
            this.dataSource.deselectAllItems();
            this.paginator.refresh();
        });
    }
    
    /**
     * Reject currently selected artists.
     */
    private rejectSelectedAlbums() {
        const ids = this.dataSource.getSelectedItems();

        this.albums.rejectAlbums(ids).subscribe(() => {
            this.dataSource.deselectAllItems();
            this.paginator.refresh();
        });
    }

    /**
     * Open crupdate album modal if album id is specified in query params.
     */
    private crupdateAlbumBasedOnQueryParams() {
        let albumId = +this.route.snapshot.queryParamMap.get('album_id'),
            newAlbum = this.route.snapshot.queryParamMap.get('newAlbum');

        if ( ! albumId && ! newAlbum) return;

        this.router.navigate([], {replaceUrl: true}).then(async () => {
            let album = albumId ? await this.albums.get(albumId).toPromise() : null;

            this.openCrupdateAlbumModal(album);
        });
    }
}
