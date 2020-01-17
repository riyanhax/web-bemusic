import {finalize} from 'rxjs/operators';
import {Component, Injector, ViewEncapsulation, OnDestroy, OnInit, NgZone, AfterViewInit, ElementRef} from '@angular/core';
import {Track} from "../../../models/Track";
import {ContextMenuComponent} from "../../context-menu/context-menu.component";
import {Player} from "../../player/player.service";
import {UserLibrary} from "../../users/user-library/user-library.service";
import {SelectedTracks} from "../track-list/selected-tracks.service";
import {Lyrics} from "../../lyrics/lyrics.service";
import {LyricsModalComponent} from "../../lyrics/lyrics-modal/lyrics-modal.component";
import {Uploads} from '../../../../vebto-client/core/files/uploads.service';

import {CurrentUser} from "vebto-client/auth/current-user";
import {Router} from '@angular/router';

import {OverlayContainer} from '@angular/cdk/overlay';

import {Modal} from "vebto-client/core/ui/modal.service";
import {ConfirmModalComponent} from "vebto-client/core/ui/confirm-modal/confirm-modal.component";

import {VoucherModalComponent} from "vebto-client/subscription/voucher-modal/voucher-modal.component";

declare let Android: any;

@Component({
    selector: 'track-context-menu',
    templateUrl: './track-context-menu.component.html',
    styleUrls: ['./track-context-menu.component.scss'],
    encapsulation: ViewEncapsulation.None,
    host: {'class': 'context-menu'},
})
export class TrackContextMenuComponent extends ContextMenuComponent<Track> implements OnInit, OnDestroy {
    
    public dataTrackToAndroid = '';       

    /**
     * Params needed to render context menu.
     */
    public data: {selectedTracks?: SelectedTracks, playlistId?: number, type: string, item: Track};

    /**
     * Whether mobile layout should be activated.
     */
    public isMobile: boolean = false;

    protected router: Router;
    public currentUser: CurrentUser;

    /**
     * TrackContextMenuComponent Constructor.
     */
    constructor(
        protected player: Player,
        protected library: UserLibrary,
        protected injector: Injector,
        protected lyrics: Lyrics,
        protected overlayContainer: OverlayContainer,
        
        protected ngZone: NgZone,   
        protected el: ElementRef,

        protected modal: Modal,
    ) {
        super(injector);        
        this.isMobile = window.matchMedia && window.matchMedia('(max-width: 768px)').matches;
    }

    ngOnInit () {
        new Promise(resolve => {
            setTimeout(() => {                 
                this.overlayContainer.getContainerElement().classList.add('context-menu-show');       
             }, 101);
        });
    }

    ngOnDestroy() {
        this.overlayContainer.getContainerElement().classList.remove('context-menu-show');       
    }

    /**
     * Called after component's view has been fully initialized.
     */
    ngAfterViewInit() {
        //wait for animations to complete
        //TODO: refactor this to use events instead
        setTimeout(() => {            
            this.bindHammerEvents();            
        }, 201);
    }
/**
     * Bind handlers to needed hammer.js events.
     */
    private bindHammerEvents() {
        let hammer, tap, pan;
        
        this.ngZone.runOutsideAngular(() => {
            hammer = new Hammer.Manager(this.el.nativeElement);
            tap = new Hammer.Tap();
            pan = new Hammer.Pan();
            hammer.add([tap, pan]);
        });
                
        hammer.on("panup pandown", e => {

            if(e.type == 'pandown') super.closeContextMenu();;
            
        });
    }
    
    public callbackMobile() {        
        
        let track_to_mobile = this.data.item;         
        if ( ! track_to_mobile){
            this.dataTrackToAndroid = '';
            this.toast.open('Could not find Track to Download.');
            return;
        }
        
        if(!this.isMobile){     
            return;
        }

        if (track_to_mobile.album.is_premium){
            if (!this.currentUser.isLoggedIn()) {            
                this.maybeConfirm('login','Login In','You must be logged in to download track "'+track_to_mobile.name+'" from premium content.');    
                return;            
            }
            if (!this.currentUser.jilSubscriptionIsActive()) {            
                this.maybeConfirm('subscription','Add Voucher','To download track "'+track_to_mobile.name+'" from premium content you need to activate the subscription.');            
                return;
            }            
        }else{
            this.callbackMobileSend(track_to_mobile);
        }
        return;                
    }

