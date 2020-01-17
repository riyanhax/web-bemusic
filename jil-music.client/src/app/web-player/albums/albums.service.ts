import {Injectable} from '@angular/core';
import {AppHttpClient} from "vebto-client/core/http/app-http-client.service";
import {Observable} from "rxjs";
import {Album} from "../../models/Album";

@Injectable()
export class Albums {

    /**
     * Albums Service Constructor.
     */
    constructor(private httpClient: AppHttpClient) {}

    /**
     * Get album matching specified id.
     */
    public get(id: number): Observable<Album> {
        return this.httpClient.get('albums/'+id);
    }

    /**
     * Get popular albums.
     */
    public getPopular(): Observable<Album[]> {
        return this.httpClient.get('albums/popular');
    }

    public fethNew(): Observable<Album[]> {
        return this.httpClient.get('albums/feth-new');
    }

    public fethNewInfo(name: string, params = {}): Observable<any> {
        return this.httpClient.get('albums/feth-new-info/'+name, params);
    }    
    /**
     * Get user resent albums.
     */
    public getUserResentAlbums(): Observable<Album[]> {
        return this.httpClient.get('albums/get/my_resent_albums');
    }

    /**
     * Get user favorite albums.
     */
    public getUserAlbums(): Observable<Album[]> {
        return this.httpClient.get('albums/get/my_albums');
    }

    /**
     * Get new releases.
     */
    public getNewReleases(): Observable<Album[]> {
        return this.httpClient.get('albums/new-releases');
    }

    /**
     * Create a new album.
     */
    public create(payload: object): Observable<Album> {
        return this.httpClient.post('albums', payload);
    }

    /**
     * Update existing album.
     */
    public update(id: number, payload: object): Observable<Album> {
        return this.httpClient.put('albums/'+id, payload);
    }

    /**
     * Delete specified albums.
     */
    public delete(ids: number[]) {
        return this.httpClient.delete('albums', {ids});
    }
    
    /**
     * Validate specified albums.
     */
    public validateAlbums(ids: number[]) {
        return this.httpClient.post('albums/validate-specified', {ids});
    }
    
    /**
     * Validate specified albums.
     */
    public rejectAlbums(ids: number[]) {
        return this.httpClient.post('albums/reject-specified', {ids});
    }

    /**
     * Get specified albums
     */
    public getAlbums(ids: number[]) {
        return this.httpClient.post('albums/get-specified', {ids});
    }
    
}
