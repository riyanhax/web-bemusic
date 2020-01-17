import {Artist} from "./Artist";
import {Track} from "./Track";

export class Album {
	id: number;
	name: string;
	release_date?: string;
	image?: string;
	artist_id?: number;
	spotify_popularity?: boolean;
	fully_scraped: boolean;
	temp_id?: string;
	artist?: Artist;
	tracks?: Track[];
        views: number = 0;
        
        distribution_comp?: string;
        label_label?: string;
        label_year?: string;
        licence_comp?: string;
        UPC?: string;
        is_premium?: boolean;
        is_live?: boolean;
        is_mix?: boolean;
        is_comp?: boolean;        
        set_state?: string;

	constructor(params: Object = {}) {
        for (let name in params) {
            this[name] = params[name];
        }
    }
}