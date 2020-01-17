import {Component, Inject, Optional, ViewEncapsulation} from '@angular/core';
// import {UploadQueueService} from 'vebto-client/uploads/upload-queue/upload-queue.service';
import {Settings} from 'vebto-client/core/config/settings.service';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
// import {openUploadWindow} from 'vebto-client/uploads/utils/open-upload-window';
// import {UploadInputTypes} from 'vebto-client/common/uploads/upload-input-config';
import {Genres} from '../../../web-player/genres/genres.service';
import {Genre} from '../../../models/Genre';
// import {ImageUploadValidator} from '../../../web-player/image-upload-validator';

import {Modal} from "vebto-client/core/ui/modal.service";
import {WebPlayerImagesService} from '../../../web-player/web-player-images.service';
import {Toast} from "vebto-client/core/ui/toast.service";
import {UploadFileModalComponent} from "vebto-client/core/files/upload-file-modal/upload-file-modal.component";

interface CrupdateGenreModalData {
    genre?: Genre;
}

@Component({
    selector: 'crupdate-genre-modal',
    templateUrl: './crupdate-genre-modal.component.html',
    styleUrls: ['./crupdate-genre-modal.component.scss'],
    // providers: [UploadQueueService],
    encapsulation: ViewEncapsulation.None
})
export class CrupdateGenreModalComponent {
    public errors: any = {};
    public updating = false;    
    public genre = new Genre();

    /**
     * Whether album is being created or updated currently.
     */
    public loading = false;

    constructor(
        public settings: Settings,
        protected genres: Genres,
        // protected uploadQueue: UploadQueueService,
        private dialogRef: MatDialogRef<CrupdateGenreModalComponent>,
        // private imageValidator: ImageUploadValidator,
        @Optional() @Inject(MAT_DIALOG_DATA) public data: CrupdateGenreModalData,

        private modal: Modal,
        private toast: Toast,
        public images: WebPlayerImagesService,
    ) {
        if (this.data.genre) {
            this.genre = this.data.genre;
            this.updating = true;
        }
    }

    public confirm() {
        let request;

        if (this.updating) {
            request = this.genres.update(this.genre.id, this.getPayload());
        } else {
            request = this.genres.create(this.getPayload());
        }

        request.subscribe(response => {
            this.loading = false;
            this.dialogRef.close(response.genre);
        }, errors => {
            this.loading = false;
            this.errors = errors.messages;
        });
    }

    public close(genre?: Genre) {
        this.dialogRef.close(genre);
    }

    /**
     * Open modal for uploading a new image for artist.
     */
    public openUploadImageModal() {

        if(!this.genre.name){
            this.toast.open('The genre name cannot be empty.');
            return;
        }

        const params = {uri: 'uploads/images', httpParams: {type: 'genre_custom', genre: this.genre.name}};
        this.modal.show(UploadFileModalComponent, params).afterClosed().subscribe(uploadedFile => {
            if ( ! uploadedFile) return;

            this.genres.getGenreByName(this.genre.name).subscribe(genre => {
                if(genre){
                    this.genre.image = genre[0].image;
                }else{
                    this.genre.image = 'images/genres/'+this.genre.name+'.'+uploadedFile.extension;
                }
                
            });

        });
    }

    private getPayload() {
        return {
            name: this.genre.name,
            image: this.genre.image,
            popularity: this.genre.popularity,
        }
    }
}