    public callbackMobileSend(track_to_mobile) {
        try {Android.getTrackObj(JSON.stringify(track_to_mobile));}catch(error) {}     
        this.dataTrackToAndroid = JSON.stringify(track_to_mobile);
        this.toast.open('Download track "'+this.data.item.name+'"');
        setTimeout( () => { this.contextMenu.close(); }, 500 );
    }
    /**
     * Ask user to confirm deletion of selected albums
     * and delete selected artists if user confirms.
     */
    public maybeConfirm(type, btn, text) {        
        this.modal.show(ConfirmModalComponent, {
            title: 'Premium Content',
            body:  text,
            ok:    btn
        }).afterClosed().subscribe(confirmed => {
            if ( ! confirmed) return;
            if (type == 'login'){
                this.router.navigate(['/login']);
            }else{
                this.openVoucherModal();                
            }            
        });
    }

    /**
     * Open modal for single artist page.
     */
    public openVoucherModal() {        
        const userInfo = this.currentUser.getModel();
        this.modal.open(
            VoucherModalComponent,
            {userInfo},
            'home-modal-container',
            'voucher-modal'
        )
        .afterClosed().subscribe(() => {            
            if(this.currentUser.jilSubscriptionIsActive()) this.callbackMobile();
        });
    }

    /**
     * Close.
     */
    public closeContextMenu() {        
        super.closeContextMenu();
    }
    
    /**
     * Check if this track is in player queue.
     */
    public inQueue() {
        return this.player.queue.has(this.data.item);
    }

    /**
     * Remove track from player queue.
     */
    public removeFromQueue() {
        this.player.queue.remove(this.data.item);
        this.contextMenu.close();
    }

    /**
     * Check if track is in user's library.
     */
    public inLibrary() {
        return this.library.has(this.data.item);
    }

    /**
     * Remove track from user's library.
     */
    public removeFromLibrary() {
        this.library.remove(this.getTracks());
        this.contextMenu.close();
    }

    /**
     * Copy fully qualified album url to clipboard.
     */
    public copyLinkToClipboard() {
        super.copyLinkToClipboard('track');
    }

    /**
     * Check if multiple tracks are selected in track list.
     */
    public multipleTracksSelected() {
        return this.data.selectedTracks && this.data.selectedTracks.all().length > 1;
    }

    /**
     * Get tracks that should be used by context menu.
     */
    public getTracks(): Track[] {
        return this.getSelectedTracks() || [this.data.item];
    }

    /**
     * Go to track radio route.
     */
    public goToTrackRadio() {
        this.contextMenu.close();
        this.router.navigate(this.urls.trackRadio(this.data.item));
    }

    /**
     * Fetch lyrics and show lyrics modal.
     */
    public showLyricsModal() {
        this.state.loading = true;
        this.contextMenu.close();

        this.lyrics.get(this.data.item.id).pipe(finalize(() => {
            this.state.loading = false;
        })).subscribe(lyric => {
            this.modal.open(LyricsModalComponent, {lyrics: lyric.text}, 'lyrics-modal-container');
        }, () => {
            this.toast.open('Could not find lyrics for this song.');
        });
    }

    public downloadTrack() {
        const track = this.data.item;
        if ( ! track) return;
        Uploads.downloadFileFromUrl(this.urls.trackDownload(track));
    }

    /**
     * Get image for context menu.
     */
    public getImage() {
        if ( ! this.data.item.album) return this.wpImages.getDefault('album');
        return this.data.item.album.image;
    }

    /**
     * Get currently selected tracks, if any.
     */
    private getSelectedTracks() {
        if ( ! this.data.selectedTracks || this.data.selectedTracks.all().length <= 1) return;
        return this.data.selectedTracks.all();
    }
}
