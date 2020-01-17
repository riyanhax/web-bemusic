import {Injectable} from '@angular/core';
import {AppHttpClient} from "vebto-client/core/http/app-http-client.service";
import {Observable} from "rxjs";
import {VideoAd} from "../../models/VideoAd";

@Injectable()
export class VideoAds {

    constructor(private http: AppHttpClient) {}

    /**
     * Get videoad by specified id.
     */
    public get(id: number): Observable<VideoAd> {
        return this.http.get(`videoads/${id}`);
    }

    /**
     * Get top 50 VideoAds.
     */
    public getTop(): Observable<VideoAd[]> {
        return this.http.get('videoads/top');
    }

    /**
     * Get Active VideoAds.
     */
    public getActive(): Observable<VideoAd[]> {
        return this.http.get('videoads/active');
    }

    /**
     * Create a new videoad.
     */
    public create(payload: object): Observable<VideoAd> {
        return this.http.post('videoads', payload);
    }

    /**
     * Update existing videoad.
     */
    public update(id: number, payload: object): Observable<VideoAd> {
        return this.http.put('videoads/'+id, payload);
    }

    /**
     * Delete specified videoads.
     */
    public delete(ids: number[]) {
        return this.http.delete('videoads', {ids});
    }

     /**
     * Activate specified videoads.
     */
    public activate(ids: number[]) {
        return this.http.post('videoads/activate', {ids});
    }

     /**
     * Disable specified videoads.
     */
    public disable(ids: number[]) {
        return this.http.post('videoads/disable', {ids});
    }

    /**
     * Increment specified videoad plays.
     */
    public incrementPlays(videoad: VideoAd) {
        return this.http.post(`videoads/${videoad.id}/plays/increment`);
    }
}
