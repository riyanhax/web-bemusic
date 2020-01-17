import {Component, Injector, ViewEncapsulation, OnDestroy, OnInit, NgZone, AfterViewInit, ElementRef} from '@angular/core';
import {ContextMenuComponent} from "../../context-menu/context-menu.component";
import {Playlists} from "../playlists.service";
import {UserPlaylists} from "../user-playlists.service";
import {Playlist} from "../../../models/Playlist";
import {CrupdatePlaylistModalComponent} from "../crupdate-playlist-modal/crupdate-playlist-modal.component";
import {ConfirmModalComponent} from "vebto-client/core/ui/confirm-modal/confirm-modal.component";

import {OverlayContainer} from '@angular/cdk/overlay';

@Component({
    selector: 'playlist-context-menu',
    templateUrl: './playlist-context-menu.component.html',
    encapsulation: ViewEncapsulation.None,
    host: {'class': 'context-menu'},
})
export class PlaylistContextMenuComponent extends ContextMenuComponent<Playlist> implements OnInit, OnDestroy{

    /**
     * PlaylistContextMenuComponent Constructor.
     */
    constructor(
        protected injector: Injector,
        protected playlists: Playlists,
        protected userPlaylists: UserPlaylists,
        protected overlayContainer: OverlayContainer,
        
        private ngZone: NgZone,   
        protected el: ElementRef,
    ) {
        super(injector);
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

    public getImage() {
        return this.data.item.image || (this.data.extra && this.data.extra.image);
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
    
    /**
     * Close.
     */
    public closeContextMenu() {        
        super.closeContextMenu();
    }
    
    /**
     * Copy fully qualified playlist url to clipboard.
     */
    public copyLinkToClipboard() {
        super.copyLinkToClipboard('playlist');
    }

    /**
     * Get tracks that should be used by context menu.
     */
    public getTracks() {
        return [];
    }

    /**
     * Delete the playlist after user confirms.
     */
    public maybeDeletePlaylist() {
        this.contextMenu.close();

        this.modal.open(ConfirmModalComponent, {
            title: 'Delete Playlist',
            body: 'Are you sure you want to delete this playlist?',
            ok: 'Delete'
        }).afterClosed().subscribe(confirmed => {
            if ( ! confirmed) return;
            this.contextMenu.close();
            this.playlists.delete([this.data.item.id]).subscribe();
            this.maybeNavigateFromPlaylistRoute();
        });
    }

    /**
     * Open playlist edit modal.
     */
    public openEditModal() {
        this.contextMenu.close();
        this.modal.open(
            CrupdatePlaylistModalComponent,
            {playlist: this.data.item},
            'crupdate-playlist-modal-container',
        );
    }

    /**
     * Check if current user is creator of playlist.
     */
    public userIsCreator() {
        return this.userPlaylists.isCreator(this.data.item);
    }

    /**
     * Check if current user is following this playlist.
     */
    public userIsFollowing() {   
                
        if ( this.data.item && this.data.item.followers){
            let checkIs = this.data.item.followers.findIndex(user => this.currentUser.get('id')) > -1;            
            if(checkIs) return true;
        }
                
        return this.userPlaylists.following(this.data.item.id);
    }

    /**
     * Follow this playlist with current user.
     */
    public follow() {
        this.userPlaylists.follow(this.data.item);
        this.contextMenu.close();
    }

    /**
     * Unfollow this playlist with current user.
     */
    public unfollow() {        
        const unfollowId = this.data.item.followers.findIndex(user => this.currentUser.get('id'));
        if(unfollowId > -1) this.data.item.followers.splice(unfollowId, 1);
        this.userPlaylists.unfollow(this.data.item);
        this.contextMenu.close();
    }

    /**
     * Check if playlist is private.
     */
    public isPublic() {
        return this.data.item.public;
    }

    /**
     * Make playlist public.
     */
    public makePublic() {
        this.contextMenu.close();

        return this.playlists.update(this.data.item.id, {'public': 1}).subscribe(() => {
            this.data.item.public = 1;
        });
    }

    /**
     * Make playlist private.
     */
    public makePrivate() {
        this.contextMenu.close();

        return this.playlists.update(this.data.item.id, {'public': 0}).subscribe(() => {
            this.data.item.public = 0;
        });
    }

    /**
     * Navigate from playlist route if current playlist's route is open.
     */
    private maybeNavigateFromPlaylistRoute() {
        if (this.router.url.indexOf('playlists/'+this.data.item.id) > -1) {
            this.router.navigate(['/']);
        }
    }
}
