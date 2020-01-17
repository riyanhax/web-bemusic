import {Component, OnInit, ViewEncapsulation} from "@angular/core";
import {SettingsPanelComponent} from 'vebto-client/admin/settings/settings-panel.component';

import {UploadFileModalComponent} from "vebto-client/core/files/upload-file-modal/upload-file-modal.component";

import {Genres} from "../../../web-player/genres/genres.service"

@Component({
    selector: 'genres-settings',
    templateUrl: './genres-settings.component.html',
    styleUrls: ['./genres-settings.component.scss'],
    encapsulation: ViewEncapsulation.None,
})
export class GenresSettingsComponent extends SettingsPanelComponent implements OnInit {

    /**
     * Genre input model.
     */
    public genre: string;

    /**
     * Homepage genres.
     */
    public genresSetting: string[] = [];

    public genresImg = [];

    ngOnInit() {        
        const genres = this.state.client['homepage.genres'];
        this.genresSetting = genres ? JSON.parse(genres) : [];

        this.genresSetting.map(key => {                                                
            this.genresImg[key] = this.getDefGenreImage();
        });

        this.genres.getPopular().toPromise().then(genres => {              
            genres.map(key => {    
                this.genresImg[key.name] = this.settings.getAssetUrl(key.image);
            });
        }) as any;

    }

    /**
     * Open modal for uploading genre image.
     */
    public openInsertImageModal(genreName) {
        const params = {uri: 'uploads/images', httpParams: {type: 'genre_custom', genre: genreName}};
        this.modal.show(UploadFileModalComponent, params).afterClosed().subscribe(uploadedFile => {
            if ( ! uploadedFile) return;
            const path = this.genresImg[genreName];
            this.genresImg[genreName] = path+'?_=' + Date.now();
        });
    }

    /**
     * Get default genre image.
     */
    public getDefGenreImage(): string {        
        return this.settings.getAssetUrl('images/default/artist_small.jpg');
    }

    /**
     * Add a new genre to homepage genres.
     */
    public addGenre() {
        if ( ! this.genre) return;

        if (this.genresSetting.findIndex(curr => curr === this.genre) > -1) {
            return this.genre = null;
        }

        this.genresSetting.push(this.genre);
        this.genresImg[this.genre] = this.getDefGenreImage();

        this.genres.getGenreByName(this.genre).toPromise().then(res => {                     
            this.genresImg[res[0].name] = this.settings.getAssetUrl(res[0].image);
        }) as any;

        this.genre = null;
    }

    /**
     * Remove specified genre from homepage genres.
     */
    public removeGenre(genre: string) {
        let i = this.genresSetting.findIndex(curr => curr === genre);
        this.genresSetting.splice(i, 1);
    }

    /**
     * Save current settings to the server.
     */
    public saveSettings() {
        let payload = {'homepage.genres': JSON.stringify(this.genresSetting)};
        this.settings.save({client: payload}).subscribe(() => {
            this.toast.open('Saved settings');
        });
    }
}
