import {Component, Inject, Optional, ViewEncapsulation} from '@angular/core';
import {SubscriptionsTypes} from "../../../web-player/subscriptions_types/subscriptions_types.service";
import {SubscriptionsType} from "../../../models/SubscriptionsType";
import {Modal} from "vebto-client/core/ui/modal.service";

import {UploadFileModalComponent} from "vebto-client/core/files/upload-file-modal/upload-file-modal.component";
import {MAT_DIALOG_DATA, MatAutocompleteSelectedEvent, MatDialogRef} from "@angular/material";
import {FormControl} from '@angular/forms';
import {debounceTime, distinctUntilChanged, map, startWith, switchMap} from 'rxjs/operators';
import {of as observableOf} from 'rxjs';
import {Search} from '../../../web-player/search/search.service';

export interface CrupdateSubscriptionsTypeModalData {
    subscriptionsType?: SubscriptionsType,
}

@Component({
    selector: 'new-subscriptions_type-modal',
    templateUrl: './new-subscriptions_type-modal.component.html',
    styleUrls: ['./new-subscriptions_type-modal.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class NewSubscriptionsTypeModalComponent {

    /**
     * Backend validation errors from last create or update request.
     */
    public errors: any = {};

   
    /**
     * Whether we are updating or creating a videoad.
     */
    public updating = false;

    public loading = false;

    /**
     * New videoad model.
     */
    public subscriptionsType = new SubscriptionsType();

    /**
     * NewTrackModalComponent Constructor.
     */
    constructor(
        protected subscriptionsTypes: SubscriptionsTypes,
        protected modal: Modal,
        private search: Search,
        private dialogRef: MatDialogRef<NewSubscriptionsTypeModalComponent>,
        @Optional() @Inject(MAT_DIALOG_DATA) public data: CrupdateSubscriptionsTypeModalData,
    ) {
        this.hydrate(this.data);
    }

    /**
     * Confirm videoad creation.
     */
    public confirm() {
        //editing existing videoad
        if (this.subscriptionsType.id) {
            this.update();
        }

        //creating or updating videoad for new album
        else {
            this.create();
            //this.close(this.getPayload());
        }
    }

    public close(data?: any) {
        this.dialogRef.close(data);
    }

    /**
     * Update existing videoad.
     */
    public update() {
        this.subscriptionsTypes.update(this.subscriptionsType.id, this.getPayload()).subscribe(subscriptionsType => {
            this.loading = false;
            this.dialogRef.close(subscriptionsType);
        }, errors => {
            this.loading = false;
            this.errors = errors.messages;
        });
    }

    /**
     * Create a new videoad.
     */
    public create() {
        this.subscriptionsTypes.create(this.getPayload()).subscribe(subscriptionsType => {
            this.loading = false;
            this.dialogRef.close(subscriptionsType);
        }, errors => {
            this.loading = false;
            this.errors = errors.messages;
        });
    }

    /**
     * Get videoad payload for backend.
     */
    private getPayload() {
        let payload = Object.assign({}, this.subscriptionsType);
        return payload;
    }

    /**
     * Hydrate videoad and album models.
     */
    private hydrate(params: CrupdateSubscriptionsTypeModalData) {
        //set videoad model on the modal
        if (params.subscriptionsType) this.subscriptionsType = Object.assign({}, params.subscriptionsType);

        //hydrate rolestovideoad input model
        //this.rolestovideoad = Object.assign({}, params.rolestovideoad, params.videoad ? params.videoad.rolestovideoad : {});
        
       //hydrate videoad number for new videoads
//if ( ! params.videoad) {
//const num = this.album.videoads && this.album.videoads.length;
//this.videoad.number = num ? num+1 : 1;
        //}

        this.updating = !!params.subscriptionsType;
    }
  
}
