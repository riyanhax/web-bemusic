import {Component, Injector, ViewEncapsulation, OnDestroy, OnInit, NgZone, AfterViewInit, ElementRef} from '@angular/core';
import {ContextMenuComponent} from "../../context-menu/context-menu.component";
import {Artist} from "../../../models/Artist";
import {Track} from "../../../models/Track";

import {OverlayContainer} from '@angular/cdk/overlay';

@Component({
    selector: 'artist-context-menu',
    templateUrl: './artist-context-menu.component.html',
    styleUrls: ['./artist-context-menu.component.scss'],
    encapsulation: ViewEncapsulation.None,
    host: {'class': 'context-menu'},
})
export class ArtistContextMenuComponent extends ContextMenuComponent<Artist> implements OnInit, OnDestroy{

    /**
     * ArtistContextMenuComponent Constructor.
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
     * Close.
     */
    public closeContextMenu() {        
        super.closeContextMenu();
    }
    
    /**
     * Copy fully qualified album url to clipboard.
     */
    public copyLinkToClipboard() {
        super.copyLinkToClipboard('artist');
    }

    /**
     * Get all current artist tracks.
     */
    public getTracks(): Track[] {
        return [];
    }

    /**
     * Go to artist radio route.
     */
    public goToArtistRadio() {
        this.contextMenu.close();
        this.router.navigate(this.urls.artistRadio(this.data.item));
    }
}
