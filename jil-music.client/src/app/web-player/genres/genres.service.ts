import {Injectable} from '@angular/core';
import {AppHttpClient} from "vebto-client/core/http/app-http-client.service";
import {Observable} from "rxjs";
import {Genre} from "../../models/Genre";
import {Artist} from "../../models/Artist";
import {PaginationResponse} from "vebto-client/core/types/pagination-response";
import {BackendResponse} from 'vebto-client/core/types/backend-response';

@Injectable()
export class Genres {

    /**
     * Genres Service Constructor.
     */
    constructor(private httpClient: AppHttpClient) {}

    /**
     * Get popular genres.
     */
    public getPopular(): Observable<Genre[]> {
        return this.httpClient.get('genres/popular');
    }

    public create(params: Partial<Genre>): BackendResponse<{genre: Genre}> {
        return this.httpClient.post('genres', params);
    }

    public update(id: number, params: Partial<Genre>): BackendResponse<{genre: Genre}> {
        return this.httpClient.put('genres/' + id, params);
    }

    public delete(ids: number[]): BackendResponse<void> {
        return this.httpClient.delete('genres', {ids});
    }
    
    public getGenreArtists(name: string, params = {}): Observable<{artistsPagination: PaginationResponse<Artist>, model: Genre}> {
        return this.httpClient.get(`genres/${name}/artists`, params);
    }

    /**
     * Get popular user genres.
     */
    public getUserGenres(): Observable<Genre[]> {
        return this.httpClient.get('genres/my_genres');
    }

    /**
     * Get single genre.
     */
    public getGenreByName(name: string): Observable<Genre[]> {
        return this.httpClient.get(`genres/${name}/genre`);
    }

    /**
     * Get user resent genres.
     */
    public getUserResentGenres(): Observable<Genre[]> {
        return this.httpClient.get('genres/${name}/my_resent_genres');
    }

    /**
     * Get all genres.
     */
    public getAllGenres(offset:number,perPage:number): Observable<Genre[]> {
        return this.httpClient.get('genres/all',{offset,perPage});
    }

    /**
     * Delete genre fron user favorite genres.
     */
    public deleteUserGenreByID(id: number): Observable<Genre[]> {
        return this.httpClient.delete('genres/my_genres',{id});
    }

    /**
     * Add genre to user favorite genres.
     */
    public addUserGenreByID(name: string): Observable<Genre[]> {
        return this.httpClient.put('genres/my_genres',{name});
    }
  
    

}
