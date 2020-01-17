import {Component, Injector, ViewEncapsulation, OnDestroy, OnInit, NgZone, AfterViewInit, ElementRef} from '@angular/core';
import {Album} from "../../../models/Album";
import {WpUtils} from "../../web-player-utils";
import {ContextMenuComponent} from "../../context-menu/context-menu.component";

import {OverlayContainer} from '@angular/cdk/overlay';

@Component({
    selector: 'album-context-menu',
    templateUrl: './album-context-menu.component.html',
    styleUrls: ['./album-context-menu.component.scss'],
    encapsulation: ViewEncapsulation.None,
    host: {'class': 'context-menu'},
})
export class AlbumContextMenuComponent extends ContextMenuComponent<Album> implements OnInit, OnDestroy{
    
    /**
     * AlbumContextMenuComponent Constructor.
     */
    constructor(
        protected injector: Injector,
        private overlayContainer: OverlayContainer,

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
     * Copy fully qualified album url to clipboard.
     */
    public copyLinkToClipboard() {
        super.copyLinkToClipboard('album');
    }

    /**
     * Close.
     */
    public closeContextMenu() {        
        super.closeContextMenu();
    }

    /**
     * Get tracks that should be used by context menu.
     */
    public getTracks() {
        return WpUtils.assignAlbumToTracks(this.data.item.tracks, this.data.item)
    }
}
